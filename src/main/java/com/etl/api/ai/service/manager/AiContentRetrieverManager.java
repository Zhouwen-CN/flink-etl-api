package com.etl.api.ai.service.manager;

import com.etl.api.ai.service.retriever.AiContentRetriever;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.router.LanguageModelQueryRouter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * RAG 管道管理器：收集平台全部 {@link AiContentRetriever} 检索源
 * （条件装配决定集合内容，如 web 源未配置 api-key 时不注册），
 * 组装 {@link LanguageModelQueryRouter} 按描述路由到命中源，经 DefaultRetrievalAugmentor 输出。
 *
 * <p>路由 prompt 提供 "0-均不相关" 出口（官方默认模板强制输出数字，闲聊也会被迫选中来源）：
 * 回答 0 → parse 时 id 0 不存在抛异常 → 默认 fallbackStrategy=DO_NOT_ROUTE → 跳过 RAG，闲聊不触发检索。
 *
 * @author chen
 * @since 2026-11-26
 */
@Component
@RequiredArgsConstructor
public class AiContentRetrieverManager {

    /**
     * 路由 prompt：强制只输出编号，并提供 "0-均不相关" 出口，供闲聊/问候类问题跳过检索
     */
    private static final PromptTemplate ROUTER_PROMPT_TEMPLATE = PromptTemplate.from("""
            根据用户问题，判断应从下列哪些数据源检索相关信息：
            {{options}}
            如果以上数据源均与用户问题无关（例如问候、闲聊），回答 0。
            非常重要：你的回答只能是单个数字，或多个数字用英文逗号分隔，不要输出任何其他内容！
            用户问题：{{query}}""");

    private final List<AiContentRetriever> contentRetrieverList;
    private final ChatModel chatModel;

    /**
     * 构建 RAG 增强器：knowledgeIds 直传各检索源（知识库源据此限定召回范围），
     * 源不可用/不适用（如未绑定知识库）时 getRetriever 返回 null，跳过该源；
     * 无可用源时返回 null，AiServices 不挂增强器，不阻断聊天。
     *
     * <p>collector 非 null 时每个源包一层录制代理（retrieve 后记录引用，原样返回结果），
     * 供引用展示；为 null 时不包装，行为与现状一致。
     */
    public RetrievalAugmentor getRetrievalAugmentor(List<Long> knowledgeIds, AiRetrievalContentCollector collector) {
        Map<ContentRetriever, String> contentRetrieverMap = new LinkedHashMap<>();
        for (AiContentRetriever source : contentRetrieverList) {
            ContentRetriever retriever = source.getRetriever(knowledgeIds);
            if (retriever != null) {
                contentRetrieverMap.put(wrap(source, retriever, collector), source.getDescription());
            }
        }
        if (contentRetrieverMap.isEmpty()) {
            return null;
        }

        LanguageModelQueryRouter queryRouter = LanguageModelQueryRouter.builder()
                .chatModel(chatModel)
                .retrieverToDescription(contentRetrieverMap)
                .promptTemplate(ROUTER_PROMPT_TEMPLATE)
                .build();

        return DefaultRetrievalAugmentor.builder()
                // .queryTransformer() // 查询转换器
                .queryRouter(queryRouter) // 查询路由器
                // .contentAggregator() // 内容聚合器
                // .contentInjector() // 内容注入器
                .build();
    }

    /**
     * 引用采集代理：retrieve 后经 collector 记录，再原样返回结果
     */
    private ContentRetriever wrap(AiContentRetriever source, ContentRetriever delegate,
                                  AiRetrievalContentCollector collector) {
        if (collector == null) {
            return delegate;
        }
        return query -> {
            List<Content> contents = delegate.retrieve(query);
            collector.record(source.getName(), contents);
            return contents;
        };
    }
}
