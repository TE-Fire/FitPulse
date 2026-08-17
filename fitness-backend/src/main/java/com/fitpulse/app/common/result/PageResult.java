package com.fitpulse.app.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private long total;
    private int pageNum;
    private int pageSize;
    private int pages;
    private List<T> list;

    public static <T> PageResult<T> of(long total, int pageNum, int pageSize, List<T> list) {
        int pages = (int) Math.ceil((double) total / pageSize);
        return new PageResult<>(total, pageNum, pageSize, pages, list);
    }
}
