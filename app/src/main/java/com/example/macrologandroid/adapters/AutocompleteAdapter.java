package com.example.macrologandroid.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AutocompleteAdapter extends ArrayAdapter<String> {

    private List<String> allItems;
    private List<String> dataList;

    public AutocompleteAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
        super(context, resource, objects);
        dataList = objects;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public @Nullable String getItem(int position) {
        return dataList.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }

        CheckedTextView strName = (CheckedTextView) convertView;
        strName.setText(getItem(position));
        return convertView;

    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (allItems == null) {
                    allItems = new ArrayList<>(dataList);
                }

                if (constraint == null || constraint.length() == 0) {
                    results.values = allItems;
                    results.count = allItems.size();
                } else {
                    List<String> filteredList = allItems.stream()
                            .filter(s -> s.toLowerCase().contains(constraint.toString().toLowerCase()))
                            .collect(Collectors.toList());
                    results.values = filteredList;
                    results.count = filteredList.size();
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results.values != null) {
                    dataList = (ArrayList<String>) results.values;
                } else {
                    dataList = null;
                }
                if (results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };

    }
}
