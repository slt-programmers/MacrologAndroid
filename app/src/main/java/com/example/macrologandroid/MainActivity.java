package com.example.macrologandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.example.macrologandroid.Fragments.DiaryFragment;
import com.example.macrologandroid.Fragments.MealsFragment;
import com.example.macrologandroid.Fragments.UserFragment;


public class MainActivity extends AppCompatActivity {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_diary:
                    changeFragment(new DiaryFragment());
                    return true;
                case R.id.navigation_meals:
                    changeFragment(new MealsFragment());
                    return true;
                case R.id.navigation_user:
                    changeFragment(new UserFragment());
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(isLoggedIn()) {
            getSharedPreferences("AUTH", MODE_PRIVATE).edit().remove("TOKEN").remove("USER").apply();

        } else {
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        init();
    }

    private void init() {
        changeFragment(new DiaryFragment());
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private void changeFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    private boolean isLoggedIn() {
        String token = getSharedPreferences("AUTH", MODE_PRIVATE).getString("TOKEN", null);
        return token != null;
    }

}
