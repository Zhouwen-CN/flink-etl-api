package com.etl.api.ai.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.etl.api.ai.domain.form.AiKnowledgeDocChunkUpdateForm;
import com.etl.api.ai.domain.vo.AiKnowledgeDocChunkVO;
import com.etl.api.ai.service.AiKnowledgeDocChunkService;
import com.etl.api.sys.domain.vo.PageVO;
import com.etl.api.sys.domain.vo.ResponseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 知识库文档分块控制器。
 *
 * <p>分块只由解析流水线产生，人工只做查看/编辑/删除（对齐 AIFlowy）。
 *
 * @author chen
 * @since 2026-11-25
 */
@RestController
@RequestMapping("/ai/doc/chunk")
@Tag(name = "AI 知识库文档分块控制器")
@RequiredArgsConstructor
public class AiKnowledgeDocChunkController {

    private final AiKnowledgeDocChunkService chunkService;

    @SaCheckPermission("doc.select")
    @Operation(summary = "分块分页查看", description = "按 id 升序，即文档顺序；keyword 可选，按内容模糊匹配")
    @GetMapping
    public ResponseVO<PageVO<AiKnowledgeDocChunkVO>> getChunkPage(
            @RequestParam("docId") @Parameter(description = "文档ID") Long docId,
            @RequestParam("currentPage") @Parameter(description = "当前页面") @Min(1) Integer currentPage,
            @RequestParam("pageSize") @Parameter(description = "页面大小") @Min(1) @Max(50) Integer pageSize,
            @RequestParam(value = "keyword", required = false) @Parameter(description = "内容关键词") String keyword
    ) {
        return ResponseVO.ok(chunkService.getPage(docId, currentPage, pageSize, keyword));
    }

    @SaCheckPermission("doc.update")
    @Operation(summary = "编辑分块", description = "改 DB 后用同一 chunk.id 同步重新向量化")
    @PutMapping
    public ResponseVO<Void> updateChunk(@RequestBody @Validated AiKnowledgeDocChunkUpdateForm form) {
        return chunkService.updateChunkContent(form);
    }

    @SaCheckPermission("doc.update")
    @Operation(summary = "删除分块", description = "先删向量库记录再删 DB")
    @DeleteMapping("/{chunkId}")
    public ResponseVO<Void> removeChunk(@PathVariable @Parameter(description = "分块ID") Long chunkId) {
        return chunkService.removeChunk(chunkId);
    }
}
