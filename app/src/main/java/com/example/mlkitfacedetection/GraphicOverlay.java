package com.example.mlkitfacedetection;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.camera.core.CameraSelector;

import java.util.ArrayList;
import java.util.List;

import kotlin.math.MathKt;

public class GraphicOverlay extends View {

    private final Object lock = new Object();
    private final List<Graphic> graphics = new ArrayList<>();
    private Float mScale = null;
    private Float mOffsetX = null;
    private Float mOffsetY = null;
    private int cameraSelector = CameraSelector.LENS_FACING_FRONT;

    public GraphicOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public abstract static class Graphic {

        private final GraphicOverlay overlay;

        public Graphic(GraphicOverlay overlay) {
            this.overlay = overlay;
        }

        public abstract void draw(Canvas canvas);

        public RectF calculateRect(float height, float width, Rect boundingBoxT) {

            float scaleX = (float) overlay.getWidth() / whenLandScapeModeWidth(width, height);
            float scaleY = (float) overlay.getHeight() / whenLandScapeModeHeight(width, height);
            float scale = Math.max(scaleX, scaleY);
            overlay.mScale = scale;

            float offsetX = ((float)overlay.getWidth() - (float)Math.ceil(whenLandScapeModeWidth(width, height) * scale)) / 2.0f;
            float offsetY = ((float)overlay.getHeight() - (float)Math.ceil(whenLandScapeModeHeight(width, height) * scale)) / 2.0f;
            overlay.mOffsetX = offsetX;
            overlay.mOffsetY = offsetY;

            RectF mappedBox = new RectF();
            mappedBox.left = boundingBoxT.right * scale + offsetX;
            mappedBox.top = boundingBoxT.top * scale + offsetY;
            mappedBox.right = boundingBoxT.left * scale + offsetX;
            mappedBox.bottom = boundingBoxT.bottom * scale + offsetY;

            if (overlay.isFrontMode()) {
                float centerX = (float) overlay.getWidth() / 2;
                mappedBox.left = centerX + (centerX - mappedBox.left);
                mappedBox.right = centerX - (mappedBox.right - centerX);
            }
            return mappedBox;
        }

        private boolean isLandScapeMode() {
            return overlay.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        }

        private float whenLandScapeModeWidth(float width, float height) {
            return isLandScapeMode() ? width : height;
        }

        private float whenLandScapeModeHeight(float width, float height) {
            return isLandScapeMode() ? height : width;
        }
    }

    public boolean isFrontMode() {
        return cameraSelector == CameraSelector.LENS_FACING_FRONT;
    }

    public void toggleSelector() {
        cameraSelector = (cameraSelector == CameraSelector.LENS_FACING_BACK) ? CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK;
    }

    public void clear() {
        synchronized (lock) {
            graphics.clear();
        }
        postInvalidate();
    }

    public void add(Graphic graphic) {
        synchronized (lock) {
            graphics.add(graphic);
        }
    }

    public void remove(Graphic graphic) {
        synchronized (lock) {
            graphics.remove(graphic);
        }
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        synchronized (lock) {
            for (Graphic graphic : graphics) {
                graphic.draw(canvas);
            }
        }
    }
}
