package com.tobyrich.app.SmartPlane;

import java.util.Iterator;
import java.util.Vector;

/**
 * Created by mgardner on 8/10/14.
 * <p/>
 * A collection of BluetoothDelegation objects.
 */
public class BluetoothDelegateCollection implements Iterable<BluetoothDelegate> {

    private BluetoothDelegate leftDelegate;
    private BluetoothDelegate rightDelegate;

    private Vector<BluetoothDelegate> bluetoothDelegates;

    /**
     * Default constructor
     */
    public BluetoothDelegateCollection() {
        this.bluetoothDelegates = new Vector<BluetoothDelegate>(2);
    }

    /**
     * Gets the left BluetoothDelegate object
     *
     * @return Left BluetoothDelegate
     */
    public BluetoothDelegate getLeftDelegate() {
        return this.leftDelegate;
    }

    /**
     * Sets the left BluetoothDelegate
     *
     * @param bluetoothDelegate BluetoothDelegate for left module
     */
    public void setLeftDelegate(BluetoothDelegate bluetoothDelegate) {
        if (this.leftDelegate != null) {
            this.removeDelegate(this.leftDelegate);
            this.leftDelegate = null;
        }
        this.leftDelegate = bluetoothDelegate;
        this.addDelegate(this.leftDelegate);
    }

    /**
     * Gets the right BluetoothDelegate object
     *
     * @return Right BluetoothDelegate
     */
    public BluetoothDelegate getRightDelegate() {
        return this.rightDelegate;
    }

    /**
     * Sets the right BluetoothDelegate
     *
     * @param bluetoothDelegate BluetoothDelegate for right module
     */
    public void setRightDelegate(BluetoothDelegate bluetoothDelegate) {
        if (this.rightDelegate != null) {
            this.removeDelegate(this.rightDelegate);
            this.rightDelegate = null;
        }

        this.rightDelegate = bluetoothDelegate;
        this.addDelegate(this.rightDelegate);
    }

    /**
     * Adds a BluetoothDelegate to the collection of delegates. Not tied to the left or right side
     * of the aircraft. This would just be an arbitrary module. Support for greater than two
     * modules in the future.
     *
     * @param bluetoothDelegate BluetoothDelegate to add to the collection.
     */
    public void addDelegate(BluetoothDelegate bluetoothDelegate) {
        this.bluetoothDelegates.add(bluetoothDelegate);
    }

    /**
     * Removes a delegate from the collection.
     *
     * @param bluetoothDelegate BluetoothDelegate to remove from the collection.
     */
    public void removeDelegate(BluetoothDelegate bluetoothDelegate) {
        if (bluetoothDelegate == null) return;

        bluetoothDelegate.disconnect();

        if (this.leftDelegate == bluetoothDelegate)
            this.leftDelegate = null;
        if (this.rightDelegate == bluetoothDelegate)
            this.rightDelegate = null;

        this.bluetoothDelegates.remove(bluetoothDelegate);
    }

    @Override
    public Iterator<BluetoothDelegate> iterator() {
        return this.bluetoothDelegates.iterator();
    }
}
