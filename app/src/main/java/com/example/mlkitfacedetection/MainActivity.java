package com.example.mlkitfacedetection;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

@ExperimentalGetImage
public class MainActivity extends AppCompatActivity implements ImageAnalysis.Analyzer {
    ImageButton flipCamera;
    private PreviewView previewView;
    ImageAnalysis imageAnalysis;
    GraphicOverlay graphicOverlay;
    FaceDetector detector;
    ProcessCameraProvider cameraProvider;
    Preview preview;


    int cameraFacing = CameraSelector.LENS_FACING_BACK;

    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
        if(result) {
            startCamera(cameraFacing);
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        previewView = findViewById(R.id.cameraPreview);
        flipCamera = findViewById(R.id.flipCamera);

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.CAMERA);
        }else {
            startCamera(cameraFacing);
        }

        flipCamera.setOnClickListener(view -> {
            cameraProvider.unbindAll();
            if(cameraFacing == CameraSelector.LENS_FACING_BACK) {
                cameraFacing = CameraSelector.LENS_FACING_FRONT;
            }else {
                cameraFacing = CameraSelector.LENS_FACING_BACK;
            }
            graphicOverlay.toggleSelector();
            startCamera(cameraFacing);
        });

        initDetector();
        graphicOverlay = findViewById(R.id.graphicOverlay_finder);
    }

    public void startCamera(int cameraFacing) {
        ListenableFuture<ProcessCameraProvider> listenableFuture = ProcessCameraProvider.getInstance(this);

        listenableFuture.addListener(() -> {
            try {
                cameraProvider = (ProcessCameraProvider) listenableFuture.get();

                preview = new Preview.Builder().build();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(cameraFacing).build();

                cameraProvider.unbindAll();

                imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), this);

                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

            }catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

        }, ContextCompat.getMainExecutor(this));
    }


    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        if (imageProxy.getImage() != null) {
            detectFaces(imageProxy);
        }
    }

    void detectFaces(ImageProxy imageProxy) {
        InputImage image = InputImage.fromMediaImage(Objects.requireNonNull(imageProxy.getImage()), imageProxy.getImageInfo().getRotationDegrees());

        detector.process(image)
                .addOnSuccessListener(faces -> {
                    graphicOverlay.clear();
                    for (Face face: faces){
                        FaceContourGraphic faceGraphic = new FaceContourGraphic(graphicOverlay, face, imageProxy.getImage().getCropRect());
                        graphicOverlay.add(faceGraphic);
                    }
                    graphicOverlay.postInvalidate();
                    imageProxy.close();

                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

    void initDetector() {
        FaceDetectorOptions realTimeOpts = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
                .build();

        detector = FaceDetection.getClient(realTimeOpts);
    }

}