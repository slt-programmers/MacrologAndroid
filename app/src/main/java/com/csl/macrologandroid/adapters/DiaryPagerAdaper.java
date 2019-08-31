package com.csl.macrologandroid.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
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

import java.util.ArrayList;
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
        List<ActivityResponse> activities = ActivityCache.getInstance().getFromCache(date);
        if (entries == null || activities == null) {
            disposableLogs = logService.getLogsForDay(date)
                    .subscribe(
                            res -> {
                                DiaryLogCache.getInstance().addToCache(date, res);
                                notifyForTotalsUpdate(date);
                                fillLogEntriesOnPage(res, layout);
                                container.removeView(layout);
                                container.addView(layout);
                            }, err -> Log.e(this.getClass().getName(), err.getMessage())
                    );
            disposableActs = activityService.getActivitiesForDay(date)
                    .subscribe(
                            res -> {
                                ActivityCache.getInstance().addToCache(date, res);
                                notifyForTotalsUpdate(date);
                                fillActivitiesOnPage(res, layout);
                                container.removeView(layout);
                                container.addView(layout);
                            },
                            err -> Log.e(this.getClass().getName(), err.getMessage())
                    );
        } else {
            fillLogEntriesOnPage(entries, layout);
            fillActivitiesOnPage(activities, layout);
            notifyForTotalsUpdate(date);
            container.removeView(layout);
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
        if (disposableActs != null) {
            disposableActs.dispose();
        }
    }

    private Date getDateFromPosition(int position) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, (position - START_COUNT));
        return DateParser.parse(DateParser.format(calendar.getTime()));
    }

    private void fillLogEntriesOnPage(List<LogEntryResponse> entries, ViewGroup view) {
        LinearLayout breakfastLayout = view.findViewById(R.id.breakfast_layout);
        LinearLayout lunchLayout = view.findViewById(R.id.lunch_layout);
        LinearLayout dinnerLayout = view.findViewById(R.id.dinner_layout);
        LinearLayout snacksLayout = view.findViewById(R.id.snacks_layout);

        List<LogEntryResponse> breakfastEntries = new ArrayList<>();
        List<LogEntryResponse> lunchEntries = new ArrayList<>();
        List<LogEntryResponse> dinnerEntries = new ArrayList<>();
        List<LogEntryResponse> snacksEntries = new ArrayList<>();

        for (LogEntryResponse entry : entries) {
            if (entry.getMeal() == Meal.BREAKFAST) {
                breakfastEntries.add(entry);
            } else if (entry.getMeal() == Meal.LUNCH) {
                lunchEntries.add(entry);
            } else if (entry.getMeal() == Meal.DINNER) {
                dinnerEntries.add(entry);
            } else {
                snacksEntries.add(entry);
            }
        }

        fillCard(breakfastLayout, breakfastEntries);
        fillCard(lunchLayout, lunchEntries);
        fillCard(dinnerLayout, dinnerEntries);
        fillCard(snacksLayout, snacksEntries);

        Button editBreakfast = view.findViewById(R.id.edit_breakfast);
        editBreakfast.setOnClickListener(v -> onMealClickListener.onMealClick(Meal.BREAKFAST));
        Button editLunch = view.findViewById(R.id.edit_lunch);
        editLunch.setOnClickListener(v -> onMealClickListener.onMealClick(Meal.LUNCH));
        Button editDinner = view.findViewById(R.id.edit_dinner);
        editDinner.setOnClickListener(v -> onMealClickListener.onMealClick(Meal.DINNER));
        Button editSnacks = view.findViewById(R.id.edit_snacks);
        editSnacks.setOnClickListener(v -> onMealClickListener.onMealClick(Meal.SNACKS));
    }

    private void fillCard(LinearLayout layout, List<LogEntryResponse> entries) {
        if (entries.size() > 0) {
            addEntryCardHeader(layout);
            for (LogEntryResponse entry : entries) {
                addEntryToTable(layout, entry);
            }
        } else {
            TextView hint = new TextView(context);
            hint.setText(R.string.eaten);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 8, 0, 8);
            layout.addView(hint, lp);
        }
    }

    private void fillActivitiesOnPage(List<ActivityResponse> activities, ViewGroup view) {
        LinearLayout activitiesLayout = view.findViewById(R.id.activities_layout);
        if (activities.size() > 0) {
            addActivityCardHeader(activitiesLayout);

            for (ActivityResponse activity : activities) {
                LinearLayout row = new LinearLayout(context);
                row.setOrientation(LinearLayout.HORIZONTAL);
                TextView name = getCustomizedTextView(new TextView(context));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 8.3f);
                lp.setMargins(0, 8, 0, 8);
                name.setText(activity.getName());
                name.setLayoutParams(lp);

                TextView kcal = getCustomizedCalorieTextView(activity.getCalories());
                row.addView(name);
                row.addView(kcal);


                activitiesLayout.addView(row);
            }
        } else {
            TextView hint = new TextView(context);
            hint.setText(R.string.activity_done);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 8, 0, 8);
            activitiesLayout.addView(hint, lp);
        }

        Button editActivities = view.findViewById(R.id.edit_activities);
        editActivities.setOnClickListener(v -> onActivityClickListener.onActivityClick());
    }

    private void addEntryCardHeader(LinearLayout layout) {
        LinearLayout header = new LinearLayout(context);
        header.setOrientation(LinearLayout.HORIZONTAL);

        TextView dummy = new TextView(context);
        dummy.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView p = new TextView(context);
        p.setText(R.string.p);
        p.setTypeface(null, Typeface.BOLD);
        p.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        TextView f = new TextView(context);
        f.setText(R.string.f);
        f.setTypeface(null, Typeface.BOLD);
        f.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        TextView c = new TextView(context);
        c.setText(R.string.c);
        c.setTypeface(null, Typeface.BOLD);
        c.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        TextView kcal = new TextView(context);
        kcal.setText(R.string.kcal);
        kcal.setTypeface(null, Typeface.BOLD);
        kcal.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(100, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 8, 0, 8);
        header.addView(dummy);
        header.addView(p, lp);
        header.addView(f, lp);
        header.addView(c, lp);
        header.addView(kcal, lp);

        layout.addView(header);
    }

    private void addActivityCardHeader(LinearLayout activitiesLayout) {
        LinearLayout header = new LinearLayout(context);
        header.setOrientation(LinearLayout.HORIZONTAL);

        TextView kcal = new TextView(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 8, 0, 8);
        kcal.setText(R.string.kcal);
        kcal.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        kcal.setTypeface(null, Typeface.BOLD);

        header.addView(kcal, lp);

        activitiesLayout.addView(header);
    }

    private void addEntryToTable(LinearLayout layout, LogEntryResponse entry) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);

        TextView name = getCustomizedTextView(new TextView(context));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
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

        layout.addView(row);
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
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(100, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 8, 0, 8);
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
