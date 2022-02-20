package com.csl.macrologandroid.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.csl.macrologandroid.ActivityActivity;
import com.csl.macrologandroid.EditEntryActivity;
import com.csl.macrologandroid.R;
import com.csl.macrologandroid.adapters.DiaryPager;
import com.csl.macrologandroid.adapters.DiaryPagerAdaper;
import com.csl.macrologandroid.cache.ActivityCache;
import com.csl.macrologandroid.cache.DiaryLogCache;
import com.csl.macrologandroid.cache.UserSettingsCache;
import com.csl.macrologandroid.dtos.ActivityResponse;
import com.csl.macrologandroid.dtos.LogEntryResponse;
import com.csl.macrologandroid.dtos.MacrosResponse;
import com.csl.macrologandroid.dtos.UserSettingsResponse;
import com.csl.macrologandroid.models.Meal;
import com.csl.macrologandroid.services.ActivityService;
import com.csl.macrologandroid.services.UserService;
import com.csl.macrologandroid.util.DateParser;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.Disposable;

import static android.content.Context.MODE_PRIVATE;

public class DiaryFragment extends Fragment {

    private static final int ADD_LOG_ENTRY_ID = 345;
    private static final int EDIT_LOG_ENTRY_ID = 456;
    private static final int EDIT_ACTIVITY_ID = 567;

    private View view;
    private DiaryPager viewPager;
    private DiaryLogCache logEntryCache;
    private ActivityCache activityCache;
    private int goalProtein;
    private int goalFat;
    private int goalCarbs;
    private int goalCalories;
    private Date selectedDate;

    private Disposable disposable;
    private DiaryPagerAdaper adapter;
    private ActivityService activityService;

    public DiaryFragment() {
        // Non arg constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logEntryCache = DiaryLogCache.getInstance();
        activityCache = ActivityCache.getInstance();
        activityService = new ActivityService(getToken());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (ADD_LOG_ENTRY_ID):
            case (EDIT_LOG_ENTRY_ID):
                if (resultCode == Activity.RESULT_OK) {
                    logEntryCache.removeFromCache(selectedDate);
                    setupViewPager(view);
                }
                break;
            case (EDIT_ACTIVITY_ID):
                if (resultCode == Activity.RESULT_OK) {
                    activityCache.removeFromCache(selectedDate);
                    setupViewPager(view);
                }
            default:
                break;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_diary, container, false);

        SwipeRefreshLayout pullToRefresh = view.findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(() -> {
            logEntryCache.clearCache();
            activityCache.clearCache();
            setupViewPager(view);
            pullToRefresh.setRefreshing(false);
        });

        UserSettingsResponse userSettings = UserSettingsCache.getInstance().getCache();
        if (userSettings == null) {
            UserService userService = new UserService(getToken());
            disposable = userService.getUserSettings()
                    .subscribe(res -> {
                                UserSettingsCache.getInstance().updateCache(res);
                                setGoalIntake(res);
                                updateTotals(DateParser.parse(DateParser.format(new Date())));
                            },
                            err -> Log.e(this.getClass().getName(), Objects.requireNonNull(err.getMessage())));
        } else {
            setGoalIntake(userSettings);
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle bundle) {
        setupViewPager(view);

        TextView dateTextView = view.findViewById(R.id.diary_date);
        dateTextView.setOnClickListener(v -> showDateDialog());
        ImageView arrowLeft = view.findViewById(R.id.arrow_left);
        ImageView arrowRight = view.findViewById(R.id.arrow_right);
        arrowLeft.setOnClickListener(args -> viewPager.arrowScroll(View.FOCUS_LEFT));
        arrowRight.setOnClickListener(args -> viewPager.arrowScroll(View.FOCUS_RIGHT));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (disposable != null) {
            disposable.dispose();
        }
        adapter.disposeServiceCall();
    }

    private void startEditMeal(Meal meal) {
        Intent intent = new Intent(getActivity(), EditEntryActivity.class);
        List<LogEntryResponse> entries = logEntryCache.getFromCache(selectedDate);
        List<LogEntryResponse> filteredEntries = new ArrayList<>();
        for (LogEntryResponse entry : entries) {
            if (entry.getMeal().equals(meal)) {
                filteredEntries.add(entry);
            }
        }
        entries = filteredEntries;

        intent.putExtra("DATE", selectedDate);
        intent.putExtra("MEAL", meal);
        intent.putExtra("LOGENTRIES", (Serializable) entries);
        startActivityForResult(intent, EDIT_LOG_ENTRY_ID);
    }

    private void startEditActivity() {
        Intent intent = new Intent(getActivity(), ActivityActivity.class);
        List<ActivityResponse> activities = activityCache.getFromCache(selectedDate);
        intent.putExtra("DATE", selectedDate);
        intent.putExtra("ACTIVITIES", (Serializable) activities);
        startActivityForResult(intent, EDIT_ACTIVITY_ID);
    }

    private void setGoalIntake(UserSettingsResponse settings) {
        goalProtein = settings.getGoalProtein();
        goalFat = settings.getGoalFat();
        goalCarbs = settings.getGoalCarbs();
        goalCalories = (goalProtein * 4) + (goalFat * 9) + (goalCarbs * 4);
    }

    private void forceSyncActivity() {
        disposable = activityService.getActivitiesForDay(selectedDate).subscribe(
                res -> {
                    logEntryCache.clearCache();
                    activityCache.clearCache();
                    setupViewPager(view);
                },
                err -> Log.e(this.getClass().getName(), err.getMessage())
        );
    }

    private void setupViewPager(View view) {
        TextView diaryDate = view.findViewById(R.id.diary_date);

        if (selectedDate == null) {
            selectedDate = DateParser.parse(DateParser.format(new Date()));
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        diaryDate.setText(simpleDateFormat.format(selectedDate));

        viewPager = view.findViewById(R.id.diary_pager_layout);
        adapter = new DiaryPagerAdaper(Objects.requireNonNull(getContext()));
        adapter.setSelectedDate(selectedDate);
        adapter.setOnTotalsUpdateListener(this::updateTotals);
        adapter.setOnMealClickListener(this::startEditMeal);
        adapter.setOnActivityClickListener(this::startEditActivity);
        adapter.setOnActivityLinkClickListener(this::openLink);
        adapter.setOnActivitySyncListener(this::forceSyncActivity);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(getPositionFromDate(selectedDate));

        viewPager.arrowScroll(View.FOCUS_LEFT);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                // Not needed
            }

            @Override
            public void onPageSelected(int i) {
                selectedDate = getDateFromPosition(i);
                diaryDate.setText(simpleDateFormat.format(selectedDate));
                updateTotals(selectedDate);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                // Not needed
            }
        });

    }

    private void openLink(String activityId) {
        Uri intentUri = Uri.parse("https://www.strava.com/activities/")
                .buildUpon()
                .appendPath(activityId)
                .build();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, intentUri);
        startActivity(browserIntent);
    }

    private void updateTotals(Date date) {
        List<LogEntryResponse> entries = logEntryCache.getFromCache(date);
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

    private Date getDateFromPosition(int position) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, (position - 500));
        return DateParser.parse(DateParser.format(calendar.getTime()));
    }

    private int getPositionFromDate(Date date) {
        long difference = new Date().getTime() - date.getTime();
        long days = TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS);
        if (difference < 0) {
            days -= 1;
        }
        return 501 - (int) days;
    }

    private void showDateDialog() {
        DateDialogFragment dialog = new DateDialogFragment();
        dialog.setCurrentDate(selectedDate);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        dialog.setOnDialogResult(date -> {
            TextView dateTextView = view.findViewById(R.id.diary_date);
            dateTextView.setText(simpleDateFormat.format(date));
            selectedDate = date;
            setupViewPager(view);

        });
        dialog.show(Objects.requireNonNull(getActivity()).getSupportFragmentManager(), "WeighDialogFragment");
    }

    private String getToken() {
        return Objects.requireNonNull(this.getContext()).getSharedPreferences("AUTH", MODE_PRIVATE).getString("TOKEN", "");
    }
}
