package com.etl.api.ai.service.manager;

import com.etl.api.ai.service.retriever.AiContentRetriever;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
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
 * @author chen
 * @since 2026-11-26
 */
@Component
@RequiredArgsConstructor
public class AiContentRetrieverManager {

    private final List<AiContentRetriever> contentRetrieverList;

    /**
     * 构建 RAG 增强器：knowledgeIds 直传各检索源（知识库源据此限定召回范围），
     * 源不可用/不适用（如未绑定知识库）时 getRetriever 返回 null，跳过该源；
     * 无可用源时返回 null，AiServices 不挂增强器，不阻断聊天。
     */
    public RetrievalAugmentor getRetrievalAugmentor(ChatModel chatModel, List<Long> knowledgeIds) {
        Map<ContentRetriever, String> contentRetrieverMap = new LinkedHashMap<>();
        for (AiContentRetriever source : contentRetrieverList) {
            ContentRetriever retriever = source.getRetriever(knowledgeIds);
            if (retriever != null) {
                contentRetrieverMap.put(retriever, source.getDescription());
            }
        }
        if (contentRetrieverMap.isEmpty()) {
            return null;
        }

        LanguageModelQueryRouter queryRouter = LanguageModelQueryRouter.builder()
                .chatModel(chatModel)
                .retrieverToDescription(contentRetrieverMap)
                .build();

        return DefaultRetrievalAugmentor.builder()
                // .queryTransformer() // 查询转换器
                .queryRouter(queryRouter) // 查询路由器
                // .contentAggregator() // 内容聚合器
                // .contentInjector() // 内容注入器
                .build();
    }
}
