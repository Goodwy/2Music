package com.goodwy.player.ui.fragments.player;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.goodwy.player.R;

public enum NowPlayingScreen {
    FLAT(R.string.flat, R.drawable.np_flat, 0),
    CARD(R.string.card, R.drawable.np_card, 1);

    @StringRes
    public final int titleRes;
    @DrawableRes
    public final int drawableResId;
    public final int id;

    NowPlayingScreen(@StringRes int titleRes, @DrawableRes int drawableResId, int id) {
        this.titleRes = titleRes;
        this.drawableResId = drawableResId;
        this.id = id;
    }
}
