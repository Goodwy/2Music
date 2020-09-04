package com.goodwy.player.ui.fragments.player;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.goodwy.player.R;
import com.goodwy.player.helper.MusicPlayerRemote;
import com.goodwy.player.helper.MusicProgressViewUpdateHelper;
import com.goodwy.player.helper.PlayPauseButtonOnClickHandler;
import com.goodwy.player.ui.fragments.AbsMusicServiceFragment;
import com.goodwy.player.util.PreferenceUtil;
import com.goodwy.player.views.PlayPauseDrawable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class MiniPlayerFragment extends AbsMusicServiceFragment implements MusicProgressViewUpdateHelper.Callback {

    private Unbinder unbinder;

    @BindView(R.id.mini_player_title)
    TextView miniPlayerTitle;
    @BindView(R.id.mini_player_artist)
    TextView miniPlayerArtistName;
    @BindView(R.id.mini_player_play_pause_button)
    ImageView miniPlayerPlayPauseButton;
    @BindView(R.id.progress_bar)
    MaterialProgressBar progressBar;

    private PlayPauseDrawable miniPlayerPlayPauseDrawable;

    private MusicProgressViewUpdateHelper progressViewUpdateHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressViewUpdateHelper = new MusicProgressViewUpdateHelper(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String miniPlayerColor = PreferenceUtil.getInstance(getActivity()).getMiniPlayerTheme();
        String material = "material";
        String white = "white";
        String gray = "gray";
        String dark = "dark";
        String black = "black";
        String green = "green";
        String red = "red";
        String yellow = "yellow";
        String blue = "blue";
        if (miniPlayerColor == material) {
            return inflater.inflate(R.layout.fragment_mini_player_material, container, false);
        } else if (miniPlayerColor == white) {
            return inflater.inflate(R.layout.fragment_mini_player_white, container, false);
        } else if (miniPlayerColor == gray) {
            return inflater.inflate(R.layout.fragment_mini_player_gray, container, false);
        } else if (miniPlayerColor == dark) {
            return inflater.inflate(R.layout.fragment_mini_player_dark, container, false);
        } else if (miniPlayerColor == black) {
            return inflater.inflate(R.layout.fragment_mini_player_black, container, false);
        } else if (miniPlayerColor == green) {
            return inflater.inflate(R.layout.fragment_mini_player_green, container, false);
        } else if (miniPlayerColor == red) {
            return inflater.inflate(R.layout.fragment_mini_player_red, container, false);
        } else if (miniPlayerColor == yellow) {
            return inflater.inflate(R.layout.fragment_mini_player_yellow, container, false);
        } else if (miniPlayerColor == blue) {
            return inflater.inflate(R.layout.fragment_mini_player_blue, container, false);
        } else {
            return inflater.inflate(R.layout.fragment_mini_player_white, container, false);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);

        view.setOnTouchListener(new FlingPlayBackController(getActivity()));
        setUpMiniPlayer();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void setUpMiniPlayer() {
        setUpPlayPauseButton();
        progressBar.setProgressTintList(ColorStateList.valueOf(ThemeStore.accentColor(getActivity())));
    }

    private void setUpPlayPauseButton() {
        miniPlayerPlayPauseDrawable = new PlayPauseDrawable(getActivity());
        miniPlayerPlayPauseButton.setImageDrawable(miniPlayerPlayPauseDrawable);
        if (PreferenceUtil.getInstance(getContext()).colorMiniPlayerIcon()) {
            miniPlayerPlayPauseButton.setColorFilter(ThemeStore.accentColor(getActivity()));
        } else {
            miniPlayerPlayPauseButton.setColorFilter(ATHUtil.resolveColor(getActivity(), R.attr.iconColor, ThemeStore.textColorSecondary(getActivity())), PorterDuff.Mode.SRC_IN);
        }
        miniPlayerPlayPauseButton.setOnClickListener(new PlayPauseButtonOnClickHandler());
    }

    private void updateSongTitle() {
        miniPlayerTitle.setText(MusicPlayerRemote.getCurrentSong().title);
        miniPlayerArtistName.setText(MusicPlayerRemote.getCurrentSong().artistName);
    }

    @Override
    public void onServiceConnected() {
        updateSongTitle();
        updatePlayPauseDrawableState(false);
    }

    @Override
    public void onPlayingMetaChanged() {
        updateSongTitle();
    }

    @Override
    public void onPlayStateChanged() {
        updatePlayPauseDrawableState(true);
    }

    @Override
    public void onUpdateProgressViews(int progress, int total) {
        progressBar.setMax(total);
        progressBar.setProgress(progress);
    }

    @Override
    public void onResume() {
        super.onResume();
        progressViewUpdateHelper.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        progressViewUpdateHelper.stop();
    }

    private static class FlingPlayBackController implements View.OnTouchListener {

        GestureDetector flingPlayBackController;

        public FlingPlayBackController(Context context) {
            flingPlayBackController = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    if (Math.abs(velocityX) > Math.abs(velocityY)) {
                        if (velocityX < 0) {
                            MusicPlayerRemote.playNextSong();
                            return true;
                        } else if (velocityX > 0) {
                            MusicPlayerRemote.playPreviousSong();
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return flingPlayBackController.onTouchEvent(event);
        }
    }

    protected void updatePlayPauseDrawableState(boolean animate) {
        if (MusicPlayerRemote.isPlaying()) {
            miniPlayerPlayPauseDrawable.setPause(animate);
        } else {
            miniPlayerPlayPauseDrawable.setPlay(animate);
        }
    }
}
