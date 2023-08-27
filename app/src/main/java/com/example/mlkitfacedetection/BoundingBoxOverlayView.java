package com.example.mlkitfacedetection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

public class BoundingBoxOverlayView extends View {
    private List<Rect> boundingBoxes;
    private Paint paint;

    public BoundingBoxOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f);
    }

    public void setBoundingBoxes(List<Rect> boundingBoxes) {
        this.boundingBoxes = boundingBoxes;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (boundingBoxes != null) {
            for (Rect rect : boundingBoxes) {
                canvas.drawRect(rect, paint);
            }
        }
    }
}
