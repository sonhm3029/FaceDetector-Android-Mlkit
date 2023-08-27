package com.example.mlkitfacedetection;

import android.annotation.SuppressLint;
import android.graphics.Rect;

import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;

@ExperimentalGetImage public abstract class BaseImageAnalyzer<T> implements ImageAnalysis.Analyzer {

    protected abstract GraphicOverlay getGraphicOverlay();

    @Override
    public void analyze(ImageProxy imageProxy) {
        android.media.Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage inputImage = InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.getImageInfo().getRotationDegrees()
            );

            detectInImage(inputImage)
                    .addOnSuccessListener(results -> {
                        onSuccess(
                                results,
                                getGraphicOverlay(),
                                mediaImage.getCropRect()
                        );
                        imageProxy.close();
                    })
                    .addOnFailureListener(e -> {
                        onFailure(e);
                        imageProxy.close();
                    });
        }
    }

    protected abstract Task<T> detectInImage(InputImage image);

    protected abstract void stop();

    protected abstract void onSuccess(
            T results,
            GraphicOverlay graphicOverlay,
            Rect rect
    );

    protected abstract void onFailure(Exception e);
}