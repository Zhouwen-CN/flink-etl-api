package com.etl.api.ai.service.impl;

import com.etl.api.ai.config.AiChatModelFactory;
import com.etl.api.ai.domain.convert.AiKnowledgeConvert;
import com.etl.api.ai.domain.entity.AiKnowledge;
import com.etl.api.ai.domain.entity.AiModel;
import com.etl.api.ai.domain.form.AiKnowledgeCreateForm;
import com.etl.api.ai.domain.form.AiKnowledgeUpdateForm;
import com.etl.api.ai.domain.vo.AiKnowledgeVO;
import com.etl.api.ai.enumeration.AiVectorDbTypeEnum;
import com.etl.api.ai.exception.AiChatException;
import com.etl.api.ai.mapper.AiKnowledgeMapper;
import com.etl.api.ai.mapper.AiModelMapper;
import com.etl.api.ai.mapper.AiVectorDatabaseMapper;
import com.etl.api.ai.service.AiKnowledgeService;
import com.etl.api.sys.domain.vo.PageVO;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * AI 知识库表 服务层实现。
 *
 * <p>向量维度在创建时自动探测固化（对绑定 Embedding 模型调用一次 embed()）；
 * 集合名/索引名不落库，由向量库工厂按知识库 id 派生（AiVectorStoreFactory create 方法）；
 * 创建时不主动连向量库建集合——LangChain4j Milvus/Redis store 均在首次使用时自动建集合/索引，惰性即可。
 *
 * @author chen
 * @since 2026-11-19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiKnowledgeServiceImpl extends ServiceImpl<AiKnowledgeMapper, AiKnowledge> implements AiKnowledgeService {

    /**
     * 维度探测固定文本，长度不影响向量维度
     */
    private static final String DIMENSION_PROBE_TEXT = "dimension probe";

    private final AiVectorDatabaseMapper vectorDatabaseMapper;
    private final AiModelMapper modelMapper;
    private final AiChatModelFactory chatModelFactory;

    @Override
    public PageVO<AiKnowledgeVO> getPage(Integer currentPage, Integer pageSize, String searchName) {
        val page = this.queryChain()
                .like(AiKnowledge::getName, searchName, StringUtils.hasText(searchName))
                .pageAs(Page.of(currentPage, pageSize), AiKnowledgeVO.class);
        return PageVO.from(page);
    }

    @Override
    public ResponseVO<Void> addKnowledge(AiKnowledgeCreateForm form) {
        val nameExists = this.queryChain()
                .eq(AiKnowledge::getName, form.getName())
                .exists();
        if (nameExists) {
            return ResponseVO.recordExistsError("Name", form.getName());
        }

        val vectorDb = vectorDatabaseMapper.selectOneById(form.getVectorDbId());
        if (vectorDb == null) {
            return ResponseVO.recordNotFoundError("VectorDbId", form.getVectorDbId());
        }
        val dbType = AiVectorDbTypeEnum.of(vectorDb.getType());
        if (dbType.isEmpty()) {
            return ResponseVO.error("不支持的向量数据库类型: " + vectorDb.getType());
        }

        val embedModel = modelMapper.selectOneById(form.getEmbedModelId());
        if (embedModel == null) {
            return ResponseVO.recordNotFoundError("EmbedModelId", form.getEmbedModelId());
        }

        // 维度探测：复用 ChatModelFactory.requireType 语义（类型不符抛 AiChatException），失败整体保存失败
        val dimension = probeDimension(embedModel);

        val entity = AiKnowledgeConvert.INSTANCE.convert(form);
        entity.setDimension(dimension);
        this.save(entity);
        log.info("新增知识库, id: {}, name: {}, vectorDbId: {}, embedModelId: {}, dimension: {}",
                entity.getId(), entity.getName(), entity.getVectorDbId(), entity.getEmbedModelId(),
                entity.getDimension());
        return ResponseVO.ok();
    }

    /**
     * 对绑定的 Embedding 模型做一次真实调用取向量维度并固化到知识库
     */
    private int probeDimension(AiModel embedModel) {
        try {
            return chatModelFactory.getEmbeddingModel(embedModel)
                    .embed(DIMENSION_PROBE_TEXT)
                    .content()
                    .vector()
                    .length;
        } catch (AiChatException e) {
            throw e;
        } catch (Exception e) {
            throw new AiChatException("Embedding 维度探测失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseVO<Void> updateKnowledge(AiKnowledgeUpdateForm form) {
        val old = this.getById(form.getId());
        if (old == null) {
            return ResponseVO.recordNotFoundError("ID", form.getId());
        }

        val nameExists = this.queryChain()
                .eq(AiKnowledge::getName, form.getName())
                .ne(AiKnowledge::getId, form.getId())
                .exists();
        if (nameExists) {
            return ResponseVO.recordExistsError("Name", form.getName());
        }

        val entity = AiKnowledgeConvert.INSTANCE.convert(form);
        this.updateById(entity);
        log.info("更新知识库, id: {}, name: {}", entity.getId(), entity.getName());
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeKnowledge(Long id) {
        // 后续文档模块落地时需补 AIFlowy beforeRemove 式级联——删向量库数据/chunk/document
        this.removeById(id);
        log.info("删除知识库, id: {}", id);
        return ResponseVO.ok();
    }
}
