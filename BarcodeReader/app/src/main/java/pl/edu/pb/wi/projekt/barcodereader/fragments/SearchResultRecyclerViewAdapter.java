package pl.edu.pb.wi.projekt.barcodereader.fragments;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import pl.edu.pb.wi.projekt.barcodereader.R;
import pl.edu.pb.wi.projekt.barcodereader.models.SearchResultRow;
import pl.edu.pb.wi.projekt.barcodereader.fragments.SearchResultFragment.OnListFragmentInteractionListener;

public class SearchResultRecyclerViewAdapter extends RecyclerView.Adapter<SearchResultRecyclerViewAdapter.ViewHolder> {

    private final List<SearchResultRow> queryResult;
    private final OnListFragmentInteractionListener mListener;

    public SearchResultRecyclerViewAdapter(List<SearchResultRow> queryResult, OnListFragmentInteractionListener listener) {
        this.queryResult = queryResult;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.resultRow = queryResult.get(position);
        holder.key.setText(queryResult.get(position).getName());
        holder.value.setText(queryResult.get(position).getDescription());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.resultRow);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return queryResult.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView key;
        public final TextView value;
        SearchResultRow resultRow;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            key = (TextView) view.findViewById(R.id.name);
            value = (TextView) view.findViewById(R.id.value);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + value.getText() + "'";
        }
    }
}
