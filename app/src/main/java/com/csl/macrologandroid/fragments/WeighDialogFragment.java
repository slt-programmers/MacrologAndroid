package com.csl.macrologandroid.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.textfield.TextInputLayout;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.TextView;

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

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        LayoutInflater inflater = LayoutInflater.from(getContext());

        @SuppressLint("InflateParams")
        ConstraintLayout dialogView = (ConstraintLayout) inflater.inflate(R.layout.dialog_weight, null);

        TextInputLayout dateInputLayout = (TextInputLayout) dialogView.getChildAt(0);
        Objects.requireNonNull(dateInputLayout.getEditText()).setText(DateParser.format(new Date()));

        TextInputLayout weightInputLayout = (TextInputLayout) dialogView.getChildAt(1);
        Objects.requireNonNull(weightInputLayout.getEditText()).setText(String.valueOf(currentWeight));

        TextView customTitle = new TextView(getContext());
        customTitle.setText(getResources().getString(R.string.measure_weight));
        customTitle.setTextSize(20);
        customTitle.setPadding(48, 48, 0 ,0);
        customTitle.setTextColor(getResources().getColor(R.color.darkblue, null));
        customTitle.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.assistant_light), Typeface.BOLD);

        builder.setCustomTitle(customTitle)
                .setView(dialogView)
                .setPositiveButton(R.string.done, (dialog, id) -> {
                    Date newDate = DateParser.parse(dateInputLayout.getEditText().getText().toString());
                    double newWeight = Double.parseDouble(weightInputLayout.getEditText().getText().toString());
                    WeightRequest weightRequest = new WeightRequest(null, newWeight, DateParser.format(newDate));
                    onDialogResult.finish(weightRequest);
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> Objects.requireNonNull(getDialog()).cancel());
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
