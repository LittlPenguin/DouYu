package cn.edu.app.douyu.model;

import java.util.ArrayList;
import java.util.List;
/**
 * 分页响应 DTO：承载后端分页列表、页码、总数等信息。
 */

public class PageResponse<T> {
    public List<T> items = new ArrayList<>();
    public Integer page;
    public Integer size;
    public Integer total;
    public Boolean hasMore;

    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }
}
