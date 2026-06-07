package cn.edu.app.douyu.feature.community;

import cn.edu.app.douyu.R;
import cn.edu.app.douyu.ui.XmlPageActivity;

public class SearchActivity extends XmlPageActivity {
    @Override
    protected int layoutRes() {
        return R.layout.activity_search;
    }

    @Override
    protected String title() {
        return "搜索";
    }
}
