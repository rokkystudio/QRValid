package com.rokkystudio.qrvalid;

import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import static android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Base64;
import android.view.KeyEvent;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;

public class MainActivity extends Activity
{
    private static final int CAMERA_REQUEST_CODE = 100;

    PowerManager.WakeLock mWakeLock = null;

    private QuaredBarcodeView mBarcodeView;
    private String mLastBarcode;
    private Vibrator mVibrator;
    private WebView mWebView;

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWebView = findViewById(R.id.WebView);
        if (mWebView == null) return;
        mWebView.setWebViewClient(new MyWebViewClient());

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(FLAG_KEEP_SCREEN_ON, "QRValid:WakeTag");

        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(MIXED_CONTENT_ALWAYS_ALLOW);
            CookieManager.getInstance().setAcceptThirdPartyCookies(mWebView, true);
        }

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        mBarcodeView = findViewById(R.id.BarcodeView);
        if (mBarcodeView == null) return;
        Collection<BarcodeFormat> formats = Collections.singletonList(BarcodeFormat.QR_CODE);
        mBarcodeView.setDecoderFactory(new DefaultDecoderFactory(formats));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA }, CAMERA_REQUEST_CODE);
        } else {
            startScanner();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanner();
            } else {
                finish();
            }
        }
    }

    private void startScanner() {
        if (mBarcodeView != null) {
            mBarcodeView.decodeContinuous(new MyBarcodeCallback());
        }
    }

    @SuppressLint("WakelockTimeout")
    @Override
    protected void onResume()
    {
        super.onResume();
        if (mBarcodeView != null) {
            mBarcodeView.resume();
        }

        if (mWakeLock != null) {
            mWakeLock.acquire();
        }


    }

    @Override
    protected void onPause()
    {
        super.onPause();

        if (mBarcodeView != null) {
            mBarcodeView.pause();
        }

        if (mWakeLock != null) {
            mWakeLock.release();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mBarcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    private class MyBarcodeCallback implements BarcodeCallback
    {
        @Override
        public void barcodeResult(BarcodeResult result)
        {
            String barcode = result.getText();
            if (barcode == null || barcode.equals(mLastBarcode)) return;
            Toast.makeText(MainActivity.this, barcode, Toast.LENGTH_SHORT).show();
            mVibrator.vibrate(400);
            mLastBarcode = barcode;

            if (mWebView == null) return;
            if (isValidUrl(barcode)) {
                // mWebView.loadUrl("file:///android_asset/loading.html");
                mWebView.loadUrl(barcode);
            } else {
                mWebView.loadUrl("file:///android_asset/wrong.html");
            }
        }
    }

    private boolean isValidUrl(String barcode) {
        return barcode.contains("gosuslugi.ru/vaccine/cert/") ||
               barcode.contains("gosuslugi.ru/covid-cert/");
    }

    private class MyWebViewClient extends WebViewClient
    {
        @Override
        public void onReceivedSslError(WebView webView, SslErrorHandler handler, SslError error) {
            handler.cancel();
        }

        @Override
        public void onPageFinished(WebView webView, String url) {
            injectJS(webView);
            super.onPageFinished(webView, url);
        }

        private void injectJS(WebView webView) {
            try {
                InputStream inputStream = getAssets().open("clean.js");
                byte[] buffer = new byte[ inputStream.available() ];
                inputStream.read(buffer);
                inputStream.close();
                String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);
                webView.loadUrl("javascript:(function() {" +
                        "var parent = document.getElementsByTagName('head').item(0);" +
                        "var script = document.createElement('script');" +
                        "script.type = 'text/javascript';" +
                        "script.innerHTML = window.atob('" + encoded + "');" +
                        "parent.appendChild(script)" +
                        "})()");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}