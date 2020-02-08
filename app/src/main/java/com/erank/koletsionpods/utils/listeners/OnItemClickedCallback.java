package com.erank.koletsionpods.utils.listeners;

public interface OnItemClickedCallback<T> {
    void onItemClicked(T item,int pos);

    default void onRemoveClicked(T item, int pos) {

    }
    default void onItemEdit(T item, int pos){}
}