package cn.edu.app.douyu.feature.community;

import cn.edu.app.douyu.R;
import cn.edu.app.douyu.ui.XmlPageActivity;

public class PostCreateActivity extends XmlPageActivity {
    @Override
    protected int layoutRes() {
        return R.layout.activity_post_create;
    }

    @Override
    protected String title() {
        return "上传帖子";
    }
}
