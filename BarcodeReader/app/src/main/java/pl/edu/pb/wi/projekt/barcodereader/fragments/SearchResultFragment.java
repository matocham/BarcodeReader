package pl.edu.pb.wi.projekt.barcodereader.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import pl.edu.pb.wi.projekt.barcodereader.R;
import pl.edu.pb.wi.projekt.barcodereader.SearchResultRow;
import pl.edu.pb.wi.projekt.barcodereader.activities.SearchActivity;
import pl.edu.pb.wi.projekt.barcodereader.database.Contract;

/**
 * Used to display search results using RecyclerView
 * Impelementation of {@link android.support.v4.content.CursorLoader} my be required when database will be big
 */
public class SearchResultFragment extends Fragment {

    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private List<SearchResultRow> searchResults;
    private RecyclerView recyclerView;

    public static SearchResultFragment getInstance(String query) {
        Bundle args = new Bundle();
        args.putString(SearchActivity.QUERY_KEY, query);
        SearchResultFragment fragment = new SearchResultFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public SearchResultFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        String query = args.getString(SearchActivity.QUERY_KEY); // query is String with data written by user in Search Widget

        ContentResolver contentResolver = getContext().getContentResolver();
        Cursor cursor = contentResolver.query(Contract.Devices.Search.CONTENT_URI,
                new String[]{Contract.Devices.COLUMN_ID, Contract.Devices.COLUMN_INVENTARY_NR,
                        Contract.Devices.COLUMN_PART_NAME}, Contract.Devices.COLUMN_INVENTARY_NR + " LIKE ?||'%'",
                new String[]{query}, Contract.Devices.SORT_ORDER_DEFAULT);

        searchResults = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String number = cursor.getString(1);
                String name = cursor.getString(2);
                searchResults.add(new SearchResultRow(String.valueOf(id), number, name));
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        if (searchResults.size() > 0) {
            view = inflater.inflate(R.layout.fragment_searchresult_list, container, false);
            // Set the adapter
            if (view instanceof RecyclerView) {
                Context context = view.getContext();
                recyclerView = (RecyclerView) view;
                if (mColumnCount <= 1) {
                    recyclerView.setLayoutManager(new LinearLayoutManager(context));
                } else {
                    recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
                }
                recyclerView.setAdapter(new SearchResultRecyclerViewAdapter(searchResults, mListener));
            }
        } else {
            view = inflater.inflate(R.layout.fragment_search_result_no_res, container, false);
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ListClickListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(SearchResultRow item);
    }
}
