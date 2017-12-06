package pl.edu.pb.wi.projekt.barcodereader.activities;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import pl.edu.pb.wi.projekt.barcodereader.R;
import pl.edu.pb.wi.projekt.barcodereader.utils.Utils;
import pl.edu.pb.wi.projekt.barcodereader.fragments.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {
    SettingsFragment settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settings = new SettingsFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, settings).commit();
    }
}
