package com.example.macrologandroid;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.macrologandroid.dtos.WeightRequest;
import com.example.macrologandroid.fragments.WeighDialogFragment;
import com.example.macrologandroid.services.WeightService;

import java.util.Comparator;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class WeightChartActivity extends AppCompatActivity {

    private Disposable disposable;
    private List<WeightRequest> weightRequests;
    private double currentWeight;
    private TextView currentWeightTextView;
    private TableLayout weightTable;
    private TableRow weightTableHeader;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_weight_chart);

        currentWeightTextView = findViewById(R.id.current_weight_number);
        weightTable = findViewById(R.id.weight_table);
        weightTableHeader = findViewById(R.id.weight_table_header);

        loadMeasurements();

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        Button weighButton = findViewById(R.id.weigh_entry);
        weighButton.setOnClickListener(v -> showWeighDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
        }
    }

    private void loadMeasurements() {
        WeightService weightService = new WeightService();
        disposable = weightService.getAllMeasurements().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    weightRequests = res;
                    sortWeightRequestsByDate();
                    currentWeight = getCurrentWeight();
                    currentWeightTextView.setText(String.valueOf(currentWeight));
                    fillTable();
                }, err -> Log.e(this.getLocalClassName(), err.getMessage()));
    }

    private void sortWeightRequestsByDate() {
         weightRequests.sort(Comparator.comparing(WeightRequest::getDay).reversed());
    }

    private void fillTable() {
        weightTable.removeAllViews();
        weightTable.addView(weightTableHeader);
        for (WeightRequest weightRequest : weightRequests) {
            TableRow row = new TableRow(getApplicationContext());
            TableRow.LayoutParams params = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(params);
            row.setGravity(Gravity.FILL_HORIZONTAL);
            row.setPadding(0, 0, 0, (8 * getApplicationContext().getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));

            TextView date = new TextView(getApplicationContext());
            date.setTextAppearance(R.style.AppTheme);
            date.setText(weightRequest.getDay());
            date.setTextColor(getResources().getColor(R.color.white, null));
            date.setTextSize(20);
            date.setLayoutParams(new TableRow.LayoutParams(-2, -2, 1.0f));

            row.addView(date);

            TextView weight = new TextView(getApplicationContext());
            weight.setTextAppearance(R.style.AppTheme);
            weight.setText(String.valueOf(weightRequest.getWeight()));
            weight.setTextSize(20);
            weight.setTextColor(getResources().getColor(R.color.white, null));
            weight.setLayoutParams(new TableRow.LayoutParams(-2, -2, 1.0f));

            row.addView(weight);

            weightTable.addView(row);
        }
    }

    private double getCurrentWeight() {
        WeightRequest latest = weightRequests.stream().max(Comparator.comparing(WeightRequest::getDay)).orElse(null);
        if (latest != null) {
            return latest.getWeight();
        } else {
            return 0.0;
        }
    }

    private void showWeighDialog() {
        WeighDialogFragment dialog = new WeighDialogFragment();
        dialog.setCurrentWeight(currentWeight);
        dialog.setOnDialogResult(new WeighDialogFragment.OnDialogResult() {
            @Override
            public void finish(WeightRequest weightRequest) {
                WeightService weightService = new WeightService();
                disposable = weightService.postMeasurement(weightRequest)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                res -> {
                                    Log.d(this.getClass().toString(), res.toString());
                                    loadMeasurements();
                                },
                                err -> Log.e(this.getClass().toString(), err.getMessage())
                        );
            }
        });
        dialog.show(getSupportFragmentManager(), "WeighDialogFragment");
    }

}
