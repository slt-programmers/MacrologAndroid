package com.csl.macrologandroid.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.viewpager.widget.PagerAdapter;

import com.csl.macrologandroid.R;
import com.csl.macrologandroid.cache.ActivityCache;
import com.csl.macrologandroid.cache.DiaryLogCache;
import com.csl.macrologandroid.dtos.ActivityResponse;
import com.csl.macrologandroid.dtos.LogEntryResponse;
import com.csl.macrologandroid.models.Meal;
import com.csl.macrologandroid.services.ActivityService;
import com.csl.macrologandroid.services.LogEntryService;
import com.csl.macrologandroid.util.DateParser;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.disposables.Disposable;

import static android.content.Context.MODE_PRIVATE;

public class DiaryPagerAdaper extends PagerAdapter {

    private final Context context;
    private final LogEntryService logService;
    private final ActivityService activityService;
    private Date selectedDate;
    private int mCurrentPosition = -1;

    private static final int LOOP_COUNT = 1000;
    private static final int START_COUNT = 500;

    private Disposable disposableLogs;
    private Disposable disposableActs;
    private OnMealClickListener onMealClickListener;
    private OnActivityClickListener onActivityClickListener;
    private OnTotalUpdateListener onTotalUpdateListener;

    public void setOnTotalsUpdateListener(OnTotalUpdateListener listener) {
        this.onTotalUpdateListener = listener;
    }

    public void setOnMealClickListener(OnMealClickListener listener) {
        this.onMealClickListener = listener;
    }

    public void setOnActivityClickListener(OnActivityClickListener listener) {
        this.onActivityClickListener = listener;
    }

    public DiaryPagerAdaper(Context context) {
        this.context = context;
        this.logService = new LogEntryService(context.getSharedPreferences("AUTH", MODE_PRIVATE).getString("TOKEN", ""));
        this.activityService = new ActivityService(context.getSharedPreferences("AUTH", MODE_PRIVATE).getString("TOKEN", ""));
    }

    public void setSelectedDate(Date date) {
        this.selectedDate = date;
    }

    @Override
    @NonNull
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.layout_diary_page, container, false);

        Date date = getDateFromPosition(position);

        List<LogEntryResponse> entries = DiaryLogCache.getInstance().getFromCache(date);
        if (entries == null) {
            disposableLogs = logService.getLogsForDay(date)
                    .subscribe(
                            res -> {
                                DiaryLogCache.getInstance().addToCache(date, res);
                                notifyForTotalsUpdate(date);
                                fillLogEntriesOnPage(res, layout);
                                container.addView(layout);
                            }, err -> Log.e(this.getClass().getName(), err.getMessage())
                    );
            disposableActs = activityService.getActivitiesForDay(date)
                    .subscribe(
                            res -> {
                                ActivityCache.getInstance().addToCache(date, res);
                                notifyForTotalsUpdate(date);
                                fillActivitiesOnPage(res, layout);
                            },
                            err -> Log.e(this.getClass().getName(), err.getMessage())
                    );
        } else {
            fillLogEntriesOnPage(entries, layout);
            notifyForTotalsUpdate(date);
            container.addView(layout);
        }
        return layout;
    }

    private void notifyForTotalsUpdate(Date date) {
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
        if (disposableLogs != null) {
            disposableLogs.dispose();
        }
    }

    private Date getDateFromPosition(int position) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, (position - START_COUNT));
        return DateParser.parse(DateParser.format(calendar.getTime()));
    }

    private void fillLogEntriesOnPage(List<LogEntryResponse> entries, ViewGroup view) {
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

        breakfastTable.setOnClickListener(v -> onMealClickListener.onMealClick(Meal.BREAKFAST));
        lunchTable.setOnClickListener(v -> onMealClickListener.onMealClick(Meal.LUNCH));
        dinnerTable.setOnClickListener(v -> onMealClickListener.onMealClick(Meal.DINNER));
        snacksTable.setOnClickListener(v -> onMealClickListener.onMealClick(Meal.SNACKS));
    }

    private void fillActivitiesOnPage(List<ActivityResponse> activities, ViewGroup view) {
        TableLayout activitiesTable = view.findViewById(R.id.activities_table);
        for (ActivityResponse activity : activities) {
            TableRow row = new TableRow(context);
            TextView name = getCustomizedTextView(new TextView(context));
            TableRow.LayoutParams lp = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 8.0f);
            name.setText(activity.getName());
            name.setLayoutParams(lp);

            // TODO custom larger kcal textview?
            TextView kcal = getCustomizedCalorieTextView(activity.getCalories());
            row.addView(name);
            row.addView(kcal);
            activitiesTable.addView(row);
        }
        activitiesTable.setOnClickListener(v -> onActivityClickListener.onActivityClick());
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
        Typeface typeface = ResourcesCompat.getFont(context, R.font.assistant_light);
        view.setTypeface(typeface);
        return view;
    }

    private void setTextViewLayout(TextView view) {
        TableRow.LayoutParams lp = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0.1f);
        view.setLayoutParams(lp);
        view.setGravity(Gravity.END);
    }

    public interface OnTotalUpdateListener {
        void updateTotals(Date date);
    }

    public interface OnMealClickListener {
        void onMealClick(Meal meal);
    }

    public interface OnActivityClickListener {
        void onActivityClick();
    }

}
