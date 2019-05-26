package com.example.macrologandroid.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.macrologandroid.AboutActivity;
import com.example.macrologandroid.AdjustIntakeActivity;
import com.example.macrologandroid.ChangePasswordActivity;
import com.example.macrologandroid.EditPersonalDetailsActivity;
import com.example.macrologandroid.MainActivity;
import com.example.macrologandroid.models.Gender;
import com.example.macrologandroid.models.UserSettings;
import com.example.macrologandroid.R;
import com.example.macrologandroid.services.UserService;

import org.jetbrains.annotations.NotNull;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class UserFragment extends Fragment {

    public static final int EDIT_DETAILS_ID = 123;
    private static final int ADJUST_INTAKE_ID = 234;

    private View view;
    private UserSettings userSettings;

    private OnLogoutPressedListener callback;

    public UserFragment() {
    }

    public void setOnLogoutPressedListener(MainActivity main) {
        callback = main;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (EDIT_DETAILS_ID):
            case (ADJUST_INTAKE_ID): {
                if (resultCode == Activity.RESULT_OK) {
                    fetchUserSettings();
                }
                break;
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("CheckResult")
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user, container, false);

        if (this.userSettings == null) {
            fetchUserSettings();
        } else {
            setUserData();
        }

        Button logoutButton = view.findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> {
            this.userSettings = null;
            callback.onLogoutPressed();
        });

        Button editDetails = view.findViewById(R.id.edit_details);
        editDetails.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditPersonalDetailsActivity.class);
            intent.putExtra("name", userSettings.getName());
            intent.putExtra("age", userSettings.getAge());
            intent.putExtra("gender", userSettings.getGender());
            intent.putExtra("height", userSettings.getHeight());
            intent.putExtra("weight", userSettings.getWeight());
            intent.putExtra("activity", userSettings.getActivity());
            startActivityForResult(intent, EDIT_DETAILS_ID);
        });

        Button adjustIntake = view.findViewById(R.id.adjust_intake);
        adjustIntake.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AdjustIntakeActivity.class);
            intent.putExtra("userSettings", userSettings);
            startActivityForResult(intent, ADJUST_INTAKE_ID);
        });

        Button changePassword = view.findViewById(R.id.change_password);
        changePassword.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
            startActivity(intent);
        });

        Button aboutButton = view.findViewById(R.id.about_button);
        aboutButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AboutActivity.class);
            startActivity(intent);
        });

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

    @SuppressLint("CheckResult")
    protected void fetchUserSettings() {
        UserService userService = new UserService();
        userService.getSettings()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        res -> {
                            this.userSettings = new UserSettings(res);
                            setUserData();
                        },
                        err -> Log.d(this.getClass().getName(), err.getMessage())

                );
    }

    private void setUserData() {
        TextView userName = view.findViewById(R.id.user_name);
        userName.setText(userSettings.getName());

        TextView userAge = view.findViewById(R.id.user_age);
        userAge.setText(String.valueOf(userSettings.getAge()));

        TextView userGender = view.findViewById(R.id.user_gender);
        Gender gender = userSettings.getGender();
        if (gender != null) {
            String genderStr = gender.toString();
            genderStr = genderStr.substring(0, 1) + genderStr.substring(1).toLowerCase();
            userGender.setText(genderStr);
        } else {
            userGender.setText(R.string.gender_unknown);
        }

        TextView userHeight = view.findViewById(R.id.user_height);
        String height = userSettings.getHeight() + " cm";
        userHeight.setText(height);

        TextView userWeight = view.findViewById(R.id.user_weight);
        String weight = userSettings.getWeight() + " kg";
        userWeight.setText(weight);

        TextView userActivity = view.findViewById(R.id.user_activity);
        String activity;
        switch (String.valueOf(userSettings.getActivity())) {
            case "1.2":
                activity = "Sedentary";
                break;
            case "1.375":
                activity = "Lightly active";
                break;
            case "1.55":
                activity = "Moderately active";
                break;
            case "1.725":
                activity = "Very active";
                break;
            case "1.9":
                activity = "Extremely active";
                break;
            default:
                activity = "Sedentary";
        }
        userActivity.setText(activity);

        TextView userProtein = view.findViewById(R.id.user_protein);
        String protein = userSettings.getProtein() + " gr";
        userProtein.setText(protein);

        TextView userFat = view.findViewById(R.id.user_fat);
        String fat = userSettings.getFat() + " gr";
        userFat.setText(fat);

        TextView userCarbs = view.findViewById(R.id.user_carbs);
        String carbs = userSettings.getCarbs() + " gr";
        userCarbs.setText(carbs);
    }

    public interface OnLogoutPressedListener {
        void onLogoutPressed();
    }

}
