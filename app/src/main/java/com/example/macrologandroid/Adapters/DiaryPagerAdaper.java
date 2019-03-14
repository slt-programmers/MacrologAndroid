package com.example.macrologandroid.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.macrologandroid.R;

import java.util.ArrayList;
import java.util.List;

public class DiaryPagerAdaper extends PagerAdapter {

    private Context context;
    private List<String> strings;

    public DiaryPagerAdaper(Context context) {
        this.context = context;
        List<String> strings = new ArrayList<>();
        strings.add("ONE");
        strings.add("TWO");
        strings.add("THREE");
        this.strings = strings;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.layout_diary_page, container, false);

        container.addView(layout);

        return layout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return this.strings.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

}
