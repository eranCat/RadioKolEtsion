package com.erank.koletsionpods.utils.listeners;

import android.text.Editable;
import android.text.TextWatcher;

public interface OnTextChangedAdapter extends TextWatcher {

    void onTextChanged(CharSequence txt);

    @Override
    default void onTextChanged(CharSequence s, int start, int before, int count){
        onTextChanged(s);
    }

    @Override
    default void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    default void afterTextChanged(Editable s) {

    }
}