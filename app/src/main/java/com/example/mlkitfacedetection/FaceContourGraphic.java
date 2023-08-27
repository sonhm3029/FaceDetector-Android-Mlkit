package com.example.mlkitfacedetection;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.FaceDetector;

import com.google.mlkit.vision.face.Face;

public class FaceContourGraphic extends GraphicOverlay.Graphic {

    private final Face face;
    private final Rect imageRect;
    private final Paint facePositionPaint;
    private final Paint idPaint;
    private final Paint boxPaint;

    private static final float BOX_STROKE_WIDTH = 5.0f;

    public FaceContourGraphic(GraphicOverlay overlay, Face face, Rect imageRect) {
        super(overlay);
        this.face = face;
        this.imageRect = imageRect;

        int selectedColor = Color.WHITE;

        facePositionPaint = new Paint();
        facePositionPaint.setColor(selectedColor);

        idPaint = new Paint();
        idPaint.setColor(selectedColor);

        boxPaint = new Paint();
        boxPaint.setColor(selectedColor);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
    }

    @Override
    public void draw(Canvas canvas) {
        RectF rect = calculateRect(
                (float)imageRect.height(),
                (float) imageRect.width(),
                face.getBoundingBox()
        );
        canvas.drawRect(rect, boxPaint);
    }
}
