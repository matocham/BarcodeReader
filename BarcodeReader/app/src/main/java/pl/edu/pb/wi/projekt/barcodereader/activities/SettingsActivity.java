package pl.edu.pb.wi.projekt.barcodereader.activities;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import pl.edu.pb.wi.projekt.barcodereader.R;
import pl.edu.pb.wi.projekt.barcodereader.Utils;
import pl.edu.pb.wi.projekt.barcodereader.fragments.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {
    private static final int REQUEST_INTERNET_CODE = 10;
    SettingsFragment settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settings = new SettingsFragment();
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, settings).commit();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET},
                    REQUEST_INTERNET_CODE);
        }
    }

    /**
     * disable network download of databse when internet is not available
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_INTERNET_CODE) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Utils.showInfoDialog(this, getString(R.string.no_internet_permission_settings_title),
                        getString(R.string.no_internet_permission_settings_message));
                settings.enableOnlineDatabaseDownload(false);
            } else {
                settings.enableOnlineDatabaseDownload(true);
            }
        }
    }
}
