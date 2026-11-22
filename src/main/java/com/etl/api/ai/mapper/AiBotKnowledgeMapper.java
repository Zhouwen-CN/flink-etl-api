package com.etl.api.ai.mapper;

import com.etl.api.ai.domain.entity.AiBotKnowledge;
import com.mybatisflex.core.BaseMapper;

/**
 * Bot 知识库挂载表 映射层。
 *
 * <p>挂载表批量插入需 insertBatch（无链式 API 等价物），保留 mapper。
 *
 * @author chen
 * @since 2026-11-22
 */
public interface AiBotKnowledgeMapper extends BaseMapper<AiBotKnowledge> {

}
