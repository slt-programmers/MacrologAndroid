package com.csl.macrologandroid.graphs;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.PathShape;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

public class LineGraph extends View {

    private ShapeDrawable drawable;
    private DisplayMetrics displayMetrics;
    private Paint paint;

    public LineGraph(Context context) {
        super(context);
        init(context);
    }

    public LineGraph(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LineGraph(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        paint = new Paint();
        paint.setColor(Color.WHITE);
        displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        width = width - dpToPixels(48);

        drawable = new ShapeDrawable();
        drawable.getPaint().setColor(Color.TRANSPARENT);
        drawable.setBounds(0, 0, width, width);
    }

    protected void onDraw(Canvas canvas) {
        int length = displayMetrics.widthPixels - dpToPixels(48);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(5f);
        canvas.drawLine(2, 2, 2, length, paint);
        canvas.drawLine(2, length, length, length, paint);

        drawable.draw(canvas);
    }

    private int dpToPixels(float dp) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics);
        return (int) px;
    }

}
