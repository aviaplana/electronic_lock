package at.nes.hlock;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.support.ConnectionSource;

import db.DatabaseHelper;
import db.Lock;

/**
 * Created by Andraz Pajtler on 30/05/14.
 */
public class BaseActivity extends ActionBarActivity{

    private final static int REQUEST_ENABLE_BT = 1;

    // ORMLite
    private volatile DatabaseHelper helper;
    private volatile boolean created = false;
    private volatile boolean destroyed = false;
    private static Logger logger = LoggerFactory.getLogger(OrmLiteBaseActivity.class);

    protected RuntimeExceptionDao<Lock, String> mSimpleDao;

    // Bluetooth
    protected BluetoothAdapter mBluetoothAdapter;

    // SharedPreferences
    protected SharedPreferences mSharedPref;
    protected SharedPreferences.Editor mSharedPrefEditor;


    /**
     * Get a helper for this action.
     */
    public DatabaseHelper getHelper() {
        if (helper == null) {
            if (!created) {
                throw new IllegalStateException("A call has not been made to onCreate() yet so the helper is null");
            } else if (destroyed) {
                throw new IllegalStateException(
                        "A call to onDestroy has already been made and the helper cannot be used after that point");
            } else {
                throw new IllegalStateException("Helper is null for some unknown reason");
            }
        } else {
            return helper;
        }
    }

    /**
     * Get a connection source for this action.
     */
    public ConnectionSource getConnectionSource() {
        return getHelper().getConnectionSource();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (helper == null) {
            helper = getHelperInternal(this);
            created = true;
        }
        super.onCreate(savedInstanceState);

        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        if(isTablet)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }else
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        mSimpleDao = getHelper().getSimpleDataDao();

        // SharedPreferences init
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mSharedPrefEditor = mSharedPref.edit();

        // Bluetooth init
        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled.
        enableBluetooth();
    }

    private void enableBluetooth(){
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseHelper(helper);
        destroyed = true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        switch (requestCode){
            case REQUEST_ENABLE_BT:
                // User chose not to enable Bluetooth.
                if(resultCode == Activity.RESULT_CANCELED){
                    finish();
                    return;
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * This is called internally by the class to populate the helper object instance. This should not be called directly
     * by client code unless you know what you are doing. Use {@link #getHelper()} to get a helper instance. If you are
     * managing your own helper creation, override this method to supply this activity with a helper instance.
     *
     * <p>
     * <b> NOTE: </b> If you override this method, you most likely will need to override the
     * </p>
     */
    protected DatabaseHelper getHelperInternal(Context context) {
        @SuppressWarnings({ "unchecked", "deprecation" })
        DatabaseHelper newHelper = (DatabaseHelper) OpenHelperManager.getHelper(context, DatabaseHelper.class);
        logger.trace("{}: got new helper {} from OpenHelperManager", this, newHelper);
        return newHelper;
    }

    /**
     * Release the helper instance created in {@link #getHelperInternal(Context)}. You most likely will not need to call
     * this directly since {@link #onDestroy()} does it for you.
     *
     * <p>
     * <b> NOTE: </b> If you override this method, you most likely will need to override the
     * {@link #getHelperInternal(Context)} method as well.
     * </p>
     */
    protected void releaseHelper(DatabaseHelper helper) {
        OpenHelperManager.releaseHelper();
        logger.trace("{}: helper {} was released, set to null", this, helper);
        this.helper = null;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "@" + Integer.toHexString(super.hashCode());
    }
}
