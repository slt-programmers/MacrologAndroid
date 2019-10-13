package com.csl.macrologandroid.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.csl.macrologandroid.AddFoodActivity;
import com.csl.macrologandroid.R;
import com.csl.macrologandroid.models.Food;
import com.csl.macrologandroid.util.KeyboardManager;
import com.csl.macrologandroid.viewmodels.FoodViewModel;
import com.csl.macrologandroid.viewmodels.ViewModelFactory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;
import static android.view.KeyEvent.KEYCODE_ENTER;

public class FoodFragment extends Fragment {

    private static final int ADD_FOOD_ID = 123;
    private List<Food> allFood;
    private List<Food> searchedFood;
    private List<Food> convertedFood;

    private TableLayout foodTable;
    private TableRow foodTableHeader;
    private ProgressBar loader;
    private SortHeader currentSortHeader = SortHeader.FOOD;
    private boolean sortDirectionReversed = false;

    private int selectedRadioId;

    private TextView foodHeader;
    private TextView proteinHeader;
    private TextView fatHeader;
    private TextView carbsHeader;
    private TextView search;
    private RadioGroup radioGroup;

    public FoodFragment() {
        // Non arg constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FoodViewModel viewModel = ViewModelProviders.of(this, new ViewModelFactory(getToken())).get(FoodViewModel.class);
        viewModel.getFoodListData().observe(this, foods -> {
            allFood = foods;
            searchedFood = allFood;
            convertedFood = searchedFood;
            fillTable(convertedFood);
        });
    }

    // OUD
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        KeyboardManager.hideKeyboard(getActivity());
        if (requestCode == ADD_FOOD_ID && resultCode == Activity.RESULT_OK) {
            search.setText("");
            radioGroup.check(R.id.grams_radio);
            currentSortHeader = SortHeader.FOOD;
            sortDirectionReversed = false;

            loader.setVisibility(View.VISIBLE);
            foodTable.setVisibility(View.INVISIBLE);
            refreshAllFood();
        }
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_food, container, false);

        FloatingActionButton floatingButton = view.findViewById(R.id.floating_button);
        floatingButton.setOnClickListener(v -> {
            Intent intent = new Intent(this.getActivity(), AddFoodActivity.class);
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

        return view;
    }

    private void setSortHeaderColor(TextView header) {
        foodHeader.setTextColor(getResources().getColor(R.color.text, null));
        proteinHeader.setTextColor(getResources().getColor(R.color.text, null));
        fatHeader.setTextColor(getResources().getColor(R.color.text, null));
        carbsHeader.setTextColor(getResources().getColor(R.color.text, null));
        header.setTextColor(getResources().getColor(R.color.colorPrimary, null));
    }

    private void selectFood(Food selectedFood) {
        Intent intent = new Intent(getContext(), AddFoodActivity.class);
        Food newFood = null;
        for (Food food : allFood) {
            if (food.getName().equals(selectedFood.getName())) {
                newFood = food;
                break;
            }
        }

        intent.putExtra("FOOD_RESPONSE", newFood);
        startActivityForResult(intent, ADD_FOOD_ID);
    }

    private void refreshAllFood() {
//        FoodCache.getInstance().clearCache();
//        FoodRepository foodRepository = new FoodRepository(getToken());
//        disposable = foodRepository.getAllFoodObservable()
//                .subscribe(res ->
//                {
//                    FoodCache.getInstance().addToCache(res);
//                    allFood = res;
//                    searchedFood = allFood;
//                    convertedFood = searchedFood;
//                    fillTable(convertedFood);
//                }, err -> Log.e(this.getClass().getName(), err.toString()));
    }

    private void determineGramsOrPercentage() {
        if (selectedRadioId == R.id.grams_radio) {
            convertedFood = searchedFood;
        } else {
            convertedFood = convertGramsToPercentage(searchedFood);
        }
    }

    private List<Food> convertGramsToPercentage(List<Food> foodList) {
        List<Food> result = new ArrayList<>();
        for (Food food : foodList) {
            double total = food.getProtein() + food.getFat() + food.getCarbs();
            Food foodPercentage = new Food(
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

    private void fillTable(List<Food> selection) {
        foodTable.removeAllViews();
        foodTable.addView(foodTableHeader);

        for (Food food : selection) {
            TableRow row = new TableRow(getContext());
            TextView foodTextView = getCustomizedTextView(new TextView(getContext()));
            TableRow.LayoutParams lp = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 8.0f);

            foodTextView.setText(food.getName());
            foodTextView.setLayoutParams(lp);
            foodTextView.setClickable(true);
            foodTextView.setOnClickListener(v -> selectFood(food));

            TextView protein = getDecimalNumberTextView(food.getProtein());
            TextView fat = getDecimalNumberTextView(food.getFat());
            TextView carbs = getDecimalNumberTextView(food.getCarbs());

            row.addView(foodTextView);
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
            case FOOD:
                Collections.sort(convertedFood, (o1, o2) -> o1.getName().compareTo(o2.getName()));
                break;
            case PROTEIN:
                Collections.sort(convertedFood, (o1, o2) -> Double.compare(o2.getProtein(), o1.getProtein()));
                break;
            case FAT:
                Collections.sort(convertedFood, (o1, o2) -> Double.compare(o2.getFat(), o1.getFat()));
                break;
            case CARBS:
                Collections.sort(convertedFood, (o1, o2) -> Double.compare(o2.getCarbs(), o1.getCarbs()));
                break;
            default:
                Collections.sort(convertedFood, (o1, o2) -> o1.getName().compareTo(o2.getName()));

        }

        if (sortDirectionReversed) {
            Collections.reverse(convertedFood);
        }

        currentSortHeader = sortHeader;
    }

    private TextView getDecimalNumberTextView(double text) {
        TextView view = new TextView(getContext());
        view.setText(String.format(Locale.ENGLISH, "%.1f", text));
        Typeface typeface = ResourcesCompat.getFont(Objects.requireNonNull(getContext()), R.font.assistant_light);
        view.setTypeface(typeface);
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

    private String getToken() {
        return Objects.requireNonNull(this.getContext()).getSharedPreferences("AUTH", MODE_PRIVATE).getString("TOKEN", "");
    }

    private final TextWatcher watcher = new TextWatcher() {

        private void searchFood(CharSequence chars) {
            searchedFood = new ArrayList<>();
            if (chars == null || chars.toString().isEmpty()) {
                searchedFood = allFood;
            } else {
                for (Food food : allFood) {
                    if (food.getName().toLowerCase().contains(chars.toString().toLowerCase())) {
                        searchedFood.add(food);
                    }
                }
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Not needed
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
            // Not needed
        }
    };

    private final TextView.OnEditorActionListener actionListener = (v, actionId, event) -> {
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
