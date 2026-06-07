package cn.edu.app.douyu.model;

import java.util.ArrayList;
import java.util.List;

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
