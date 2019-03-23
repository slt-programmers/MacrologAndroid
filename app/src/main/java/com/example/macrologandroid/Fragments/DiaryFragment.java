package com.example.macrologandroid.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.macrologandroid.Adapters.DiaryPagerAdaper;
import com.example.macrologandroid.Cache.DiaryLogCache;
import com.example.macrologandroid.DTO.LogEntryResponse;
import com.example.macrologandroid.DTO.MacrosResponse;
import com.example.macrologandroid.Models.Meal;
import com.example.macrologandroid.R;
import com.example.macrologandroid.Services.DiaryLogService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

public class DiaryFragment extends Fragment {

    private View view;

    private ViewPager viewPager;

    private DiaryLogCache cache;
    private DiaryLogService service;

    public DiaryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cache = DiaryLogCache.getInstance();
        service = new DiaryLogService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_diary, container, false);
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

    @SuppressLint("CheckResult")
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void setupViewPager(View view) {
        TextView diaryDate = view.findViewById(R.id.diary_date);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        diaryDate.setText(LocalDate.now().format(formatter));

        viewPager = view.findViewById(R.id.day_view_pager);
        DiaryPagerAdaper adapter = new DiaryPagerAdaper(getContext(), cache, service);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(501);

        viewPager.arrowScroll(View.FOCUS_LEFT);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int i) {
                LocalDate date = getDateFromPosition(i);
                diaryDate.setText(date.format(formatter));
                updateTotals(date);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });

    }

    private void updateTotals(LocalDate date) {
        List<LogEntryResponse> entries = cache.getFromCache(date);
        double totalProtein = 0.0;
        double totalFat = 0.0;
        double totalCarbs = 0.0;

        for (LogEntryResponse entry : entries) {
            MacrosResponse macros = entry.getMacrosCalculated();
            totalProtein += macros.getProtein();
            totalFat += macros.getFat();
            totalCarbs += macros.getCarbs();
        }

        TextView totalProteinView = view.findViewById(R.id.total_protein);
        totalProteinView.setText(String.valueOf(totalProtein));
        TextView totalFatView = view.findViewById(R.id.total_fat);
        totalFatView.setText(String.valueOf(totalFat));
        TextView totalCarbsView = view.findViewById(R.id.total_carbs);
        totalCarbsView.setText(String.valueOf(totalCarbs));
    }

    private LocalDate getDateFromPosition(int position) {
        return LocalDate.now().plusDays(position - 500);
    }

}
