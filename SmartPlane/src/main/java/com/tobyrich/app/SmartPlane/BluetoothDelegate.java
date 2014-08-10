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
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.TextView;

import com.tailortoys.app.PowerUp.R;
import com.tobyrich.app.SmartPlane.util.Const;
import com.tobyrich.app.SmartPlane.util.Util;

import java.lang.ref.WeakReference;
import java.util.Timer;

import lib.smartlink.BLEService;
import lib.smartlink.BluetoothDevice;
import lib.smartlink.BluetoothDisabledException;
import lib.smartlink.driver.BLEBatteryService;
import lib.smartlink.driver.BLEDeviceInformationService;
import lib.smartlink.driver.BLESmartplaneService;

import static com.tobyrich.app.SmartPlane.BluetoothTasks.*;
import static com.tobyrich.app.SmartPlane.UIChangers.*;

/**
 * Class responsible for callbacks from bluetooth devices
 */

public class BluetoothDelegate
        implements BluetoothDevice.Delegate, BLESmartplaneService.Delegate,
        BLEDeviceInformationService.Delegate, BLEBatteryService.Delegate {
    private final String TAG = "BluetoothDelegate";

    private BluetoothDevice device;
    private BLESmartplaneService smartplaneService;
    @SuppressWarnings("FieldCanBeLocal")
    private BLEDeviceInformationService deviceInfoService;
    @SuppressWarnings("FieldCanBeLocal")
    private BLEBatteryService batteryService;

    private WeakReference<OnFoundListener> onFoundListener;
    private WeakReference<OnDisconnectListener> onDisconnectListener;

    private PlaneState planeState;
    private Timer timer;

    private Activity activity;
    private String idName;

    private boolean isConnected = false;

    public String getIdName() {
        return this.idName;
    }

    public boolean isConnected() {
        return this.isConnected;
    }

    public BluetoothDelegate(Activity activity, String idName) {
        this.activity = activity;
        this.idName = idName;

        this.planeState = (PlaneState) activity.getApplicationContext();

        try {
            device = new BluetoothDevice(activity.getResources().openRawResource(R.raw.services),
                    activity);
            device.delegate = new WeakReference<BluetoothDevice.Delegate>(this);
            device.automaticallyReconnect = true;
        } catch (IllegalArgumentException e) {
            Log.wtf(TAG, this.idName + " - Could not create BluetoothDevice (maybe invalid plist?)");
            e.printStackTrace();
        }
    }

    public BLESmartplaneService getSmartplaneService() {
        return smartplaneService;
    }

    public void connect() throws BluetoothDisabledException {
        device.connect();
    }

    public void disconnect() {
        device.disconnect();
    }

    public void close() {
        device.automaticallyReconnect = false;
        device.disconnect();
        device.delegate.clear();
        device = null;
        this.planeState = null;
        this.activity = null;
    }

    @Override
    public void didStartChargingBattery() {
        activity.runOnUiThread(new ChargeStatusTextChanger(activity,
                Const.IS_CHARGING));
    }

    @Override
    public void didStopChargingBattery() {
        activity.runOnUiThread(new ChargeStatusTextChanger(activity,
                Const.IS_NOT_CHARGING));
    }

    @Override
    public void didUpdateSerialNumber(BLEDeviceInformationService device, String serialNumber) {
        final String hardwareDataInfo = "Hardware: " + serialNumber;

        Log.d(TAG, hardwareDataInfo);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) activity.findViewById(R.id.hardwareInfoData)).setText(hardwareDataInfo);
            }
        });
    }

    @Override
    public void didUpdateBatteryLevel(float percent) {
        Log.i(TAG, "did update battery level - " + this.idName);
        final float R_batt = 0.520f;  // Ohm
        /* 0.5 Amps is the current through the motor at MAX_MOTOR_SPEED */
        final float I_motor = (planeState.getAdjustedMotorSpeed() / Const.MAX_MOTOR_SPEED) * 0.5f;  // Amps
        /* We don't consider the contribution of the rudder & chip if the motor is off */
        final float I_rudder = I_motor == 0 ? 0 : 0.013f;  // estimate
        final float I_chip = I_motor == 0 ? 0 : 0.019f;  // estimate

        final float Vdrop = (I_motor + I_rudder + I_chip) * R_batt;
        /* The voltage takes values between 3.0V and 3.75V */
        final float Vmeasured = 3.0f + (0.75f * percent / 100.0f);
        final float Vbatt = Vmeasured + Vdrop;

        int adjustedPercent = Math.round(100.0f * ((Vbatt - 3.0f) / 0.75f));
        if (adjustedPercent > 100)
            adjustedPercent = 100;

        activity.runOnUiThread(new BatteryLevelUIChanger(activity, adjustedPercent));
    }

    @Override
    public void didStartService(BluetoothDevice device, String serviceName, BLEService service) {
        Log.i(TAG, this.idName + " - did start service: " + service.toString());
        Log.d(TAG, this.idName + " - service name: " + serviceName);
        // We are no longer "searching" for the device
        Util.showSearching(activity, this.idName, false);


        if (serviceName.equalsIgnoreCase("powerup") ||
                serviceName.equalsIgnoreCase("sml1test")) {

            Util.inform(activity, "Pull Up to Start the Motor");
            Util.inform(activity, "Module " + idName + " is ready.");

            smartplaneService = (BLESmartplaneService) service;
            smartplaneService.delegate = new WeakReference<BLESmartplaneService.Delegate>(this);

            activity.runOnUiThread(new ChargeStatusTextChanger(activity, Const.IS_NOT_CHARGING));

            // if disconnected, or not initialized
            if (timer == null) {
                timer = new Timer();
            }

            this.isConnected = true;

            ChargeTimerTask chargeTimerTask = new ChargeTimerTask(smartplaneService);
            timer.scheduleAtFixedRate(chargeTimerTask, Const.TIMER_DELAY, Const.TIMER_PERIOD);

            MediaPlayer engineSound = MediaPlayer.create(activity, R.raw.engine_sound);
            engineSound.start();

            SignalTimerTask sigTask = new SignalTimerTask(device);
            timer.scheduleAtFixedRate(sigTask, Const.TIMER_DELAY, Const.TIMER_PERIOD);
            this.handle();
            return;

        }

        if (serviceName.equalsIgnoreCase("devinfo")) { // check for devinfo service
            deviceInfoService = (BLEDeviceInformationService) service;
            deviceInfoService.delegate = new WeakReference<BLEDeviceInformationService.Delegate>(this);
            this.handle();
            return;
        }

        if (serviceName.equalsIgnoreCase("battery")) { // check for battery service
            batteryService = (BLEBatteryService) service;
            batteryService.delegate = new WeakReference<BLEBatteryService.Delegate>(this);
            this.handle();
        }

    }

    void handle() {
        if (smartplaneService == null || deviceInfoService == null || batteryService == null)
            return;

        if (this.onFoundListener != null) {
            Log.d(TAG, "Informing the onFoundListener that " + this.idName + " has found a device. The next module can start connecting...");
            OnFoundListener listener = this.onFoundListener.get();
            if (listener != null) {
                try {
                    listener.onFound();
                } catch (Exception e) {
                    Log.e(TAG, "Error trying to call onFound delegate.", e);
                }
            }
        }
    }

    @Override
    public void didUpdateSignalStrength(BluetoothDevice device, float signalStrength) {
        // scaling signalStrength to range from 0 to 1
        float scaledSignalStrength =
                ((signalStrength - Const.MIN_BLUETOOTH_STRENGTH) / (Const.MAX_BLUETOOTH_STRENGTH - Const.MIN_BLUETOOTH_STRENGTH));

        // for signal needle
        activity.runOnUiThread(new SignalLevelUIChanger(activity, scaledSignalStrength, signalStrength));
    }

    @Override
    public void didStartScanning(BluetoothDevice device) {
        Log.i(TAG, "started scanning - " + this.idName);
        Util.showSearching(activity, this.idName, true);
    }

    @Override
    public void didStartConnectingTo(BluetoothDevice device, float signalStrength) {
        Log.i(TAG, this.idName + " - did start connecting to " + device.toString());
        activity.runOnUiThread(new SearchingStatusChanger(activity));
    }

    @Override
    public void didDisconnect(BluetoothDevice device) {
        Log.i(TAG, this.idName + " - did disconnect from" + device.toString());
        if (timer != null) {
            timer.cancel();
        }
        // resetting all fields
        timer = null;
        smartplaneService = null;
        batteryService = null;
        deviceInfoService = null;
        this.isConnected = false;
        // if the smartplane is disconnected, show hardware as "unknown"
        final String hardwareDataInfo = "Hardware: unknown";
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) activity.findViewById(R.id.hardwareInfoData)).setText(hardwareDataInfo);
            }
        });
        Util.showSearching(activity, this.idName, true);
        /*
        If there is a listener registered for disconnect get it and call the onDisconnect method.
         */
        if (this.onDisconnectListener != null) {
            OnDisconnectListener listener = this.onDisconnectListener.get();
            if (listener != null) {
                Util.inform(activity, "Announcing disconnection...");
                try {
                    listener.onDisconnect();
                } catch (Exception e) {
                    Log.e(TAG, "Error when calling disconnect delegate.", e);
                }
            }

        }
    }

    /**
     * Register a listener for when the delegate connects to a BLESmartplaneService.
     *
     * @param listener Listener for onFound.
     */
    public void setOnFoundListener(WeakReference<OnFoundListener> listener) {
        this.onFoundListener = listener;
    }

    /**
     * Register a listener for when the delegate disconnects.
     *
     * @param listener
     */
    public void setOnDisconnectListener(WeakReference<OnDisconnectListener> listener) {
        this.onDisconnectListener = listener;
    }

    public interface OnFoundListener {
        void onFound();
    }

    public interface OnDisconnectListener {
        void onDisconnect();
    }
}
