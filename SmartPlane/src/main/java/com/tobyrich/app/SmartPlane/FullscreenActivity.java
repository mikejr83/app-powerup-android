/*

Copyright (c) 2014, TobyRich GmbH
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/
package com.tobyrich.app.SmartPlane;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.ToggleButton;

import com.monstarmike.PowerUp.R;
import com.tobyrich.app.SmartPlane.util.Const;
import com.tobyrich.app.SmartPlane.util.MeteoTask;
import com.tobyrich.app.SmartPlane.util.Util;
import com.viewpagerindicator.CirclePageIndicator;

import lib.smartlink.BluetoothDisabledException;

/**
 * @author Samit Vaidya
 * @date 04 March 2014
 * @edit Radu Hambasan
 * @date 19 Jun 2014
 * @edit Radu Hambasan
 * @date 17 Jul 2014
 */

public class FullscreenActivity extends Activity {
    private static final String TAG = "FullscreenActivity";
    @SuppressWarnings("FieldCanBeLocal")
    private static final int NUM_SCREENS = 4;

    private boolean[] initializedScreen = {false, false, false, false};

    // sound played when the user presses the "Control Tower" button
    private MediaPlayer atcSound;

    private BluetoothDelegateCollection delegateCollection = new BluetoothDelegateCollection();

    private SensorHandler sensorHandler;  // accelerometer & magnetometer
    private GestureDetector gestureDetector;  // touch events

    private PlaneState planeState;  // singleton with variables used app-wide

    private AudioManager audioManager;
    private SharedPreferences buttonConfig;  // cached button configuration

    @Override
    public void onResume() {
        super.onResume();

        // The resolution might have changed while the app was paused
        ViewTreeObserver viewTree = findViewById(R.id.controlPanel).getViewTreeObserver();
        viewTree.addOnGlobalLayoutListener(new GlobalLayoutListener(this));

        if (sensorHandler != null) {
            sensorHandler.registerListener();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorHandler != null) {
            sensorHandler.unregisterListener();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        ImageView slider = (ImageView) this.findViewById(R.id.throttleCursor);
        if (slider != null) {
            float panelHeight = this.findViewById(R.id.imgPanel).getHeight();
            float maxCursorRange = panelHeight * Const.SCALE_FOR_CURSOR_RANGE;
            slider.setY(maxCursorRange);
        }

        // Instantiate a ViewPager and a PagerAdapter
        ViewPager screenPager = (ViewPager) findViewById(R.id.screenPager);
        screenPager.setAdapter(new ScreenSlideAdapter());

        CirclePageIndicator screenIndicator = null;

        try {
            screenIndicator =
                    (CirclePageIndicator) findViewById(R.id.screenIndicator);

            screenIndicator.setViewPager(screenPager);
        } catch (Exception e) {

        }

        screenPager.setCurrentItem(1);  // horizon screen
        screenPager.setOffscreenPageLimit(2);

        buttonConfig = this.getSharedPreferences("button_config", MODE_PRIVATE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Util.BT_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    try {
                        this.delegateCollection.getLeftDelegate().connect();
                    } catch (BluetoothDisabledException ex) {
                        Log.wtf(TAG, "Left - user enabled BT, but we still couldn't connect");
                    }
                } else {
                    Log.e(TAG, "Left - Bluetooth enabling was canceled by user");
                }
                return;
            case Util.BT_REQUEST_CODE2:
                if (resultCode == RESULT_OK) {
                    if (this.delegateCollection.getRightDelegate() == null)
                        Log.wtf(TAG, "Right - How can connect when I'm null?! - onActivityResult");
                    try {
                        this.delegateCollection.getRightDelegate().connect();
                    } catch (BluetoothDisabledException ex) {
                        Log.wtf(TAG, "Right - user enabled BT, but we still couldn't connect");
                    }
                } else {
                    Log.e(TAG, "Right - Bluetooth enabling was canceled by user");
                }
                return;
            case Util.PHOTO_REQUEST_CODE:
                if (resultCode == RESULT_CANCELED)
                    return;

                Uri photoUri = Util.photoUri;
                if (photoUri == null) {
                    Util.inform(FullscreenActivity.this,
                            getString(R.string.social_share_picture_problem));
                    return;
                }
                Util.socialShare(this, photoUri);
                return;
            case Util.SHARE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Util.inform(this, getString(R.string.social_share_success));
                } else {
                    Log.e(TAG, "Sharing not successful");
                }
                // noinspection UnnecessaryReturnStatement
                return;
        }  // end switch
    }

    @Override
    public void onBackPressed() { //change functionality of back button
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        //This resets all cached data from the app and breaks the connection.
                        //The app itself is only minimized, but not closed.
                        int pid = android.os.Process.myPid();
                        android.os.Process.killProcess(pid);
                    }
                }).create().show();
    }

    private void initializeMainScreen() {
        this.delegateCollection.setLeftDelegate(new BluetoothDelegate(this, Const.MODULE_ONE_NAME));

        try {
            this.delegateCollection.getLeftDelegate().connect();
        } catch (BluetoothDisabledException ex) {
            Intent enableBtIntent =
                    new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, Util.BT_REQUEST_CODE);
        }
        sensorHandler = new SensorHandler(this, this.delegateCollection);
        sensorHandler.registerListener();
        GestureListener gestureListener = new GestureListener(this, this.delegateCollection);
        gestureDetector = new GestureDetector(this, gestureListener);

        planeState = (PlaneState) getApplicationContext();

         /* setting the trivial listeners */
        ImageView socialShare = (ImageView) findViewById(R.id.socialShare);
        socialShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.showSocialShareDialog(FullscreenActivity.this);
            }
        });

        ImageView horizonImage = (ImageView) findViewById(R.id.imageHorizon);
        horizonImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

        final ImageView atcOffButton = (ImageView) findViewById(R.id.atcOff);
        final ImageView atcOnButton = (ImageView) findViewById(R.id.atcOn);

        atcSound = MediaPlayer.create(this, R.raw.atc_sounds1);
        atcOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                atcOnButton.setVisibility(View.VISIBLE);
                v.setVisibility(View.GONE);
                if (atcSound != null) atcSound.start();
            }
        });

        atcOnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                atcOffButton.setVisibility(View.VISIBLE);
                v.setVisibility(View.GONE);
                if (atcSound != null) atcSound.pause();
            }
        });

        ImageView controlPanel = (ImageView) findViewById(R.id.imgPanel);
        controlPanel.setOnTouchListener(new PanelTouchListener(this,
                this.delegateCollection));

        final ImageView checklist_vw = (ImageView) findViewById(R.id.checklist);
        checklist_vw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog checklist = new Dialog(FullscreenActivity.this);
                checklist.setContentView(R.layout.checklist_layout);
                checklist.setCancelable(true);
                checklist.setCanceledOnTouchOutside(true);

                String checklist_title = getString(R.string.checklist_title);
                checklist.setTitle(checklist_title);

                // dismiss the dialog on touch
                checklist.findViewById(R.id.checklist_linearlayout)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                checklist.dismiss();
                            }
                        });

                checklist.show();
            }
        });


        final Activity activity = this;

        final ImageView settings_vw = (ImageView) findViewById(R.id.settings);
        settings_vw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsIntent = new Intent(activity, SettingsActivity.class);
                activity.startActivity(settingsIntent);
            }
        });
    }

    public void initializeSettingsScreen() {
        final float FX_VOLUME = 10.0f;
        /* setting the version data at the bottom of the screen */
        String appVersion = getString(R.string.unknown);
        try {
            appVersion = this.getPackageManager()
                    .getPackageInfo(this.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Could not locate package; needed to set appVersion.");
            e.printStackTrace();
        }

//        ((TextView) findViewById(R.id.softwareInfoData)).setText("Software: " + appVersion);

        /* setting the switch listeners */
        final Switch rudderReverse = (Switch) findViewById(R.id.rudderSwitch);
        rudderReverse.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, FX_VOLUME);
                planeState.setRudderReversed(isChecked);

                buttonConfig.edit().putBoolean("rudderReversed", isChecked).apply();
            }
        });

        boolean isRudderReversed = buttonConfig.getBoolean("rudderReversed",
                Const.DEFAULT_RUDDER_REVERSE);
        rudderReverse.setChecked(isRudderReversed);

        final Switch flAssistSwitch = (Switch) findViewById(R.id.flAssistSwitch);
        flAssistSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, FX_VOLUME);
                planeState.enableFlightAssist(isChecked);

                buttonConfig.edit().putBoolean("flAssist", isChecked).apply();
            }
        });

        boolean enableFlAssist = buttonConfig.getBoolean("flAssist",
                Const.DEFAULT_FLIGHT_ASSIST);
        flAssistSwitch.setChecked(enableFlAssist);

        final Switch towerSwitch = (Switch) findViewById(R.id.towerSwitch);
        final Switch multipleModSwitch = (Switch) findViewById(R.id.multipleModSwitch);
        final Switch motorsForRudderSwitch = (Switch) findViewById(R.id.multiple_mod_rudder_switch);

        towerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, FX_VOLUME);
                ImageView atcOn = (ImageView) findViewById(R.id.atcOn);
                ImageView atcOff = (ImageView) findViewById(R.id.atcOff);

                if ((atcOn == null) || (atcOff == null)) {
                    Log.e(TAG, "Main screen was destroyed.");
                    return;
                }

                if (atcSound != null && atcSound.isPlaying()) {
                    atcSound.pause();
                }

                if (isChecked) {
                    atcOff.setVisibility(View.VISIBLE);
                    atcOn.setVisibility(View.GONE);
                } else {
                    atcOn.setVisibility(View.GONE);
                    atcOff.setVisibility(View.GONE);
                }

                buttonConfig.edit().putBoolean("atcTower", isChecked).apply();
            }  // end onCheckedChanged()
        });

        final Activity activity = this;
        final ViewGroup multipleModButtonLayout = (ViewGroup) findViewById(R.id.multiple_mod_buttons_item);
        final ViewGroup multipleModeRudderLayout = (ViewGroup) findViewById(R.id.multiple_mod_rudder_item);
        multipleModSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                planeState.setMultipleModulesEnabled(isChecked);

                if (flAssistSwitch != null)
                    flAssistSwitch.setEnabled(!isChecked);

                if (isChecked) {
                    Util.inform(activity, "Turn on the left module then click the left bind button.");

                    multipleModButtonLayout.setVisibility(View.VISIBLE);
                    multipleModeRudderLayout.setVisibility(View.VISIBLE);
                    Log.i(TAG, "Closing the bluetoothLeftModule.");

                    if (delegateCollection.getLeftDelegate() != null) {
                        delegateCollection.getLeftDelegate().close();
                        delegateCollection.removeDelegate(delegateCollection.getLeftDelegate());
                    }
                } else {
                    multipleModButtonLayout.setVisibility(View.GONE);
                    multipleModeRudderLayout.setVisibility(View.GONE);
                }
            }
        });

        motorsForRudderSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.i(TAG, b ? "Enabling motor speed for rudder." : "Disabling motor speed for rudder.");
                planeState.enableMotorSpeedForRudder(b);
            }
        });

        final ToggleButton bindLeftBtn = (ToggleButton) findViewById(R.id.bindLeftButton);
        final ToggleButton bindRightBtn = (ToggleButton) findViewById(R.id.bindRightButton);

        BindingButtonChangeHandlers btnChngHndlrs = new BindingButtonChangeHandlers(this, this.delegateCollection);
        bindLeftBtn.setOnCheckedChangeListener(btnChngHndlrs);
        bindRightBtn.setOnCheckedChangeListener(btnChngHndlrs);

        boolean enableAtcTower = buttonConfig.getBoolean("atcTower",
                Const.DEFAULT_ATC_TOWER);
        towerSwitch.setChecked(enableAtcTower);

        boolean enableMultipleMod = buttonConfig.getBoolean("multipleMod",
                Const.DEFAULT_MULTIPLE_MOD);
        multipleModSwitch.setChecked(enableMultipleMod);

    }  // end initializeSettintsScreen()

    protected void initializeDiagnosticsScreen() {

    }

    private class ScreenSlideAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return NUM_SCREENS;
        }

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {
            LayoutInflater inflater = (LayoutInflater) collection.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            int layout_id = 1;
            switch (position) {
                case 0:
                    layout_id = R.layout.weather_center;
                    break;
                case 1:
                    layout_id = R.layout.horizon_screen;
                    break;
                case 2:
                    layout_id = R.layout.plane_settings;
                    break;
                case 3:
                    layout_id = R.layout.activity_diagnostics;
                    break;
            }
            @SuppressWarnings("ResourceType")
            View screen = inflater.inflate(layout_id, null);
            collection.addView(screen, 0);

            switch (position) {
                case 0:
                    if (!initializedScreen[0]) {
                        initializedScreen[0] = true;
                        new MeteoTask(FullscreenActivity.this).execute();
                    }
                    break;
                case 1:
                    if (!initializedScreen[1]) {
                        initializedScreen[1] = true;
                        initializeMainScreen();
                        Log.i(TAG, "initializing main screen");
                    }
                    break;
                case 2:
                    if (!initializedScreen[2]) {
                        initializedScreen[2] = true;
                        initializeSettingsScreen();
                        Log.i(TAG, "initializing settings screen");
                    }
                    break;
                case 3:
                    if (!initializedScreen[3]) {
                        initializedScreen[3] = true;
                        initializeDiagnosticsScreen();
                        Log.i(TAG, "initializing diagnostics screen");
                    }
                    break;
            }
            return screen;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object o) {
            View screen = (View) o;
            collection.removeView(screen);
            initializedScreen[position] = false;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    }
}
