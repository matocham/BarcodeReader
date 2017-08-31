package pl.edu.pb.wi.projekt.barcodereader.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import pl.edu.pb.wi.projekt.barcodereader.R;

/**
 * fagment responsible for displaying menu list
 */
public class MenuFragment extends ListFragment implements AdapterView.OnItemClickListener {
    ListClickListener clickListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu_list, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(getActivity(), R.array.menu_items, android.R.layout.simple_list_item_1);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);
        clickListener = (ListClickListener) getActivity();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        clickListener.onListFragmentInteraction((String) getListAdapter().getItem(position));
    }

    public interface ListClickListener {
        void onListFragmentInteraction(String item);
    }
}
