package com.ajithpoison.lovequotes;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ajithpoison.IOnBackPressed;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

public class IndividualQuoteFragment extends Fragment implements IOnBackPressed {
    private ClipboardManager myClipboard;
    private ClipData myClip;
    ImageButton b1, b2, b3;
    private TextView quoteFull;
    DatabaseHelper mDatabaseHelper;
    private InterstitialAd mInterstitialAd;
    DatabaseHelper databaseHelper;
    String quoteText;
    Integer quoteIDMF;

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item=menu.findItem(R.id.action_search);
        item.setVisible(false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.fragment_individual_quote, container, false);
        b1 = myView.findViewById(R.id.copyButton);
        b2 = myView.findViewById(R.id.shareButton);
        b3 = myView.findViewById(R.id.favoriteButton);
        quoteFull = myView.findViewById(R.id.quote_full);
        mDatabaseHelper = new DatabaseHelper(requireActivity());
        setHasOptionsMenu(true);

        MobileAds.initialize(requireActivity(), "ca-app-pub-8453607245436940~8868276246");
        AdView mAdViewTop = myView.findViewById(R.id.topAdView);
        AdView mAdViewBottom = myView.findViewById(R.id.bottomAdView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdViewTop.loadAd(adRequest);
        mAdViewBottom.loadAd(adRequest);
        mInterstitialAd = new InterstitialAd(requireActivity());
        mInterstitialAd.setAdUnitId("ca-app-pub-8453607245436940/5686389765");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Code to be executed when when the interstitial ad is closed.
                requireActivity().getFragmentManager().popBackStackImmediate();
            }
        });

        mDatabaseHelper.openDataBase();

        myClipboard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);

        SharedViewModel viewModel = ViewModelProviders.of(this.requireActivity()).get(SharedViewModel.class);

        final Observer<Integer> quoteObserver = new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable final Integer quoteID) {
                quoteIDMF = quoteID;
                databaseHelper = new DatabaseHelper(getContext());
                databaseHelper.openDataBase();
                Cursor cursor = databaseHelper.QueryData("select quote from quotes where id=" + quoteID);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            // Update the UI, in this case, a TextView.
                            quoteText = cursor.getString(0);
                            quoteFull.setText(quoteText);
                        } while (cursor.moveToNext());
                    }
                }

                updateFavIcon(quoteID);
            }
        };


        viewModel.getFullQuoteID().observe(this, quoteObserver);



        // copy button

        b1.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                String text;
                text = quoteFull.getText().toString();

                myClip = ClipData.newPlainText("text", text);
                myClipboard.setPrimaryClip(myClip);

                Toast.makeText(getActivity(), "Quote Copied",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // share button

        b2.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent myIntent = new Intent(Intent.ACTION_SEND);
                myIntent.setType("text/plain");
                String shareBody = quoteFull.getText().toString();
                String shareSub = "Your Subject here";
                myIntent.putExtra(Intent.EXTRA_SUBJECT, shareSub);
                myIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(myIntent, "Share this quote via"));
            }
        });

        // add to WishList button

        b3.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                String quote = quoteFull.getText().toString();
                if (quoteFull.length() != 0) {
                    toggleFavorite(quoteIDMF);
                } else {
                    Toast.makeText(getActivity(), "You must put something in the text field!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });



        return myView;
    }

    public void toggleFavorite(Integer quoteID) {
        int insertData = mDatabaseHelper.markAsFavID(quoteID);

        if (insertData == 1) {
            Toast.makeText(getActivity(), "Quote added to wishlist!",
                    Toast.LENGTH_SHORT).show();
            fillFavIcon();
        } else if (insertData == 2) {
            Toast.makeText(getActivity(), "Quote removed from wishlist!",
                    Toast.LENGTH_SHORT).show();
            unfillFavIcon();
        } else {
            Toast.makeText(getActivity(), "Something went wrong. Please contact the developer.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void updateFavIcon(Integer quoteID) {
        boolean check = mDatabaseHelper.checkFavQuote(quoteID);

        if (check) {
            b3.setImageResource(R.drawable.ic_favorite_black_24dp);
        } else {
            b3.setImageResource(R.drawable.ic_favorite_border_black_24dp);
        }
    }

    public void fillFavIcon() {
        b3.setImageResource(R.drawable.ic_favorite_black_24dp);
    }

    public void unfillFavIcon() {
        b3.setImageResource(R.drawable.ic_favorite_border_black_24dp);
    }

    @Override
    public boolean onBackPressed() {
        if (mInterstitialAd.isLoaded() && ((quoteIDMF%2) == 0)) {
            mInterstitialAd.show();
            return false;
        } else {
            return false;
        }
    }

}
