package com.erank.koletsionpods.utils.listeners;

import android.widget.SeekBar;

public interface OnSeekBarChangeListenerAdapter extends SeekBar.OnSeekBarChangeListener {
    @Override
    default void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    default void onStopTrackingTouch(SeekBar seekBar) {

    }
}
