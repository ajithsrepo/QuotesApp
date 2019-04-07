package com.ajithpoison.lovequotes;

import android.app.SearchManager;

import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import com.wooplr.spotlight.SpotlightView;

import java.util.ArrayList;
import java.util.List;

public class QuotesFragment extends Fragment implements QuotesAdapter.QuotesAdapterListener {
    private List<Quote> quoteList;
    private QuotesAdapter mAdapter;
    private SharedViewModel viewModel;
    DatabaseHelper databaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View myView = inflater.inflate(R.layout.fragment_quotes, container, false);
        RecyclerView recyclerView = myView.findViewById(R.id.recycler_view);
        quoteList = new ArrayList<>();
        mAdapter = new QuotesAdapter(requireActivity(), quoteList, this);
        viewModel = ViewModelProviders.of(this.requireActivity()).get(SharedViewModel.class);
        setHasOptionsMenu(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(requireActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        FragmentManager fm = requireActivity().getSupportFragmentManager();
        for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }

        loadDatabase();
        loadTutorial(recyclerView);
        return myView;
    }

    private void loadTutorial(final RecyclerView recyclerView) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                View tutView = recyclerView.findViewHolderForAdapterPosition(1).itemView;
                new SpotlightView.Builder(requireActivity())
                        .introAnimationDuration(400)
                        .performClick(true)
                        .fadeinTextDuration(400)
                        .headingTvColor(Color.parseColor("#ffffff"))
                        .headingTvSize(32)
                        .headingTvText("Loved the quote?")
                        .subHeadingTvColor(Color.parseColor("#ffffff"))
                        .subHeadingTvSize(24)
                        .subHeadingTvText("Click on it to share it!")
                        .maskColor(Color.parseColor("#dc000000"))
                        .target(tutView)
                        .lineAnimDuration(400)
                        .lineAndArcColor(Color.parseColor("#eb273f"))
                        .dismissOnTouch(true)
                        .dismissOnBackPress(true)
                        .enableDismissAfterShown(true)
                        .show();
            }
        }, 4000);

    }

    private void runLayoutAnimation(RecyclerView recyclerView) {
        Context context = recyclerView.getContext();
        LayoutAnimationController layoutAnimationController = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down);

        recyclerView.setLayoutAnimation(layoutAnimationController);
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    public void loadDatabase() {
        databaseHelper = new DatabaseHelper(getContext());
        databaseHelper.openDataBase();
        Cursor cursor = databaseHelper.QueryData("select * from quotes");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Quote quote = new Quote();
                    quote.setQuote(cursor.getString(1));
                    quoteList.add(quote);
                } while (cursor.moveToNext());
            }
        }

        // refreshing recycler view
        mAdapter.notifyDataSetChanged();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) requireActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                mAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                mAdapter.getFilter().filter(query);
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onQuoteSelected(int quoteID) {
        viewModel.selectQuoteID(quoteID);
        IndividualQuoteFragment nextFrag = new IndividualQuoteFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
        transaction.replace(R.id.fragment_container, nextFrag, "IndividualQuoteFragment")
                .addToBackStack(null)
                .commit();
    }
}