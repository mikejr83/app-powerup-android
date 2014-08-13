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

import android.app.Application;

import com.tobyrich.app.SmartPlane.util.Const;

/**
 * @author Radu Hambasan
 * @date Jun 13 2014
 * <p/>
 * Singleton handling data shared at global level
 */

/* TODO: define a better interface */
public class PlaneState extends Application {

    private boolean rudderReversed = false;
    private boolean screenLocked = false;
    private boolean useMotorSpeedForRudder = false;
    private boolean multipleModulesEnabled = false;
    private float motorSpeed;
    private double scaler = 0;
    private boolean flAssistEnabled = false;

    public double getScaler() {
        return scaler;
    }

    public void setScaler(double scaler) {
        this.scaler = scaler;
    }

    /**
     * @return If flight assist is enabled, it returns the scaled motorSpeed,
     * otherwise, just the motorSpeed.
     */
    public float getAdjustedMotorSpeed() {
        if (flAssistEnabled) {
            float adjustedMotorSpeed = (float) (motorSpeed * (1 + scaler));
            if (adjustedMotorSpeed > 1)
                adjustedMotorSpeed = 1;
            return adjustedMotorSpeed;
        } else {
            return motorSpeed;
        }
    }

    /**
     * Is the multiple modules feature enabled.
     *
     * @return true / false for multiple modules
     */
    public boolean isMultipleModulesEnabled() {
        return multipleModulesEnabled;
    }

    /**
     * Set the multiple modules feature
     *
     * @param multipleModulesEnabled true / false for multiple modules
     */
    public void setMultipleModulesEnabled(boolean multipleModulesEnabled) {
        this.multipleModulesEnabled = multipleModulesEnabled;
    }

    /**
     * Gets whether the rudder should be reversed
     *
     * @return true / false for reversing the rudder
     */
    public boolean isRudderReversed() {
        return rudderReversed;
    }

    /**
     * Sets whether the rudder should be reversed.
     *
     * @param rudderReversed true / false for reversing the rudder
     */
    public void setRudderReversed(boolean rudderReversed) {
        this.rudderReversed = rudderReversed;
    }

    /**
     * Gets whether or not the screen is locked
     *
     * @return true / false for the screen being locked
     */
    public boolean isScreenLocked() {
        return screenLocked;
    }

    /**
     * Sets whether or not the screen is locked
     *
     * @param screenLocked true / false for the screen being locked
     */
    public void setScreenLocked(boolean screenLocked) {
        this.screenLocked = screenLocked;
    }

    /**
     * Gets whether to use the multi-modules to control yaw as a rudder
     *
     * @return true / false for using the motors for yaw control
     */
    public boolean isMotorSpeedUsedForRudder() {
        return useMotorSpeedForRudder;
    }

    /**
     * Sets whether to use the multi-modules to control yaw as a rudder
     *
     * @param useMotorSpeedForRudder true / false for using the motors for yaw control
     */
    public void enableMotorSpeedForRudder(boolean useMotorSpeedForRudder) {
        this.useMotorSpeedForRudder = useMotorSpeedForRudder;
    }

    /**
     * Gets the the current motor speed
     *
     * @return Motor speed
     */
    @SuppressWarnings("UnusedDeclaration")
    public float getMotorSpeed() {
        return this.motorSpeed;
    }

    /**
     * Sets the state of the motor's speed.
     *
     * @param motorSpeed Speed of motor 0 - 255.
     */
    public void setMotorSpeed(float motorSpeed) {
        this.motorSpeed = motorSpeed;
    }

    /**
     * Enable or disable flight assistance
     *
     * @param flAssistEnabled true / false for setting flight assistance
     */
    public void enableFlightAssist(boolean flAssistEnabled) {
        // Cutoff speed for flight assist
        if (!this.flAssistEnabled && motorSpeed > Const.SCALE_FASSIST_THROTTLE) {
            motorSpeed = (float) Const.SCALE_FASSIST_THROTTLE;
        }
        this.flAssistEnabled = flAssistEnabled;
    }

    /**
     * Check for flight assist enabled.
     *
     * @return true / false for flight assist
     */
    public boolean isFlAssistEnabled() {
        return this.flAssistEnabled;
    }
}
