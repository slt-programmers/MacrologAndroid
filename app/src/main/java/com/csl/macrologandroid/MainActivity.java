package com.csl.macrologandroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.csl.macrologandroid.cache.ActivityCache;
import com.csl.macrologandroid.cache.DiaryLogCache;
import com.csl.macrologandroid.cache.FoodCache;
import com.csl.macrologandroid.cache.UserSettingsCache;
import com.csl.macrologandroid.fragments.DiaryFragment;
import com.csl.macrologandroid.fragments.DishFragment;
import com.csl.macrologandroid.fragments.FoodFragment;
import com.csl.macrologandroid.fragments.UserFragment;
import com.csl.macrologandroid.lifecycle.Session;
import com.csl.macrologandroid.notifications.NotificationSender;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;


public class MainActivity extends AppCompatActivity implements UserFragment.OnLogoutPressedListener {

    private BottomNavigationView navigation;

    private final ActivityResultLauncher<Intent> loginRegisterForResult =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            FoodCache.getInstance().clearCache();
                            DiaryLogCache.getInstance().clearCache();
                            ActivityCache.getInstance().clearCache();
                            navigation.setSelectedItemId(R.id.navigation_diary);
                        }
                    });

    private final NavigationBarView.OnItemSelectedListener mOnNavigationItemSelectedListener = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_diary:
                setFragment(new DiaryFragment());
                return true;
            case R.id.navigation_food:
                setFragment(new FoodFragment());
                return true;
            case R.id.navigation_dish:
                setFragment(new DishFragment());
                return true;
            case R.id.navigation_user:
                UserFragment userFragment = new UserFragment();
                userFragment.setOnLogoutPressedListener(this::logout);
                setFragment(userFragment);
                return true;
            default:
                setFragment(new DiaryFragment());
        }
        return false;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NotificationSender.initNotificationSending(getApplicationContext());
        setFragment(new DiaryFragment());
        navigation = findViewById(R.id.navigation);
        navigation.setOnItemSelectedListener(mOnNavigationItemSelectedListener);

        if (!isLoggedIn()) {
            loginRegisterForResult.launch(new Intent(this, LoginActivity.class));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Session.getInstance().isExpired()) {
            Intent intent = new Intent(MainActivity.this, SplashscreenActivity.class);
            intent.putExtra("SESSION_EXPIRED", true);
            startActivity(intent);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Session.resetTimestamp();
    }

    private void logout() {
        getSharedPreferences("AUTH", MODE_PRIVATE).edit().remove("TOKEN").remove("USER").apply();
        UserSettingsCache.getInstance().clearCache();
        FoodCache.getInstance().clearCache();
        DiaryLogCache.getInstance().clearCache();
        ActivityCache.getInstance().clearCache();
        loginRegisterForResult.launch(new Intent(this, LoginActivity.class));
        navigation.callOnClick();
    }

    private void setFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, fragment);
        ft.commit();
    }

    private boolean isLoggedIn() {
        boolean tokenExpired = getIntent().getBooleanExtra("TOKEN_EXPIRED", false);
        String token = getSharedPreferences("AUTH", MODE_PRIVATE).getString("TOKEN", null);
        return token != null && !tokenExpired;
    }

    @Override
    public void onLogoutPressed() {
        logout();
    }

}
