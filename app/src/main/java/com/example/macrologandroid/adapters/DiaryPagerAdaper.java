package com.example.macrologandroid.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.macrologandroid.cache.DiaryLogCache;
import com.example.macrologandroid.dtos.LogEntryResponse;
import com.example.macrologandroid.models.Meal;
import com.example.macrologandroid.R;
import com.example.macrologandroid.services.LogEntryService;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DiaryPagerAdaper extends PagerAdapter {

    private Context context;
    private LogEntryService service;
    private LocalDate selectedDate;
    private int mCurrentPosition = -1;

    private static final int LOOP_COUNT = 1000;
    private static final int START_COUNT = 500;

    private Disposable disposable;
    private OnTableClickListener onTableClickListener;
    private OnTotalUpdateListener onTotalUpdateListener;

    public void setOnTotalsUpdateListener(OnTotalUpdateListener listener) {
        this.onTotalUpdateListener = listener;
    }

    public void setOnTableClickListener(OnTableClickListener listener) {
        this.onTableClickListener = listener;
    }

    public DiaryPagerAdaper(Context context) {
        this.context = context;
        this.service = new LogEntryService();
    }

    public void setSelectedDate(LocalDate date) {
        this.selectedDate = date;
    }

    @Override
    @NonNull
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.layout_diary_page, container, false);

        LocalDate date = getDateFromPosition(position);

        List<LogEntryResponse> entries = DiaryLogCache.getInstance().getFromCache(date);
        if (entries == null) {
            disposable = service.getLogsForDay(date)
                    .subscribe(
                            res -> {
                                DiaryLogCache.getInstance().addToCache(date, res);
                                notifyForTotalsUpdate(date);
                                fillDiaryPage(res, layout);
                                container.addView(layout);
                            }, err -> Log.e(this.getClass().getName(), err.getMessage())
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
            onTotalUpdateListener.updateTotals(date);
        }
    }

    @Override
    public void setPrimaryItem(@NotNull ViewGroup container, int position, @NotNull Object object) {
        super.setPrimaryItem(container, position, object);
        if (position != mCurrentPosition) {
            LinearLayout diaryPage = (LinearLayout) object;
            DiaryPager pager = (DiaryPager) container;
            mCurrentPosition = position;
            pager.measureCurrentView(diaryPage);
        }
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
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

    public void disposeServiceCall() {
        if (disposable != null) {
            disposable.dispose();
        }
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

        breakfastTable.setOnClickListener((v) -> onTableClickListener.onTableClick(Meal.BREAKFAST));
        lunchTable.setOnClickListener((v) -> onTableClickListener.onTableClick(Meal.LUNCH));
        dinnerTable.setOnClickListener((v) -> onTableClickListener.onTableClick(Meal.DINNER));
        snacksTable.setOnClickListener((v) -> onTableClickListener.onTableClick(Meal.SNACKS));
    }

    private void addEntryToTable(TableLayout table, LogEntryResponse entry) {
        TableRow row = new TableRow(context);
        TextView name = getCustomizedTextView(new TextView(context));
        TableRow.LayoutParams lp = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 8.0f);
        name.setText(entry.getFood().getName());
        name.setLayoutParams(lp);

        TextView protein = getCustomizedMacroTextView(entry.getMacrosCalculated().getProtein());
        TextView fat = getCustomizedMacroTextView(entry.getMacrosCalculated().getFat());
        TextView carbs = getCustomizedMacroTextView(entry.getMacrosCalculated().getCarbs());
        TextView kcal = getCustomizedCalorieTextView(entry.getMacrosCalculated().getCalories());

        row.addView(name);
        row.addView(protein);
        row.addView(fat);
        row.addView(carbs);
        row.addView(kcal);
        table.addView(row);
    }

    private TextView getCustomizedCalorieTextView(double text) {
        TextView view = new TextView(context);
        view.setText(String.format(Locale.ENGLISH, "%1.0f", text));
        setTextViewLayout(view);
        return getCustomizedTextView(view);
    }

    private TextView getCustomizedMacroTextView(double text) {
        TextView view = new TextView(context);
        view.setText(String.format(Locale.ENGLISH, "%.1f", text));
        setTextViewLayout(view);
        return getCustomizedTextView(view);
    }

    private TextView getCustomizedTextView(TextView view) {
        view.setTextSize(16);
        return view;
    }

    private void setTextViewLayout(TextView view) {
        TableRow.LayoutParams lp = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0.1f);
        view.setLayoutParams(lp);
        view.setGravity(Gravity.END);
    }

    public interface OnTotalUpdateListener {
        void updateTotals(LocalDate date);
    }

    public interface OnTableClickListener {
        void onTableClick(Meal meal);
    }

}
