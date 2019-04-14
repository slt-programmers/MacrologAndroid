package com.example.macrologandroid.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.macrologandroid.Cache.DiaryLogCache;
import com.example.macrologandroid.DTO.LogEntryResponse;
import com.example.macrologandroid.EditLogEntryActivity;
import com.example.macrologandroid.Fragments.DiaryFragment;
import com.example.macrologandroid.MainActivity;
import com.example.macrologandroid.Models.Meal;
import com.example.macrologandroid.R;
import com.example.macrologandroid.Services.DiaryLogService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DiaryPagerAdaper extends PagerAdapter {

    private Context context;
    private DiaryLogService service;
    private DiaryLogCache cache;
    private LocalDate selectedDate;
    private DiaryFragment diaryFragmentReference;

    private static final int LOOP_COUNT = 1000;
    private static final int START_COUNT = 500;

    private OnTotalUpdateListener callback;

    public void setOnTotalsUpdateListener(DiaryFragment fragment) {
        callback = fragment;
    }

    public DiaryPagerAdaper(Context context, DiaryLogCache cache, DiaryFragment diaryFragment) {
        this.context = context;
        this.cache = cache;
        this.service = new DiaryLogService();
        this.diaryFragmentReference = diaryFragment;
    }

    public void setSelectedDate(LocalDate date) {
        this.selectedDate = date;
    }

    @SuppressLint("CheckResult")
    @Override
    @NonNull
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.layout_diary_page, container, false);

        if (service.isTokenEmpty()) {
            return layout;
        }
        LocalDate date = getDateFromPosition(position);

        List<LogEntryResponse> entries = cache.getFromCache(date);
        if (entries == null) {
            service.getLogsForDay(date)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            res -> {
                                cache.addToCache(date, res);
                                notifyForTotalsUpdate(date);
                                fillDiaryPage(res, layout);
                                container.addView(layout);
                            }, err -> {
                                Log.d("Macrolog", err.getMessage());
                            }
                    );
        } else {
            fillDiaryPage(entries, layout);
            notifyForTotalsUpdate(date);
            container.addView(layout);
        }
        return layout;
    }

    private void notifyForTotalsUpdate(LocalDate date) {
        if (date.equals(selectedDate)) {
            callback.updateTotals(date);
        }
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position,@NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return LOOP_COUNT;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    private LocalDate getDateFromPosition(int position) {
        return LocalDate.now().plusDays(position - START_COUNT);
    }

    private void fillDiaryPage(List<LogEntryResponse> entries, ViewGroup view) {
        TableLayout breakfastTable = view.findViewById(R.id.breakfast_table);
        TableLayout lunchTable = view.findViewById(R.id.lunch_table);
        TableLayout dinnerTable = view.findViewById(R.id.dinner_table);
        TableLayout snacksTable = view.findViewById(R.id.snacks_table);
        for (LogEntryResponse entry : entries) {
            if (entry.getMeal() == Meal.BREAKFAST) {
                addEntryToTable(breakfastTable, entry);
            } else if (entry.getMeal() == Meal.LUNCH) {
                addEntryToTable(lunchTable, entry);
            } else if (entry.getMeal() == Meal.DINNER) {
                addEntryToTable(dinnerTable, entry);
            } else {
                addEntryToTable(snacksTable, entry);
            }
        }

        breakfastTable.setOnClickListener((v) -> {
            diaryFragmentReference.startEditActivity(Meal.BREAKFAST);
        });
        lunchTable.setOnClickListener((v) -> {
            diaryFragmentReference.startEditActivity(Meal.LUNCH);
        });
        dinnerTable.setOnClickListener((v) -> {
            diaryFragmentReference.startEditActivity(Meal.DINNER);
        });
        snacksTable.setOnClickListener((v) -> {
            diaryFragmentReference.startEditActivity(Meal.SNACKS);
        });

    }

    private void addEntryToTable(TableLayout table, LogEntryResponse entry) {
        TableRow row = new TableRow(context);
        TextView name = getCustomizedTextView();
        TableRow.LayoutParams lp = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 8.0f);
        name.setText(entry.getFood().getName());
        name.setLayoutParams(lp);

        TextView protein = getCustomizedMacroTextView(entry.getMacrosCalculated().getProtein());
        TextView fat = getCustomizedMacroTextView(entry.getMacrosCalculated().getFat());
        TextView carbs = getCustomizedMacroTextView(entry.getMacrosCalculated().getCarbs());

        row.addView(name);
        row.addView(protein);
        row.addView(fat);
        row.addView(carbs);
        table.addView(row);
    }

    private TextView getCustomizedMacroTextView(double text) {
        TextView view = new TextView(context);
        view.setText(String.format(Locale.ENGLISH, "%.1f", text));
        TableRow.LayoutParams lp = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0.1f);
        view.setLayoutParams(lp);
        view.setGravity(Gravity.END);

        if (context != null) {
            final float scale = context.getResources().getDisplayMetrics().density;
            int pixels = (int) (48 * scale + 0.5f);
            view.setWidth(pixels);
        } else {
            view.setWidth(120);
        }
        view.setTextSize(16);
        return view;
    }

    private TextView getCustomizedTextView() {
        TextView view = new TextView(context);
        view.setTextSize(16);
        return view;
    }

    public interface OnTotalUpdateListener {
        void updateTotals(LocalDate date);
    }

}
