package com.etl.api.ai.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.etl.api.ai.domain.convert.AiKnowledgeConvert;
import com.etl.api.ai.domain.form.AiKnowledgeDocChunkUpdateForm;
import com.etl.api.ai.domain.vo.AiKnowledgeDocChunkVO;
import com.etl.api.ai.domain.vo.AiKnowledgeDocVO;
import com.etl.api.ai.service.AiKnowledgeDocChunkService;
import com.etl.api.ai.service.AiKnowledgeDocService;
import com.etl.api.ai.service.AiKnowledgeService;
import com.etl.api.ai.util.FileTypeDetectUtil;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * AI 知识库文档控制器（上传即异步解析，前端轮询 parse_status）。
 *
 * <p>查询类端点一律静态路径 + @RequestParam 传参；
 * 写操作端点（删除/重解析）保留路径变量置于路径末尾。
 *
 * @author chen
 * @since 2026-11-20
 */
@Slf4j
@RestController
@RequestMapping("/ai/doc")
@Tag(name = "AI 知识库文档控制器")
@RequiredArgsConstructor
public class AiKnowledgeDocController {

    private final AiKnowledgeDocService docService;
    private final AiKnowledgeDocChunkService chunkService;
    private final AiKnowledgeService knowledgeService;

    @SaCheckPermission("doc.select")
    @Operation(summary = "知识库选择器", description = "上传文档时选择目标知识库")
    @GetMapping("/selector")
    public ResponseVO<List<DictionaryVO>> knowledgeSelector() {
        val vos = knowledgeService.queryChain()
                .list()
                .stream()
                .map(AiKnowledgeConvert.INSTANCE::convert)
                .toList();
        return ResponseVO.ok(vos);
    }

    @SaCheckPermission("doc.insert")
    @Operation(summary = "上传文档", description = "异步解析，返回即成功；解析状态轮询 GET /doc 列表")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseVO<Void> addDoc(
            @RequestParam("knowledgeId") @Parameter(description = "知识库ID") Long knowledgeId,
            @RequestParam("file") @Parameter(description = "文档文件（pdf/docx/xlsx/md/txt）") MultipartFile file
    ) {
        return docService.addDoc(knowledgeId, file);
    }

    @SaCheckPermission("doc.update")
    @Operation(summary = "更新文档", description = "替换文件并重走异步解析；旧向量与分块同步清理")
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseVO<Void> updateDoc(
            @RequestParam("docId") @Parameter(description = "文档ID") Long docId,
            @RequestParam("file") @Parameter(description = "文档文件（pdf/docx/xlsx/md/txt）") MultipartFile file
    ) {
        return docService.updateDoc(docId, file);
    }

    @SaCheckPermission("doc.select")
    @Operation(summary = "文档分页", description = "含 chunk_count、parse_status，不带 blob 大字段")
    @GetMapping
    public ResponseVO<PageVO<AiKnowledgeDocVO>> getDocPage(
            @RequestParam(value = "knowledgeId",required = false) @Parameter(description = "知识库ID") Long knowledgeId,
            @RequestParam("currentPage") @Parameter(description = "当前页面") @Min(1) Integer currentPage,
            @RequestParam("pageSize") @Parameter(description = "页面大小") @Min(1) @Max(50) Integer pageSize,
            @RequestParam(value = "searchFileName", required = false) @Parameter(description = "文件名") String searchFileName
    ) {
        return ResponseVO.ok(docService.getPage(knowledgeId, currentPage, pageSize, searchFileName));
    }

    @SaCheckPermission("doc.select")
    @Operation(summary = "下载原始文件", description = "单独查询 blob 大字段，返回文件流")
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadDoc(
            @RequestParam("docId") @Parameter(description = "文档ID") Long docId
    ) {
        val doc = docService.getDocForDownload(docId);
        if (doc == null || doc.getFileContent() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        val fileName = URLEncoder.encode(doc.getFileName(), StandardCharsets.UTF_8).replace("+", "%20");
        val contentType = FileTypeDetectUtil.CONTENT_TYPES.getOrDefault(doc.getFileType(), "application/octet-stream");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + fileName)
                .contentType(MediaType.parseMediaType(contentType))
                .body(doc.getFileContent());
    }

    @SaCheckPermission("doc.select")
    @Operation(summary = "分块分页查看", description = "按 id 升序，即文档顺序；keyword 可选，按内容模糊匹配")
    @GetMapping("/chunk")
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
    @PutMapping("/chunk")
    public ResponseVO<Void> updateChunk(@RequestBody @Validated AiKnowledgeDocChunkUpdateForm form) {
        return chunkService.updateChunkContent(form);
    }

    @SaCheckPermission("doc.update")
    @Operation(summary = "删除分块", description = "先删向量库记录再删 DB")
    @DeleteMapping("/chunk/{chunkId}")
    public ResponseVO<Void> removeChunk(@PathVariable @Parameter(description = "分块ID") Long chunkId) {
        return chunkService.removeChunk(chunkId);
    }

    @SaCheckPermission("doc.update")
    @Operation(summary = "重新解析", description = "删旧向量+旧分块后按知识库当前分块参数重走流水线")
    @PostMapping("/reparse/{docId}")
    public ResponseVO<Void> reparse(@PathVariable @Parameter(description = "文档ID") Long docId) {
        return docService.reparse(docId);
    }

    @SaCheckPermission("doc.delete")
    @Operation(summary = "删除文档", description = "级联删除向量库数据与分块")
    @DeleteMapping("/{docId}")
    public ResponseVO<Void> removeDoc(@PathVariable @Parameter(description = "文档ID") Long docId) {
        return docService.removeDoc(docId);
    }
}
