package com.example.macrologandroid.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.macrologandroid.AddFoodActivity;
import com.example.macrologandroid.R;
import com.example.macrologandroid.cache.FoodCache;
import com.example.macrologandroid.dtos.FoodResponse;
import com.example.macrologandroid.services.FoodService;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FoodFragment extends Fragment {

    private static final int ADD_FOOD_ID = 123;
    private List<FoodResponse> allFood;

    private TableLayout foodTable;
    private TableRow foodTableHeader;
    private ProgressBar loader;
    private SortHeader currentSortHeader = SortHeader.FOOD;

    private Disposable disposable;

    public FoodFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_FOOD_ID) {
            if (resultCode == Activity.RESULT_OK) {
                loader.setVisibility(View.VISIBLE);
                foodTableHeader.setVisibility(View.INVISIBLE);
                refreshAllFood();
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_food, container, false);
        loader = view.findViewById(R.id.loader);
        foodTable = view.findViewById(R.id.food_table_layout);
        foodTableHeader = view.findViewById(R.id.food_table_header);

        TextView foodHeader = view.findViewById(R.id.food_header);
        foodHeader.setOnClickListener(v -> sortTable(SortHeader.FOOD));
        TextView proteinHeader = view.findViewById(R.id.protein_header);
        proteinHeader.setOnClickListener(v -> sortTable(SortHeader.PROTEIN));
        TextView fatHeader = view.findViewById(R.id.fat_header);
        fatHeader.setOnClickListener(v -> sortTable(SortHeader.FAT));
        TextView carbsHeader = view.findViewById(R.id.carbs_header);
        carbsHeader.setOnClickListener(v -> sortTable(SortHeader.CARBS));

        allFood = FoodCache.getInstance().getCache();
        if (allFood.isEmpty()) {
            refreshAllFood();
        } else {
            fillTable();
            loader.setVisibility(View.GONE);
            foodTableHeader.setVisibility(View.VISIBLE);
        }
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (disposable != null) {
            disposable.dispose();
        }
    }

    private void refreshAllFood() {
        FoodCache.getInstance().clearCache();
        FoodService foodService = new FoodService();
        disposable = foodService.getAlFood().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe((res) ->
                {
                    FoodCache.getInstance().addToCache(res);
                    allFood = res;
                    fillTable();
                    loader.setVisibility(View.GONE);
                    foodTableHeader.setVisibility(View.VISIBLE);
                }, (err) -> Log.d(this.getClass().getName(), err.toString()));
    }

    private void selectFood(FoodResponse foodResponse) {
        Intent intent = new Intent(getContext(), AddFoodActivity.class);
        intent.putExtra("foodResponse", foodResponse);
        startActivityForResult(intent, ADD_FOOD_ID);
    }

    private void fillTable() {
        for (FoodResponse foodResponse : allFood) {
            TableRow row = new TableRow(getContext());
            TextView food = getCustomizedTextView(new TextView(getContext()));
            TableRow.LayoutParams lp = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 8.0f);

            food.setText(foodResponse.getName());
            food.setLayoutParams(lp);
            food.setClickable(true);
            food.setOnClickListener(v -> selectFood(foodResponse));

            TextView protein = getDecimalNumberTextView(foodResponse.getProtein());
            TextView fat = getDecimalNumberTextView(foodResponse.getFat());
            TextView carbs = getDecimalNumberTextView(foodResponse.getCarbs());

            row.addView(food);
            row.addView(protein);
            row.addView(fat);
            row.addView(carbs);
            foodTable.addView(row);
        }
    }

    private void sortTable(SortHeader sortHeader) {
//        for (int i = foodTable.getChildCount() -1; i <= 0; i--) {
//            foodTable.removeViewAt(i);
//        }
//        TableLayout copy = foodTable;

        foodTableHeader.setVisibility(View.INVISIBLE);
        loader.setVisibility(View.VISIBLE);
        foodTable.removeAllViews();
        foodTable.addView(foodTableHeader);

        if (sortHeader == currentSortHeader) {
            Collections.reverse(allFood);
        } else {
            switch (sortHeader) {
                case FOOD: {
                    Comparator<FoodResponse> comparator = (FoodResponse f1, FoodResponse f2) -> f1.getName().compareTo(f2.getName());
                    allFood = allFood.stream().sorted(comparator).collect(Collectors.toList());
                    break;
                }
                case PROTEIN: {
                    Comparator<FoodResponse> comparator = (FoodResponse f1, FoodResponse f2) -> Double.compare(f2.getProtein(), f1.getProtein());
                    allFood = allFood.stream().sorted(comparator).collect(Collectors.toList());
                    break;
                }
                case FAT: {
                    Comparator<FoodResponse> comparator = (FoodResponse f1, FoodResponse f2) -> Double.compare(f2.getFat(), f1.getFat());
                    allFood = allFood.stream().sorted(comparator).collect(Collectors.toList());
                    break;
                }
                case CARBS: {
                    Comparator<FoodResponse> comparator = (FoodResponse f1, FoodResponse f2) -> Double.compare(f2.getCarbs(), f1.getCarbs());
                    allFood = allFood.stream().sorted(comparator).collect(Collectors.toList());
                    break;
                }
                default: {
                    Comparator<FoodResponse> comparator = (FoodResponse f1, FoodResponse f2) -> f1.getName().compareTo(f2.getName());
                    allFood = allFood.stream().sorted(comparator).collect(Collectors.toList());
                }
            }
        }
        currentSortHeader = sortHeader;
        fillTable();
        loader.setVisibility(View.GONE);
        foodTableHeader.setVisibility(View.VISIBLE);
    }

    private TextView getDecimalNumberTextView(double text) {
        TextView view = new TextView(getContext());
        view.setText(String.format(Locale.ENGLISH, "%.1f", text));
        setTextViewLayout(view);
        return getCustomizedTextView(view);
    }

    private TextView getCustomizedTextView(TextView view) {
        view.setTextSize(18);
        view.setPadding(0, 0, 0, 16);
        return view;
    }

    private void setTextViewLayout(TextView view) {
        TableRow.LayoutParams lp = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0.1f);
        view.setLayoutParams(lp);
        view.setGravity(Gravity.END);
    }

    enum SortHeader {
        FOOD, PROTEIN, FAT, CARBS
    }
}
