package com.erank.koletsionpods.utils.listeners;


public interface OnDataLoadedCallback<T> {
    void onLoaded(T data);
    void onCancelled(Exception e);
}
