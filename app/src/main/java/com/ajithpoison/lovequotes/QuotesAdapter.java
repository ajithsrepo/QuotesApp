package com.ajithpoison.lovequotes;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class QuotesAdapter extends RecyclerView.Adapter<QuotesAdapter.MyViewHolder>
        implements Filterable {
    private List<Quote> quoteList;
    private List<Quote> quoteListFiltered;
    private QuotesAdapterListener listener;
    private int lastPosition = -1;
    private Context context;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name;

        MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // send selected quoteID in callback
                    listener.onQuoteSelected(getAdapterPosition()+1);
                }
            });
        }
    }


    QuotesAdapter(Context context, List<Quote> quoteList, QuotesAdapterListener listener) {
        this.listener = listener;
        this.quoteList = quoteList;
        this.quoteListFiltered = quoteList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.quote_row_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        final Quote quote = quoteListFiltered.get(position);
        holder.name.setText(quote.getQuote());

        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_in_right);
            holder.itemView.startAnimation(animation);
            lastPosition = position;
        }

    }

    @Override
    public int getItemCount() {
        return quoteListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    quoteListFiltered = quoteList;
                } else {
                    List<Quote> filteredList = new ArrayList<>();
                    for (Quote row : quoteList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getQuote().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    quoteListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = quoteListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                quoteListFiltered = (ArrayList<Quote>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface QuotesAdapterListener {
        void onQuoteSelected(int quoteID);
    }
}
