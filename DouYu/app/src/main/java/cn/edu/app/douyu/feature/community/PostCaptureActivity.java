package cn.edu.app.douyu.feature.community;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
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

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;

import cn.edu.app.douyu.R;
import cn.edu.app.douyu.core.IntentExtras;
import cn.edu.app.douyu.core.SystemBarInsets;
/**
 * 拍摄上传页：使用 CameraX 拍照并把结果带回发帖流程。
 */

public class PostCaptureActivity extends AppCompatActivity {
    private PreviewView previewView;
    private TextView status;
    private View captureButton;
    private ImageCapture imageCapture;
    private final Executor mainExecutor = command -> ContextCompat.getMainExecutor(this).execute(command);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_capture);
        SystemBarInsets.applyToContent(this);
        previewView = findViewById(R.id.post_capture_preview);
        status = findViewById(R.id.post_capture_status);
        captureButton = findViewById(R.id.post_capture_button);
        findViewById(R.id.post_capture_close).setOnClickListener(v -> finish());
        captureButton.setOnClickListener(v -> takePhoto());
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            status.setText("需要相机权限才能拍照上传。");
            captureButton.setEnabled(false);
        }
    }

    private void startCamera() {
        status.setText("正在启动相机");
        ListenableFuture<ProcessCameraProvider> providerFuture = ProcessCameraProvider.getInstance(this);
        providerFuture.addListener(() -> {
            try {
                ProcessCameraProvider provider = providerFuture.get();
                Preview preview = new Preview.Builder().build();
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();
                provider.unbindAll();
                provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture);
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                status.setText("对准作品后拍照");
                captureButton.setEnabled(true);
            } catch (Exception exception) {
                status.setText("相机启动失败，请返回后重试。");
                captureButton.setEnabled(false);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) {
            status.setText("相机还未准备好。");
            return;
        }
        captureButton.setEnabled(false);
        status.setText("正在保存照片");
        File file = new File(getCacheDir(), "post-capture-"
                + new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date()) + ".jpg");
        ImageCapture.OutputFileOptions options = new ImageCapture.OutputFileOptions.Builder(file).build();
        imageCapture.takePicture(options, mainExecutor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                Intent result = new Intent();
                result.putExtra(IntentExtras.CAPTURED_IMAGE_URI, Uri.fromFile(file).toString());
                setResult(RESULT_OK, result);
                finish();
            }

            @Override
            public void onError(ImageCaptureException exception) {
                status.setText("照片保存失败，请重试。");
                captureButton.setEnabled(true);
            }
        });
    }
}
