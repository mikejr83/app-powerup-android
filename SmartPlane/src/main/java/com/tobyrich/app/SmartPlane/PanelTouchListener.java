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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.monstarmike.PowerUp.R;
import com.tobyrich.app.SmartPlane.util.Const;
import com.tobyrich.app.SmartPlane.util.Util;

import lib.smartlink.driver.BLESmartplaneService;

/**
 * @author Samit Vaidya
 * @date 04 March 2014
 * Refactored by: Radu Hambasan
 */

public class PanelTouchListener implements View.OnTouchListener {
    public final static String TAG = "PanelTouchListener";
    ImageView slider;
    ImageView throttleNeedle;
    TextView throttleText;
    /* constant only for a specific device */
    float maxCursorRange = -1;  // uninitialized
    private Activity activity;
    private PlaneState planeState;
    private BluetoothDelegateCollection delegateCollection;

    public PanelTouchListener(Activity activity, BluetoothDelegateCollection delegateCollection) {
        this.activity = activity;
        this.planeState = (PlaneState) activity.getApplicationContext();
        this.delegateCollection = delegateCollection;

        slider = (ImageView) activity.findViewById(R.id.throttleCursor);
        throttleNeedle = (ImageView) activity.findViewById(R.id.imgThrottleNeedle);
        throttleText = (TextView) activity.findViewById(R.id.throttleValue);

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if ((event.getAction() != MotionEvent.ACTION_MOVE) &&
                (event.getAction() != MotionEvent.ACTION_DOWN)) {
            // digest the event
            return true;
        }

        /* If uninitialized, initialize maxCursorRange */
        if (maxCursorRange == -1) {
            float panelHeight = activity.findViewById(R.id.imgPanel).getHeight();
            maxCursorRange = panelHeight * Const.SCALE_FOR_CURSOR_RANGE;
        }
        final float eventYValue = event.getY();

        /* Note: The coordinate system of the screen has the following properties:
         * (0,0) is in the upper-left corner.
         * and there are no negative coordinates, i.e. X and Y always increase.
         */

        float motorSpeed;
        /* check if the touch event went outside the bottom of the panel */
        if (eventYValue >= maxCursorRange) {
            slider.setY(maxCursorRange);
            motorSpeed = 0;
        /* check if the slider tries to go above the control panel height */
        } else if (eventYValue <= 0) {
            /* 0 corresponds to the max position, because the slider cannot go outside the
             * containing relative layout (the control panel)
             */
            slider.setY(0);
            motorSpeed = 1; // 100% throttle
        } else {
            slider.setY(eventYValue);
            motorSpeed = 1 - (eventYValue / maxCursorRange);
        }

        // Adjust if flight assist is on is NOT needed in PowerUp
        // Shai requested 100% throttle even in FlightAssist mode.

        planeState.setMotorSpeed(motorSpeed);
        float adjustedMotorSpeed = planeState.getAdjustedMotorSpeed();

        Util.rotateImageView(throttleNeedle, adjustedMotorSpeed,
                Const.THROTTLE_NEEDLE_MIN_ANGLE, Const.THROTTLE_NEEDLE_MAX_ANGLE);
        throttleText.setText((short) (adjustedMotorSpeed * 100) + "%");

        short newMotorSpeed = (short) (adjustedMotorSpeed * Const.MAX_MOTOR_SPEED);
        for (BluetoothDelegate bluetoothDelegate : this.delegateCollection) {
            Log.d(TAG, "Setting motor speed for " + bluetoothDelegate.getIdName() + ": " + newMotorSpeed);

            @SuppressWarnings("SpellCheckingInspection")
            BLESmartplaneService smartplaneService = bluetoothDelegate.getSmartplaneService();
            // The smartPlaneService might not be available
            if (smartplaneService == null) continue;

            smartplaneService.setMotor(newMotorSpeed);
            // the event was digested, keep listening for touch events
        }

        return true;
    }

}
