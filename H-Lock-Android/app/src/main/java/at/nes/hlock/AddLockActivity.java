package at.nes.hlock;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.ButterKnife;
import butterknife.InjectView;
import db.Lock;


public class AddLockActivity extends BaseActivity  {

    @InjectView(R.id.listView)
    ListView listView;

    private Menu actionMenu;

    private String mDeviceName;
    private String mDeviceAddress;

    private BLService mBluetoothLeService;
    private boolean mScanning;
    private Handler mScanHandler;
    private boolean mWelcomeOverlayVisible = false;

    boolean mBound = false; // For mServiceConnection

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {
            mBluetoothLeService = ((BLService.LocalBinder) service)
                    .getService();
            if (!mBluetoothLeService.initialize()) {
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
    private ProgressDialog barProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_new);
        ButterKnife.inject(this);

        // Check if it's the first run, show welcome view
        if(mSharedPref.getBoolean(MyApplication.SHARED_PREF_FIRST_RUN, true)){
            mWelcomeOverlayVisible = true;
            final View welcomeOverlay = ((ViewStub) findViewById(R.id.stub_welcome)).inflate();
            Button welcomeButton = ButterKnife.findById(welcomeOverlay, R.id.welcomeButton);
            welcomeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    welcomeOverlay.setVisibility(View.INVISIBLE);
                    mWelcomeOverlayVisible = false;
                    mSharedPrefEditor.putBoolean(MyApplication.SHARED_PREF_FIRST_RUN, false);
                    mSharedPrefEditor.commit();
                    scanLeDevice(true);
                }
            });
        }

        mScanHandler = new Handler();

        Intent gattServiceIntent = new Intent(AddLockActivity.this, BLService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                scanLeDevice(false);

                barProgressDialog = new ProgressDialog(AddLockActivity.this);
                barProgressDialog.setTitle("Connecting...");
                barProgressDialog.setProgressStyle(barProgressDialog.STYLE_HORIZONTAL);
                barProgressDialog.setIndeterminate(true);
                barProgressDialog.setCancelable(false);
                barProgressDialog.show();

                BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
                mDeviceAddress = device.getAddress();
                mDeviceName = device.getName();

                mBluetoothLeService.connect(mDeviceAddress);
            }
        });
    }

    private void sendRegistrationRequest(){
        if(barProgressDialog != null) {
            barProgressDialog.dismiss();
        }
        barProgressDialog = new ProgressDialog(AddLockActivity.this);
        barProgressDialog.setTitle("Complete registration");
        barProgressDialog.setMessage("Press button on the lock to complete registration.");
        barProgressDialog.setCancelable(false);
        barProgressDialog.setProgressStyle(barProgressDialog.STYLE_HORIZONTAL);
        barProgressDialog.setIndeterminate(false);
        barProgressDialog.setMax(5);
        barProgressDialog.setProgress(5);
        barProgressDialog.show();

//        final Handler h = new Handler();
//        h.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if(barProgressDialog != null) {
//                    if(barProgressDialog.getProgress() == 0){
//                        barProgressDialog.dismiss();
//                    }else {
//                        barProgressDialog.incrementProgressBy(-1);
//                        h.postDelayed(this, 1000);
//                    }
//                }
//            }
//        }, 1000);

//        final Timer mTimer = new Timer();
//        mTimer.schedule(new TimerTask() {
//
//            @Override
//            public void run() {
//                if(barProgressDialog != null) {
//                    if(barProgressDialog.getProgress() == 0){
//                        mTimer.cancel();
//                        barProgressDialog.dismiss();
//                    }else {
//                        barProgressDialog.incrementProgressBy(-1);
//                    }
//                }
//            }
//        }, 1000, 1000);

        BluetoothGattCharacteristic characteristicTx= mBluetoothLeService.map.get(BLService.UUID_BLE_SHIELD_TX);

        characteristicTx.setValue(new byte[]{LockProtocol.types.REGISTRATION_REQ});
        mBluetoothLeService.writeCharacteristic(characteristicTx);
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BLService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.d("dev", "blabla");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendRegistrationRequest();
                    }
                }, 500);
            }else
            if (BLService.ACTION_GATT_DISCONNECTED.equals(action)) {

            }else if (BLService.ACTION_DATA_AVAILABLE.equals(action)) {
                String characteristicName = intent.getStringExtra(BLService.CHARACTERISTIC_NAME);

                if(GattAttributes.BLE_SHIELD_RX.equals(characteristicName)){
                    handleRegistrationResponse(intent.getByteArrayExtra(BLService.EXTRA_DATA));
                }
            }
        }
    };

    private void handleRegistrationResponse(byte[] byteArray) {
        barProgressDialog.dismiss();
        barProgressDialog = null;

        if(byteArray[0] == LockProtocol.types.KEY_EXCHANGE){
            Lock newLock = new Lock(mDeviceAddress, mDeviceName,
                                    byteArray[LockProtocol.lengths.TYPE],
                                    Arrays.copyOfRange(byteArray, LockProtocol.lengths.TYPE+LockProtocol.lengths.ID, LockProtocol.lengths.MESSAGE));
            // Persist the object to the database
            mSimpleDao.create(newLock);
            Toast.makeText(this, "Registration successful", Toast.LENGTH_LONG).show();

            finish();
        }else{
            mBluetoothLeService.disconnect();
            Toast.makeText(this, "Registration failed", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        listView.setAdapter(mLeDeviceListAdapter);

        if(!mWelcomeOverlayVisible){
            scanLeDevice(true);
        }

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
        Log.d("dev", "pause");
        unregisterReceiver(mGattUpdateReceiver);
        super.onPause();
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
    }

    @Override
    protected void onStop() {
        Log.d("dev", "stop");
//        unregisterReceiver(mGattUpdateReceiver);
        super.onStop();
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
            mInflator = AddLockActivity.this.getLayoutInflater();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        this.actionMenu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_new, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                scanLeDevice(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
