package com.etl.api.etl.service.impl;

import com.etl.api.etl.domain.convert.EtlProjectConvert;
import com.etl.api.etl.domain.entity.EtlJob;
import com.etl.api.etl.domain.entity.EtlProject;
import com.etl.api.etl.domain.form.EtlProjectCreateForm;
import com.etl.api.etl.mapper.EtlJobMapper;
import com.etl.api.etl.mapper.EtlProjectMapper;
import com.etl.api.etl.service.EtlProjectService;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * ETL项目表 服务层实现。
 *
 * @author chen
 * @since 2026-09-18
 */
@Service
@RequiredArgsConstructor
public class EtlProjectServiceImpl extends ServiceImpl<EtlProjectMapper, EtlProject> implements EtlProjectService {

    private final EtlJobMapper jobMapper;

    @Override
    public ResponseVO<Void> addProject(EtlProjectCreateForm form) {
        val name = form.getName();
        val exists = this.queryChain()
                .eq(EtlProject::getName, name)
                .exists();
        if (exists) {
            return ResponseVO.recordExistsError(name);
        }

        val entity = EtlProjectConvert.INSTANCE.convert(form);
        this.save(entity);

        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeProject(Long id) {
        val count = jobMapper.selectCountByQuery(
                QueryWrapper.create()
                        .eq(EtlJob::getProjectId, id)
        );
        if (count > 0) {
            return ResponseVO.error("删除失败，尚有任务依赖");
        }

        this.removeById(id);
        return ResponseVO.ok();
    }
}
