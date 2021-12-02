package com.rokkystudio.qrvalid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import java.util.Collection;
import java.util.Collections;

public class MainActivity extends Activity
{
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

        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);

        mBarcodeView = findViewById(R.id.BarcodeView);
        if (mBarcodeView == null) return;

        Collection<BarcodeFormat> formats = Collections.singletonList(BarcodeFormat.QR_CODE);
        mBarcodeView.setDecoderFactory(new DefaultDecoderFactory(formats));
        mBarcodeView.decodeContinuous(new MyBarcodeCallback());

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBarcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBarcodeView.pause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mBarcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    private class MyBarcodeCallback implements BarcodeCallback
    {
        @Override
        public void barcodeResult (BarcodeResult result){
            String barcode = result.getText();
            if (barcode == null || barcode.equals(mLastBarcode)) return;
            Toast.makeText(MainActivity.this, barcode, Toast.LENGTH_SHORT).show();
            mVibrator.vibrate(400);
            mLastBarcode = barcode;

            if (mWebView == null) return;
            if (barcode.contains("gosuslugi.ru/vaccine/cert/verify/") ||
                barcode.contains("gosuslugi.ru/covid-cert/verify/")    ) {
                mWebView.loadUrl(barcode);
            }
        }
    }

    private static class MyWebViewClient extends WebViewClient {
        @Override
        public void onReceivedSslError(WebView webView, SslErrorHandler handler, SslError error) {
            handler.cancel();
        }
    }
}