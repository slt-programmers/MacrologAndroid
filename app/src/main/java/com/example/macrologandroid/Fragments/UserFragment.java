package com.example.macrologandroid.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.macrologandroid.EditPersonalDetailsActivity;
import com.example.macrologandroid.MainActivity;
import com.example.macrologandroid.DTO.UserSettingResponse;
import com.example.macrologandroid.Models.UserSettings;
import com.example.macrologandroid.R;
import com.example.macrologandroid.Services.UserService;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class UserFragment extends Fragment {

    private UserService userService;
    private View view;
    private UserSettings userSettings;

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
                        this.userSettings = new UserSettings(res);
                        setUserData();
                    },
                    err -> {
                        System.out.println(err.toString());
                    }

        );

        Button logoutButton = view.findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> callback.onLogoutPressed());

        Button editDetails = view.findViewById(R.id.edit_details);
        editDetails.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditPersonalDetailsActivity.class);
            intent.putExtra("name", userSettings.getName());
            intent.putExtra("age", userSettings.getAge());
            intent.putExtra("gender", userSettings.getGender());
            intent.putExtra("height", userSettings.getHeight());
            intent.putExtra("weight", userSettings.getWeight());
            intent.putExtra("activity", userSettings.getActivity());
            startActivity(intent);
        });

        Button adjustIntake = view.findViewById(R.id.adjust_intake);
        adjustIntake.setOnClickListener(v -> {});

        Button changePassword = view.findViewById(R.id.change_password);
        changePassword.setOnClickListener(v -> {});

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
        userName.setText(userSettings.getName());

        TextView userAge = view.findViewById(R.id.user_age);
        userAge.setText(userSettings.getAge());

        TextView userGender = view.findViewById(R.id.user_gender);
        userGender.setText(userSettings.getGender().toString());

        TextView userHeight = view.findViewById(R.id.user_height);
        userHeight.setText(userSettings.getHeight());

        TextView userWeight = view.findViewById(R.id.user_weight);
        userWeight.setText(String.valueOf(userSettings.getWeight()));

        TextView userActivity = view.findViewById(R.id.user_activity);
        userActivity.setText(String.valueOf(userSettings.getActivity()));


        TextView userProtein = view.findViewById(R.id.user_protein);
        userProtein.setText(userSettings.getProtein());

        TextView userFat = view.findViewById(R.id.user_fat);
        userFat.setText(userSettings.getFat());

        TextView userCarbs = view.findViewById(R.id.user_carbs);
        userCarbs.setText(userSettings.getCarbs());
    }


    public interface OnLogoutPressedListener {
        void onLogoutPressed();
    }

}
