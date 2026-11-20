package com.etl.api.ai.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.etl.api.ai.domain.convert.AiModelConvert;
import com.etl.api.ai.domain.convert.AiVectorDatabaseConvert;
import com.etl.api.ai.domain.entity.AiModel;
import com.etl.api.ai.domain.form.AiKnowledgeCreateForm;
import com.etl.api.ai.domain.form.AiKnowledgeUpdateForm;
import com.etl.api.ai.domain.vo.AiKnowledgeVO;
import com.etl.api.ai.enumeration.AiModelTypeEnum;
import com.etl.api.ai.service.AiKnowledgeService;
import com.etl.api.ai.service.AiModelService;
import com.etl.api.ai.service.AiVectorDatabaseService;
import com.etl.api.sys.domain.vo.DictionaryVO;
import com.etl.api.sys.domain.vo.PageVO;
import com.etl.api.sys.domain.vo.ResponseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/ai/knowledge")
@Tag(name = "AI 知识库控制器")
@RequiredArgsConstructor
public class AiKnowledgeController {

    private final AiKnowledgeService knowledgeService;
    private final AiVectorDatabaseService vectorDatabaseService;
    private final AiModelService modelService;

    @SaCheckPermission("knowledge.select")
    @Operation(summary = "分页查询")
    @GetMapping
    public ResponseVO<PageVO<AiKnowledgeVO>> getPage(
            @RequestParam("currentPage") @Parameter(description = "当前页面") @Min(1) Integer currentPage,
            @RequestParam("pageSize") @Parameter(description = "页面大小") @Min(1) @Max(50) Integer pageSize,
            @RequestParam(value = "searchName", required = false) @Parameter(description = "知识库名称") String searchName
    ) {
        return ResponseVO.ok(knowledgeService.getPage(currentPage, pageSize, searchName));
    }

    @SaCheckPermission("knowledge.select")
    @Operation(summary = "向量数据库选择器")
    @GetMapping("/vectordb/selector")
    public ResponseVO<List<DictionaryVO>> vectorDbSelector() {
        val vos = vectorDatabaseService.queryChain()
                .list()
                .stream()
                .map(AiVectorDatabaseConvert.INSTANCE::convert)
                .toList();
        return ResponseVO.ok(vos);
    }

    @SaCheckPermission("knowledge.select")
    @Operation(summary = "Embedding模型选择器")
    @GetMapping("/embed-model/selector")
    public ResponseVO<List<DictionaryVO>> embedModelSelector() {
        val vos = modelService.queryChain()
                .eq(AiModel::getType, AiModelTypeEnum.EMBEDDING.getCode())
                .list()
                .stream()
                .map(AiModelConvert.INSTANCE::convert)
                .toList();
        return ResponseVO.ok(vos);
    }

    @SaCheckPermission("knowledge.insert")
    @Operation(summary = "新增", description = "含 Embedding 维度探测，探测失败则整体保存失败")
    @PostMapping
    public ResponseVO<Void> add(@RequestBody @Validated AiKnowledgeCreateForm form) {
        return knowledgeService.addKnowledge(form);
    }

    @SaCheckPermission("knowledge.update")
    @Operation(summary = "更新", description = "vectorDbId/embedModelId/dimension 不可变更；集合名按 id 派生")
    @PutMapping
    public ResponseVO<Void> modify(@RequestBody @Validated AiKnowledgeUpdateForm form) {
        return knowledgeService.updateKnowledge(form);
    }

    @SaCheckPermission("knowledge.delete")
    @Operation(summary = "删除", description = "物理删除；后续文档模块落地时补级联清理")
    @DeleteMapping("/{id}")
    public ResponseVO<Void> remove(@PathVariable @Parameter(description = "ID") Long id) {
        return knowledgeService.removeKnowledge(id);
    }
}
