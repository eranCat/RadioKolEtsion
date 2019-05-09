package com.erank.radiokoletsionv2.utils.media_player;

public enum MediaPlayerAction {
    ACTION_PLAY ,
    ACTION_PAUSE ,
    ACTION_NEXT ,
    ACTION_PREVIOUS,
    ACTION_RESET,
    ACTION_SWAP,
    ACTION_PREPARING,
    ACTION_PREPARED,
    ACTION_COMPLETED;

    @Override
    public String toString() {
        return this.name();
    }
}
