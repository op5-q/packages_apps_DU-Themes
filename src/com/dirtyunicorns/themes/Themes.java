/*
 * Copyright (C) 2019-2020 The Dirty Unicorns Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dirtyunicorns.themes;

import static android.os.UserHandle.USER_SYSTEM;
import static com.dirtyunicorns.themes.utils.Utils.getScheduledStartThemeSummary;
import static com.dirtyunicorns.themes.utils.Utils.getScheduledStartThemeTime;
import static com.dirtyunicorns.themes.utils.Utils.getThemeSchedule;
import static com.dirtyunicorns.themes.utils.Utils.handleBackgrounds;
import static com.dirtyunicorns.themes.utils.Utils.handleOverlays;
import static com.dirtyunicorns.themes.utils.Utils.isLiveWallpaper;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.UiModeManager;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.om.IOverlayManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.MenuItem;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;

import com.android.internal.util.nitrogen.NitrogenUtils;
import com.android.internal.util.pixeldust.ThemesUtils;

import com.dirtyunicorns.themes.db.ThemeDatabase;

import java.util.Calendar;

public class Themes extends PreferenceFragment implements ThemesListener {

    private static final String TAG = "Themes";

    private static final String PREF_BACKUP_THEMES = "backup_themes";
    private static final String PREF_RESTORE_THEMES = "restore_themes";
    private static final String PREF_WP_PREVIEW = "wp_preview";
    private static final String PREF_THEME_SCHEDULE = "theme_schedule";
    private static final String PREF_THEME_ACCENT_PICKER = "theme_accent_picker";
    public static final String PREF_THEME_ACCENT_COLOR = "theme_accent_color";
    public static final String PREF_ADAPTIVE_ICON_SHAPE = "adapative_icon_shape";
    public static final String PREF_FONT_PICKER = "font_picker";
    public static final String PREF_STATUSBAR_ICONS = "statusbar_icons";
    public static final String PREF_THEME_SWITCH = "theme_switch";

    private int mBackupLimit = 10;
    private static boolean mUseSharedPrefListener;

    private Context mContext;
    private IOverlayManager mOverlayManager;
    private SharedPreferences mSharedPreferences;
    private ThemeDatabase mThemeDatabase;
    private UiModeManager mUiModeManager;

    private ListPreference mAdaptiveIconShape;
    private ListPreference mFontPicker;
    private ListPreference mStatusbarIcons;
    private ListPreference mThemeSwitch;
    private Preference mAccentPicker;
    private Preference mBackupThemes;
    private Preference mRestoreThemes;
    private Preference mThemeSchedule;
    private Preference mWpPreview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        addPreferencesFromResource(R.xml.themes);

        mContext = getActivity();

        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mUiModeManager = getContext().getSystemService(UiModeManager.class);
        mThemeDatabase = new ThemeDatabase(mContext);

        // Shared preferences
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mSharedPreferences.registerOnSharedPreferenceChangeListener(mSharedPrefListener);

        // Theme services
        UiModeManager mUiModeManager = mContext.getSystemService(UiModeManager.class);
        mOverlayManager = IOverlayManager.Stub.asInterface(
                ServiceManager.getService(Context.OVERLAY_SERVICE));

        mWpPreview = findPreference(PREF_WP_PREVIEW);

        // Theme schedule
        mThemeSchedule = (Preference) findPreference(PREF_THEME_SCHEDULE);
        assert mThemeSchedule != null;
        mThemeSchedule.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), Schedule.class);
                startActivity(intent);
                return true;
            }
        });

        // Accent picker
        mAccentPicker = (Preference) findPreference(PREF_THEME_ACCENT_PICKER);
        assert mAccentPicker != null;
        mAccentPicker.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                FragmentManager manager = getFragmentManager();
                Fragment frag = manager.findFragmentByTag(AccentPicker.TAG_ACCENT_PICKER);
                if (frag != null) {
                    manager.beginTransaction().remove(frag).commit();
                }
                AccentPicker accentPickerFragment = new AccentPicker();
                accentPickerFragment.show(manager, AccentPicker.TAG_ACCENT_PICKER);
                return true;
            }
        });

        // Themes backup
        mBackupThemes = (Preference) findPreference(PREF_BACKUP_THEMES);
        assert mBackupThemes != null;
        mBackupThemes.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (isLiveWallpaper(getActivity())) {
                    new AlertDialog.Builder(getActivity(), R.style.AccentDialogTheme)
                            .setTitle(getContext().getString(R.string.theme_backup_dialog_title))
                            .setMessage(getContext().getString(R.string.theme_backup_dialog_message))
                            .setCancelable(false)
                            .setPositiveButton(getContext().getString(R.string.theme_backup_dialog_positive),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            FragmentManager manager = getFragmentManager();
                                            Fragment frag = manager.findFragmentByTag(BackupThemes.TAG_BACKUP_THEMES);
                                            if (frag != null) {
                                                manager.beginTransaction().remove(frag).commit();
                                            }
                                            BackupThemes backupThemesFragment = new BackupThemes(Themes.this);
                                            backupThemesFragment.show(manager, BackupThemes.TAG_BACKUP_THEMES);
                                        }
                                    })
                            .setNegativeButton(getContext().getString(R.string.theme_backup_dialog_negative),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).show();

                } else {
                    FragmentManager manager = getFragmentManager();
                    Fragment frag = manager.findFragmentByTag(BackupThemes.TAG_BACKUP_THEMES);
                    if (frag != null) {
                        manager.beginTransaction().remove(frag).commit();
                    }
                    BackupThemes backupThemesFragment = new BackupThemes(Themes.this);
                    backupThemesFragment.show(manager, BackupThemes.TAG_BACKUP_THEMES);
                }
                return true;
            }
        });

        // Themes restore
        mRestoreThemes = (Preference) findPreference(PREF_RESTORE_THEMES);
        assert mRestoreThemes != null;
        mRestoreThemes.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(mContext, RestoreThemes.class);
                if (intent != null) {
                    setSharedPrefListener(true);
                    startActivity(intent);
                }
                return true;
            }
        });

        // Themes
        mThemeSwitch = (ListPreference) findPreference(PREF_THEME_SWITCH);
        if (NitrogenUtils.isThemeEnabled("com.android.theme.solarizeddark.system")) {
            mThemeSwitch.setValue("4");
        } else if (NitrogenUtils.isThemeEnabled("com.android.theme.pitchblack.system")) {
            mThemeSwitch.setValue("3");
        } else if (mUiModeManager != null) {
            if (mUiModeManager.getNightMode() == UiModeManager.MODE_NIGHT_YES) {
                mThemeSwitch.setValue("2");
            } else {
                mThemeSwitch.setValue("1");
            }
        }
        mThemeSwitch.setSummary(mThemeSwitch.getEntry());

        // Font picker
        mFontPicker = (ListPreference) findPreference(PREF_FONT_PICKER);
        if (NitrogenUtils.isThemeEnabled("com.android.theme.font.notoserifsource")) {
            mFontPicker.setValue("2");
        } else if (NitrogenUtils.isThemeEnabled("com.android.theme.font.aclonicasource")) {
            mFontPicker.setValue("3");
        } else if (NitrogenUtils.isThemeEnabled("com.android.theme.font.amarantesource")) {
            mFontPicker.setValue("4");
        } else if (NitrogenUtils.isThemeEnabled("com.android.theme.font.bariolsource")) {
            mFontPicker.setValue("5");
        } else if (NitrogenUtils.isThemeEnabled("com.android.theme.font.cagliostrosource")) {
            mFontPicker.setValue("6");
        } else if (NitrogenUtils.isThemeEnabled("com.android.theme.font.comicsanssource")) {
            mFontPicker.setValue("7");
        } else if (NitrogenUtils.isThemeEnabled("com.android.theme.font.coolstorysource")) {
            mFontPicker.setValue("8");
        } else if (NitrogenUtils.isThemeEnabled("com.android.theme.font.firasans")) {
            mFontPicker.setValue("9");
        } else if (NitrogenUtils.isThemeEnabled("com.android.theme.font.googlesans")) {
            mFontPicker.setValue("10");
        } else if (NitrogenUtils.isThemeEnabled("com.android.theme.font.lgsmartgothicsource")) {
            mFontPicker.setValue("11");
        } else if (NitrogenUtils.isThemeEnabled("com.android.theme.font.oneplusslate")) {
            mFontPicker.setValue("12");
        } else if (NitrogenUtils.isThemeEnabled("com.android.theme.font.rosemarysource")) {
            mFontPicker.setValue("13");
        } else if (NitrogenUtils.isThemeEnabled("com.android.theme.font.samsungone")) {
            mFontPicker.setValue("14");
        } else if (NitrogenUtils.isThemeEnabled("com.android.theme.font.sanfrancisco")) {
            mFontPicker.setValue("15");
        } else if (NitrogenUtils.isThemeEnabled("com.android.theme.font.sonysketchsource")) {
            mFontPicker.setValue("16");
        } else if (NitrogenUtils.isThemeEnabled("com.android.theme.font.surfersource")) {
            mFontPicker.setValue("17");
        } else {
            mFontPicker.setValue("1");
        }
        mFontPicker.setSummary(mFontPicker.getEntry());

        // Adaptive icon shape
        mAdaptiveIconShape = (ListPreference) findPreference(PREF_ADAPTIVE_ICON_SHAPE);
        if (NitrogenUtils.isThemeEnabled("com.android.theme.icon.teardrop")) {
            mAdaptiveIconShape.setValue("2");
        } else if (NitrogenUtils.isThemeEnabled("com.android.theme.icon.squircle")) {
            mAdaptiveIconShape.setValue("3");
        } else if (NitrogenUtils.isThemeEnabled("com.android.theme.icon.roundedrect")) {
            mAdaptiveIconShape.setValue("4");
        } else if (NitrogenUtils.isThemeEnabled("com.android.theme.icon.cylinder")) {
            mAdaptiveIconShape.setValue("5");
        } else if (NitrogenUtils.isThemeEnabled("com.android.theme.icon.hexagon")) {
            mAdaptiveIconShape.setValue("6");
        } else {
            mAdaptiveIconShape.setValue("1");
        }
        mAdaptiveIconShape.setSummary(mAdaptiveIconShape.getEntry());

        // Statusbar icons
        mStatusbarIcons = (ListPreference) findPreference(PREF_STATUSBAR_ICONS);
        if (NitrogenUtils.isThemeEnabled("com.android.theme.icon_pack.filled.android")) {
            mStatusbarIcons.setValue("2");
        } else if (NitrogenUtils.isThemeEnabled("com.android.theme.icon_pack.rounded.android")) {
            mStatusbarIcons.setValue("3");
        } else if (NitrogenUtils.isThemeEnabled("com.android.theme.icon_pack.circular.android")) {
            mStatusbarIcons.setValue("4");
        } else {
            mStatusbarIcons.setValue("1");
        }
        mStatusbarIcons.setSummary(mStatusbarIcons.getEntry());

        updateThemeScheduleSummary();
        setWallpaperPreview();
        updateBackupPref();
        updateRestorePref();
    }

    private void setWallpaperPreview() {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(getActivity());
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        mWpPreview.setIcon(wallpaperDrawable);
    }

    private void updateBackupPref() {
        mBackupThemes.setEnabled(getThemeCount() < mBackupLimit ? true : false);
        if (getThemeCount() == mBackupLimit) {
            mBackupThemes.setSummary(R.string.theme_backup_reach_limit_summary);
        } else {
            mBackupThemes.setSummary(R.string.theme_backup_summary);
        }
    }

    private void updateRestorePref() {
        mRestoreThemes.setEnabled(getThemeCount() > 0 ? true : false);
        if (getThemeCount() == 0) {
            mRestoreThemes.setSummary(R.string.theme_restore_no_backup_summary);
        } else {
            mRestoreThemes.setSummary(R.string.theme_restore_summary);
        }
    }

    private int getThemeCount() {
        int count = mThemeDatabase.getThemeDbUtilsCount();
        return count;
    }

    public OnSharedPreferenceChangeListener mSharedPrefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, String key) {
            class FontPicker extends AsyncTask<Void, Void, Void> {

                protected Void doInBackground(Void... param) {
                    return null;
                }

                protected void onPostExecute(Void param) {
                }

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    String font_type = sharedPreferences.getString(PREF_FONT_PICKER, "1");
                    if (NitrogenUtils.isThemeEnabled("com.android.theme.font.notoserifsource")) {
                        handleOverlays("com.android.theme.font.notoserifsource", false, mOverlayManager);
                    }
                    if (NitrogenUtils.isThemeEnabled("com.android.theme.font.aclonicasource")) {
                        handleOverlays("com.android.theme.font.aclonicasource", false, mOverlayManager);
                    }
                    if (NitrogenUtils.isThemeEnabled("com.android.theme.font.amarantesource")) {
                        handleOverlays("com.android.theme.font.amarantesource", false, mOverlayManager);
                    }
                    if (NitrogenUtils.isThemeEnabled("com.android.theme.font.bariolsource")) {
                        handleOverlays("com.android.theme.font.bariolsource", false, mOverlayManager);
                    }
                    if (NitrogenUtils.isThemeEnabled("com.android.theme.font.cagliostrosource")) {
                        handleOverlays("com.android.theme.font.cagliostrosource", false, mOverlayManager);
                    }
                    if (NitrogenUtils.isThemeEnabled("com.android.theme.font.comicsanssource")) {
                        handleOverlays("com.android.theme.font.comicsanssource", false, mOverlayManager);
                    }
                    if (NitrogenUtils.isThemeEnabled("com.android.theme.font.coolstorysource")) {
                        handleOverlays("com.android.theme.font.coolstorysource", false, mOverlayManager);
                    }
                    if (NitrogenUtils.isThemeEnabled("com.android.theme.font.firasans")) {
                        handleOverlays("com.android.theme.font.firasans", false, mOverlayManager);
                    }
                    if (NitrogenUtils.isThemeEnabled("com.android.theme.font.googlesans")) {
                        handleOverlays("com.android.theme.font.googlesans", false, mOverlayManager);
                    }
                    if (NitrogenUtils.isThemeEnabled("com.android.theme.font.lgsmartgothicsource")) {
                        handleOverlays("com.android.theme.font.lgsmartgothicsource", false, mOverlayManager);
                    }
                    if (NitrogenUtils.isThemeEnabled("com.android.theme.font.oneplusslate")) {
                        handleOverlays("com.android.theme.font.oneplusslate", false, mOverlayManager);
                    }
                    if (NitrogenUtils.isThemeEnabled("com.android.theme.font.rosemarysource")) {
                        handleOverlays("com.android.theme.font.rosemarysource", false, mOverlayManager);
                    }
                    if (NitrogenUtils.isThemeEnabled("com.android.theme.font.samsungone")) {
                        handleOverlays("com.android.theme.font.samsungone", false, mOverlayManager);
                    }
                    if (NitrogenUtils.isThemeEnabled("com.android.theme.font.sanfrancisco")) {
                        handleOverlays("com.android.theme.font.sanfrancisco", false, mOverlayManager);
                    }
                    if (NitrogenUtils.isThemeEnabled("com.android.theme.font.sonysketchsource")) {
                        handleOverlays("com.android.theme.font.sonysketchsource", false, mOverlayManager);
                    }
                    if (NitrogenUtils.isThemeEnabled("com.android.theme.font.surfersource")) {
                        handleOverlays("com.android.theme.font.surfersource", false, mOverlayManager);
                    }
                    switch (font_type) {
                        case "1":
                            for (int i = 0; i < ThemesUtils.FONTS.length; i++) {
                                String fonts = ThemesUtils.FONTS[i];
                                try {
                                    mOverlayManager.setEnabled(fonts, false, USER_SYSTEM);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        case "2":
                        handleOverlays("com.android.theme.font.notoserifsource", true, mOverlayManager);
                        break;
                    case "3":
                        handleOverlays("com.android.theme.font.aclonicasource", true, mOverlayManager);
                        break;
                    case "4":
                        handleOverlays("com.android.theme.font.amarantesource", true, mOverlayManager);
                        break;
                    case "5":
                        handleOverlays("com.android.theme.font.bariolsource", true, mOverlayManager);
                        break;
                    case "6":
                        handleOverlays("com.android.theme.font.cagliostrosource", true, mOverlayManager);
                        break;
                    case "7":
                        handleOverlays("com.android.theme.font.comicsanssource", true, mOverlayManager);
                        break;
                    case "8":
                        handleOverlays("com.android.theme.font.coolstorysource", true, mOverlayManager);
                        break;
                    case "9":
                        handleOverlays("com.android.theme.font.firasans", true, mOverlayManager);
                        break;
                    case "10":
                        handleOverlays("com.android.theme.font.googlesans", true, mOverlayManager);
                        break;
                    case "11":
                        handleOverlays("com.android.theme.font.lgsmartgothicsource", true, mOverlayManager);
                        break;
                    case "12":
                        handleOverlays("com.android.theme.font.oneplusslate", true, mOverlayManager);
                        break;
                    case "13":
                        handleOverlays("com.android.theme.font.rosemarysource", true, mOverlayManager);
                        break;
                    case "14":
                        handleOverlays("com.android.theme.font.samsungone", true, mOverlayManager);
                        break;
                    case "15":
                        handleOverlays("com.android.theme.font.sanfrancisco", true, mOverlayManager);
                        break;
                    case "16":
                        handleOverlays("com.android.theme.font.sonysketchsource", true, mOverlayManager);
                        break;
                    case "17":
                        handleOverlays("com.android.theme.font.surfersource", true, mOverlayManager);
                        break;
                    }
                    mFontPicker.setSummary(mFontPicker.getEntry());
                }
            }

            if (key.equals(PREF_FONT_PICKER)) {
                new FontPicker().execute();
            }

            if (key.equals(PREF_ADAPTIVE_ICON_SHAPE)) {
                String adapative_icon_shape = sharedPreferences.getString(PREF_ADAPTIVE_ICON_SHAPE, "1");

                handleOverlays("com.android.theme.icon.teardrop", false, mOverlayManager);
                handleOverlays("com.android.theme.icon.squircle", false, mOverlayManager);
                handleOverlays("com.android.theme.icon.roundedrect", false, mOverlayManager);
                handleOverlays("com.android.theme.icon.cylinder", false, mOverlayManager);
                handleOverlays("com.android.theme.icon.hexagon", false, mOverlayManager);

                switch (adapative_icon_shape) {
                    case "2":
                        handleOverlays("com.android.theme.icon.teardrop", true, mOverlayManager);
                        break;
                    case "3":
                        handleOverlays("com.android.theme.icon.squircle", true, mOverlayManager);
                        break;
                    case "4":
                        handleOverlays("com.android.theme.icon.roundedrect", true, mOverlayManager);
                        break;
                    case "5":
                        handleOverlays("com.android.theme.icon.cylinder", true, mOverlayManager);
                        break;
                    case "6":
                        handleOverlays("com.android.theme.icon.hexagon", true, mOverlayManager);
                        break;
                }
                mAdaptiveIconShape.setSummary(mAdaptiveIconShape.getEntry());
            }

            if (key.equals(PREF_STATUSBAR_ICONS)) {
                String statusbar_icons = sharedPreferences.getString(PREF_STATUSBAR_ICONS, "1");
                switch (statusbar_icons) {
                    case "1":
                        handleOverlays("com.android.theme.icon_pack.filled.android", false, mOverlayManager);
                        handleOverlays("com.android.theme.icon_pack.rounded.android", false, mOverlayManager);
                        handleOverlays("com.android.theme.icon_pack.circular.android", false, mOverlayManager);
                        break;
                    case "2":
                        handleOverlays("com.android.theme.icon_pack.filled.android", true, mOverlayManager);
                        handleOverlays("com.android.theme.icon_pack.rounded.android", false, mOverlayManager);
                        handleOverlays("com.android.theme.icon_pack.circular.android", false, mOverlayManager);
                        break;
                    case "3":
                        handleOverlays("com.android.theme.icon_pack.filled.android", false, mOverlayManager);
                        handleOverlays("com.android.theme.icon_pack.rounded.android", true, mOverlayManager);
                        handleOverlays("com.android.theme.icon_pack.circular.android", false, mOverlayManager);
                        break;
                    case "4":
                        handleOverlays("com.android.theme.icon_pack.filled.android", false, mOverlayManager);
                        handleOverlays("com.android.theme.icon_pack.rounded.android", false, mOverlayManager);
                        handleOverlays("com.android.theme.icon_pack.circular.android", true, mOverlayManager);
                        break;
                }
                mStatusbarIcons.setSummary(mStatusbarIcons.getEntry());
            }

            if (key.equals(PREF_THEME_SWITCH)) {
                String theme_switch = sharedPreferences.getString(PREF_THEME_SWITCH, "1");
                switch (theme_switch) {
                    case "1":
                        handleBackgrounds(false, mContext, UiModeManager.MODE_NIGHT_NO,
                                ThemesUtils.PITCH_BLACK, mOverlayManager);
                        handleBackgrounds(false, mContext, UiModeManager.MODE_NIGHT_NO,
                                ThemesUtils.SOLARIZED_DARK, mOverlayManager);
                        break;
                    case "2":
                        handleBackgrounds(false, mContext, UiModeManager.MODE_NIGHT_YES,
                                ThemesUtils.PITCH_BLACK, mOverlayManager);
                        handleBackgrounds(false, mContext, UiModeManager.MODE_NIGHT_YES,
                                ThemesUtils.SOLARIZED_DARK, mOverlayManager);
                        break;
                    case "3":
                        handleBackgrounds(true, mContext, UiModeManager.MODE_NIGHT_YES,
                                ThemesUtils.PITCH_BLACK, mOverlayManager);
                        handleBackgrounds(false, mContext, UiModeManager.MODE_NIGHT_YES,
                                ThemesUtils.SOLARIZED_DARK, mOverlayManager);
                        break;
                    case "4":
                        handleBackgrounds(false, mContext, UiModeManager.MODE_NIGHT_YES,
                                ThemesUtils.PITCH_BLACK, mOverlayManager);
                        handleBackgrounds(true, mContext, UiModeManager.MODE_NIGHT_YES,
                                ThemesUtils.SOLARIZED_DARK, mOverlayManager);
                        break;
                }
                mThemeSwitch.setSummary(mThemeSwitch.getEntry());
            }
        }
    };

    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    }

    public static void setSharedPrefListener(boolean listener) {
        mUseSharedPrefListener = listener;
    }

    @Override
    public void onCloseBackupDialog(DialogFragment dialog) {
        updateBackupPref();
        updateRestorePref();
    }

    @Override
    public void onResume() {
        super.onResume();
        mSharedPreferences.registerOnSharedPreferenceChangeListener(mSharedPrefListener);
        setWallpaperPreview();
        updateBackupPref();
        updateRestorePref();
        updateAccentSummary();
        updateIconShapeSummary();
        updateStatusbarIconsSummary();
        updateThemeScheduleSummary();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!mUseSharedPrefListener) {
            mSharedPreferences.unregisterOnSharedPreferenceChangeListener(mSharedPrefListener);
        }
        updateThemeScheduleSummary();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!mUseSharedPrefListener) {
            mSharedPreferences.unregisterOnSharedPreferenceChangeListener(mSharedPrefListener);
        }
        updateThemeScheduleSummary();
    }

    private void updateThemeScheduleSummary() {
        if (mThemeSchedule != null) {
            if (getThemeSchedule(mSharedPreferences).equals("1")) {
                mThemeSchedule.setSummary(mContext.getString(R.string.theme_schedule_summary));
            } else {
                if (!Calendar.getInstance().before(getScheduledStartThemeTime(mSharedPreferences))) {
                    mThemeSchedule.setSummary(getScheduledStartThemeSummary(mSharedPreferences, mContext)
                            + " " + mContext.getString(R.string.theme_schedule_dyn_summary));
                }
            }
        }
    }

    private void updateAccentSummary() {
        if (mAccentPicker != null) {
            if (NitrogenUtils.isThemeEnabled("com.android.theme.color.space")) {
                mAccentPicker.setSummary("Space");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.purple")) {
                mAccentPicker.setSummary("Purple");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.orchid")) {
                mAccentPicker.setSummary("Orchid");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.ocean")) {
                mAccentPicker.setSummary("Ocean");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.green")) {
                mAccentPicker.setSummary("Green");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.cinnamon")) {
                mAccentPicker.setSummary("Cinnamon");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.amber")) {
                mAccentPicker.setSummary("Amber");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.blue")) {
                mAccentPicker.setSummary("Blue");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.bluegrey")) {
                mAccentPicker.setSummary("Blue Grey");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.brown")) {
                mAccentPicker.setSummary("Brown");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.cyan")) {
                mAccentPicker.setSummary("Cyan");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.deeporange")) {
                mAccentPicker.setSummary("Deep Orange");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.deeppurple")) {
                mAccentPicker.setSummary("Deep Purple");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.grey")) {
                mAccentPicker.setSummary("Grey");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.indigo")) {
                mAccentPicker.setSummary("Indigo");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.lightblue")) {
                mAccentPicker.setSummary("Light Blue");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.lightgreen")) {
                mAccentPicker.setSummary("Light Green");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.lime")) {
                mAccentPicker.setSummary("Lime");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.orange")) {
                mAccentPicker.setSummary("Orange");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.pink")) {
                mAccentPicker.setSummary("Pink");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.red")) {
                mAccentPicker.setSummary("Red");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.teal")) {
                mAccentPicker.setSummary("Teal");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.yellow")) {
                mAccentPicker.setSummary("Yellow");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.androidonegreen")) {
                mAccentPicker.setSummary("AndroidOneGreen");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.cocacolared")) {
                mAccentPicker.setSummary("CocaColaRed");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.discordpurple")) {
                mAccentPicker.setSummary("DiscordPurple");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.facebookblue")) {
                mAccentPicker.setSummary("FacebookBlue");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.instagramcerise")) {
                mAccentPicker.setSummary("InstagramCerise");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.jollibeecrimson")) {
                mAccentPicker.setSummary("JollibeeCrimson");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.monsterenergygreen")) {
                mAccentPicker.setSummary("MonsterEnergyGreen");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.nextbitmint")) {
                mAccentPicker.setSummary("NextbitMint");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.oneplusred")) {
                mAccentPicker.setSummary("OneplusRed");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.pepsiblue")) {
                mAccentPicker.setSummary("PepsiBlue");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.pocophoneyellow")) {
                mAccentPicker.setSummary("PocophoneYellow");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.razergreen")) {
                mAccentPicker.setSummary("RazerGreen");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.samsungblue")) {
                mAccentPicker.setSummary("SamsungBlue");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.spotifygreen")) {
                mAccentPicker.setSummary("SpotifyGreen");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.starbucksgreen")) {
                mAccentPicker.setSummary("StarbucksGreen");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.twitchpurple")) {
                mAccentPicker.setSummary("TwitchPurple");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.twitterblue")) {
                mAccentPicker.setSummary("TwitterBlue");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.xboxgreen")) {
                mAccentPicker.setSummary("XboxGreen");
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.color.xiaomiorange")) {
                mAccentPicker.setSummary("XiaomiOrange");
            } else {
                mAccentPicker.setSummary(mContext.getString(R.string.theme_accent_picker_default));
            }
        }
    }

    private void updateIconShapeSummary() {
        if (mAdaptiveIconShape != null) {
            if (NitrogenUtils.isThemeEnabled("com.android.theme.icon.teardrop")) {
                mAdaptiveIconShape.setSummary(getString(R.string.adaptive_icon_shape_teardrop));
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.icon.squircle")) {
                mAdaptiveIconShape.setSummary(getString(R.string.adaptive_icon_shape_squircle));
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.icon.roundedrect")) {
                mAdaptiveIconShape.setSummary(getString(R.string.adaptive_icon_shape_roundedrect));
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.icon.cylinder")) {
                mAdaptiveIconShape.setSummary(getString(R.string.adaptive_icon_shape_cylinder));
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.icon.hexagon")) {
                mAdaptiveIconShape.setSummary(getString(R.string.adaptive_icon_shape_hexagon));
            } else {
                mAdaptiveIconShape.setSummary(mContext.getString(R.string.adaptive_icon_shape_default));
            }
        }
    }

    private void updateStatusbarIconsSummary() {
        if (mStatusbarIcons != null) {
            if (NitrogenUtils.isThemeEnabled("com.android.theme.icon_pack.filled.android")) {
                mStatusbarIcons.setSummary(getString(R.string.statusbar_icons_filled));
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.icon_pack.rounded.android")) {
                mStatusbarIcons.setSummary(getString(R.string.statusbar_icons_rounded));
            } else if (NitrogenUtils.isThemeEnabled("com.android.theme.icon_pack.circular.android")) {
                mStatusbarIcons.setSummary(getString(R.string.statusbar_icons_circular));
            } else {
                mStatusbarIcons.setSummary(mContext.getString(R.string.statusbar_icons_default));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().finish();
            return true;
        }
        return false;
    }
}
