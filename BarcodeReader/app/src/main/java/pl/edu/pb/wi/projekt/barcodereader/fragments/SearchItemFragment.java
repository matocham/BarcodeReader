package pl.edu.pb.wi.projekt.barcodereader.fragments;


import android.app.AlertDialog;
import android.content.ContentUris;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.LinkedHashMap;
import java.util.Map;

import pl.edu.pb.wi.projekt.barcodereader.R;
import pl.edu.pb.wi.projekt.barcodereader.Utils;
import pl.edu.pb.wi.projekt.barcodereader.activities.SearchActivity;
import pl.edu.pb.wi.projekt.barcodereader.database.Contract;

/**
 * display information about single barcode
 * using {@link CursorLoader} to fetch data
 * to ways do display result - using database id or using scanned barcode
 */
public class SearchItemFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String DEFAULT_VALUE = "-1";
    private static final int URL_LOADER = 5;
    private Map<String, String> columns;
    private String barcodeInfo;
    private LinearLayout content;
    private TextView failView;
    private TextView failViewText;
    private AlertDialog dialog;
    private Button buttonMore;

    private boolean fullViewMode = false;

    public static SearchItemFragment getInstanceWithId(String rowId) {
        Bundle args = new Bundle();
        args.putString(SearchActivity.BARCODE_ID_KEY, rowId);
        SearchItemFragment instance = new SearchItemFragment();
        instance.setArguments(args);
        return instance;
    }

    public static SearchItemFragment getInstanceWithCode(String code) {
        Bundle args = new Bundle();
        args.putString(SearchActivity.BARCODE_KEY, code);
        SearchItemFragment instance = new SearchItemFragment();
        instance.setArguments(args);
        return instance;
    }

    public SearchItemFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            columns = new LinkedHashMap<>();
            getLoaderManager().initLoader(URL_LOADER, null, this);
        }
    }

    private CursorLoader getBarcodeById(String id) {
        return new CursorLoader(getActivity(), ContentUris.withAppendedId(Contract.SummaryData.CONTENT_URI, Integer.parseInt(barcodeInfo)),
                null, null, null, null);
    }

    private CursorLoader getBarcodeByCode(String code) {
        return new CursorLoader(getActivity(), Contract.SummaryData.CONTENT_URI, Contract.SummaryData.getAliasedProjection(),
                Contract.SummaryData.COLUMN_CODE + " = ?", new String[]{code}, null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup root = (LinearLayout) inflater.inflate(R.layout.fragment_search_item, container, false);
        content = (LinearLayout) root.findViewById(R.id.barcode_containter);
        failView = (TextView) root.findViewById(R.id.query_result);
        failViewText = (TextView) root.findViewById(R.id.query_result_text);

        buttonMore = (Button) content.findViewById(R.id.button);
        buttonMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!fullViewMode) {
                    viewAllInfo();
                    buttonMore.setText(R.string.show_less);
                } else {
                    viewBasicInfo();
                    buttonMore.setText(R.string.show_more);
                }
            }
        });
        return root;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader;
        dialog = Utils.showProgressDialog(getContext(), getString(R.string.data_loading), getString(R.string.please_wait));
        switch (id) {
            case URL_LOADER:
                if ((barcodeInfo = getArguments().getString(SearchActivity.BARCODE_ID_KEY, DEFAULT_VALUE)).equals(DEFAULT_VALUE)) {
                    barcodeInfo = getArguments().getString(SearchActivity.BARCODE_KEY, "default");
                    loader = getBarcodeByCode(barcodeInfo);
                } else {
                    loader = getBarcodeById(barcodeInfo);
                }
                return loader;
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        dialog.dismiss();
        data.moveToFirst();
        if (!data.isAfterLast()) {
            for (int i = 0; i < data.getColumnCount(); i++) {
                columns.put(data.getColumnName(i), data.getString(i));
            }
        }
        data.close();

        if (columns.size() > 0) {
            if (!fullViewMode) {
                viewBasicInfo();
                buttonMore.setText(R.string.show_more);
            } else {
                viewAllInfo();
                buttonMore.setText(R.string.show_less);
            }
            buttonMore.setVisibility(View.VISIBLE);
        } else {
            failView.setVisibility(View.VISIBLE);
            failViewText.setText(barcodeInfo);
            failViewText.setVisibility(View.VISIBLE);
        }
    }

    private void viewAllInfo() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        content.removeAllViews();
        for (String key : columns.keySet()) {
            View singleView = inflater.inflate(R.layout.single_item_layout, content, false);
            TextView nameTextView = (TextView) singleView.findViewById(R.id.name);
            TextView valueTextView = (TextView) singleView.findViewById(R.id.value);

            nameTextView.setText(key);
            valueTextView.setText(columns.get(key));
            content.addView(singleView);
        }
        content.addView(buttonMore);
        fullViewMode = true;
    }

    private void viewBasicInfo() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        content.removeAllViews();

        String number = Contract.SummaryData.ALIASES[0].substring(1, Contract.SummaryData.ALIASES[0].length() - 1);
        String person = Contract.SummaryData.ALIASES[Contract.SummaryData.ALIASES.length - 2];
        person = person.substring(1, person.length() - 1);

        View singleView = inflater.inflate(R.layout.single_item_basic, content, false);
        TextView nameTextView = (TextView) singleView.findViewById(R.id.name);
        TextView valueTextView = (TextView) singleView.findViewById(R.id.value);
        nameTextView.setText(number);
        valueTextView.setText(columns.get(number));
        content.addView(singleView);

        singleView = inflater.inflate(R.layout.single_item_basic, content, false);
        nameTextView = (TextView) singleView.findViewById(R.id.name);
        valueTextView = (TextView) singleView.findViewById(R.id.value);
        nameTextView.setText(person);
        valueTextView.setText(columns.get(person));
        content.addView(singleView);
        content.addView(buttonMore);
        fullViewMode = false;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(dialog!= null && dialog.isShowing()){
            dialog.dismiss();
        }
    }
}
