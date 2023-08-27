package com.example.mlkitfacedetection;

import android.content.Context;
import android.util.Log;

import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ExperimentalGetImage public class CameraManager {

    private final Context context;
    private final PreviewView finderView;
    private final LifecycleOwner lifecycleOwner;
    private final GraphicOverlay graphicOverlay;

    private Preview preview;
    private Camera camera;
    private ExecutorService cameraExecutor;
    private int cameraSelectorOption = CameraSelector.LENS_FACING_FRONT;
    private ProcessCameraProvider cameraProvider;

    private ImageAnalysis imageAnalyzer;

    private static final String TAG = "CameraXBasic";

    public CameraManager(
            Context context,
            PreviewView finderView,
            LifecycleOwner lifecycleOwner,
            GraphicOverlay graphicOverlay
    ) {
        this.context = context;
        this.finderView = finderView;
        this.lifecycleOwner = lifecycleOwner;
        this.graphicOverlay = graphicOverlay;
        createNewExecutor();
    }

    private void createNewExecutor() {
        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    public void startCamera() {
        ProcessCameraProvider.getInstance(context).addListener(() -> {
            try {
                cameraProvider = ProcessCameraProvider.getInstance(context).get();
                preview = new Preview.Builder().build();

                imageAnalyzer = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                imageAnalyzer.setAnalyzer(cameraExecutor, selectAnalyzer());

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(cameraSelectorOption)
                        .build();

                setCameraConfig(cameraProvider, cameraSelector);
            } catch (Exception e) {
                Log.e(TAG, "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(context));
    }

    private ImageAnalysis.Analyzer selectAnalyzer() {
        return new FaceContourDetectionProcessor(graphicOverlay);
    }

    private void setCameraConfig(ProcessCameraProvider cameraProvider, CameraSelector cameraSelector) {
        try {
            cameraProvider.unbindAll();
            camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalyzer
            );
            preview.setSurfaceProvider(finderView.getSurfaceProvider());
        } catch (Exception e) {
            Log.e(TAG, "Use case binding failed", e);
        }
    }

    public void changeCameraSelector() {
        cameraProvider.unbindAll();
        cameraSelectorOption = (cameraSelectorOption == CameraSelector.LENS_FACING_BACK)
                ? CameraSelector.LENS_FACING_FRONT
                : CameraSelector.LENS_FACING_BACK;
        graphicOverlay.toggleSelector();
        startCamera();
    }
}
