package pl.edu.pb.wi.projekt.barcodereader.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import pl.edu.pb.wi.projekt.barcodereader.R;
import pl.edu.pb.wi.projekt.barcodereader.fragments.SearchItemFragment;

public class AddItemActivity extends AppCompatActivity {
    private String barcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        barcode = getIntent().getStringExtra(SearchItemFragment.BARCODE_KEY);
    }
}
