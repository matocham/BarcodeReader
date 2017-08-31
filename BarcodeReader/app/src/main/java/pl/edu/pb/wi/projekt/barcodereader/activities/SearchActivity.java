package pl.edu.pb.wi.projekt.barcodereader.activities;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import pl.edu.pb.wi.projekt.barcodereader.R;
import pl.edu.pb.wi.projekt.barcodereader.SearchResultRow;
import pl.edu.pb.wi.projekt.barcodereader.Utils;
import pl.edu.pb.wi.projekt.barcodereader.fragments.SearchItemFragment;
import pl.edu.pb.wi.projekt.barcodereader.fragments.SearchResultFragment;

public class SearchActivity extends AppCompatActivity implements SearchResultFragment.OnListFragmentInteractionListener {

    public static final String SCAN_RESULT_ACTION = "SCAN_RESULT";
    public static final String QUERY_KEY = "query";
    public static String BARCODE_ID_KEY = "BARCODE_ID";
    public static String BARCODE_KEY = "BARCODE_CODE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        TextView noDatabase = (TextView) this.findViewById(R.id.query);
        TextView queryValue = (TextView) this.findViewById(R.id.query_value);

        if (Utils.checkDatabaseAvailable(this)) {
            String query;
            Intent intent = getIntent();
            switch (intent.getAction()) {
                case Intent.ACTION_SEARCH:
                    query = intent.getStringExtra(SearchManager.QUERY);
                    addFragment(SearchResultFragment.getInstance(query));
                    break;
                case Intent.ACTION_VIEW:
                    query = getIntent().getDataString();
                    addFragment(SearchItemFragment.getInstanceWithId(query));
                    break;
                case SCAN_RESULT_ACTION:
                    query = intent.getStringExtra(BARCODE_KEY);
                    addFragment(SearchItemFragment.getInstanceWithCode(query));
                    break;
            }
        } else{
            noDatabase.setVisibility(View.VISIBLE);
            queryValue.setVisibility(View.VISIBLE);
            // only way to invoke this view without database is scanning barcode so this code will always work
            queryValue.setText(getIntent().getStringExtra(BARCODE_KEY));
        }
    }

    private void addFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
                .replace(R.id.fragment_containter, fragment).commit();
    }

    private void addFragmentWithBackStack(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
                .replace(R.id.fragment_containter, fragment).addToBackStack("").commit();
    }

    @Override
    public void onListFragmentInteraction(SearchResultRow item) {
        addFragmentWithBackStack(SearchItemFragment.getInstanceWithId(item.getId()));

    }
}
