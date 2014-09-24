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

import java.lang.ref.WeakReference;
import java.util.TimerTask;

import lib.smartlink.BluetoothDevice;
import lib.smartlink.driver.BLESmartplaneService;

/**
 * Tasks that require bluetooth interaction
 */
public class BluetoothTasks {

    /**
     * Task for updating single strength
     */
    public static class SignalTimerTask extends TimerTask {
        /*
        We have to avoid circular references, i.e.: the bluetooth device will hold a reference
        to this activity and the activity will have a reference to the bluetooth device,
        so without WeakReferences, none would get garbage collected.
         */
        WeakReference<BluetoothDevice> weakDevice;

        /**
         * Create a SignalTimerTask with a BluetoothDevice
         *
         * @param device device to use for updating signal strength
         */
        public SignalTimerTask(BluetoothDevice device) {
            weakDevice = new WeakReference<BluetoothDevice>(device);
        }

        @Override
        public void run() {
            if (!weakDevice.isEnqueued()) return;

            BluetoothDevice dev = weakDevice.get();
            if (dev != null) {
                dev.updateSignalStrength();
            }
        }
    }

    /**
     * Task for updating charging status
     */
    public static class ChargeTimerTask extends TimerTask {
        /*
        Using WeakReference to hold a reference to BLESmartplaneService for the same reasons as was
        used for the SignalTimerTask.
         */
        WeakReference<BLESmartplaneService> service;

        /**
         * Create a ChargeTimerTask with a BLESmartplaneService
         *
         * @param service The smart plane service that will be used to update the charging status.
         */
        public ChargeTimerTask(BLESmartplaneService service) {
            this.service = new WeakReference<BLESmartplaneService>(service);
        }

        @Override
        public void run() {
            if (!this.service.isEnqueued()) return;

            BLESmartplaneService planeService = this.service.get();
            if (planeService != null) {
                planeService.updateChargingStatus();
            }
        }
    }
}
