package com.erank.koletsionpods.utils.listeners;

import android.view.animation.Animation;

public interface AnimationListenerAdapter extends Animation.AnimationListener {
    @Override
    default void onAnimationStart(Animation animation) {

    }

    @Override
    default void onAnimationRepeat(Animation animation) {

    }
}
