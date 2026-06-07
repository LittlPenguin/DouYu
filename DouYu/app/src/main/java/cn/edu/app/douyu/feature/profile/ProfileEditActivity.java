package cn.edu.app.douyu.feature.profile;

import cn.edu.app.douyu.R;
import cn.edu.app.douyu.ui.XmlPageActivity;

public class ProfileEditActivity extends XmlPageActivity {
    @Override
    protected int layoutRes() {
        return R.layout.activity_profile_edit;
    }

    @Override
    protected String title() {
        return "编辑资料";
    }
}
