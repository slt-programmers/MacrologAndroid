package com.example.macrologandroid.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.macrologandroid.Adapters.DiaryPagerAdaper;
import com.example.macrologandroid.AddLogEntryActivity;
import com.example.macrologandroid.Cache.DiaryLogCache;
import com.example.macrologandroid.DTO.LogEntryResponse;
import com.example.macrologandroid.DTO.MacrosResponse;
import com.example.macrologandroid.Models.UserSettings;
import com.example.macrologandroid.R;
import com.example.macrologandroid.Services.UserService;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class DiaryFragment extends Fragment implements Serializable, DiaryPagerAdaper.OnTotalUpdateListener {

    private static final int ADD_LOG_ENTRY_ID = 345;

    private View view;
    private ViewPager viewPager;
    private DiaryLogCache cache;
    private UserService userService;
    private int goalProtein, goalFat, goalCarbs;
    private LocalDate selectedDate;

    public DiaryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cache = DiaryLogCache.getInstance();
        userService = new UserService();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (ADD_LOG_ENTRY_ID) : {
                if (resultCode == Activity.RESULT_OK) {
                    invalidateCache();
                    setupViewPager(view);
                }
                break;
            }
        }
    }


    @SuppressLint("CheckResult")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_diary, container, false);
        userService.getSettings().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe((result) -> {
                    setGoalIntake(new UserSettings(result));
                }, (error) -> {
                    System.out.print(error.getMessage());
                });

        FloatingActionButton button = view.findViewById(R.id.floating_button);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddLogEntryActivity.class);
            intent.putExtra("date", selectedDate);
            startActivityForResult(intent, ADD_LOG_ENTRY_ID);
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        setupViewPager(view);

        ImageView arrowLeft = view.findViewById(R.id.arrow_left);
        ImageView arrowRight = view.findViewById(R.id.arrow_right);
        arrowLeft.setOnClickListener((args) -> {
            viewPager.arrowScroll(View.FOCUS_LEFT);
        });

        arrowRight.setOnClickListener((args) -> {
            viewPager.arrowScroll(View.FOCUS_RIGHT);
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private void invalidateCache() {
        cache.removeFromCache(selectedDate);
    }

    private void setGoalIntake(UserSettings settings) {
        goalProtein = settings.getProtein();
        goalFat = settings.getFat();
        goalCarbs = settings.getCarbs();
    }

    private void setupViewPager(View view) {
        TextView diaryDate = view.findViewById(R.id.diary_date);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        selectedDate = LocalDate.now();
        diaryDate.setText(selectedDate.format(formatter));

        viewPager = view.findViewById(R.id.day_view_pager);
        DiaryPagerAdaper adapter = new DiaryPagerAdaper(getContext(), cache);
        adapter.setOnTotalsUpdateListener(this);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(501);

        viewPager.arrowScroll(View.FOCUS_LEFT);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int i) {
                selectedDate = getDateFromPosition(i);
                diaryDate.setText(selectedDate.format(formatter));
                updateTotals(selectedDate);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });

    }

    public void updateTotals(LocalDate date) {
        List<LogEntryResponse> entries = cache.getFromCache(date);
        double totalProtein = 0.0;
        double totalFat = 0.0;
        double totalCarbs = 0.0;

        if (entries != null) {
            for (LogEntryResponse entry : entries) {
                MacrosResponse macros = entry.getMacrosCalculated();
                totalProtein += macros.getProtein();
                totalFat += macros.getFat();
                totalCarbs += macros.getCarbs();
            }
        }

        TextView totalProteinView = view.findViewById(R.id.total_protein);
        totalProteinView.setText(String.valueOf(Math.round(totalProtein * 10) / 10f));
        TextView totalFatView = view.findViewById(R.id.total_fat);
        totalFatView.setText(String.valueOf(Math.round(totalFat * 10) /10f));
        TextView totalCarbsView = view.findViewById(R.id.total_carbs);
        totalCarbsView.setText(String.valueOf(Math.round(totalCarbs * 10) / 10f));

        setProgress(totalProtein, totalFat, totalCarbs);
    }

    private void setProgress(double protein, double fat, double carbs) {
        ProgressBar progressProtein = view.findViewById(R.id.progress_ring_protein);
        progressProtein.setMax(goalProtein);
        progressProtein.setProgress((int) Math.round(protein));

        ProgressBar progressFat = view.findViewById(R.id.progress_ring_fat);
        progressFat.setMax(goalFat);
        progressFat.setProgress((int) Math.round(fat));

        ProgressBar progressCarbs = view.findViewById(R.id.progress_ring_carbs);
        progressCarbs.setMax(goalCarbs);
        progressCarbs.setProgress((int) Math.round(carbs));
    }

    private LocalDate getDateFromPosition(int position) {
        return LocalDate.now().plusDays(position - 500);
    }

//    private int getPositionFromDate(LocalDate date) {
//
//    }

    @SuppressLint("CheckResult")
    public void updatePage() {
        userService = new UserService();
        userService.getSettings().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe((result) -> {
                    setGoalIntake(new UserSettings(result));
                    setupViewPager(view);
                }, (error) -> {
                    System.out.print(error.getMessage());
                });
    }
}
