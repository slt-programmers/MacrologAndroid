package com.example.macrologandroid.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.macrologandroid.Models.UserSettingResponse;
import com.example.macrologandroid.R;
import com.example.macrologandroid.Services.UserService;

import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class UserFragment extends Fragment {

    private UserService userService;
    private View view;
    private List<UserSettingResponse> userSettings;

    public UserFragment() {
        this.userService = new UserService();
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
                        fillView();
                    },
                    err -> {
                        System.out.println(err.toString());
                    }

        );

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

    private void fillView() {
        TextView username = view.findViewById(R.id.user_name);
        List<UserSettingResponse> setting = userSettings.stream().filter(s -> s.getName().equals("name")).collect(Collectors.toList());
        username.setText(setting.get(0).getValue());
    }

}
