package com.etl.api.ai.service.retriever;

import com.etl.api.ai.service.manager.AiRetrievalContentCollector;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.WebSearchContentRetriever;
import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.tavily.TavilyWebSearchEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 联网搜索检索源：包装 Tavily {@link WebSearchContentRetriever}（RAG 管道的 Web 源）。
 *
 * <p>按 {@code custom.ai.web-search.tavily.api-key} 条件装配：配置了 api-key 才注册为 Bean，
 * 未配置时 RAG 管道收集 {@link AiContentRetriever} 集合天然不含本源；api-key 为空串的边缘情况
 * 由 {@link #afterPropertiesSet()} 兜底（delegate 不构建，retrieve 返回空列表，不阻断聊天）。
 *
 * @author chen
 * @since 2026-11-25
 */
@Slf4j
@Component
public class AiWebSearchContentRetriever implements AiContentRetriever {

    @Value("${custom.ai.web-search.tavily.api-key:}")
    private String tavilyApiKey;

    @Value("${custom.ai.retrieval.max-results:5}")
    private Integer maxResults;

    private volatile WebSearchContentRetriever delegate;

    @Override
    public void afterPropertiesSet() {
        if (!StringUtils.hasText(tavilyApiKey)) {
            return;
        }
        WebSearchEngine webSearchEngine = TavilyWebSearchEngine.builder()
                .apiKey(tavilyApiKey) // get a free key: https://app.tavily.com/sign-in
                .build();

        delegate = WebSearchContentRetriever.builder()
                .webSearchEngine(webSearchEngine)
                .maxResults(maxResults)
                .build();
    }

    @Override
    public String getName() {
        return AiRetrievalContentCollector.SOURCE_WEB_SEARCH;
    }

    @Override
    public String getDescription() {
        return "联网搜索实时信息与公开资料（新闻、时事、文档之外的公开内容）。";
    }

    @Override
    public ContentRetriever getRetriever(List<Long> knowledgeIds) {
        if (!StringUtils.hasText(tavilyApiKey)) {
            return null;
        }
        return delegate;
    }
}
