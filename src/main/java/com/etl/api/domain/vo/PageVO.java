package com.etl.api.domain.vo;

import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页视图")
public final class PageVO<E> {

    @Schema(description = "分页数据")
    private List<E> list;

    @Schema(description = "分页总条数")
    private long total;

    public static <E> PageVO<E> from(Page<E> page) {
        return new PageVO<>(page.getRecords(), page.getTotalPage());
    }
}
