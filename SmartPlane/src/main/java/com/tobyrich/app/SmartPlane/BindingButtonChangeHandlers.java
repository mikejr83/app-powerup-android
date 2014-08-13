package com.tobyrich.app.SmartPlane;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.util.Log;
import android.widget.CompoundButton;

import com.monstarmike.PowerUp.R;
import com.tobyrich.app.SmartPlane.util.Const;
import com.tobyrich.app.SmartPlane.util.Util;

import java.lang.ref.WeakReference;

import lib.smartlink.BluetoothDisabledException;

/**
 * Created by mgardner on 8/9/14.
 * <p/>
 * Handler for button changes on the setting page.
 */
public class BindingButtonChangeHandlers implements CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "BindingButtonChangeHandlers";
    private FullscreenActivity activity;
    private BluetoothDelegateCollection delegateCollection;

    /**
     * Default constructor
     *
     * @param activity           FullscreenActivity that is the main activity for the app.
     * @param delegateCollection A delegate collection which is managing all BluetoothDelegate objects for the app.
     */
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
        /*
        If the button changed event has a true value then the user is binding the left module. If
        the value is false then the user is unbinding the module.
         */
        if (b) {
            Log.d(TAG, "Bind left button changed. b: true");
            this.delegateCollection.setLeftDelegate(new BluetoothDelegate(activity, Const.MODULE_ONE_NAME));

            activity.findViewById(R.id.bindRightButton).setEnabled(false);
            this.delegateCollection.getLeftDelegate().setOnFoundListener(new WeakReference<BluetoothDelegate.OnFoundListener>(new BluetoothDelegate.OnFoundListener() {
                @Override
                public void onFound() {
                    Log.d(TAG, "Left module has been found. Going to enable the right module binding button.");

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Util.inform(activity, "Left module bound. Turn on right module then click bind.");
                            activity.findViewById(R.id.bindRightButton).setEnabled(true);
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
        }
    }

    void handleRightButtonChanged(boolean b) {
        final Activity activity = this.activity;

        /*
        If the button changed event has a true value then the user is binding the right module. If
        the value is false then the user is unbinding the module.
         */
        if (b) {
            Log.d(TAG, "Bind right button changed. b: true");
            this.delegateCollection.setRightDelegate(new BluetoothDelegate(activity, Const.MODULE_TWO_NAME));

            this.delegateCollection.getRightDelegate().setOnFoundListener(new WeakReference<BluetoothDelegate.OnFoundListener>(new BluetoothDelegate.OnFoundListener() {
                @Override
                public void onFound() {
                    Util.inform(activity, "Both modules are now bound.");
                }
            }));

            try {
                this.delegateCollection.getRightDelegate().connect();
            } catch (BluetoothDisabledException ex) {
                Intent enableBtIntent =
                        new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBtIntent, Util.BT_REQUEST_CODE2);
            }
        } else {
            Log.d(TAG, "Bind right button changed. b: false");
            this.delegateCollection.getRightDelegate().disconnect();
            this.delegateCollection.getRightDelegate().close();

            this.delegateCollection.removeDelegate(this.delegateCollection.getRightDelegate());
        }
    }
}