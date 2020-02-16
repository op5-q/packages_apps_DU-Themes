/*
 * Copyright (C) 2018-2020 The Dirty Unicorns Project
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
import static com.dirtyunicorns.themes.utils.Utils.enableAccentColor;
import static com.dirtyunicorns.themes.utils.Utils.setDefaultAccentColor;
import static com.dirtyunicorns.themes.utils.Utils.setForegroundDrawable;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.om.IOverlayManager;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.preference.PreferenceManager;

import com.android.internal.util.pixeldust.ThemesUtils;
import com.android.internal.util.nitrogen.NitrogenUtils;

public class AccentPicker extends DialogFragment {

    public static final String TAG_ACCENT_PICKER = "accent_picker";

    private Context mContext;
    private IOverlayManager mOverlayManager;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mSharedPreferencesEditor;
    private View mView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
        mOverlayManager = IOverlayManager.Stub.asInterface(
                ServiceManager.getService(Context.OVERLAY_SERVICE));
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mSharedPreferencesEditor = mSharedPreferences.edit();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(
                getActivity(), R.style.AccentDialogTheme);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        mView = inflater.inflate(R.layout.accent_picker, null);

        if (mView != null) {
            initView();
        }

        builder.setNegativeButton(mContext.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        builder.setNeutralButton(mContext.getString(R.string.theme_accent_picker_default), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                setDefaultAccentColor(mOverlayManager);
                mSharedPreferencesEditor.putString("theme_accent_color", "default");
                mSharedPreferencesEditor.apply();
                dismiss();
            }
        });

        builder.setView(mView);

        return builder.create();
    }

    private void initView() {
        Button spaceAccent = mView.findViewById(R.id.spaceAccent);
        setAccent("com.android.theme.color.space", spaceAccent);
        setForegroundDrawable("com.android.theme.color.space", spaceAccent, getActivity());

        Button purpleAccent = mView.findViewById(R.id.purpleAccent);
        setAccent("com.android.theme.color.purple", purpleAccent);
        setForegroundDrawable("com.android.theme.color.purple", purpleAccent, getActivity());

        Button orchidAccent = mView.findViewById(R.id.orchidAccent);
        setAccent("com.android.theme.color.orchid", orchidAccent);
        setForegroundDrawable("com.android.theme.color.orchid", orchidAccent, getActivity());

        Button oceanAccent = mView.findViewById(R.id.oceanAccent);
        setAccent("com.android.theme.color.ocean", oceanAccent);
        setForegroundDrawable("com.android.theme.color.ocean", oceanAccent, getActivity());

        Button greenAccent = mView.findViewById(R.id.greenAccent);
        setAccent("com.android.theme.color.green", greenAccent);
        setForegroundDrawable("com.android.theme.color.green", greenAccent, getActivity());

        Button cinnamonAccent = mView.findViewById(R.id.cinnamonAccent);
        setAccent("com.android.theme.color.cinnamon", cinnamonAccent);
        setForegroundDrawable("com.android.theme.color.cinnamon", cinnamonAccent, getActivity());

        Button amberAccent = mView.findViewById(R.id.amberAccent);
        setAccent("com.android.theme.color.amber", amberAccent);
        setForegroundDrawable("com.android.theme.color.amber", amberAccent, getActivity());

        Button blueAccent = mView.findViewById(R.id.blueAccent);
        setAccent("com.android.theme.color.blue", blueAccent);
        setForegroundDrawable("com.android.theme.color.blue", blueAccent, getActivity());

        Button blueGreyAccent = mView.findViewById(R.id.blueGreyAccent);
        setAccent("com.android.theme.color.bluegrey", blueGreyAccent);
        setForegroundDrawable("com.android.theme.color.bluegrey", blueGreyAccent, getActivity());

        Button brownAccent = mView.findViewById(R.id.brownAccent);
        setAccent("com.android.theme.color.brown", brownAccent);
        setForegroundDrawable("com.android.theme.color.brown", brownAccent, getActivity());

        Button cyanAccent = mView.findViewById(R.id.cyanAccent);
        setAccent("com.android.theme.color.cyan", cyanAccent);
        setForegroundDrawable("com.android.theme.color.cyan", cyanAccent, getActivity());

        Button deepOrangeAccent = mView.findViewById(R.id.deepOrangeAccent);
        setAccent("com.android.theme.color.deeporange", deepOrangeAccent);
        setForegroundDrawable("com.android.theme.color.deeporange", deepOrangeAccent, getActivity());

        Button deepPurpleAccent = mView.findViewById(R.id.deepPurpleAccent);
        setAccent("com.android.theme.color.deeppurple", deepPurpleAccent);
        setForegroundDrawable("com.android.theme.color.deeppurple", deepPurpleAccent, getActivity());

        Button greyAccent = mView.findViewById(R.id.greyAccent);
        setAccent("com.android.theme.color.grey", greyAccent);
        setForegroundDrawable("com.android.theme.color.grey", greyAccent, getActivity());

        Button indigoAccent = mView.findViewById(R.id.indigoAccent);
        setAccent("com.android.theme.color.indigo", indigoAccent);
        setForegroundDrawable("com.android.theme.color.indigo", indigoAccent, getActivity());

        Button lightBlueAccent = mView.findViewById(R.id.lightBlueAccent);
        setAccent("com.android.theme.color.lightblue", lightBlueAccent);
        setForegroundDrawable("com.android.theme.color.lightblue", lightBlueAccent, getActivity());

        Button lightGreenAccent = mView.findViewById(R.id.lightGreenAccent);
        setAccent("com.android.theme.color.lightgreen", lightGreenAccent);
        setForegroundDrawable("com.android.theme.color.lightgreen", lightGreenAccent, getActivity());

        Button limeAccent = mView.findViewById(R.id.limeAccent);
        setAccent("com.android.theme.color.lime", limeAccent);
        setForegroundDrawable("com.android.theme.color.lime", limeAccent, getActivity());

        Button orangeAccent = mView.findViewById(R.id.orangeAccent);
        setAccent("com.android.theme.color.orange", orangeAccent);
        setForegroundDrawable("com.android.theme.color.orange", orangeAccent, getActivity());

        Button pinkAccent = mView.findViewById(R.id.pinkAccent);
        setAccent("com.android.theme.color.pink", pinkAccent);
        setForegroundDrawable("com.android.theme.color.pink", pinkAccent, getActivity());

        Button redAccent = mView.findViewById(R.id.redAccent);
        setAccent("com.android.theme.color.red", redAccent);
        setForegroundDrawable("com.android.theme.color.red", redAccent, getActivity());

        Button tealAccent = mView.findViewById(R.id.tealAccent);
        setAccent("com.android.theme.color.teal", tealAccent);
        setForegroundDrawable("com.android.theme.color.teal", tealAccent, getActivity());

        Button yellowAccent = mView.findViewById(R.id.yellowAccent);
        setAccent("com.android.theme.color.yellow", yellowAccent);
        setForegroundDrawable("com.android.theme.color.yellow", yellowAccent, getActivity());

        Button AndroidOneGreenAccent = mView.findViewById(R.id.AndroidOneGreenAccent);
        setAccent("com.android.theme.color.androidonegreen", AndroidOneGreenAccent);
        setForegroundDrawable("com.android.theme.color.androidonegreen", AndroidOneGreenAccent, getActivity());

        Button CocaColaRedAccent = mView.findViewById(R.id.CocaColaRedAccent);
        setAccent("com.android.theme.color.cocacolared", CocaColaRedAccent);
        setForegroundDrawable("com.android.theme.color.cocacolared", CocaColaRedAccent, getActivity());

        Button DiscordPurpleAccent = mView.findViewById(R.id.DiscordPurpleAccent);
        setAccent("com.android.theme.color.discordpurple", DiscordPurpleAccent);
        setForegroundDrawable("com.android.theme.color.discordpurple", DiscordPurpleAccent, getActivity());

        Button FacebookBlueAccent = mView.findViewById(R.id.FacebookBlueAccent);
        setAccent("com.android.theme.color.facebookblue", FacebookBlueAccent);
        setForegroundDrawable("com.android.theme.color.facebookblue", FacebookBlueAccent, getActivity());

        Button InstagramCeriseAccent = mView.findViewById(R.id.InstagramCeriseAccent);
        setAccent("com.android.theme.color.instagramcerise", InstagramCeriseAccent);
        setForegroundDrawable("com.android.theme.color.instagramcerise", InstagramCeriseAccent, getActivity());

        Button JollibeeCrimsonAccent = mView.findViewById(R.id.JollibeeCrimsonAccent);
        setAccent("com.android.theme.color.jollibeecrimson", JollibeeCrimsonAccent);
        setForegroundDrawable("com.android.theme.color.jollibeecrimson", JollibeeCrimsonAccent, getActivity());

        Button MonsterEnergyGreenAccent = mView.findViewById(R.id.MonsterEnergyGreenAccent);
        setAccent("com.android.theme.color.monsterenergygreen", MonsterEnergyGreenAccent);
        setForegroundDrawable("com.android.theme.color.monsterenergygreen", MonsterEnergyGreenAccent, getActivity());

        Button NextbitMintAccent = mView.findViewById(R.id.NextbitMintAccent);
        setAccent("com.android.theme.color.nextbitmint", NextbitMintAccent);
        setForegroundDrawable("com.android.theme.color.nextbitmint", NextbitMintAccent, getActivity());

        Button OneplusRedAccent = mView.findViewById(R.id.OneplusRedAccent);
        setAccent("com.android.theme.color.oneplusred", OneplusRedAccent);
        setForegroundDrawable("com.android.theme.color.oneplusred", OneplusRedAccent, getActivity());

        Button PepsiBlueAccent = mView.findViewById(R.id.PepsiBlueAccent);
        setAccent("com.android.theme.color.pepsiblue", PepsiBlueAccent);
        setForegroundDrawable("com.android.theme.color.pepsiblue", PepsiBlueAccent, getActivity());

        Button PocophoneYellowAccent = mView.findViewById(R.id.PocophoneYellowAccent);
        setAccent("com.android.theme.color.pocophoneyellow", PocophoneYellowAccent);
        setForegroundDrawable("com.android.theme.color.pocophoneyellow", PocophoneYellowAccent, getActivity());

        Button RazerGreenAccent = mView.findViewById(R.id.RazerGreenAccent);
        setAccent("com.android.theme.color.razergreen", RazerGreenAccent);
        setForegroundDrawable("com.android.theme.color.razergreen", RazerGreenAccent, getActivity());

        Button SamsungBlueAccent = mView.findViewById(R.id.SamsungBlueAccent);
        setAccent("com.android.theme.color.samsungblue", SamsungBlueAccent);
        setForegroundDrawable("com.android.theme.color.samsungblue", SamsungBlueAccent, getActivity());

        Button SpotifyGreenAccent = mView.findViewById(R.id.SpotifyGreenAccent);
        setAccent("com.android.theme.color.spotifygreen", SpotifyGreenAccent);
        setForegroundDrawable("com.android.theme.color.spotifygreen", SpotifyGreenAccent, getActivity());

        Button StarbucksGreenAccent = mView.findViewById(R.id.StarbucksGreenAccent);
        setAccent("com.android.theme.color.starbucksgreen", StarbucksGreenAccent);
        setForegroundDrawable("com.android.theme.color.starbucksgreen", StarbucksGreenAccent, getActivity());

        Button TwitchPurpleAccent = mView.findViewById(R.id.TwitchPurpleAccent);
        setAccent("com.android.theme.color.twitchpurple", TwitchPurpleAccent);
        setForegroundDrawable("com.android.theme.color.twitchpurple", TwitchPurpleAccent, getActivity());

        Button TwitterBlueAccent = mView.findViewById(R.id.TwitterBlueAccent);
        setAccent("com.android.theme.color.twitterblue", TwitterBlueAccent);
        setForegroundDrawable("com.android.theme.color.twitterblue", TwitterBlueAccent, getActivity());

        Button XboxGreenAccent = mView.findViewById(R.id.XboxGreenAccent);
        setAccent("com.android.theme.color.xboxgreen", XboxGreenAccent);
        setForegroundDrawable("com.android.theme.color.xboxgreen", XboxGreenAccent, getActivity());

        Button XiaomiOrangeAccent = mView.findViewById(R.id.XiaomiOrangeAccent);
        setAccent("com.android.theme.color.xiaomiorange", XiaomiOrangeAccent);
        setForegroundDrawable("com.android.theme.color.xiaomiorange", XiaomiOrangeAccent, getActivity());
    }

    private void setAccent(String accent, Button buttonAccent) {
        if (buttonAccent != null) {
            buttonAccent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    enableAccentColor(mOverlayManager, accent);
                    mSharedPreferencesEditor.putString("theme_accent_color", accent);
                    mSharedPreferencesEditor.apply();
                    dismiss();
                }
            });
        }
    }
}
