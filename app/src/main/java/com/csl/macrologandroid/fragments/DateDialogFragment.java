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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;

import com.csl.macrologandroid.R;
import com.csl.macrologandroid.util.DateParser;
import com.csl.macrologandroid.util.LocalDateParser;

import java.util.Date;
import java.util.Objects;

public class DateDialogFragment extends DialogFragment {

    private OnDialogResult onDialogResult;
    private Date currentDate;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        LayoutInflater inflater = LayoutInflater.from(getContext());

        @SuppressLint("InflateParams")
        ConstraintLayout dialogView = (ConstraintLayout) inflater.inflate(R.layout.dialog_date, null);

        TextInputLayout dateInputLayout = (TextInputLayout) dialogView.getChildAt(0);
        Objects.requireNonNull(dateInputLayout.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (DateParser.parse(s.toString()) == null) {
                    dateInputLayout.setErrorEnabled(true);
                    dateInputLayout.setError("Invalid format");
                } else {
                    dateInputLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        Objects.requireNonNull(dateInputLayout.getEditText()).setText(DateParser.format(new Date()));

        builder.setTitle(R.string.choose_date)
                .setView(dialogView)
                .setPositiveButton(R.string.done, (dialog, id) -> {
//                    LocalDate newDate = LocalDateParser.parse(dateInputLayout.getEditText().getText().toString());
                    Date newDate = DateParser.parse(dateInputLayout.getEditText().getText().toString());
                    if (newDate != null) {
                        onDialogResult.finish(newDate);
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> getDialog().cancel());
        return builder.create();
    }

    public void setCurrentDate(Date currentDate) {
        this.currentDate = currentDate;
    }

    public void setOnDialogResult(OnDialogResult onDialogResult) {
        this.onDialogResult = onDialogResult;
    }

    public interface OnDialogResult {
        void finish(Date date);
    }
}
