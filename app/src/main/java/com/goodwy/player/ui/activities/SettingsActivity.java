package com.goodwy.player.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.media.audiofx.AudioEffect;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.TwoStatePreference;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.common.prefs.supportv7.ATEColorPreference;
import com.kabouzeid.appthemehelper.common.prefs.supportv7.ATEPreferenceFragmentCompat;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.goodwy.player.App;
import com.goodwy.player.R;
import com.goodwy.player.appshortcuts.DynamicShortcutManager;
import com.goodwy.player.misc.NonProAllowedColors;
import com.goodwy.player.preferences.BlacklistPreference;
import com.goodwy.player.preferences.BlacklistPreferenceDialog;
import com.goodwy.player.preferences.LibraryPreference;
import com.goodwy.player.preferences.LibraryPreferenceDialog;
import com.goodwy.player.preferences.NowPlayingScreenPreference;
import com.goodwy.player.preferences.NowPlayingScreenPreferenceDialog;
import com.goodwy.player.ui.activities.base.AbsBaseActivity;
import com.goodwy.player.util.NavigationUtil;
import com.goodwy.player.util.PreferenceUtil;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends AbsBaseActivity implements ColorChooserDialog.ColorCallback {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        setDrawUnderStatusbar();
        ButterKnife.bind(this);

        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        toolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new SettingsFragment()).commit();
        } else {
            SettingsFragment frag = (SettingsFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
            if (frag != null) frag.invalidateSettings();
        }
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        switch (dialog.getTitle()) {
            case R.string.primary_color:
                /*if (!App.isProVersion()) {
                    Arrays.sort(NonProAllowedColors.PRIMARY_COLORS);
                    if (Arrays.binarySearch(NonProAllowedColors.PRIMARY_COLORS, selectedColor) < 0) {
                        // color wasn't found
                        Toast.makeText(this, R.string.only_the_first_5_colors_available, Toast.LENGTH_LONG).show();
                        startActivity(new Intent(this, PurchaseActivity.class));
                        return;
                    }
                }*/
                ThemeStore.editTheme(this)
                        .primaryColor(selectedColor)
                        .commit();
                break;
            case R.string.accent_color:
                /*if (!App.isProVersion()) {
                    Arrays.sort(NonProAllowedColors.ACCENT_COLORS);
                    if (Arrays.binarySearch(NonProAllowedColors.ACCENT_COLORS, selectedColor) < 0) {
                        // color wasn't found
                        Toast.makeText(this, R.string.only_the_first_5_colors_available, Toast.LENGTH_LONG).show();
                        startActivity(new Intent(this, PurchaseActivity.class));
                        return;
                    }
                }*/
                ThemeStore.editTheme(this)
                        .accentColor(selectedColor)
                        .commit();
                break;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            new DynamicShortcutManager(this).updateDynamicShortcuts();
        }
        recreate();
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends ATEPreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

        private static void setSummary(@NonNull Preference preference) {
            setSummary(preference, PreferenceManager
                    .getDefaultSharedPreferences(preference.getContext())
                    .getString(preference.getKey(), ""));
        }

        private static void setSummary(Preference preference, @NonNull Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);
            } else {
                preference.setSummary(stringValue);
            }
        }

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            addPreferencesFromResource(R.xml.pref_library);
            addPreferencesFromResource(R.xml.pref_colors);
            addPreferencesFromResource(R.xml.pref_notification);
            addPreferencesFromResource(R.xml.pref_now_playing_screen);
            addPreferencesFromResource(R.xml.pref_images);
            addPreferencesFromResource(R.xml.pref_lockscreen);
            addPreferencesFromResource(R.xml.pref_audio);
            addPreferencesFromResource(R.xml.pref_playlists);
/**            addPreferencesFromResource(R.xml.pref_blacklist);*/
        }

        @Nullable
        @Override
        public DialogFragment onCreatePreferenceDialog(Preference preference) {
            if (preference instanceof NowPlayingScreenPreference) {
                return NowPlayingScreenPreferenceDialog.newInstance();
            } else if (preference instanceof BlacklistPreference) {
                return BlacklistPreferenceDialog.newInstance();
            } else if (preference instanceof LibraryPreference) {
                return LibraryPreferenceDialog.newInstance();
            }
            return super.onCreatePreferenceDialog(preference);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            getListView().setPadding(0, 0, 0, 0);
            invalidateSettings();
            PreferenceUtil.getInstance(getActivity()).registerOnSharedPreferenceChangedListener(this);
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            PreferenceUtil.getInstance(getActivity()).unregisterOnSharedPreferenceChangedListener(this);
        }

        private void invalidateSettings() {
            final Preference generalTheme = findPreference("general_theme");
            setSummary(generalTheme);
            generalTheme.setOnPreferenceChangeListener((preference, o) -> {
                String themeName = (String) o;
                if ((themeName.equals("black")||themeName.equals("blackNoDivider")||themeName.equals("darkNoDivider")) && !App.isProVersion()) {
                    Toast.makeText(getActivity(), R.string.black_theme_is_a_pro_feature, Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getContext(), PurchaseActivity.class));
                    return false;
                }

                setSummary(generalTheme, o);

                ThemeStore.markChanged(getActivity());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    // Set the new theme so that updateAppShortcuts can pull it
                    getActivity().setTheme(PreferenceUtil.getThemeResFromPrefValue(themeName));
                    new DynamicShortcutManager(getActivity()).updateDynamicShortcuts();
                }

                getActivity().recreate();
                return true;
            });

            final Preference miniPlayerTheme = findPreference("mini_player_theme");
            setSummary(miniPlayerTheme);
            miniPlayerTheme.setOnPreferenceChangeListener((preference, o) -> {
                String themeName = (String) o;

                setSummary(miniPlayerTheme, o);

                ThemeStore.markChanged(getActivity());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    // Set the new theme so that updateAppShortcuts can pull it
                    getActivity().setTheme(PreferenceUtil.getThemeResFromPrefValue(themeName));
                    new DynamicShortcutManager(getActivity()).updateDynamicShortcuts();
                }

                getActivity().recreate();
                return true;
            });

            final Preference autoDownloadImagesPolicy = findPreference("auto_download_images_policy");
            setSummary(autoDownloadImagesPolicy);
            autoDownloadImagesPolicy.setOnPreferenceChangeListener((preference, o) -> {
                setSummary(autoDownloadImagesPolicy, o);
                return true;
            });

            final ATEColorPreference primaryColorPref = (ATEColorPreference) findPreference("primary_color");
            final int primaryColor = ThemeStore.primaryColor(getActivity());
            int[] primary = new int[] {
                    Color.parseColor("#FFFFFF"),
                    Color.parseColor("#F44336"),
                    Color.parseColor("#E91E63"),
                    Color.parseColor("#9C27B0"),
                    Color.parseColor("#673AB7"),
                    Color.parseColor("#3F51B5"),
                    Color.parseColor("#2196F3"),
                    Color.parseColor("#03A9F4"),
                    Color.parseColor("#00BCD4"),
                    Color.parseColor("#009688"),
                    Color.parseColor("#4CAF50"),
                    Color.parseColor("#8BC34A"),
                    Color.parseColor("#CDDC39"),
                    Color.parseColor("#FFEB3B"),
                    Color.parseColor("#FFC107"),
                    Color.parseColor("#FF9800"),
                    Color.parseColor("#FF5722"),
                    Color.parseColor("#795548"),
                    Color.parseColor("#9E9E9E"),
                    Color.parseColor("#607D8B")
            };
            int[][] secondary = new int[][] {
                    new int[] {
                            Color.parseColor("#FFFFFF"),
                            Color.parseColor("#FAFAFA"),
                            Color.parseColor("#FFCCCCCC"),
                            Color.parseColor("#FF888888"),
                            Color.parseColor("#FF444444"),
                            Color.parseColor("#000000"),
                            Color.parseColor("#15B76C")
                    },
                    new int[] {
                            Color.parseColor("#FFEBEE"),
                            Color.parseColor("#FFCDD2"),
                            Color.parseColor("#EF9A9A"),
                            Color.parseColor("#E57373"),
                            Color.parseColor("#EF5350"),
                            Color.parseColor("#F44336"),
                            Color.parseColor("#E53935"),
                            Color.parseColor("#D32F2F"),
                            Color.parseColor("#C62828"),
                            Color.parseColor("#B71C1C")
                    },
                    new int[] {
                            Color.parseColor("#FCE4EC"),
                            Color.parseColor("#F8BBD0"),
                            Color.parseColor("#F48FB1"),
                            Color.parseColor("#F06292"),
                            Color.parseColor("#EC407A"),
                            Color.parseColor("#E91E63"),
                            Color.parseColor("#D81B60"),
                            Color.parseColor("#C2185B"),
                            Color.parseColor("#AD1457"),
                            Color.parseColor("#880E4F")
                    },
                    new int[] {
                            Color.parseColor("#F3E5F5"),
                            Color.parseColor("#E1BEE7"),
                            Color.parseColor("#CE93D8"),
                            Color.parseColor("#BA68C8"),
                            Color.parseColor("#AB47BC"),
                            Color.parseColor("#9C27B0"),
                            Color.parseColor("#8E24AA"),
                            Color.parseColor("#7B1FA2"),
                            Color.parseColor("#6A1B9A"),
                            Color.parseColor("#4A148C")
                    },
                    new int[] {
                            Color.parseColor("#EDE7F6"),
                            Color.parseColor("#D1C4E9"),
                            Color.parseColor("#B39DDB"),
                            Color.parseColor("#9575CD"),
                            Color.parseColor("#7E57C2"),
                            Color.parseColor("#673AB7"),
                            Color.parseColor("#5E35B1"),
                            Color.parseColor("#512DA8"),
                            Color.parseColor("#4527A0"),
                            Color.parseColor("#311B92")
                    },
                    new int[] {
                            Color.parseColor("#E8EAF6"),
                            Color.parseColor("#C5CAE9"),
                            Color.parseColor("#9FA8DA"),
                            Color.parseColor("#7986CB"),
                            Color.parseColor("#5C6BC0"),
                            Color.parseColor("#3F51B5"),
                            Color.parseColor("#3949AB"),
                            Color.parseColor("#303F9F"),
                            Color.parseColor("#283593"),
                            Color.parseColor("#1A237E")
                    },
                    new int[] {
                            Color.parseColor("#E3F2FD"),
                            Color.parseColor("#BBDEFB"),
                            Color.parseColor("#90CAF9"),
                            Color.parseColor("#64B5F6"),
                            Color.parseColor("#42A5F5"),
                            Color.parseColor("#2196F3"),
                            Color.parseColor("#1E88E5"),
                            Color.parseColor("#1976D2"),
                            Color.parseColor("#1565C0"),
                            Color.parseColor("#0D47A1")
                    },
                    new int[] {
                            Color.parseColor("#E1F5FE"),
                            Color.parseColor("#B3E5FC"),
                            Color.parseColor("#81D4FA"),
                            Color.parseColor("#4FC3F7"),
                            Color.parseColor("#29B6F6"),
                            Color.parseColor("#03A9F4"),
                            Color.parseColor("#039BE5"),
                            Color.parseColor("#0288D1"),
                            Color.parseColor("#0277BD"),
                            Color.parseColor("#01579B")
                    },
                    new int[] {
                            Color.parseColor("#E0F7FA"),
                            Color.parseColor("#B2EBF2"),
                            Color.parseColor("#80DEEA"),
                            Color.parseColor("#4DD0E1"),
                            Color.parseColor("#26C6DA"),
                            Color.parseColor("#00BCD4"),
                            Color.parseColor("#00ACC1"),
                            Color.parseColor("#0097A7"),
                            Color.parseColor("#00838F"),
                            Color.parseColor("#006064")
                    },
                    new int[] {
                            Color.parseColor("#E0F2F1"),
                            Color.parseColor("#B2DFDB"),
                            Color.parseColor("#80CBC4"),
                            Color.parseColor("#4DB6AC"),
                            Color.parseColor("#26A69A"),
                            Color.parseColor("#009688"),
                            Color.parseColor("#00897B"),
                            Color.parseColor("#00796B"),
                            Color.parseColor("#00695C"),
                            Color.parseColor("#004D40")
                    },
                    new int[] {
                            Color.parseColor("#E8F5E9"),
                            Color.parseColor("#C8E6C9"),
                            Color.parseColor("#A5D6A7"),
                            Color.parseColor("#81C784"),
                            Color.parseColor("#66BB6A"),
                            Color.parseColor("#4CAF50"),
                            Color.parseColor("#43A047"),
                            Color.parseColor("#388E3C"),
                            Color.parseColor("#2E7D32"),
                            Color.parseColor("#1B5E20")
                    },
                    new int[] {
                            Color.parseColor("#F1F8E9"),
                            Color.parseColor("#DCEDC8"),
                            Color.parseColor("#C5E1A5"),
                            Color.parseColor("#AED581"),
                            Color.parseColor("#9CCC65"),
                            Color.parseColor("#8BC34A"),
                            Color.parseColor("#7CB342"),
                            Color.parseColor("#689F38"),
                            Color.parseColor("#558B2F"),
                            Color.parseColor("#33691E")
                    },
                    new int[] {
                            Color.parseColor("#F9FBE7"),
                            Color.parseColor("#F0F4C3"),
                            Color.parseColor("#E6EE9C"),
                            Color.parseColor("#DCE775"),
                            Color.parseColor("#D4E157"),
                            Color.parseColor("#CDDC39"),
                            Color.parseColor("#C0CA33"),
                            Color.parseColor("#AFB42B"),
                            Color.parseColor("#9E9D24"),
                            Color.parseColor("#827717")
                    },
                    new int[] {
                            Color.parseColor("#FFFDE7"),
                            Color.parseColor("#FFF9C4"),
                            Color.parseColor("#FFF59D"),
                            Color.parseColor("#FFF176"),
                            Color.parseColor("#FFEE58"),
                            Color.parseColor("#FFEB3B"),
                            Color.parseColor("#FDD835"),
                            Color.parseColor("#FBC02D"),
                            Color.parseColor("#F9A825"),
                            Color.parseColor("#F57F17")
                    },
                    new int[] {
                            Color.parseColor("#FFF8E1"),
                            Color.parseColor("#FFECB3"),
                            Color.parseColor("#FFE082"),
                            Color.parseColor("#FFD54F"),
                            Color.parseColor("#FFCA28"),
                            Color.parseColor("#FFC107"),
                            Color.parseColor("#FFB300"),
                            Color.parseColor("#FFA000"),
                            Color.parseColor("#FF8F00"),
                            Color.parseColor("#FF6F00")
                    },
                    new int[] {
                            Color.parseColor("#FFF3E0"),
                            Color.parseColor("#FFE0B2"),
                            Color.parseColor("#FFCC80"),
                            Color.parseColor("#FFB74D"),
                            Color.parseColor("#FFA726"),
                            Color.parseColor("#FF9800"),
                            Color.parseColor("#FB8C00"),
                            Color.parseColor("#F57C00"),
                            Color.parseColor("#EF6C00"),
                            Color.parseColor("#E65100")
                    },
                    new int[] {
                            Color.parseColor("#FBE9E7"),
                            Color.parseColor("#FFCCBC"),
                            Color.parseColor("#FFAB91"),
                            Color.parseColor("#FF8A65"),
                            Color.parseColor("#FF7043"),
                            Color.parseColor("#FF5722"),
                            Color.parseColor("#F4511E"),
                            Color.parseColor("#E64A19"),
                            Color.parseColor("#D84315"),
                            Color.parseColor("#BF360C")
                    },
                    new int[] {
                            Color.parseColor("#EFEBE9"),
                            Color.parseColor("#D7CCC8"),
                            Color.parseColor("#BCAAA4"),
                            Color.parseColor("#A1887F"),
                            Color.parseColor("#8D6E63"),
                            Color.parseColor("#795548"),
                            Color.parseColor("#6D4C41"),
                            Color.parseColor("#5D4037"),
                            Color.parseColor("#4E342E"),
                            Color.parseColor("#3E2723")
                    },
                    new int[] {
                            Color.parseColor("#FAFAFA"),
                            Color.parseColor("#F5F5F5"),
                            Color.parseColor("#EEEEEE"),
                            Color.parseColor("#E0E0E0"),
                            Color.parseColor("#BDBDBD"),
                            Color.parseColor("#9E9E9E"),
                            Color.parseColor("#757575"),
                            Color.parseColor("#616161"),
                            Color.parseColor("#424242"),
                            Color.parseColor("#212121")
                    },
                    new int[] {
                            Color.parseColor("#ECEFF1"),
                            Color.parseColor("#CFD8DC"),
                            Color.parseColor("#B0BEC5"),
                            Color.parseColor("#90A4AE"),
                            Color.parseColor("#78909C"),
                            Color.parseColor("#607D8B"),
                            Color.parseColor("#546E7A"),
                            Color.parseColor("#455A64"),
                            Color.parseColor("#37474F"),
                            Color.parseColor("#263238")
                    }
            };
            primaryColorPref.setColor(primaryColor, ColorUtil.darkenColor(primaryColor));
            primaryColorPref.setOnPreferenceClickListener(preference -> {
                new ColorChooserDialog.Builder(getActivity(), R.string.primary_color)
                        .customColors(primary, secondary)
                        .accentMode(false)  // when true, will display accent palette instead of primary palette
                        .allowUserColorInput(true)  // custom color using RGB sliders
                        .allowUserColorInputAlpha(true) // change transparency (alpha)
                        .preselect(primaryColor)
                        .dynamicButtonColor(true)  // defaults to true, false will disable changing action buttons' color to currently selected color
                        .show(getActivity());
                return true;
            });

            final ATEColorPreference accentColorPref = (ATEColorPreference) findPreference("accent_color");
            final int accentColor = ThemeStore.accentColor(getActivity());
            accentColorPref.setColor(accentColor, ColorUtil.darkenColor(accentColor));
            accentColorPref.setOnPreferenceClickListener(preference -> {
                new ColorChooserDialog.Builder(getActivity(), R.string.accent_color)
                        .customColors(primary, secondary)
                        .accentMode(false)  // when true, will display accent palette instead of primary palette
                        .allowUserColorInput(true)  // custom color using RGB sliders
                        .allowUserColorInputAlpha(true) // change transparency (alpha)
                        .preselect(accentColor)
                        .dynamicButtonColor(true)  // defaults to true, false will disable changing action buttons' color to currently selected color
                        .show(getActivity());
                return true;
            });

            TwoStatePreference colorNavBar = (TwoStatePreference) findPreference("should_color_navigation_bar");
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                colorNavBar.setVisible(false);
            } else {
                colorNavBar.setChecked(ThemeStore.coloredNavigationBar(getActivity()));
                colorNavBar.setOnPreferenceChangeListener((preference, newValue) -> {
                    ThemeStore.editTheme(getActivity())
                            .coloredNavigationBar((Boolean) newValue)
                            .commit();
                    getActivity().recreate();
                    return true;
                });
            }

            final TwoStatePreference classicNotification = (TwoStatePreference) findPreference("classic_notification");
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                classicNotification.setVisible(false);
            } else {
                classicNotification.setChecked(PreferenceUtil.getInstance(getActivity()).classicNotification());
                classicNotification.setOnPreferenceChangeListener((preference, newValue) -> {
                    // Save preference
                    PreferenceUtil.getInstance(getActivity()).setClassicNotification((Boolean) newValue);
                    return true;
                });
            }

            final TwoStatePreference coloredNotification = (TwoStatePreference) findPreference("colored_notification");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                coloredNotification.setEnabled(PreferenceUtil.getInstance(getActivity()).classicNotification());
            } else {
                coloredNotification.setChecked(PreferenceUtil.getInstance(getActivity()).coloredNotification());
                coloredNotification.setOnPreferenceChangeListener((preference, newValue) -> {
                    // Save preference
                    PreferenceUtil.getInstance(getActivity()).setColoredNotification((Boolean) newValue);
                    return true;
                });
            }

            final TwoStatePreference colorAppShortcuts = (TwoStatePreference) findPreference("should_color_app_shortcuts");
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
                colorAppShortcuts.setVisible(false);
            } else {
                colorAppShortcuts.setChecked(PreferenceUtil.getInstance(getActivity()).coloredAppShortcuts());
                colorAppShortcuts.setOnPreferenceChangeListener((preference, newValue) -> {
                    // Save preference
                    PreferenceUtil.getInstance(getActivity()).setColoredAppShortcuts((Boolean) newValue);

                    // Update app shortcuts
                    new DynamicShortcutManager(getActivity()).updateDynamicShortcuts();

                    return true;
                });
            }

            TwoStatePreference colorTabText = (TwoStatePreference) findPreference("color_tab_text");
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                colorTabText.setVisible(false);
            } else {
                colorTabText.setChecked(ThemeStore.autoGeneratePrimaryDark(getActivity()));
                colorTabText.setOnPreferenceChangeListener((preference, newValue) -> {
                    ThemeStore.editTheme(getActivity())
                            .autoGeneratePrimaryDark((Boolean) newValue)
                            .commit();
                    getActivity().recreate();
                    return true;
                });
            }

            TwoStatePreference colorMiniPlayerIcon = (TwoStatePreference) findPreference("color_mini_player_icon");
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                colorMiniPlayerIcon.setVisible(false);
            } else {
                colorMiniPlayerIcon.setChecked(ThemeStore.coloredStatusBar(getActivity()));
                colorMiniPlayerIcon.setOnPreferenceChangeListener((preference, newValue) -> {
                    ThemeStore.editTheme(getActivity())
                            .coloredStatusBar((Boolean) newValue)
                            .commit();
                    getActivity().recreate();
                    return true;
                });
            }

/**            final Preference equalizer = findPreference("equalizer");
            if (!hasEqualizer()) {
                equalizer.setEnabled(false);
                equalizer.setSummary(getResources().getString(R.string.no_equalizer));
            }
            equalizer.setOnPreferenceClickListener(preference -> {
                NavigationUtil.openEqualizer(getActivity());
                return true;
            });*/

            updateNowPlayingScreenSummary();
        }

/**        private boolean hasEqualizer() {
            final Intent effects = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
            PackageManager pm = getActivity().getPackageManager();
            ResolveInfo ri = pm.resolveActivity(effects, 0);
            return ri != null;
        }*/

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch (key) {
                case PreferenceUtil.NOW_PLAYING_SCREEN_ID:
                    updateNowPlayingScreenSummary();
                    break;
                case PreferenceUtil.CLASSIC_NOTIFICATION:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        findPreference("colored_notification").setEnabled(sharedPreferences.getBoolean(key, false));
                    }
                    break;
            }
        }

        private void updateNowPlayingScreenSummary() {
            findPreference("now_playing_screen_id").setSummary(PreferenceUtil.getInstance(getActivity()).getNowPlayingScreen().titleRes);
        }
    }
}
