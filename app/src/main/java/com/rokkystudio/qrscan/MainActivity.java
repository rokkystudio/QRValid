package com.rokkystudio.qrscan;

import android.Manifest;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.List;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.property.Telephone;

public class MainActivity extends AppCompatActivity
{
    // Webpage
    // Location
    // Google Maps
    // WAZE
    // vCard
    // Social media
    // Facebook
    // LinkedIn
    // Instagram Nametag
    // Pinterest Pincode
    // Snapchat Snapcode
    // PDF
    // App download
    // Restaurant
    // Landing Page
    // Video
    // Image Gallery
    // MP3
    // Coupon
    // Text
    // Rating and Feedback
    // Event
    // Payment
    // Message
    // SMS
    // WhatsApp
    // meCard
    // Wi-Fi
    // Dynamic

    public static final String FRONT_CAMERA     = "CAMERA";
    public static final String STATE_TORCH      = "TORCH";
    public static final String STATE_SOUND      = "SOUND";
    public static final String STATE_VIBRATION  = "VIBRATION";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        Toolbar toolbar = findViewById(R.id.ScannerToolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        mPermissionResult.launch(Manifest.permission.CAMERA);
    }

    private void startScanner() {
        getSupportFragmentManager().beginTransaction()
            .add(R.id.MainFrame, new ScannerFragment())
            .commit();
    }

    private final ActivityResultLauncher<String> mPermissionResult = registerForActivityResult(
        new ActivityResultContracts.RequestPermission(),
        result -> {
            if (result) {
                startScanner();
            } else {
                onBackPressed();
            }
        }
    );

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        // SETTINGS MENU BUTTON CLICK
        if (item.getItemId() == R.id.MenuSettings) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.MainFrame, new SettingsFragment())
                .addToBackStack(SettingsFragment.class.getName())
                .commit();

            return true;
        }
        return false;
    }

    private void parseVCard(String data)
    {
        List<VCard> vcards = Ezvcard.parse(data).all();
        for (VCard vcard : vcards)
        {
            // StructuredName name = vcard.getStructuredName();
            VCardVersion version = vcard.getVersion();
            if (version != null) {
                //Toast.makeText(this, name.getFamily() + " " + name.getGiven(), Toast.LENGTH_SHORT).show();
                Toast.makeText(this, version.getVersion(), Toast.LENGTH_SHORT).show();
            }

            for (Telephone tel : vcard.getTelephoneNumbers()) {
                System.out.println(tel.getTypes() + ": " + tel.getText());
            }
        }
    }

    private boolean isGosUslugiUrl(String barcode) {
        return barcode.contains("gosuslugi.ru/vaccine/cert/") ||
               barcode.contains("gosuslugi.ru/covid-cert/");
    }

    private void validateCertificate(String html) {
        Toast.makeText(this, html, Toast.LENGTH_LONG).show();
    }
}