package com.csl.macrologandroid.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;

import com.csl.macrologandroid.R;
import com.csl.macrologandroid.dtos.WeightRequest;
import com.csl.macrologandroid.util.DateParser;
import java.util.Date;
import java.util.Objects;


public class WeighDialogFragment extends DialogFragment {

    private double currentWeight = 0.0;

    private OnDialogResult onDialogResult;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));

        LayoutInflater inflater = LayoutInflater.from(getContext());

        @SuppressLint("InflateParams")
        ConstraintLayout dialogView = (ConstraintLayout) inflater.inflate(R.layout.dialog_weight, null);

        TextInputLayout dateInputLayout = (TextInputLayout) dialogView.getChildAt(0);
        Objects.requireNonNull(dateInputLayout.getEditText()).setText(DateParser.format(new Date()));

        TextInputLayout weightInputLayout = (TextInputLayout) dialogView.getChildAt(1);
        Objects.requireNonNull(weightInputLayout.getEditText()).setText(String.valueOf(currentWeight));

        builder.setTitle(R.string.measure_weight)
                .setView(dialogView)
                .setPositiveButton(R.string.done, (dialog, id) -> {
                    Date newDate = DateParser.parse(dateInputLayout.getEditText().getText().toString());
                    double newWeight = Double.valueOf(weightInputLayout.getEditText().getText().toString());
                    WeightRequest weightRequest = new WeightRequest(null, newWeight, newDate);
                    onDialogResult.finish(weightRequest);
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> getDialog().cancel());
        return builder.create();
    }

    public void setCurrentWeight(double currentWeight) {
        this.currentWeight = currentWeight;
    }

    public void setOnDialogResult(OnDialogResult onDialogResult) {
        this.onDialogResult = onDialogResult;
    }

    public interface OnDialogResult {
        void finish(WeightRequest weightRequest);
    }
}
