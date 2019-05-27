package com.example.macrologandroid.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.macrologandroid.AddFoodActivity;
import com.example.macrologandroid.R;
import com.example.macrologandroid.cache.FoodCache;
import com.example.macrologandroid.dtos.FoodResponse;
import com.example.macrologandroid.services.FoodService;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.view.KeyEvent.KEYCODE_ENTER;

public class FoodFragment extends Fragment {

    private static final int ADD_FOOD_ID = 123;
    private List<FoodResponse> allFood;
    private List<FoodResponse> searchedFood;

    private TableLayout foodTable;
    private TableRow foodTableHeader;
    private ProgressBar loader;
    private SortHeader currentSortHeader = SortHeader.FOOD;
    private int selectedRadioId;

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

        TextView search = view.findViewById(R.id.search);
        search.addTextChangedListener(watcher);
        search.setOnEditorActionListener(actionListener);
        search.setImeOptions(EditorInfo.IME_ACTION_DONE);

        RadioGroup radioGroup = view.findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener((v, id) -> {
            selectedRadioId = id;
            determineGramsOrPercentage();
        });
        selectedRadioId = R.id.grams_radio;

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
        searchedFood = allFood;
        if (allFood.isEmpty()) {
            refreshAllFood();
        } else {
            determineGramsOrPercentage();
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
                    searchedFood = allFood;
                    determineGramsOrPercentage();
                }, (err) -> Log.d(this.getClass().getName(), err.toString()));
    }

    private void selectFood(FoodResponse foodResponse) {
        Intent intent = new Intent(getContext(), AddFoodActivity.class);
        intent.putExtra("foodResponse", foodResponse);
        startActivityForResult(intent, ADD_FOOD_ID);
    }

    private void determineGramsOrPercentage() {
        if (selectedRadioId == R.id.grams_radio) {
            fillTable(searchedFood);
            // use grams list
        } else {
            fillTable(convertGramsToPercentage(searchedFood));
        }
    }

    private List<FoodResponse> convertGramsToPercentage(List<FoodResponse> foodResponses) {
        List<FoodResponse> result = new ArrayList<>();
        for (FoodResponse food: foodResponses) {
            double total = food.getProtein() + food.getFat() + food.getCarbs();
            FoodResponse foodPercentage = new FoodResponse(
                    food.getId(),
                    food.getName(),
                    (food.getProtein() / total * 100),
                    (food.getFat() / total * 100),
                    (food.getCarbs() / total * 100),
                    null
            );
            result.add(foodPercentage);
        }
        return result;
    }

    private void fillTable(List<FoodResponse> selection) {
        foodTable.removeAllViews();
        foodTable.addView(foodTableHeader);

        for (FoodResponse foodResponse : selection) {
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

        loader.setVisibility(View.GONE);
        foodTableHeader.setVisibility(View.VISIBLE);
    }

    private void sortTable(SortHeader sortHeader) {
        foodTableHeader.setVisibility(View.INVISIBLE);
        loader.setVisibility(View.VISIBLE);

        if (sortHeader == currentSortHeader) {
            Collections.reverse(searchedFood);
        } else {
            switch (sortHeader) {
                case FOOD: {
                    Comparator<FoodResponse> comparator = (FoodResponse f1, FoodResponse f2) -> f1.getName().compareTo(f2.getName());
                    searchedFood = searchedFood.stream().sorted(comparator).collect(Collectors.toList());
                    break;
                }
                case PROTEIN: {
                    Comparator<FoodResponse> comparator = (FoodResponse f1, FoodResponse f2) -> Double.compare(f2.getProtein(), f1.getProtein());
                    searchedFood = searchedFood.stream().sorted(comparator).collect(Collectors.toList());
                    break;
                }
                case FAT: {
                    Comparator<FoodResponse> comparator = (FoodResponse f1, FoodResponse f2) -> Double.compare(f2.getFat(), f1.getFat());
                    searchedFood = searchedFood.stream().sorted(comparator).collect(Collectors.toList());
                    break;
                }
                case CARBS: {
                    Comparator<FoodResponse> comparator = (FoodResponse f1, FoodResponse f2) -> Double.compare(f2.getCarbs(), f1.getCarbs());
                    searchedFood = searchedFood.stream().sorted(comparator).collect(Collectors.toList());
                    break;
                }
                default: {
                    Comparator<FoodResponse> comparator = (FoodResponse f1, FoodResponse f2) -> f1.getName().compareTo(f2.getName());
                    searchedFood = searchedFood.stream().sorted(comparator).collect(Collectors.toList());
                }
            }
        }
        currentSortHeader = sortHeader;
        determineGramsOrPercentage();
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

    TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            searchedFood = new ArrayList<>();
            if (s == null || s.toString().isEmpty()) {
                searchedFood = allFood;
            } else {
                for (FoodResponse food : allFood) {
                    if (food.getName().contains(s)) {
                        searchedFood.add(food);
                    }
                }
            }
            determineGramsOrPercentage();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    TextView.OnEditorActionListener actionListener = (v, actionId, event) -> {
        if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE || event.getKeyCode() == KEYCODE_ENTER) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            v.clearFocus();
            return true;
        }
        return false;
    };

    enum SortHeader {
        FOOD, PROTEIN, FAT, CARBS
    }
}
