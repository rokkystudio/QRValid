package com.rokkystudio.qrvalid;

import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import static android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW;

import static com.rokkystudio.qrvalid.MainActivity.FRONT_CAMERA;
import static com.rokkystudio.qrvalid.MainActivity.STATE_SOUND;
import static com.rokkystudio.qrvalid.MainActivity.STATE_TORCH;
import static com.rokkystudio.qrvalid.MainActivity.STATE_VIBRATION;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;

public class ScannerFragment extends Fragment implements
    SharedPreferences.OnSharedPreferenceChangeListener
{
    private WebView mWebView;
    private ProgressBar mProgress;

    private ScannerView mScannerBarcode;
    private String mLastBarcode;

    private boolean mIsScannerActive = true;

    private SharedPreferences mSharedPreferences = null;
    private PowerManager.WakeLock mWakeLock = null;
    private ResponseManager mResponseManager = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_scanner, container, false);

        Activity activity = getActivity();
        if (activity == null) return root;

        mProgress = root.findViewById(R.id.ScannerProgress);

        mWebView = root.findViewById(R.id.ScannerWebView);
        mWebView.setWebViewClient(new MyWebViewClient());
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(MIXED_CONTENT_ALWAYS_ALLOW);
            CookieManager.getInstance().setAcceptThirdPartyCookies(mWebView, true);
        }

        mScannerBarcode = root.findViewById(R.id.ScannerBarcode);
        Collection<BarcodeFormat> formats = Collections.singletonList(BarcodeFormat.QR_CODE);
        mScannerBarcode.setDecoderFactory(new DefaultDecoderFactory(formats));
        mScannerBarcode.setOnClickListener(view -> showClearDialog());

        mResponseManager = new ResponseManager(activity);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);

        PowerManager powerManager = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(FLAG_KEEP_SCREEN_ON, "QRValid:WakeTag");

        initScanner();

        setHasOptionsMenu(true);
        return root;
    }

    /*
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {

            }
        }
    }
    */

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu)
    {
        super.onPrepareOptionsMenu(menu);
        if (mSharedPreferences == null) return;

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
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if (mSharedPreferences == null) return false;
        if (getContext() == null) return false;

        // SCANNER ACTIVATE MENU BUTTON CLICK
        if (item.getItemId() == R.id.MenuScanner)
        {
            if (mIsScannerActive) {
                mIsScannerActive = false;
                mScannerBarcode.pause();
                mScannerBarcode.setVisibility(View.GONE);
                item.setIcon(R.drawable.scanner_inactive);
            } else {
                mIsScannerActive = true;
                mScannerBarcode.resume();
                mScannerBarcode.setVisibility(View.VISIBLE);
                item.setIcon(R.drawable.scanner_active);
            }
        }

        // CAMERA MENU BUTTON CLICK
        else if (item.getItemId() == R.id.MenuCamera)
        {
            boolean isFrontCamera = !mSharedPreferences.getBoolean(FRONT_CAMERA, false);
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(FRONT_CAMERA, isFrontCamera);
            editor.apply();

            if (isFrontCamera) {
                Toast.makeText(getContext(), "Front camera activation", Toast.LENGTH_SHORT).show();
                item.setIcon(R.drawable.camera_front);

                if (mScannerBarcode != null) {
                    mScannerBarcode.switchToFront();
                }

            } else {
                Toast.makeText(getContext(), "Back camera activation", Toast.LENGTH_SHORT).show();
                item.setIcon(R.drawable.camera_back);

                if (mScannerBarcode != null) {
                    mScannerBarcode.switchToBack();
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
                Toast.makeText(getContext(), "Torch is burning.", Toast.LENGTH_SHORT).show();
                item.setIcon(R.drawable.torch_on);
            } else {
                Toast.makeText(getContext(), "Torch is extinguished.", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getContext(), "Sounds are heard.", Toast.LENGTH_SHORT).show();
                item.setIcon(R.drawable.sound_on);

                if (mResponseManager != null) {
                    mResponseManager.soundActivate();
                }
            } else {
                Toast.makeText(getContext(), "Sounds are silent.", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getContext(), "Vibration is on.", Toast.LENGTH_SHORT).show();
                item.setIcon(R.drawable.vibration_on);

                if (mResponseManager != null) {
                    mResponseManager.vibrateActivate();
                }
            } else {
                Toast.makeText(getContext(), "Vibration is off.", Toast.LENGTH_SHORT).show();
                item.setIcon(R.drawable.vibration_off);
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
            mScannerBarcode.getBarcodeView();
        }
    }

    @SuppressLint("WakelockTimeout")
    @Override
    public void onResume()
    {
        super.onResume();
        if (mScannerBarcode != null) {
            mScannerBarcode.resume();
        }

        if (mWakeLock != null) {
            mWakeLock.acquire();
        }

        if (mSharedPreferences != null) {
            mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();

        if (mScannerBarcode != null) {
            mScannerBarcode.pause();
        }

        if (mWakeLock != null) {
            mWakeLock.release();
        }

        if (mSharedPreferences != null) {
            mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    /*
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mScannerBarcode.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }
    */

    private void initScanner()
    {
        if (mScannerBarcode == null || mSharedPreferences == null) return;

        if (mSharedPreferences.getBoolean(FRONT_CAMERA, false)) {
            mScannerBarcode.switchToFront();
        } else {
            mScannerBarcode.switchToBack();
        }

        mScannerBarcode.decodeContinuous(new MyBarcodeCallback());
        mIsScannerActive = true;

        initTorch();
        initWebView();
    }

    private void initTorch()
    {
        if (!hasTorch() && getContext() != null) {
            Toast.makeText(getContext(), "Torch feature not available", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mScannerBarcode == null || mSharedPreferences == null) return;

        if (mSharedPreferences.getBoolean(STATE_TORCH, false)) {
            mScannerBarcode.setTorchOn();
        } else {
            mScannerBarcode.setTorchOff();
        }
    }

    private boolean hasTorch() {
        if (getActivity() == null) return false;
        PackageManager manager = getActivity().getPackageManager();
        return manager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    private void showClearDialog()
    {
        if (getContext() == null) return;

        if (mResponseManager != null) {
            mResponseManager.vibrateOnce();
        }

        new AlertDialog.Builder(getContext(), R.style.Theme_AppCompat_Dialog_Alert)
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

    private class MyBarcodeCallback implements BarcodeCallback
    {
        @Override
        public void barcodeResult(BarcodeResult result)
        {
            String barcode = result.getText();
            if (barcode == null || barcode.equals(mLastBarcode)) return;

            if (mWebView == null) return;
            // mWebView.loadUrl("about:blank");
            // mWebView.loadUrl("file:///android_asset/loading.html");

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

            /*
            if (URLUtil.isValidUrl(barcode)) {
                // Диалог:
                // Открыть в браузере
                // Открыть в программе
                // Показать текст ссылки (с кнопкой открывания)
                // Запомнить и автоматически выбирать (можно изменить через настройки)
                String url = "<a href=\"" + barcode + "\">" + barcode + "</a>";
                mWebView.loadData(url, "text/html", "utf-8");
            }
            */

            // else if (vcard) {
            // Диалог:
            // Сохранить контакт
            // Отобразить данные (с кнопкой добления)
            // Запомнить и автоматически выбирать (можно изменить через настройки)
            //parseVCard(barcode);
            //String data = barcode.replace("\r\n", "<br>").replace ("\n", "<br>");
            // mWebView.loadData(data, "text/html", "utf-8");
            // }

            if (URLUtil.isValidUrl(barcode)) {
                mWebView.loadUrl(barcode);
            } else {
                mWebView.loadData(barcode, "text/html", "utf-8");
                // mWebView.loadUrl("file:///android_asset/wrong.html");
            }
        }
    }

    private class MyWebViewClient extends WebViewClient
    {
        @Override
        public void onReceivedSslError(WebView webView, SslErrorHandler handler, SslError error) {
            handler.cancel();
        }

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            mProgress.setVisibility(View.VISIBLE);
            mWebView.setVisibility(View.GONE);
        }

        @Override
        public void onPageFinished(WebView webView, String url)
        {
            mProgress.setVisibility(View.GONE);
            mWebView.setVisibility(View.VISIBLE);

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
        }

        private void injectJS(WebView webView)
        {
            if (getContext() == null) return;

            try {
                InputStream inputStream = getContext().getAssets().open("clean.js");
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

        private void injectCSS(WebView webView)
        {
            if (getContext() == null) return;

            try {
                InputStream inputStream = getContext().getAssets().open("clean.css");
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