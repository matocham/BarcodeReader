package pl.edu.pb.wi.projekt.barcodereader.activities;

import android.Manifest;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Collection;

import pl.edu.pb.wi.projekt.barcodereader.R;
import pl.edu.pb.wi.projekt.barcodereader.fragments.MenuFragment;
import pl.edu.pb.wi.projekt.barcodereader.utils.Utils;

public class MenuActivity extends AppCompatActivity implements MenuFragment.ListClickListener {
    public static final int REQUEST_CAMERA_CODE = 11;

    private MenuItem searchItem;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_CODE);
        }

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);

        searchItem = menu.findItem(R.id.search);
        if(Utils.checkDatabaseAvailable(this)){
            searchItem.setVisible(true);
        } else{
            searchItem.setVisible(false);
        }
        if (Utils.checkDatabaseAvailable(this)) {
            searchItem.setEnabled(true);
        } else {
            searchItem.setEnabled(false);
        }
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) searchItem.getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, SearchActivity.class)));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                if (result.getContents() != null) {
                    Intent intent = new Intent(this, SearchActivity.class);
                    intent.putExtra(SearchActivity.BARCODE_KEY, result.getContents());
                    intent.setAction(SearchActivity.SCAN_RESULT_ACTION);
                    startActivity(intent);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Utils.checkDatabaseAvailable(this) && searchItem != null) {
            searchItem.setEnabled(true);
        }
    }

    @Override
    public void onListFragmentInteraction(String item) {
        Intent intent;
        switch (item) {
            case "Skanuj kod":
                intent = new Intent(this, CameraCaptureActivity.class);
                startActivity(intent);
                break;
            case "Skanuj z ZXing":
                IntentIntegrator integrator = new IntentIntegrator(this);
                Collection<String> formats = new ArrayList<>();
                formats.add("CODE_128");
                integrator.setDesiredBarcodeFormats(formats);
                integrator.setPrompt(getString(R.string.zxing_prompt));
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(true);
                integrator.initiateScan();
                break;
            case "Ustawienia":
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_CODE) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, R.string.setting_camera_acces_required, Toast.LENGTH_LONG).show();
                findViewById(R.id.fragment1).setEnabled(false);
                Utils.showCloseDialog(this, getString(R.string.setting_camera_acces_required),
                        getString(R.string.setting_camera_acces_required_msg));
            }
        }
    }
}
