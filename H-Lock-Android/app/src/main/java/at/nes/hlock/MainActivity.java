package at.nes.hlock;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


import net.sebastianopoggi.ui.GlowPadBackport.GlowPadView;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import butterknife.ButterKnife;
import butterknife.InjectView;
import db.Lock;


public class MainActivity extends BaseActivity  {

    private final static int REQUEST_ADD_LOCK = 2;

    private Lock selectedLock;
    private boolean mLocked = true;

    @InjectView(R.id.lockNameTV)
    TextView lockNameTV;
    @InjectView(R.id.lockStatusTV)
    TextView lockStatusTV;

    @InjectView(R.id.glowPadWidget)
    GlowPadView glowPad;

    private BLService mBluetoothLeService;

    private boolean mBound = false; // For mServiceConnection

    private boolean forcedDisconnect = false;

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

            connectToLock();
//            scanLeDevice(true);
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
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        if(mSharedPref.getBoolean(MyApplication.SHARED_PREF_FIRST_RUN, true) ||  mSimpleDao.countOf() == 0){
            Intent enableBtIntent = new Intent(this, AddLockActivity.class);
            startActivityForResult(enableBtIntent, REQUEST_ADD_LOCK);
        }

        // Give an option to edit lock name
        lockNameTV.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final EditText editText = new EditText(MainActivity.this);
                editText.setText(selectedLock.name);

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Edit lock name:")
                        .setView(editText)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if(editText.getText().length() > 0){
                                    selectedLock.name = editText.getText().toString();
                                    mSimpleDao.update(selectedLock);

                                    lockNameTV.setText(selectedLock.name);
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                return true;
            }
        });

        glowPad.setOnTriggerListener(new GlowPadView.OnTriggerListener() {
            @Override
            public void onGrabbed(View v, int handle) {
                // Do nothing
            }

            @Override
            public void onReleased(View v, int handle) {
                // Do nothing
            }

            @Override
            public void onTrigger(View v, int target) {
                mLocked = !mLocked;
//                changeGlowPadDrawables(mLocked);
                glowPad.setTargetResources(R.array.empty);
                glowPad.reset(true);
                changeLockStatus(mLocked);
            }

            @Override
            public void onGrabbedStateChange(View v, int handle) {
                // Do nothing
            }

            @Override
            public void onFinishFinalAnimation() {
                // Do nothing
            }
        });
        glowPad.setEnabled(false);

        Intent gattServiceIntent = new Intent(MainActivity.this, BLService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    private void connectToLock(){
        List<Lock> list = mSimpleDao.queryForAll();
        if(list.size() > 0) {
            forcedDisconnect = false;
            selectedLock = list.get(0);
            lockNameTV.setText(selectedLock.name);
            lockStatusTV.setText(R.string.status_connecting);
            mBluetoothLeService.connect(selectedLock.address);
        }
    }

    private void disconnectToLock(){
        forcedDisconnect = true;
        mBluetoothLeService.disconnect();
    }

    private void changeGlowPadDrawables(boolean locked){
        glowPad.setHandleDrawable((locked) ? R.drawable.ic_lockscreen_handle : R.drawable.ic_unlockscreen_handle);
        glowPad.setTargetResources((locked) ? R.array.unlock : R.array.lock);
        glowPad.reset(true);
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (BLService.ACTION_GATT_CONNECTED.equals(action)) {
                lockStatusTV.setText(R.string.status_connected);
                BluetoothGattCharacteristic characteristicLock= mBluetoothLeService.map.get(BLService.UUID_BLE_SHIELD_LOCK);
                if(characteristicLock != null) {
                    mBluetoothLeService.readCharacteristic(characteristicLock);
                }
            }else
            if (BLService.ACTION_GATT_DISCONNECTED.equals(action)) {
                lockStatusTV.setText(R.string.status_disconnected);
                glowPad.setTargetResources(R.array.empty);
                glowPad.reset(true);
                if(!forcedDisconnect) {
                    mBluetoothLeService.connect(selectedLock.address); // Try to reconnect
                }
            }else if (BLService.ACTION_DATA_AVAILABLE.equals(action)) {
                String characteristicName = intent.getStringExtra(BLService.CHARACTERISTIC_NAME);

                if(GattAttributes.BLE_SHIELD_LOCK.equals(characteristicName)){
                    lockStatusChanged(intent.getByteArrayExtra(BLService.EXTRA_DATA));
                }else {
                    displayErrorResponse(intent.getByteArrayExtra(BLService.EXTRA_DATA));
                }
            }
        }
    };

    private void changeLockStatus(boolean lock){
        lockStatusTV.setText(R.string.status_in_progress);
        BluetoothGattCharacteristic characteristic = mBluetoothLeService.map.get(BLService.UUID_BLE_SHIELD_TX);

        byte[] byteArray = new byte[LockProtocol.lengths.MESSAGE];
        byteArray[0] = (lock) ? LockProtocol.types.LOCK_REQ : LockProtocol.types.UNLOCK_REQ;

        SecureRandom secureRandom = new SecureRandom();
        final byte[] randomNumber = new byte[LockProtocol.lengths.RANDOM];
        secureRandom.nextBytes(randomNumber);
        System.arraycopy(randomNumber, 0, byteArray, LockProtocol.positions.RANDOM, randomNumber.length);

        byteArray[LockProtocol.positions.ID] = selectedLock.id;

        Mac hMac = null;
        try {
            hMac = Mac.getInstance("HmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        Key secretKey = new SecretKeySpec(selectedLock.secretKey, "HmacSHA256");
        try {
            hMac.init(secretKey);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        // MESSAGE is the message to sign in bytes
        byte[] messageFirstPart = Arrays.copyOfRange(byteArray, 0, LockProtocol.positions.HMAC);
        hMac.update(messageFirstPart);
        byte[] hash = hMac.doFinal();

        // Combine message with hash
        System.arraycopy(hash, 0, byteArray, LockProtocol.positions.HMAC, LockProtocol.lengths.HMAC);

        characteristic.setValue(byteArray);
        mBluetoothLeService.writeCharacteristic(characteristic);
    }

    private void lockStatusChanged(byte[] byteArray){
        if(byteArray.length > 0){
            mLocked = byteArray[0] == LockProtocol.types.STATUS_LOCKED;
            updateStatusText(mLocked);
            changeGlowPadDrawables(mLocked);
        }
    }

    private void updateStatusText(boolean locked){
        lockStatusTV.setText( (locked) ? R.string.status_locked : R.string.status_unlocked);
    }

    private void displayErrorResponse(byte[] byteArray) {
        String data = new String(byteArray);
        Log.d("dev", data);
    }

    @Override
    protected void onResume() {
        super.onResume();

//        scanLeDevice(true);

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
//        scanLeDevice(false);
    }

    @Override
    protected void onStop() {
        Log.d("dev", "on stop main");
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

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            mLeDeviceListAdapter.addDevice(device);
//                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Super handles Bluetooth
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case REQUEST_ADD_LOCK:
                // User chose not to add at least one lock
                if(mSimpleDao.countOf() == 0) {
                    finish();
                    return;
                }else {
                    connectToLock();
                }
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.removeLock:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                // Set dialog
                alertDialogBuilder
                        .setTitle("Remove lock")
                        .setMessage("Are you sure?")
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.yes,new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
//                                mBluetoothLeService.disconnect();
                                disconnectToLock();
                                mSimpleDao.delete(selectedLock);
                                Intent intent = new Intent(MainActivity.this, AddLockActivity.class);
                                startActivityForResult(intent, REQUEST_ADD_LOCK);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null);

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();

                return true;
            case R.id.action_settings:
//                openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
