package com.example.absen.ui.absen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import com.example.absen.core.FaceRecognation;
import com.example.absen.databinding.ActivityAbsenBinding;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.IOException;

public class AbsenActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    private static final String TAG = "AbsenActivity";
    private ActivityAbsenBinding binding;
    private CameraBridgeViewBase cameraBridgeViewBase;
    private Mat mRgba, mGrey;
    private int mCameraId = 0;

    private BaseLoaderCallback baseLoaderCallback;
    private FaceRecognation faceRecognation;

    private String readFace;
    private String nama;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAbsenBinding.inflate(getLayoutInflater());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(binding.getRoot());

        readFace = getIntent().getStringExtra("readFace");
        nama = getIntent().getStringExtra("nama");

        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                if (status == LoaderCallbackInterface.SUCCESS) {
                    cameraBridgeViewBase.enableView();
                }
                super.onManagerConnected(status);
            }
        };

        cameraBridgeViewBase = binding.frameSurface;
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        mCameraId = mCameraId^1;
        cameraBridgeViewBase.setCameraIndex(mCameraId);
        cameraBridgeViewBase.setCvCameraViewListener(this);

        try {
            faceRecognation = new FaceRecognation(getAssets(),
                    AbsenActivity.this,
                    "model_efficientNetB0.tflite",
                    96);
        }catch (IOException e){
            Log.e(TAG,e.getMessage());
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height,width, CvType.CV_8UC4);
        mGrey = new Mat(height,width,CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGrey = inputFrame.gray();

        if (mCameraId==1){
            Core.flip(mRgba,mRgba,-1);
            Core.flip(mGrey,mGrey,-1);
        }

        if (faceRecognation.recognizeImage(mRgba, readFace)){
            Intent intent = new Intent(AbsenActivity.this,ResultAbsenActivity.class);
            intent.putExtra("nama",nama);
            startActivity(intent);
        }

        return mRgba;
    }

    @Override
    protected void onStart() {
        super.onStart();
        int MY_PERMISSIONS_REQUEST_CAMERA = 0;

        if (ContextCompat.checkSelfPermission(AbsenActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(AbsenActivity.this, new String[] {Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()){
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }else {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0,this,baseLoaderCallback);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraBridgeViewBase != null){
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraBridgeViewBase != null){
            cameraBridgeViewBase.disableView();
        }
    }
}