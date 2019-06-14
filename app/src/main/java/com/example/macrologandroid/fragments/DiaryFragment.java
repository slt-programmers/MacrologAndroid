package com.example.macrologandroid.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.macrologandroid.adapters.DiaryPager;
import com.example.macrologandroid.adapters.DiaryPagerAdaper;
import com.example.macrologandroid.AddLogEntryActivity;
import com.example.macrologandroid.cache.DiaryLogCache;
import com.example.macrologandroid.cache.UserSettingsCache;
import com.example.macrologandroid.dtos.LogEntryResponse;
import com.example.macrologandroid.dtos.MacrosResponse;
import com.example.macrologandroid.EditLogEntryActivity;
import com.example.macrologandroid.dtos.UserSettingsResponse;
import com.example.macrologandroid.models.Meal;
import com.example.macrologandroid.R;
import com.example.macrologandroid.services.UserService;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DiaryFragment extends Fragment implements Serializable {

    private static final int ADD_LOG_ENTRY_ID = 345;
    private static final int EDIT_LOG_ENTRY_ID = 456;

    private View view;
    private DiaryPager viewPager;
    private DiaryLogCache cache;
    private int goalProtein, goalFat, goalCarbs, goalCalories;
    private LocalDate selectedDate;

    private Disposable disposable;
    private DiaryPagerAdaper adapter;

    public DiaryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cache = DiaryLogCache.getInstance();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (ADD_LOG_ENTRY_ID):
            case (EDIT_LOG_ENTRY_ID): {
                if (resultCode == Activity.RESULT_OK) {
                    invalidateCache();
                    setupViewPager(view);
                }
                break;
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_diary, container, false);

        SwipeRefreshLayout pullToRefresh = view.findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(() -> {
            cache.clearCache();
            setupViewPager(view);
            pullToRefresh.setRefreshing(false);
        });

        UserSettingsResponse userSettings = UserSettingsCache.getInstance().getCache();
        if (userSettings == null) {
            UserService userService = new UserService();
            disposable = userService.getUserSettings().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(res -> {
                                UserSettingsCache.getInstance().updateCache(res);
                                setGoalIntake(res);
                                updateTotals(LocalDate.now());
                            },
                            (error) -> Log.e(this.getClass().getName(), error.getMessage()));
        } else {
            setGoalIntake(userSettings);
        }

        FloatingActionButton button = view.findViewById(R.id.floating_button);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddLogEntryActivity.class);
            intent.putExtra("DATE", selectedDate);
            startActivityForResult(intent, ADD_LOG_ENTRY_ID);
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle bundle) {
        setupViewPager(view);

        TextView dateTextView = view.findViewById(R.id.diary_date);
        dateTextView.setOnClickListener(v -> showDateDialog());
        ImageView arrowLeft = view.findViewById(R.id.arrow_left);
        ImageView arrowRight = view.findViewById(R.id.arrow_right);
        arrowLeft.setOnClickListener((args) -> viewPager.arrowScroll(View.FOCUS_LEFT));

        arrowRight.setOnClickListener((args) -> viewPager.arrowScroll(View.FOCUS_RIGHT));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (disposable != null) {
            disposable.dispose();
        }
        adapter.disposeServiceCall();
    }

    public void startEditActivity(Meal meal) {
        Intent intent = new Intent(getActivity(), EditLogEntryActivity.class);
        List<LogEntryResponse> entries = cache.getFromCache(selectedDate);
        entries = entries.stream().filter(entry -> entry.getMeal().equals(meal)).collect(Collectors.toList());
        intent.putExtra("DATE", selectedDate);
        intent.putExtra("MEAL", meal);
        intent.putExtra("LOGENTRIES", (Serializable) entries);
        startActivityForResult(intent, EDIT_LOG_ENTRY_ID);
    }

    private void invalidateCache() {
        cache.removeFromCache(selectedDate);
    }

    private void setGoalIntake(UserSettingsResponse settings) {
        goalProtein = settings.getGoalProtein();
        goalFat = settings.getGoalFat();
        goalCarbs = settings.getGoalCarbs();
        goalCalories = (goalProtein * 4) + (goalFat * 9) + (goalCarbs * 4);
    }

    private void setupViewPager(View view) {
        TextView diaryDate = view.findViewById(R.id.diary_date);

        if (selectedDate == null) {
            selectedDate = LocalDate.now();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        diaryDate.setText(selectedDate.format(formatter));

        viewPager = view.findViewById(R.id.day_view_pager);
        adapter = new DiaryPagerAdaper(getContext());
        adapter.setSelectedDate(selectedDate);
        adapter.setOnTotalsUpdateListener(this::updateTotals);
        adapter.setOnTableClickListener(this::startEditActivity);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(getPositionFromDate(selectedDate));

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
        int totalCalories = 0;

        if (entries != null) {
            for (LogEntryResponse entry : entries) {
                MacrosResponse macros = entry.getMacrosCalculated();
                totalProtein += macros.getProtein();
                totalFat += macros.getFat();
                totalCarbs += macros.getCarbs();
            }
            totalCalories = (int) ((totalProtein * 4) + (totalFat * 9) + (totalCarbs * 4));
        }

        TextView totalProteinView = view.findViewById(R.id.total_protein);
        totalProteinView.setText(String.valueOf(Math.round(totalProtein * 10) / 10f));
        TextView totalFatView = view.findViewById(R.id.total_fat);
        totalFatView.setText(String.valueOf(Math.round(totalFat * 10) / 10f));
        TextView totalCarbsView = view.findViewById(R.id.total_carbs);
        totalCarbsView.setText(String.valueOf(Math.round(totalCarbs * 10) / 10f));

        TextView totalCaloriesView = view.findViewById(R.id.total_calories);
        totalCaloriesView.setText(String.format(Locale.getDefault(), "%d/%d", totalCalories, goalCalories));

        setProgress(totalProtein, totalFat, totalCarbs);
    }

    private void setProgress(double protein, double fat, double carbs) {
        ProgressBar progressProtein = view.findViewById(R.id.progress_ring_protein);
        ProgressBar surplusProtein = view.findViewById(R.id.outer_progress_ring_protein);
        progressProtein.setMax(goalProtein);
        progressProtein.setProgress((int) Math.round(protein));
        surplusProtein.setMax(goalProtein);
        if (protein < goalProtein) {
            surplusProtein.setVisibility(View.INVISIBLE);
        } else {
            surplusProtein.setVisibility(View.VISIBLE);
            surplusProtein.setProgress((int) Math.round(protein - goalProtein));
        }

        ProgressBar progressFat = view.findViewById(R.id.progress_ring_fat);
        ProgressBar surplusFat = view.findViewById(R.id.outer_progress_ring_fat);
        progressFat.setMax(goalFat);
        progressFat.setProgress((int) Math.round(fat));
        if (fat < goalFat) {
            surplusFat.setVisibility(View.INVISIBLE);
        } else {
            surplusFat.setVisibility(View.VISIBLE);
            surplusFat.setProgress((int) Math.round(fat - goalFat));
        }

        ProgressBar progressCarbs = view.findViewById(R.id.progress_ring_carbs);
        ProgressBar surplusCarbs = view.findViewById(R.id.outer_progress_ring_carbs);
        progressCarbs.setMax(goalCarbs);
        progressCarbs.setProgress((int) Math.round(carbs));
        if (carbs < goalCarbs) {
            surplusCarbs.setVisibility(View.INVISIBLE);
        } else {
            surplusCarbs.setVisibility(View.VISIBLE);
            surplusCarbs.setProgress((int) Math.round(carbs - goalCarbs));
        }
    }

    private LocalDate getDateFromPosition(int position) {
        return LocalDate.now().plusDays(position - 500);
    }

    private int getPositionFromDate(LocalDate date) {
        return 501 + (int) ChronoUnit.DAYS.between(LocalDate.now(), date);
    }

    private void showDateDialog() {
        DateDialogFragment dialog = new DateDialogFragment();
        dialog.setCurrentDate(selectedDate);
        dialog.setOnDialogResult(date -> {
            TextView dateTextView = view.findViewById(R.id.diary_date);
            dateTextView.setText(date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
            selectedDate = date;
            setupViewPager(view);

        });
        dialog.show(getActivity().getSupportFragmentManager(), "WeighDialogFragment");
    }
}
