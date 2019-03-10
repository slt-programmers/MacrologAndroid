package com.example.macrologandroid.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.macrologandroid.DTO.LogEntryResponse;
import com.example.macrologandroid.Models.Meal;
import com.example.macrologandroid.R;
import com.example.macrologandroid.Services.DiaryLogService;

import java.sql.Date;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DiaryFragment extends Fragment {

    private View view;

    public DiaryFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("CheckResult")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_diary, container, false);

        DiaryLogService service = new DiaryLogService();
        service.getLogsForDay(LocalDate.now())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    fillDiary(res);
                }, err -> {
                    Log.d("Macrolog", err.getMessage());
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

    private void fillDiary(List<LogEntryResponse> entries) {
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
    }

    private void addEntryToTable(TableLayout table, LogEntryResponse entry) {
        TableRow row = new TableRow(getContext());
        TextView name = getCustomizedTextView();
        TableRow.LayoutParams lp = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 3f);
        name.setText(entry.getFood().getName());
        name.setLayoutParams(lp);

        TableRow.LayoutParams lpSmall = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);

        TextView protein = getCustomizedTextView();
        protein.setText(String.format(Locale.ENGLISH,"%.1f", entry.getMacrosCalculated().getProtein()));
        protein.setLayoutParams(lpSmall);

        TextView fat = getCustomizedTextView();
        fat.setText(String.format(Locale.ENGLISH,"%.1f", entry.getMacrosCalculated().getFat()));
        fat.setLayoutParams(lpSmall);

        TextView carbs = getCustomizedTextView();
        carbs.setText(String.format(Locale.ENGLISH,"%.1f", entry.getMacrosCalculated().getCarbs()));
        carbs.setLayoutParams(lpSmall);

        row.addView(name);
        row.addView(protein);
        row.addView(fat);
        row.addView(carbs);
        table.addView(row);
    }

    private TextView getCustomizedTextView() {
        TextView view = new TextView(getContext());
        view.setTextSize(18);
        return view;
    }
}
