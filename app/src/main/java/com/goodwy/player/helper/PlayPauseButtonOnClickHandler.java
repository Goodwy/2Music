package com.goodwy.player.helper;

import android.view.View;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlayPauseButtonOnClickHandler implements View.OnClickListener {
    @Override
    public void onClick(View v) {
        if (MusicPlayerRemote.isPlaying()) {
            MusicPlayerRemote.pauseSong();
        } else {
            MusicPlayerRemote.resumePlaying();
        }
    }
}
