package cn.edu.app.douyu.server.common;

import java.util.List;
/**
 * 分页响应 record：承载列表数据、页码、每页数量和总数。
 */

public record PageResult<T>(
        List<T> items,
        int page,
        int size,
        long total,
        boolean hasMore
) {
    public static <T> PageResult<T> of(List<T> items, int page, int size, long total) {
        return new PageResult<>(items, page, size, total, (long) page * size < total);
    }
}
