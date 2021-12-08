package com.rokkystudio.qrvalid;

import android.app.Activity;
import android.content.Context;
import android.os.Vibrator;

import com.google.zxing.client.android.BeepManager;

public class ResponseManager
{
    private final Vibrator mVibrator;
    private final BeepManager mBeepManager;

    public ResponseManager(Activity activity) {
        mVibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        mBeepManager = new BeepManager(activity);
    }

    public void soundActivate() {
        if (mBeepManager != null) {
            mBeepManager.playBeepSound();
        }
    }

    public void soundScan() {
        if (mBeepManager != null) {
            mBeepManager.playBeepSound();
        }
    }

    public void vibrateActivate() {
        if (mVibrator != null) {
            long[] pattern = { 0, 50, 30, 50, 30, 50, 30, 200 };
            mVibrator.vibrate(pattern, -1);
        }
    }

    public void vibrateOnce() {
        if (mVibrator != null) {
            mVibrator.vibrate(50);
        }
    }

    public void vibrateValid() {
        if (mVibrator != null) {
            long[] pattern = { 0, 100, 30, 100, 30, 100, 30, 200 };
            mVibrator.vibrate(pattern, -1);
        }
    }

    public void vibrateInvalid() {
        if (mVibrator != null) {
            mVibrator.vibrate(1000);
        }
    }
}
