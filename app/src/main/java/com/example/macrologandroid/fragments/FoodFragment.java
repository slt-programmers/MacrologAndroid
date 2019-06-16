package com.example.macrologandroid.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.Button;
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
import java.util.Objects;
import java.util.stream.Collectors;

import io.reactivex.disposables.Disposable;

import static android.view.KeyEvent.KEYCODE_ENTER;

public class FoodFragment extends Fragment {

    private static final int ADD_FOOD_ID = 123;
    private List<FoodResponse> allFood;
    private List<FoodResponse> searchedFood;
    private List<FoodResponse> convertedFood;

    private TableLayout foodTable;
    private TableRow foodTableHeader;
    private ProgressBar loader;
    private SortHeader currentSortHeader = SortHeader.FOOD;
    private boolean sortDirectionReversed = false;

    private int selectedRadioId;

    private Disposable disposable;
    private TextView foodHeader;
    private TextView proteinHeader;
    private TextView fatHeader;
    private TextView carbsHeader;
    private TextView search;
    private RadioGroup radioGroup;

    public FoodFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_FOOD_ID) {
            if (resultCode == Activity.RESULT_OK) {
                search.setText("");
                radioGroup.check(R.id.grams_radio);
                currentSortHeader = SortHeader.FOOD;
                sortDirectionReversed = false;

                loader.setVisibility(View.VISIBLE);
                foodTable.setVisibility(View.INVISIBLE);
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

        FloatingActionButton floatingButton = view.findViewById(R.id.floating_button);
        floatingButton.setOnClickListener(v -> {
            Intent intent = new Intent (this.getActivity(), AddFoodActivity.class);
            startActivityForResult(intent, ADD_FOOD_ID);
        });

        search = view.findViewById(R.id.search);
        search.addTextChangedListener(watcher);
        search.setOnEditorActionListener(actionListener);
        search.setImeOptions(EditorInfo.IME_ACTION_DONE);

        radioGroup = view.findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener((v, id) -> {
            selectedRadioId = id;
            determineGramsOrPercentage();
            sortTable(currentSortHeader, false);
            fillTable(convertedFood);
        });
        selectedRadioId = R.id.grams_radio;

        loader = view.findViewById(R.id.loader);
        foodTable = view.findViewById(R.id.food_table_layout);
        foodTableHeader = view.findViewById(R.id.food_table_header);

        foodHeader = view.findViewById(R.id.food_header);
        foodHeader.setOnClickListener(v -> {
            sortTable(SortHeader.FOOD, true);
            setSortHeaderColor(foodHeader);
            fillTable(convertedFood);
        });

        proteinHeader = view.findViewById(R.id.protein_header);
        proteinHeader.setOnClickListener(v -> {
            sortTable(SortHeader.PROTEIN, true);
            setSortHeaderColor(proteinHeader);
            fillTable(convertedFood);
        });

        fatHeader = view.findViewById(R.id.fat_header);
        fatHeader.setOnClickListener(v -> {
            sortTable(SortHeader.FAT, true);
            setSortHeaderColor(fatHeader);
            fillTable(convertedFood);
        });

        carbsHeader = view.findViewById(R.id.carbs_header);
        carbsHeader.setOnClickListener(v -> {
            sortTable(SortHeader.CARBS, true);
            setSortHeaderColor(carbsHeader);
            fillTable(convertedFood);
        });

        setSortHeaderColor(foodHeader);

        allFood = FoodCache.getInstance().getCache();
        searchedFood = allFood;
        convertedFood = searchedFood;
        if (allFood.isEmpty()) {
            refreshAllFood();
        } else {
            fillTable(convertedFood);
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

    private void setSortHeaderColor(TextView header) {
        foodHeader.setTextColor(getResources().getColor(R.color.text, null));
        proteinHeader.setTextColor(getResources().getColor(R.color.text, null));
        fatHeader.setTextColor(getResources().getColor(R.color.text, null));
        carbsHeader.setTextColor(getResources().getColor(R.color.text, null));
        header.setTextColor(getResources().getColor(R.color.colorPrimary, null));
    }

    private void selectFood(FoodResponse foodResponse) {
        Intent intent = new Intent(getContext(), AddFoodActivity.class);
        FoodResponse food = allFood.stream().filter(f -> f.getName().equals(foodResponse.getName())).findFirst().orElse(null);
        intent.putExtra("FOOD_RESPONSE", food);
        startActivityForResult(intent, ADD_FOOD_ID);
    }

    private void refreshAllFood() {
        FoodCache.getInstance().clearCache();
        FoodService foodService = new FoodService();
        disposable = foodService.getAlFood()
                .subscribe((res) ->
                {
                    FoodCache.getInstance().addToCache(res);
                    allFood = res;
                    searchedFood = allFood;
                    convertedFood = searchedFood;
                    fillTable(convertedFood);
                }, (err) -> Log.d(this.getClass().getName(), err.toString()));
    }

    private void searchFood(CharSequence chars) {
        searchedFood = new ArrayList<>();
        if (chars == null || chars.toString().isEmpty()) {
            searchedFood = allFood;
        } else {
            for (FoodResponse food : allFood) {
                if (food.getName().toLowerCase().contains(chars.toString().toLowerCase())) {
                    searchedFood.add(food);
                }
            }
        }
    }

    private void determineGramsOrPercentage() {
        if (selectedRadioId == R.id.grams_radio) {
            convertedFood = searchedFood;
        } else {
            convertedFood = convertGramsToPercentage(searchedFood);
        }
    }

    private List<FoodResponse> convertGramsToPercentage(List<FoodResponse> foodResponses) {
        List<FoodResponse> result = new ArrayList<>();
        for (FoodResponse food : foodResponses) {
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
        foodTable.setVisibility(View.VISIBLE);
    }

    private void sortTable(SortHeader sortHeader, boolean flip) {
        foodTable.setVisibility(View.INVISIBLE);
        loader.setVisibility(View.VISIBLE);

        if (sortHeader == currentSortHeader && flip) {
            sortDirectionReversed = !sortDirectionReversed;
        }

        switch (sortHeader) {
            case FOOD: {
                Comparator<FoodResponse> comparator = (FoodResponse f1, FoodResponse f2) -> f1.getName().compareTo(f2.getName());
                convertedFood = convertedFood.stream().sorted(comparator).collect(Collectors.toList());
                break;
            }
            case PROTEIN: {
                Comparator<FoodResponse> comparator = (FoodResponse f1, FoodResponse f2) -> Double.compare(f2.getProtein(), f1.getProtein());
                convertedFood = convertedFood.stream().sorted(comparator).collect(Collectors.toList());
                break;
            }
            case FAT: {
                Comparator<FoodResponse> comparator = (FoodResponse f1, FoodResponse f2) -> Double.compare(f2.getFat(), f1.getFat());
                convertedFood = convertedFood.stream().sorted(comparator).collect(Collectors.toList());
                break;
            }
            case CARBS: {
                Comparator<FoodResponse> comparator = (FoodResponse f1, FoodResponse f2) -> Double.compare(f2.getCarbs(), f1.getCarbs());
                convertedFood = convertedFood.stream().sorted(comparator).collect(Collectors.toList());
                break;
            }
            default: {
                Comparator<FoodResponse> comparator = (FoodResponse f1, FoodResponse f2) -> f1.getName().compareTo(f2.getName());
                convertedFood = convertedFood.stream().sorted(comparator).collect(Collectors.toList());
            }
        }

        if (sortDirectionReversed) {
            Collections.reverse(convertedFood);
        }

        currentSortHeader = sortHeader;
    }

    private TextView getDecimalNumberTextView(double text) {
        TextView view = new TextView(getContext());
        view.setText(String.format(Locale.ENGLISH, "%.1f", text));
        setTextViewLayout(view);
        return getCustomizedTextView(view);
    }

    private TextView getCustomizedTextView(TextView view) {
        view.setTextSize(16);
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
            searchFood(s);
            determineGramsOrPercentage();
            sortTable(currentSortHeader, false);
            fillTable(convertedFood);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    TextView.OnEditorActionListener actionListener = (v, actionId, event) -> {
        if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE || event.getKeyCode() == KEYCODE_ENTER) {
            InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(Objects.requireNonNull(getActivity().getCurrentFocus()).getWindowToken(), 0);
            v.clearFocus();
            return true;
        }
        return false;
    };

    enum SortHeader {
        FOOD, PROTEIN, FAT, CARBS
    }

}
