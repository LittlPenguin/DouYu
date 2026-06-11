package cn.edu.app.douyu;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import cn.edu.app.douyu.core.IntentExtras;
import cn.edu.app.douyu.core.SystemBarInsets;
import cn.edu.app.douyu.feature.commerce.CommerceFragment;
import cn.edu.app.douyu.feature.community.CommunityFragment;
import cn.edu.app.douyu.feature.community.PostCreateFragment;
import cn.edu.app.douyu.feature.community.SearchActivity;
import cn.edu.app.douyu.feature.message.MessagesFragment;
import cn.edu.app.douyu.feature.profile.ProfileFragment;
import cn.edu.app.douyu.feature.profile.SettingsActivity;

/**
 * 主页面容器：承载五个底部 Tab，并负责主页面 Fragment 切换。
 */
public class MainActivity extends AppCompatActivity implements PostCreateFragment.Host {
    // 顶部标题和设置按钮随当前 Tab 更新展示。
    private TextView title;
    private View[] tabs;
    private ImageView[] tabIcons;
    private TextView[] tabLabels;
    private View[] tabIndicators;
    private ImageButton settingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SystemBarInsets.applyToContent(this);

        title = findViewById(R.id.top_title);
        settingsButton = findViewById(R.id.action_settings);
        findViewById(R.id.action_search).setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        settingsButton.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        findViewById(R.id.tab_upload).setOnClickListener(v -> openTab(2));

        tabs = new View[]{
                findViewById(R.id.tab_community),
                findViewById(R.id.tab_commerce),
                findViewById(R.id.tab_upload),
                findViewById(R.id.tab_messages),
                findViewById(R.id.tab_profile)
        };
        tabIcons = new ImageView[]{
                findViewById(R.id.nav_icon_community),
                findViewById(R.id.nav_icon_commerce),
                findViewById(R.id.nav_icon_upload),
                findViewById(R.id.nav_icon_messages),
                findViewById(R.id.nav_icon_profile)
        };
        tabLabels = new TextView[]{
                findViewById(R.id.nav_label_community),
                findViewById(R.id.nav_label_commerce),
                findViewById(R.id.nav_label_upload),
                findViewById(R.id.nav_label_messages),
                findViewById(R.id.nav_label_profile)
        };
        tabIndicators = new View[]{
                findViewById(R.id.nav_indicator_community),
                findViewById(R.id.nav_indicator_commerce),
                findViewById(R.id.nav_indicator_upload),
                findViewById(R.id.nav_indicator_messages),
                findViewById(R.id.nav_indicator_profile)
        };
        tabs[0].setOnClickListener(v -> openTab(0));
        tabs[1].setOnClickListener(v -> openTab(1));
        tabs[2].setOnClickListener(v -> openTab(2));
        tabs[3].setOnClickListener(v -> openTab(3));
        tabs[4].setOnClickListener(v -> openTab(4));
        if (savedInstanceState == null) {
            handleSectionIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleSectionIntent(intent);
    }

    // 处理外部或子页面传回的 section 参数，把用户带到指定主 Tab。
    private void handleSectionIntent(Intent intent) {
        openTab(sectionToTabIndex(intent == null ? null : intent.getStringExtra(IntentExtras.SECTION)));
    }

    // 将统一的 section 常量转换成底部导航下标。
    private int sectionToTabIndex(String section) {
        if (IntentExtras.SECTION_COMMUNITY.equals(section)) {
            return 0;
        }
        if (IntentExtras.SECTION_COMMERCE.equals(section)) {
            return 1;
        }
        if (IntentExtras.SECTION_UPLOAD.equals(section)) {
            return 2;
        }
        if (IntentExtras.SECTION_MESSAGES.equals(section)) {
            return 3;
        }
        if (IntentExtras.SECTION_PROFILE.equals(section)) {
            return 4;
        }
        return 0;
    }

    // 根据 Tab 下标创建对应 Fragment；上传页也是主 Tab 的一个 Fragment。
    private void openTab(int index) {
        if (index == 0) {
            open("社区", new CommunityFragment());
        } else if (index == 1) {
            open("商城", new CommerceFragment());
        } else if (index == 2) {
            open("上传", new PostCreateFragment());
        } else if (index == 3) {
            open("消息", new MessagesFragment());
        } else {
            open("我的", new ProfileFragment());
        }
        selectTab(index);
    }

    // 同步底部导航的选中态、图标颜色、文字颜色和指示条。
    private void selectTab(int selectedIndex) {
        int active = ContextCompat.getColor(this, R.color.doyu_petal_deep);
        int inactive = ContextCompat.getColor(this, R.color.doyu_text);
        for (int i = 0; i < tabs.length; i++) {
            boolean selected = i == selectedIndex;
            tabs[i].setBackgroundResource(selected ? R.drawable.bg_nav_item_selected : 0);
            tabs[i].setSelected(selected);
            tabIcons[i].setColorFilter(selected ? active : inactive);
            tabLabels[i].setTextColor(selected ? active : inactive);
            tabIndicators[i].setVisibility(selected ? View.VISIBLE : View.GONE);
        }
    }

    // 使用 FragmentManager 替换主内容区，保持单 Activity 多页面结构。
    private void open(String label, Fragment fragment) {
        title.setText(label);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    public void navigateToSection(String section) {
        openTab(sectionToTabIndex(section));
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        return true;
    }
}
