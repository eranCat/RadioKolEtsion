package com.erank.koletsionpods.utils.enums;

public enum MPServiceStates {
    ACTION_PLAY("action_play"),
    ACTION_PAUSE("action_pause"),
    ACTION_FORWARD("action_fast_forward"),
    ACTION_REWIND("action_fast_rewind"),
    ACTION_NEXT("action_next"),
    ACTION_PREVIOUS("action_previous");

    public final String value;

    MPServiceStates(String value) {
        this.value = value;
    }
}
