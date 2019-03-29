package com.example.macrologandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.macrologandroid.Fragments.DiaryFragment;
import com.example.macrologandroid.Fragments.MealsFragment;
import com.example.macrologandroid.Fragments.UserFragment;


public class MainActivity extends AppCompatActivity implements UserFragment.OnLogoutPressedListener, LoginActivity.OnLoggedInListener {

    private static SharedPreferences preferences;

    private static MainActivity instance;

    public static MainActivity getInstance() {
        return instance;
    }

    private DiaryFragment diaryFragment;

    private UserFragment userFragment;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_diary:
                setFragment(diaryFragment);
                return true;
            case R.id.navigation_meals:
                setFragment(new MealsFragment());
                return true;
            case R.id.navigation_user:
                userFragment.setOnLogoutPressedListener(this);
                setFragment(userFragment);
                return true;
        }
        return false;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
        diaryFragment = new DiaryFragment();
        userFragment = new UserFragment();

        preferences = getSharedPreferences("AUTH", MODE_PRIVATE);

        setFragment(diaryFragment);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        if (!isLoggedIn()) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getTitle().equals(getResources().getString(R.string.logout))) {
            logout();
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public static SharedPreferences getPreferences() {
        return preferences;
    }

    private void logout() {
        getSharedPreferences("AUTH", MODE_PRIVATE).edit().remove("TOKEN").remove("USER").apply();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
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

    @Override
    public void updatePage() {
        diaryFragment.updatePage();
    }

}
