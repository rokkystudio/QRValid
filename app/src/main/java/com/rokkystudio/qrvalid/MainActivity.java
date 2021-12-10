package com.rokkystudio.qrvalid;

import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import static android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;

public class MainActivity extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener
{
    private static final int CAMERA_REQUEST_CODE = 100;

    private SharedPreferences mSharedPreferences = null;
    private static final String FRONT_CAMERA     = "CAMERA";
    private static final String STATE_TORCH      = "TORCH";
    private static final String STATE_SOUND      = "SOUND";
    private static final String STATE_VIBRATION  = "VIBRATION";

    private PowerManager.WakeLock mWakeLock = null;

    private ResponseManager mResponseManager = null;

    private ScannerView mBarcodeView;
    private String mLastBarcode;

    private WebView mWebView;

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        Toolbar toolbar = findViewById(R.id.Toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

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

        mResponseManager = new ResponseManager(this);

        mBarcodeView = findViewById(R.id.BarcodeView);
        if (mBarcodeView == null) return;
        Collection<BarcodeFormat> formats = Collections.singletonList(BarcodeFormat.QR_CODE);
        mBarcodeView.setDecoderFactory(new DefaultDecoderFactory(formats));

        mBarcodeView.setOnClickListener(view -> showClearDialog());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA }, CAMERA_REQUEST_CODE);
        } else {
            initScanner();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initScanner();
            } else {
                finish();
            }
        }
    }

    private void initScanner()
    {
        if (mBarcodeView == null || mSharedPreferences == null) return;

        if (mSharedPreferences.getBoolean(FRONT_CAMERA, false)) {
            mBarcodeView.switchToFront();
        } else {

            mBarcodeView.switchToBack();
        }

        mBarcodeView.decodeContinuous(new MyBarcodeCallback());

        initTorch();
        initWebView();
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

        if (mSharedPreferences != null) {
            mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
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

        if (mSharedPreferences != null) {
            mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mBarcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu)
    {
        if (mSharedPreferences == null) return false;

        boolean isFrontCamera = mSharedPreferences.getBoolean(FRONT_CAMERA, false);
        MenuItem itemCamera = menu.findItem(R.id.MenuCamera);
        if (itemCamera != null) {
            if (isFrontCamera) {
                itemCamera.setIcon(R.drawable.camera_front);
            } else {
                itemCamera.setIcon(R.drawable.camera_back);
            }
        }

        boolean stateTorch = mSharedPreferences.getBoolean(STATE_TORCH, false);
        MenuItem itemTorch = menu.findItem(R.id.MenuTorch);
        if (itemTorch != null) {
            if (stateTorch) {
                itemTorch.setIcon(R.drawable.torch_on);
            } else {
                itemTorch.setIcon(R.drawable.torch_off);
            }
        }

        boolean stateSound = mSharedPreferences.getBoolean(STATE_SOUND, false);
        MenuItem itemSound = menu.findItem(R.id.MenuSound);
        if (itemSound != null) {
            if (stateSound) {
                itemSound.setIcon(R.drawable.sound_on);
            } else {
                itemSound.setIcon(R.drawable.sound_off);
            }
        }

        boolean stateVibrate = mSharedPreferences.getBoolean(STATE_VIBRATION, false);
        MenuItem itemVibrate = menu.findItem(R.id.MenuVibration);
        if (itemVibrate != null) {
            if (stateVibrate) {
                itemVibrate.setIcon(R.drawable.vibration_on);
            } else {
                itemVibrate.setIcon(R.drawable.vibration_off);
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (mSharedPreferences == null) return false;

        // CAMERA MENU BUTTON CLICK
        if (item.getItemId() == R.id.MenuCamera)
        {
            boolean isFrontCamera = !mSharedPreferences.getBoolean(FRONT_CAMERA, false);
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(FRONT_CAMERA, isFrontCamera);
            editor.apply();

            if (isFrontCamera) {
                Toast.makeText(this, "Front camera activation", Toast.LENGTH_SHORT).show();
                item.setIcon(R.drawable.camera_front);

                if (mBarcodeView != null) {
                    mBarcodeView.switchToFront();
                }

            } else {
                Toast.makeText(this, "Back camera activation", Toast.LENGTH_SHORT).show();
                item.setIcon(R.drawable.camera_back);

                if (mBarcodeView != null) {
                    mBarcodeView.switchToBack();
                }
            }
        }

        // TORCH MENU BUTTON CLICK
        else if (item.getItemId() == R.id.MenuTorch)
        {
            boolean stateTorch = !mSharedPreferences.getBoolean(STATE_TORCH, false);
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(STATE_TORCH, stateTorch);
            editor.apply();

            if (stateTorch) {
                Toast.makeText(this, "Torch is burning.", Toast.LENGTH_SHORT).show();
                item.setIcon(R.drawable.torch_on);
            } else {
                Toast.makeText(this, "Torch is extinguished.", Toast.LENGTH_SHORT).show();
                item.setIcon(R.drawable.torch_off);
            }
        }

        // SOUND MENU BUTTON CLICK
        else if (item.getItemId() == R.id.MenuSound)
        {
            boolean stateSound = !mSharedPreferences.getBoolean(STATE_SOUND, false);
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(STATE_SOUND, stateSound);
            editor.apply();

            if (stateSound) {
                Toast.makeText(this, "Sounds are heard.", Toast.LENGTH_SHORT).show();
                item.setIcon(R.drawable.sound_on);

                if (mResponseManager != null) {
                    mResponseManager.soundActivate();
                }
            } else {
                Toast.makeText(this, "Sounds are silent.", Toast.LENGTH_SHORT).show();
                item.setIcon(R.drawable.sound_off);
            }
        }

        // VIBRATE MENU BUTTON CLICK
        else if (item.getItemId() == R.id.MenuVibration)
        {
            boolean stateVibration = !mSharedPreferences.getBoolean(STATE_VIBRATION, false);
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(STATE_VIBRATION, stateVibration);
            editor.apply();

            if (stateVibration) {
                Toast.makeText(this, "Vibration is on.", Toast.LENGTH_SHORT).show();
                item.setIcon(R.drawable.vibration_on);

                if (mResponseManager != null) {
                    mResponseManager.vibrateActivate();
                }
            } else {
                Toast.makeText(this, "Vibration is off.", Toast.LENGTH_SHORT).show();
                item.setIcon(R.drawable.vibration_off);
            }
        }

        // HELP MENU BUTTON CLICK
        else if (item.getItemId() == R.id.MenuHelp) {
            if (mWebView != null) {
                mWebView.loadUrl("file:///android_asset/help.html");
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if (STATE_TORCH.equals(key)) {
            initTorch();
        }

        if (FRONT_CAMERA.equals(key)) {
            mBarcodeView.getBarcodeView();
        }
    }

    private void initTorch()
    {
        if (!hasTorch()) {
            Toast.makeText(this, "Torch feature not available", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mBarcodeView == null || mSharedPreferences == null) return;

        if (mSharedPreferences.getBoolean(STATE_TORCH, false)) {
            mBarcodeView.setTorchOn();
        } else {
            mBarcodeView.setTorchOff();
        }
    }

    private boolean hasTorch() {
        PackageManager manager = getApplicationContext().getPackageManager();
        return manager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    private class MyBarcodeCallback implements BarcodeCallback
    {
        @Override
        public void barcodeResult(BarcodeResult result)
        {
            String barcode = result.getText();
            if (barcode == null || barcode.equals(mLastBarcode)) return;

            mWebView.loadUrl("about:blank");
            Toast.makeText(MainActivity.this, barcode, Toast.LENGTH_SHORT).show();

            if (mSharedPreferences.getBoolean(STATE_SOUND, false)) {
                if (mResponseManager != null) {
                    mResponseManager.soundScan();
                }
            }

            if (mSharedPreferences.getBoolean(STATE_VIBRATION, false)) {
                if (mResponseManager != null) {
                    mResponseManager.vibrateOnce();
                }
            }

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

    private void showClearDialog()
    {
        if (mResponseManager != null) {
            mResponseManager.vibrateOnce();
        }

        new AlertDialog.Builder(this, R.style.Theme_AppCompat_Dialog_Alert)
            .setMessage(R.string.dialog_clear)
            .setPositiveButton(android.R.string.yes, (dialog, which) -> initWebView())
            .setNegativeButton(android.R.string.no, null)
            .show();
    }

    private void initWebView() {
        if (mWebView != null) {
            mLastBarcode = "";
            mWebView.loadUrl("file:///android_asset/index.html");
        }
    }

    private void validateCertificate(String html) {
        Toast.makeText(this, html, Toast.LENGTH_LONG).show();
    }

    private class MyWebViewClient extends WebViewClient
    {
        @Override
        public void onReceivedSslError(WebView webView, SslErrorHandler handler, SslError error) {
            handler.cancel();
        }

        @Override
        public void onPageFinished(WebView webView, String url) {
            /*
            webView.evaluateJavascript(
              // "(function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();",
                "(function() {" +
                          "var elem = document.querySelectorAll('.status-container.complete, .complete-image');" +
                          "var style = getComputedStyle(elem);" +
                          "return (elem);" +
                      "})();",
                    MainActivity.this::validateCertificate);
             */

            injectCSS(webView);
            super.onPageFinished(webView, url);
        }

        private void injectJS(WebView webView)
        {
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

        private void injectCSS(WebView webView) {
            try {
                InputStream inputStream = getAssets().open("clean.css");
                byte[] buffer = new byte[inputStream.available()];
                inputStream.read(buffer);
                inputStream.close();
                String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);
                webView.loadUrl("javascript:(function() {" +
                        "var parent = document.getElementsByTagName('head').item(0);" +
                        "var style = document.createElement('style');" +
                        "style.type = 'text/css';" +
                        // Tell the browser to BASE64-decode the string into your script !!!
                        "style.innerHTML = window.atob('" + encoded + "');" +
                        "parent.appendChild(style)" +
                        "})()");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}