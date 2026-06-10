package cn.edu.app.douyu;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import cn.edu.app.douyu.auth.AuthGate;
import cn.edu.app.douyu.auth.LoginActivity;
import cn.edu.app.douyu.core.IntentExtras;
import cn.edu.app.douyu.core.SystemBarInsets;
import cn.edu.app.douyu.feature.commerce.CommerceFragment;
import cn.edu.app.douyu.feature.community.CommunityFragment;
import cn.edu.app.douyu.feature.community.PostCreateActivity;
import cn.edu.app.douyu.feature.community.SearchActivity;
import cn.edu.app.douyu.feature.message.MessagesFragment;
import cn.edu.app.douyu.feature.profile.ProfileFragment;
import cn.edu.app.douyu.feature.profile.SettingsActivity;

public class MainActivity extends AppCompatActivity {
    private TextView title;
    private View[] tabs;
    private ImageView[] tabIcons;
    private TextView[] tabLabels;
    private View[] tabIndicators;
    private ImageButton settingsButton;
    private ActivityResultLauncher<Intent> loginLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SystemBarInsets.applyToContent(this);
        loginLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK
                    && result.getData() != null
                    && AuthGate.RETURN_ACTION_POST_CREATE.equals(result.getData().getStringExtra(LoginActivity.EXTRA_RETURN_ACTION))) {
                startActivity(new Intent(this, PostCreateActivity.class));
            }
        });

        title = findViewById(R.id.top_title);
        settingsButton = findViewById(R.id.action_settings);
        findViewById(R.id.action_search).setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        settingsButton.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        findViewById(R.id.tab_upload).setOnClickListener(v -> {
            AuthGate.runOrRequestLogin(this, loginLauncher, AuthGate.RETURN_ACTION_POST_CREATE,
                    () -> startActivity(new Intent(this, PostCreateActivity.class)));
        });

        tabs = new View[]{
                findViewById(R.id.tab_community),
                findViewById(R.id.tab_commerce),
                findViewById(R.id.tab_messages),
                findViewById(R.id.tab_profile)
        };
        tabIcons = new ImageView[]{
                findViewById(R.id.nav_icon_community),
                findViewById(R.id.nav_icon_commerce),
                findViewById(R.id.nav_icon_messages),
                findViewById(R.id.nav_icon_profile)
        };
        tabLabels = new TextView[]{
                findViewById(R.id.nav_label_community),
                findViewById(R.id.nav_label_commerce),
                findViewById(R.id.nav_label_messages),
                findViewById(R.id.nav_label_profile)
        };
        tabIndicators = new View[]{
                findViewById(R.id.nav_indicator_community),
                findViewById(R.id.nav_indicator_commerce),
                findViewById(R.id.nav_indicator_messages),
                findViewById(R.id.nav_indicator_profile)
        };
        tabs[0].setOnClickListener(v -> openTab(0));
        tabs[1].setOnClickListener(v -> openTab(1));
        tabs[2].setOnClickListener(v -> openTab(2));
        tabs[3].setOnClickListener(v -> openTab(3));
        if (savedInstanceState == null) {
            openTab(initialTab());
        }
    }

    private int initialTab() {
        String section = getIntent().getStringExtra(IntentExtras.SECTION);
        if (IntentExtras.SECTION_COMMERCE.equals(section)) {
            return 1;
        }
        if (IntentExtras.SECTION_MESSAGES.equals(section)) {
            return 2;
        }
        if (IntentExtras.SECTION_PROFILE.equals(section)) {
            return 3;
        }
        return 0;
    }

    private void openTab(int index) {
        if (index == 0) {
            open("社区", new CommunityFragment());
        } else if (index == 1) {
            open("商城", new CommerceFragment());
        } else if (index == 2) {
            open("消息", new MessagesFragment());
        } else {
            open("我的", new ProfileFragment());
        }
        selectTab(index);
    }

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

    private void open(String label, Fragment fragment) {
        title.setText(label);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        return true;
    }
}
