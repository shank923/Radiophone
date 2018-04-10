package com.example.root.radiophone;

import android.Manifest;
import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends ListActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private BluetoothService mBluetoothLeService;
    //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    ArrayList<String> listItems = new ArrayList<String>();

    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
    ArrayAdapter<String> adapter;

    private String mDeviceName;
    private String mDeviceAddress;
    private boolean mConnected = false;

    String acc;

    ImageView image;
    EditText input;
    String nickname;

    int i = 0, j = 20;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };



    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show();
            } else if (BluetoothService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                Toast.makeText(context, "Disconnected", Toast.LENGTH_SHORT).show();
            }
            else if (BluetoothService.ACTION_DATA_AVAILABLE.equals(action)) {
                adapter.add(intent.getStringExtra(BluetoothService.EXTRA_DATA));
                Log.d("SERVICECONNECTION", intent.getStringExtra(BluetoothService.EXTRA_DATA));
                //acc += intent.getStringExtra(BluetoothService.EXTRA_DATA);
               // Bitmap bitmap = BitmapFactory.decodeByteArray(intent.getStringExtra(BluetoothService.EXTRA_DATA).getBytes(),
                 //       0, intent.getStringExtra(BluetoothService.EXTRA_DATA).getBytes().length);
                //image.setImageBitmap(bitmap);
               adapter.notifyDataSetChanged();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        input = (EditText)findViewById(R.id.input);

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        //image = (ImageView) findViewById(R.id.imageView1);

        nickname = getIntent().getStringExtra("NickName");

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);

        setListAdapter(adapter);

        Intent gattServiceIntent = new Intent(this, BluetoothService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!input.getText().toString().isEmpty())
                {

                    InputMethodManager inputManager = (InputMethodManager)
                            getSystemService(MainActivity.INPUT_METHOD_SERVICE);

                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);

                    adapter.add(input.getText().toString());
                    adapter.notifyDataSetChanged();

                    Intent intent = new Intent(MainActivity.this,BluetoothService.class);
                    intent.putExtra("message", nickname + ':' + '\n' + input.getText().toString());

                    MainActivity.this.startService(intent);

                    // Clear the input
                    input.setText("");
                }
            }
        });


//        fab.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                try {
//                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 003);
//                    } else {
//                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
//                        photoPickerIntent.setType("image/*");
//                        startActivityForResult(photoPickerIntent, 002);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                return true;
//            }
//        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }


//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
//        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
//
//        switch(requestCode) {
//            case 002:
//                if(resultCode == RESULT_OK){
//                    Uri selectedImage = imageReturnedIntent.getData();
//                    InputStream imageStream = null;
//                    try {
//                        imageStream = getContentResolver().openInputStream(selectedImage);
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    }
//                    Bitmap rfphoto = BitmapFactory.decodeStream(imageStream);
//
//                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                    rfphoto.compress(Bitmap.CompressFormat.JPEG, 70, stream);
//                    final byte[] rfbytes = stream.toByteArray();
//
//                    final String coded = Base64.encodeToString(rfbytes, Base64.DEFAULT);
//
//                    Log.d(TAG, "IMAGEBITMAPSTRING = \n" + coded);
//
//                   final Handler handler = new Handler();
//
//                    final Runnable r = new Runnable() {
//                        public void run() {
//                            Intent intent = new Intent(MainActivity.this, BluetoothService.class);
//                            intent.putExtra("message", coded.substring(i, j));
//
//                            MainActivity.this.startService(intent);
//                            i+=20; j+=20;
//                            handler.postDelayed(this, 150);
//                        }
//                    };
//
//                    handler.postDelayed(r, 150);
//
//
//                    //decoding
//                   // Bitmap bitmap = BitmapFactory.decodeByteArray(rfbytes, 0, rfbytes.length);
//
//                    //image.setImageBitmap(bitmap);
//                }
//        }
//    }
}
