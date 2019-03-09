package com.example.macrologandroid.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.macrologandroid.LoginActivity;
import com.example.macrologandroid.MainActivity;
import com.example.macrologandroid.Models.UserSetting;
import com.example.macrologandroid.R;
import com.example.macrologandroid.Services.UserService;

import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;

public class UserFragment extends Fragment {

    private UserService userService;
    private View view;
    private List<UserSetting> userSettings;

    private OnLogoutPressedListener callback;

    public UserFragment() {
        this.userService = new UserService();
    }

    public void setOnLogoutPressedListener(MainActivity main) {
        callback = main;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("CheckResult")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user, container, false);

        userService.getSettings()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    res -> {
                        System.out.println(res.toString());
                        this.userSettings = res;
                        setUserData();
                    },
                    err -> {
                        System.out.println(err.toString());
                    }

        );

        Button logoutButton = view.findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> callback.onLogoutPressed());

        return view;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void setUserData() {
        TextView userName = view.findViewById(R.id.user_name);
        userName.setText(findSetting("name"));

        TextView userAge = view.findViewById(R.id.user_age);
        userAge.setText(findSetting("age"));

        TextView userGender = view.findViewById(R.id.user_gender);
        userGender.setText(findSetting("gender"));

        TextView userHeight = view.findViewById(R.id.user_height);
        userHeight.setText(findSetting("height"));

        TextView userWeight = view.findViewById(R.id.user_weight);
        userWeight.setText(findSetting("weight"));

        TextView userActivity = view.findViewById(R.id.user_activity);
        userActivity.setText(findSetting("activity"));


        TextView userProtein = view.findViewById(R.id.user_protein);
        userProtein.setText(findSetting("goalProtein"));

        TextView userFat = view.findViewById(R.id.user_fat);
        userFat.setText(findSetting("goalFat"));

        TextView userCarbs = view.findViewById(R.id.user_carbs);
        userCarbs.setText(findSetting("goalCarbs"));
    }

    private String findSetting(String identifier) {
        UserSetting setting = userSettings.stream()
                .filter(s -> s.getName().equals(identifier))
                .findAny()
                .orElse(new UserSetting(0, "", ""));
        return setting.getValue();
    }

    public interface OnLogoutPressedListener {
        void onLogoutPressed();
    }

}
