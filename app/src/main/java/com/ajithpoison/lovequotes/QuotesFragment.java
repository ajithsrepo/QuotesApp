package com.ajithpoison.lovequotes;

import android.app.SearchManager;

import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;
import uk.co.samuelwall.materialtaptargetprompt.extras.backgrounds.RectanglePromptBackground;
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal;

import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class QuotesFragment extends Fragment implements QuotesAdapter.QuotesAdapterListener {
    private List<Quote> quoteList;
    private QuotesAdapter mAdapter;
    private SharedViewModel viewModel;
    private View myView;
    DatabaseHelper databaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myView = inflater.inflate(R.layout.fragment_quotes, container, false);
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
        myView.setClickable(true);
        return myView;
    }

    private void loadTutorial(final RecyclerView recyclerView) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isAdded()) {
                    final SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(requireActivity());
                    if (!prefManager.getBoolean("didShowPrompt", false)) {
                        myView.setClickable(false);
                        new MaterialTapTargetPrompt.Builder(requireActivity())
                                .setTarget(R.id.action_search)
                                .setPrimaryText("Search for any quote using your favorite keywords!")
                                .setIcon(R.mipmap.outline_search_black_24)
                                .setCaptureTouchEventOutsidePrompt(true)
                                .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener() {
                                    @Override
                                    public void onPromptStateChanged(@NonNull MaterialTapTargetPrompt prompt, int state) {
                                        if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED || state == MaterialTapTargetPrompt.STATE_DISMISSING) {
                                            // User has pressed the prompt target

                                            SharedPreferences.Editor prefEditor = prefManager.edit();
                                            prefEditor.putBoolean("didShowPrompt", true);
                                            prefEditor.apply();

                                            showQuotePrompt(recyclerView);

                                        }
                                    }
                                })
                                .show();
                    }

                }
            }
        }, 5000);

    }

    private void showQuotePrompt(RecyclerView recyclerView) {
        LinearLayoutManager layoutManager = ((LinearLayoutManager) recyclerView.getLayoutManager());
        int firstVisiblePosition = 0;
        if (layoutManager != null) {
            firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
        }
        View tutView = recyclerView.findViewHolderForAdapterPosition(firstVisiblePosition + 2).itemView;


        new MaterialTapTargetPrompt.Builder(requireActivity())
                .setTarget(tutView)
                .setPrimaryText("Loved the quote?")
                .setSecondaryText("Tap on it to see a host of options.")
                .setBackButtonDismissEnabled(true)
                .setCaptureTouchEventOutsidePrompt(true)
                .setPromptBackground(new RectanglePromptBackground())
                .setPromptFocal(new RectanglePromptFocal())
                .show();
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
        FragmentTransaction transaction;
        if (getFragmentManager() != null) {
            transaction = getFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
            transaction.replace(R.id.fragment_container, nextFrag, "IndividualQuoteFragment")
                    .addToBackStack(null)
                    .commit();
        }

    }
}