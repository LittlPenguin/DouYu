package cn.edu.app.douyu.feature.ai;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.edu.app.douyu.R;
import cn.edu.app.douyu.core.SystemBarInsets;

public class CameraActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST = 7001;

    private PreviewView previewView;
    private TextView status;
    private MaterialButton captureButton;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        SystemBarInsets.applyToContent(this);

        previewView = findViewById(R.id.camera_preview);
        status = findViewById(R.id.camera_status);
        captureButton = findViewById(R.id.camera_capture);
        cameraExecutor = Executors.newSingleThreadExecutor();
        captureButton.setEnabled(false);
        captureButton.setOnClickListener(v -> capturePhoto());

        if (hasCameraPermission()) {
            startCamera();
        } else {
            status.setText("需要相机权限才能取景；拒绝后仅展示上传边界。");
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            captureButton.setEnabled(false);
            status.setText("相机权限未开启。可以返回后从系统设置授权，或改用相册上传。");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> providerFuture = ProcessCameraProvider.getInstance(this);
        providerFuture.addListener(() -> {
            try {
                ProcessCameraProvider provider = providerFuture.get();
                Preview preview = new Preview.Builder().build();
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                provider.unbindAll();
                provider.bindToLifecycle(
                        this,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture
                );
                captureButton.setEnabled(true);
                status.setText("相机已就绪。拍照后图片保存到应用缓存，用于后续真实上传。");
            } catch (Exception e) {
                captureButton.setEnabled(false);
                status.setText("相机启动失败：" + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void capturePhoto() {
        if (imageCapture == null) {
            status.setText("相机尚未就绪，不能拍照。");
            return;
        }
        File directory = new File(getCacheDir(), "captures");
        if (!directory.exists() && !directory.mkdirs()) {
            status.setText("无法创建拍照缓存目录。");
            return;
        }
        String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                .format(System.currentTimeMillis());
        File photoFile = new File(directory, "doyu_" + stamp + ".jpg");
        ImageCapture.OutputFileOptions options = new ImageCapture.OutputFileOptions.Builder(photoFile).build();
        captureButton.setEnabled(false);
        status.setText("正在保存照片...");
        imageCapture.takePicture(options, cameraExecutor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                runOnUiThread(() -> {
                    captureButton.setEnabled(true);
                    status.setText("照片已保存，等待上传接入：" + photoFile.getName());
                });
            }

            @Override
            public void onError(ImageCaptureException exception) {
                runOnUiThread(() -> {
                    captureButton.setEnabled(true);
                    status.setText("照片保存失败：" + exception.getMessage());
                });
            }
        });
    }
}
