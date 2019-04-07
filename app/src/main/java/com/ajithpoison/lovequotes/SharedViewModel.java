package com.ajithpoison.lovequotes;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    public final MutableLiveData<String> selectedQuote = new MutableLiveData<>();
    public final MutableLiveData<Integer> selectedQuoteID = new MutableLiveData<>();

    public void selectQuote(String quote) {
        selectedQuote.setValue(quote);
    }

    public void selectQuoteID(Integer id) {
        selectedQuoteID.setValue(id);
    }

    public  MutableLiveData<String> getFullQuote() {
        return selectedQuote;
    }

    public MutableLiveData<Integer> getFullQuoteID() {
        return selectedQuoteID;
    }
}

