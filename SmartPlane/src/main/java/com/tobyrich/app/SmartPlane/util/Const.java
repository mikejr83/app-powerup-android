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
package com.tobyrich.app.SmartPlane.util;

/**
 * @author Radu Hambasan
 * @date 07 Jul 2014
 * <p/>
 * <p/>
 * Wrapper class for constants that are app-relevant..
 */

/* TODO: document each constant better */

public class Const {
    public static final int MAX_ROLL_ANGLE = 45;
    /* input to the rudder is between -128 and 127,
     * 0 would center it, 127 turns it fully right,
     * -128 fully left and intermediary value turn it proportionally
     */
    public static final int MAX_RUDDER_INPUT = 127;
    public static final int MAX_MOTOR_SPEED = 254;
    public static final double PITCH_ANGLE_MAX = 50;
    public static final double PITCH_ANGLE_MIN = -50;
    public static final long ANIMATION_DURATION_MILLISEC = 1000;
    /* don't let the cursor go below 80% of panel height */
    public static final float SCALE_FOR_CURSOR_RANGE = 0.8f;
    public static final float SCALE_FOR_VERT_MOVEMENT_HORIZON = 4.0f;
    public static final float THROTTLE_NEEDLE_MAX_ANGLE = 40;  // in degrees
    public static final float THROTTLE_NEEDLE_MIN_ANGLE = -135;  // in degrees
    public static final float SIGNAL_NEEDLE_MIN_ANGLE = 0;  // in degrees
    public static final float SIGNAL_NEEDLE_MAX_ANGLE = 180;  // in degrees
    public static final float MAX_BLUETOOTH_STRENGTH = -20;
    public static final float MIN_BLUETOOTH_STRENGTH = -100;
    public static final float FUEL_NEEDLE_MIN_ANGLE = -90;  // in degrees
    public static final float FUEL_NEEDLE_MAX_ANGLE = 90;  // in degrees
    public static final float MAX_BATTERY_VALUE = 100;  // in degrees
    /* the delay in milliseconds before task is to be executed */
    public static final long TIMER_DELAY = 500;
    /* the time in milliseconds between successive task executions */
    public static final long TIMER_PERIOD = 6000;
    public static final double RULER_MOVEMENT_SPEED = 1.4;
    public static final int RULER_MOVEMENT_HEIGHT = 200;
    /* messages displayed when the charging status changes */
    public static final String IS_CHARGING = "CHARGING";
    public static final String IS_NOT_CHARGING = "IN USE";
    /* 360 degrees to be added to a negative angle, etc. TODO: find better name */
    public static final int FULL_DEGREES = 360;
    public static final double TO_DEGREES = 180 / Math.PI;  // change radians to degrees
    public static final int SENSOR_DELAY = 20 * 1000;  // micro sec
    /* In flight assist mode, don't allow for more throttle */
    public static final double SCALE_FASSIST_THROTTLE = 0.77;
    /* The rudder tends to go more to the left, so we need to account for that */
    public static final double SCALE_LEFT_RUDDER = 0.75;
    public static final boolean DEFAULT_FLIGHT_ASSIST = true;
    public static final boolean DEFAULT_RUDDER_REVERSE = false;
    public static final boolean DEFAULT_ATC_TOWER = true;
    public static final boolean DEFAULT_MULTIPLE_MOD = false;
    public static final String MODULE_ONE_NAME = "One";
    public static final String MODULE_TWO_NAME = "Two";
    /* Conversion for pitch to a percentage to be applied to motor speed */
    public static final float ROLL_PERCENTAGE_CONVERSION = 80f;
}

