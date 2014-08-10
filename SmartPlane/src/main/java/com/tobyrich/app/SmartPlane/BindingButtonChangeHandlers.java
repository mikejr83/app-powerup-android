package com.tobyrich.app.SmartPlane;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.util.Log;
import android.widget.CompoundButton;

import com.tailortoys.app.PowerUp.R;
import com.tobyrich.app.SmartPlane.util.Util;

import java.lang.ref.WeakReference;

import lib.smartlink.BluetoothDisabledException;

/**
 * Created by mgardner on 8/9/14.
 */
public class BindingButtonChangeHandlers implements CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "BindingButtonChangeHandlers";
    private FullscreenActivity activity;
    private BluetoothDelegateCollection delegateCollection;

    public BindingButtonChangeHandlers(FullscreenActivity activity, BluetoothDelegateCollection delegateCollection) {
        this.activity = activity;
        this.delegateCollection = delegateCollection;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.bindLeftButton:
                this.handleLeftButtonChanged(b);
                break;

            case R.id.bindRightButton:
                this.handleRightButtonChanged(b);
                break;
        }
    }

    void handleLeftButtonChanged(boolean b) {
        final Activity activity = this.activity;
        final CompoundButton bindRightBtn = (CompoundButton) activity.findViewById(R.id.bindRightButton);
        if (b) {
            Log.d(TAG, "Bind left button changed. b: true");
            this.delegateCollection.setLeftDelegate(new BluetoothDelegate(activity, "LEFT"));

            bindRightBtn.setEnabled(false);
            this.delegateCollection.getLeftDelegate().setOnFoundListener(new WeakReference<BluetoothDelegate.OnFoundListener>(new BluetoothDelegate.OnFoundListener() {
                @Override
                public void onFound() {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            bindRightBtn.setEnabled(true);
                        }
                    });
                }
            }));

            try {
                this.delegateCollection.getLeftDelegate().connect();
            } catch (BluetoothDisabledException ex) {
                Intent enableBtIntent =
                        new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBtIntent, Util.BT_REQUEST_CODE);
            }
        } else {
            Log.d(TAG, "Bind left button changed. b: false");

            /*if (bluetoothRightModule != null) {

            }*/
        }
    }

    void handleRightButtonChanged(boolean b) {
        if (b) {
            Log.d(TAG, "Bind right button changed. b: true");
            this.delegateCollection.setRightDelegate(new BluetoothDelegate(activity, "RIGHT"));

/*                    bindRightBtn.setEnabled(false);
                    bluetoothLeftModule.setOnFoundListener(new WeakReference<BluetoothDelegate.OnFoundListener>(new BluetoothDelegate.OnFoundListener() {
                        @Override
                        public void onFound() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    findViewById(R.id.bindRightButton).setEnabled(true);
                                }
                            });
                        }
                    }));*/

            try {
                this.delegateCollection.getRightDelegate().connect();
            } catch (BluetoothDisabledException ex) {
                Intent enableBtIntent =
                        new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBtIntent, Util.BT_REQUEST_CODE);
            }
        } else {
            Log.d(TAG, "Bind right button changed. b: false");
            this.delegateCollection.getRightDelegate().disconnect();
            this.delegateCollection.getRightDelegate().close();

            this.delegateCollection.removeDelegate(this.delegateCollection.getRightDelegate());
        }
    }
}
