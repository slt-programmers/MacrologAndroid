package com.example.macrologandroid;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
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
                    setFragment(new DiaryFragment());
                    return true;
                case R.id.navigation_meals:
                    setFragment(new MealsFragment());
                    return true;
                case R.id.navigation_user:
                    setFragment(new UserFragment());
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        Log.d("LOG", menuItem.toString());
        if (menuItem.getTitle().equals(getResources().getString(R.string.logout))) {
            logout();
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        init();
    }

    private void logout() {
        getSharedPreferences("AUTH", MODE_PRIVATE).edit().remove("TOKEN").remove("USER").apply();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
    }

    private void init() {
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        setFragment(new DiaryFragment());

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private void setFragment(Fragment fragment) {
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
