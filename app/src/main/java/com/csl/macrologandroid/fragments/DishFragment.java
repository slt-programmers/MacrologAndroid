package com.csl.macrologandroid.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.csl.macrologandroid.AddDishActivity;
import com.csl.macrologandroid.R;
import com.csl.macrologandroid.adapters.DishRecyclerViewAdapter;
import com.csl.macrologandroid.cache.DishCache;
import com.csl.macrologandroid.dtos.DishResponse;
import com.csl.macrologandroid.services.DishService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.disposables.Disposable;

import static android.content.Context.MODE_PRIVATE;

public class DishFragment extends Fragment {

    private static final int ADD_DISH_ID = 1;
    private static final int EDIT_DISH_ID = 2;

    private Disposable dishDisposable;
    private List<DishResponse> allDishes = new ArrayList<>();
    private DishRecyclerViewAdapter dishAdapter;

    public DishFragment() {
        // Non arg constructor
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            DishCache.getInstance().clearCache();
            DishService dishService = new DishService(getToken());
            dishDisposable = dishService.getAllDishes()
                    .subscribe(res ->
                    {
                        DishCache.getInstance().addToCache(res);
                        allDishes.clear();
                        allDishes.addAll(res);
                        dishAdapter.notifyDataSetChanged();
                    }, err -> Log.e(this.getClass().getName(), err.toString()));

            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dish, container, false);
        RecyclerView dishRecyclerView = view.findViewById(R.id.list_view);
        dishRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        dishAdapter = new DishRecyclerViewAdapter(this.getContext(), allDishes);
        dishAdapter.setOnEditClickListener(this::onEditClick);
        dishRecyclerView.setAdapter(dishAdapter);

        FloatingActionButton fab = view.findViewById(R.id.floating_button);
        fab.setOnClickListener((v) -> {
            Intent intent = new Intent(this.getActivity(), AddDishActivity.class);
            startActivityForResult(intent, ADD_DISH_ID);
        });

        DishService dishService = new DishService(getToken());
        dishDisposable = dishService.getAllDishes().subscribe(
                res ->
                {
                    DishCache.getInstance().addToCache(res);
                    allDishes.clear();
                    allDishes.addAll(res);
                    dishAdapter.notifyDataSetChanged();
                },
                err -> Log.e(this.getClass().getName(), err.toString())
        );

        return view;
    }

    @Override
    public void onDestroyView() {
        if (dishDisposable != null) {
            dishDisposable.dispose();
        }

        super.onDestroyView();
    }

    private void onEditClick(DishResponse dish) {
        Intent intent = new Intent(this.getActivity(), AddDishActivity.class);
        intent.putExtra("DISH", dish);
        startActivityForResult(intent, EDIT_DISH_ID);
    }

    private String getToken() {
        return Objects.requireNonNull(this.getContext()).getSharedPreferences("AUTH", MODE_PRIVATE).getString("TOKEN", "");
    }


}
