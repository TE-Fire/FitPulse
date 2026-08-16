package com.fitpulse.app.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class PageResult<T> implements Serializable {
    private List<T> records;
    private Long total;
    private Long pageNum;
    private Long pageSize;
    private Long pages;

    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> r = new PageResult<>();
        r.setRecords(page.getRecords());
        r.setTotal(page.getTotal());
        r.setPageNum(page.getCurrent());
        r.setPageSize(page.getSize());
        r.setPages(page.getPages());
        return r;
    }
}
