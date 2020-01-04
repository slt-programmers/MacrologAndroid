package com.csl.macrologandroid.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.csl.macrologandroid.R;
import com.csl.macrologandroid.dtos.DishResponse;
import com.csl.macrologandroid.dtos.IngredientResponse;
import com.csl.macrologandroid.dtos.PortionResponse;

import java.util.List;
import java.util.Locale;

public class DishRecyclerViewAdapter extends RecyclerView.Adapter<DishRecyclerViewAdapter.DishViewHolder> {

    private List<DishResponse> dishList;
    private Context context;

    public DishRecyclerViewAdapter(Context context, List<DishResponse> dishList) {
        this.dishList = dishList;
        this.context = context;
    }

    @NonNull
    @Override
    public DishViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_dish_card, parent, false);
        return new DishViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DishViewHolder viewHolder, int position) {
        DishResponse dish = dishList.get(position);
        viewHolder.title.setText(dish.getName());

        List<IngredientResponse> ingredients = dish.getIngredients();
        for (IngredientResponse ingredient : ingredients) {
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);

            TextView foodName = getCustomStringTextView(ingredient.getFood().getName());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(0,8,16,8);
            foodName.setLayoutParams(lp);
            TextView amount;
            TextView portionName;

            if (ingredient.getPortionId() != null) {
                PortionResponse usedPortion = null;
                for (PortionResponse portion : ingredient.getFood().getPortions()) {
                    if (portion.getId().equals(ingredient.getPortionId())) {
                        usedPortion = portion;
                        break;
                    }
                }
                if (usedPortion != null) {
                    amount = getCustomDoubleTextView(ingredient.getMultiplier());
                    portionName = getCustomStringTextView(usedPortion.getDescription());
                } else {
                    amount = getCustomDoubleTextView(0);
                    portionName = getCustomStringTextView("");
                }
            } else {
                amount = getCustomDoubleTextView(ingredient.getMultiplier() * 100);
                portionName = getCustomStringTextView(context.getResources().getString(R.string.grams));
            }

            row.addView(foodName);
            row.addView(amount);
            row.addView(portionName);

            viewHolder.ingredientLayout.addView(row);
        }
    }

    private TextView getCustomStringTextView(String text) {
        TextView view = new TextView(context);
        view.setText(text);

        // layout
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 8, 0, 8);
        view.setLayoutParams(lp);

        // font
        view.setTextSize(16);
        Typeface typeface = ResourcesCompat.getFont(context, R.font.assistant_light);
        view.setTypeface(typeface);
        return view;
    }

    private TextView getCustomDoubleTextView(double text) {
        TextView view = new TextView(context);
        view.setText(String.format(Locale.ENGLISH, "%.1f", text));

        // layout
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(100, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 8, 16, 8);
        view.setLayoutParams(lp);
        view.setGravity(Gravity.END);

        // font
        view.setTextSize(16);
        Typeface typeface = ResourcesCompat.getFont(context, R.font.assistant_light);
        view.setTypeface(typeface);
        return view;
    }


    @Override
    public int getItemCount() {
        if (dishList != null) {
            return dishList.size();
        }
        return 0;
    }

    class DishViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        LinearLayout ingredientLayout;

        DishViewHolder(View view) {
            super(view);

            title = view.findViewById(R.id.dish_title);
            ingredientLayout = view.findViewById(R.id.dish_ingredient_layout);
        }
    }
}
