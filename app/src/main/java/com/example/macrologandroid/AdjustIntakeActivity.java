package com.example.macrologandroid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

import com.example.macrologandroid.DTO.UserSettingResponse;
import com.example.macrologandroid.Fragments.ChangeCaloriesFragment;
import com.example.macrologandroid.Lifecycle.Session;
import com.example.macrologandroid.Models.ChangeGoalMacros;
import com.example.macrologandroid.Fragments.ChangeMacrosFragment;
import com.example.macrologandroid.Models.UserSettings;
import com.example.macrologandroid.Services.UserService;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class AdjustIntakeActivity extends AppCompatActivity {

    private UserService service;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_adjust_intake);
        Intent intent = getIntent();

        service = new UserService();

        UserSettings userSettings = (UserSettings) intent.getSerializableExtra("userSettings");

        Button changeMacros = findViewById(R.id.button_macros);
        changeMacros.setOnClickListener(v -> {
            ChangeMacrosFragment fragment = new ChangeMacrosFragment();
            Bundle goalMacros = new Bundle();
            goalMacros.putInt("goalProtein", userSettings.getProtein());
            goalMacros.putInt("goalFat", userSettings.getFat());
            goalMacros.putInt("goalCarbs", userSettings.getCarbs());
            fragment.setArguments(goalMacros);
            setFragment(fragment);
        });

        Button changeCalories = findViewById(R.id.button_calories);
        changeCalories.setOnClickListener(v -> {
            ChangeCaloriesFragment fragment = new ChangeCaloriesFragment();
            Bundle settings = new Bundle();
            settings.putSerializable("userSettings", userSettings);
            fragment.setArguments(settings);
            setFragment(fragment);
        });

        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> {
            saveGoalMacros();
        });

        Button backButton = findViewById(R.id.backbutton);
        backButton.setOnClickListener(v -> finish());
    }

    @Override
    public void onPause() {
        super.onPause();
        Session.getInstance().resetTimestamp();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Session.getInstance().isExpired()) {
            Intent intent = new Intent(AdjustIntakeActivity.this, SplashscreenActivity.class);
            intent.putExtra("SESSION_EXPIRED", true);
            startActivity(intent);        }
    }

    private void setFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    @SuppressLint("CheckResult")
    private void saveGoalMacros() {
        FragmentManager fm = getSupportFragmentManager();
        List<Fragment> list = fm.getFragments();
        if (!list.isEmpty()) {
            Fragment frag = list.get(0);
            if (frag instanceof ChangeGoalMacros) {
                Bundle goal = ((ChangeGoalMacros) frag).getGoalMacros();
                List<Observable<ResponseBody>> obsList = new ArrayList<>();
                obsList.add(service.putSetting(new UserSettingResponse(1, "goalProtein",
                        String.valueOf(goal.getInt("goalProtein")))));
                obsList.add(service.putSetting(new UserSettingResponse(1, "goalFat",
                        String.valueOf(goal.getInt("goalFat")))));
                obsList.add(service.putSetting(new UserSettingResponse(1, "goalCarbs",
                        String.valueOf(goal.getInt("goalCarbs")))));

                Observable.zip(obsList, i -> i)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(res -> {
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("RELOAD", true);
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
                        }, err -> Log.d("Macrolog", err.getMessage()));

            }
        }
    }

}
