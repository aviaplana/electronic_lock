package at.nes.hlock;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class AddNewActivity extends ActionBarActivity {

    private final static int REQUEST_ENABLE_BT = 1;

    @InjectView(R.id.listView)
    ListView listView;

    private Menu actionMenu;

    private String mDeviceName;
    private String mDeviceAddress;

    private BluetoothAdapter mBluetoothAdapter;
    private BLService mBluetoothLeService;
    private boolean mScanning;
    private Handler mScanHandler;

    boolean mBound = false; // mServiceConnection

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {
            mBluetoothLeService = ((BLService.LocalBinder) service)
                    .getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e("dev", "Unable to initialize Bluetooth");
                finish();
            }
            mBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new);
        ButterKnife.inject(this);

        mScanHandler = new Handler();

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

        Intent gattServiceIntent = new Intent(AddNewActivity.this, BLService.class);
        getApplicationContext().bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
                mDeviceAddress = device.getAddress();
                mDeviceName = device.getName();

                mBluetoothLeService.connect(mDeviceAddress);

            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothGattCharacteristic characteristic = mBluetoothLeService.map.get(BLService.UUID_BLE_SHIELD_TX);
                characteristic.setValue(new byte[]{LockProtocol.types.REGISTRATION_REQ});
                mBluetoothLeService.writeCharacteristic(characteristic);
                return false;
            }
        });
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BLService.ACTION_GATT_DISCONNECTED.equals(action)) {

            }else if (BLService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getByteArrayExtra(BLService.EXTRA_DATA));
            }
        }
    };

    private void displayData(byte[] response) {
        if(response[0] == LockProtocol.types.KEY_EXCHANGE){

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled.
        enableBluetooth();

        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        listView.setAdapter(mLeDeviceListAdapter);
        scanLeDevice(true);

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BLService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        mBluetoothLeService.disconnect();
//        mBluetoothLeService.close();
        if(mBound) {
            unbindService(mServiceConnection);
        }
        mBluetoothLeService = null;
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

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            showProgress(true);
            // Stops scanning after a pre-defined scan period.
            mScanHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    showProgress(false);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            showProgress(false);
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private void showProgress(final boolean enable) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (actionMenu != null) {
                    final MenuItem refreshItem = actionMenu
                            .findItem(R.id.action_refresh);
                    if (refreshItem != null) {
                        if (enable) {
                            refreshItem.setActionView(R.layout.progress_action_item);
                        } else {
                            refreshItem.setActionView(null);
                        }
                    }
                }
            }
        }, 500);
    }

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = AddNewActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view != null) {
                viewHolder = (ViewHolder) view.getTag();
            } else {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder(view);
                view.setTag(viewHolder);
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }

    static class ViewHolder {
        @InjectView(R.id.device_address) TextView deviceAddress;
        @InjectView(R.id.device_name) TextView deviceName;

        ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    private LeDeviceListAdapter mLeDeviceListAdapter;

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLeDeviceListAdapter.addDevice(device);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.actionMenu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                scanLeDevice(true);
                return true;
            case R.id.action_new:
                return true;
            case R.id.action_settings:
//                openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
