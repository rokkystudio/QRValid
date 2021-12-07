package com.rokkystudio.qrvalid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.camera.CameraSettings;

public class ScannerView extends DecoratedBarcodeView
{
    private static final int CAMERA_BACK_ID  = 0;
    private static final int CAMERA_FRONT_ID = 1;
    private Drawable mAimDrawable;

    public ScannerView(Context context) {
        super(context);
        init();
    }

    public ScannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init()
    {
        setStatusText("");
        getViewFinder().setLaserVisibility(false);

        // mAimDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_aim);
        // Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_aim);
        // getViewFinder().drawResultBitmap(bitmap);
    }

    public void switchToFront() {
        pause();
        CameraSettings cameraSettings = getCameraSettings();
        cameraSettings.setRequestedCameraId(CAMERA_FRONT_ID);
        setCameraSettings(cameraSettings);
        resume();
    }

    public void switchToBack() {
        pause();
        CameraSettings cameraSettings = getCameraSettings();
        cameraSettings.setRequestedCameraId(CAMERA_BACK_ID);
        setCameraSettings(cameraSettings);
        resume();
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mAimDrawable.setBounds(0, 0, getWidth(), getHeight());
        mAimDrawable.draw(canvas);
    }
}
