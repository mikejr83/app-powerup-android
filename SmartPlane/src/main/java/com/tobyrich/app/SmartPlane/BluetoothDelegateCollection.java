package com.tobyrich.app.SmartPlane;

import java.util.Iterator;
import java.util.Vector;

/**
 * Created by mgardner on 8/10/14.
 */
public class BluetoothDelegateCollection implements Iterable<BluetoothDelegate> {

    private BluetoothDelegate leftDelegate;
    private BluetoothDelegate rightDelegate;

    private Vector<BluetoothDelegate> bluetoothDelegates;

    public BluetoothDelegate getLeftDelegate() {
        return this.leftDelegate;
    }

    public BluetoothDelegate getRightDelegate() {
        return this.rightDelegate;
    }

    public void setLeftDelegate(BluetoothDelegate bluetoothDelegate) {
        if (this.leftDelegate != null) {
            this.removeDelegate(this.leftDelegate);
            this.leftDelegate = null;
        }
        this.leftDelegate = bluetoothDelegate;
        this.addDelegate(this.leftDelegate);
    }

    public void setRightDelegate(BluetoothDelegate bluetoothDelegate) {
        if (this.rightDelegate != null) {
            this.removeDelegate(this.rightDelegate);
            this.rightDelegate = null;
        }

        this.rightDelegate = bluetoothDelegate;
        this.addDelegate(this.rightDelegate);
    }

    public BluetoothDelegateCollection() {
        this.bluetoothDelegates = new Vector<BluetoothDelegate>(2);
    }

    public void addDelegate(BluetoothDelegate bluetoothDelegate) {
        this.bluetoothDelegates.add(bluetoothDelegate);
    }

    public void removeDelegate(BluetoothDelegate bluetoothDelegate) {
        this.bluetoothDelegates.remove(bluetoothDelegate);
    }

    @Override
    public Iterator<BluetoothDelegate> iterator() {
        return this.bluetoothDelegates.iterator();
    }
}
