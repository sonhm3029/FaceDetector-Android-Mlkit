package com.example.mlkitfacedetection;

import android.graphics.Rect;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.ExperimentalGetImage;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.IOException;
import java.util.List;

@ExperimentalGetImage class FaceContourDetectionProcessor extends BaseImageAnalyzer<List<Face>> {
    GraphicOverlay graphicOverlay;

    private final FaceDetectorOptions realTimeOpts = new FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            .build();

    private final FaceDetector detector = FaceDetection.getClient(realTimeOpts);

    private static final String TAG = "FaceDetectorProcessor";

    public FaceContourDetectionProcessor(GraphicOverlay view) {
        graphicOverlay = view;
    }

    @Override
    protected GraphicOverlay getGraphicOverlay() {
        return graphicOverlay;
    }

    @Override
    public Task<List<Face>> detectInImage(InputImage image) {
        return detector.process(image);
    }

    @Override
    public void stop() {
        try {
            detector.close();
        } catch (Exception e) {
            Log.e(TAG, "Exception thrown while trying to close Face Detector: " + e);
        }
    }

    @Override
    public void onSuccess(
            @NonNull List<Face> results,
            @NonNull GraphicOverlay graphicOverlay,
            @NonNull Rect rect) {
        graphicOverlay.clear();
        for (Face face : results) {
            FaceContourGraphic faceGraphic = new FaceContourGraphic(graphicOverlay, face, rect);
            graphicOverlay.add(faceGraphic);
        }
        graphicOverlay.postInvalidate();
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        Log.w(TAG, "Face Detector failed: " + e);
    }
}