package edu.cuc.stephen.bluetoothdiscovery;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import edu.cuc.stephen.bluetoothstrength.R;

public class MainActivity extends Activity {
    private static final int REQUEST_ENABLE_BT = 587;
    private Button buttonSwitch;
    private TextView textView;
    private ListView listViewResult;
    private BluetoothAdapter bluetoothAdapter;
    private List<Map<String, String>> listItem;
    private SimpleAdapter simpleAdapter;
    private Button buttonStart;
    private int searchTimes;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK)
            CheckBluetoothState();
    }

    // Don't forget to unregister during onDestroy
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        if(timer != null)
        timer.cancel();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        buttonSwitch = findViewById(R.id.main_switch);
        listViewResult = findViewById(R.id.list_view);
        buttonStart = findViewById(R.id.start_scan);
        buttonStart.setEnabled(false);
        textView = findViewById(R.id.text_info);
        searchTimes = 0;

        //获取蓝牙适配器
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        CheckBluetoothState();
        //设置过滤器，过滤因远程蓝牙设备被找到而发送的广播
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        // Register the BroadcastReceiver // Don't forget to unregister during onDestroy
        this.registerReceiver(broadcastReceiver, filter);

        listItem = new ArrayList<>();
        ResetListItem();
        simpleAdapter = new SimpleAdapter(this, listItem, R.layout.item_list,
                new String[]{"bt_name", "bt_address", "bt_rssi"},
                new int[]{R.id.bt_name, R.id.bt_address, R.id.bt_rssi});
        listViewResult.setAdapter(simpleAdapter);

        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    private void ResetListItem() {
        listItem.clear();
        Map<String, String> map = new HashMap<>();
        map.put("bt_name", "名称");
        map.put("bt_address", "地址");
        map.put("bt_rssi", "信号强度");
        listItem.add(map);
    }

    private void CheckBluetoothState() {
        if (bluetoothAdapter == null){
            textView.setText("本设备不支持蓝牙");
            buttonSwitch.setEnabled(false);
        }else{
            buttonSwitch.setEnabled(true);
            if (bluetoothAdapter.isEnabled()){
                if(bluetoothAdapter.isDiscovering())
                    textView.setText("正在搜索蓝牙设备");
                else {
                    textView.setText("蓝牙已开启");
                }
                buttonStart.setEnabled(true);
                buttonSwitch.setText("关闭蓝牙");
            }else{
                textView.setText("蓝牙已关闭");
                buttonSwitch.setText("开启蓝牙");
            }
        }
    }

    private Timer timer;

    public void startScan(View view) {
        if(bluetoothAdapter.isEnabled()){
            if(!bluetoothAdapter.isDiscovering()) {
                ResetListItem();
                bluetoothAdapter.startDiscovery();
                buttonStart.setText("第"+(++searchTimes)+"次扫描中...");
                if (timer != null) {
                    timer.cancel();
                }
                timer = new Timer();
                timer.schedule(timerTask, 12000, 12000);
            }else{
                buttonStart.setText("停止扫描");
            }
        }else{
            Toast.makeText(getApplicationContext(), "蓝牙未打开", Toast.LENGTH_LONG).show();
        }
    }

    public void switchBT(View view) {
        if (!bluetoothAdapter.isEnabled()){
            bluetoothAdapter.enable();
            Toast.makeText(getApplicationContext(), "蓝牙已开启", Toast.LENGTH_LONG).show();
        }else{
            bluetoothAdapter.disable();
            Toast.makeText(getApplicationContext(), "蓝牙已关闭", Toast.LENGTH_LONG).show();
            searchTimes = 0;
        }
        CheckBluetoothState();
    }

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(getApplicationContext(), "BroadcastReceiver", Toast.LENGTH_LONG).show();
            String action = intent.getAction();
            //  When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals((action))){
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getBondState() != BluetoothDevice.BOND_BONDED){
                    //信号强度
                    short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                    // Add the name and address to an array adapter to show in a ListView
                    Map<String, String> map = new HashMap<>();
                    map.put("bt_name", device.getName());
                    map.put("bt_address", device.getAddress());
                    map.put("bt_rssi", String.valueOf(rssi));
                    listItem.add(map);
                    simpleAdapter.notifyDataSetChanged();
                }
                //Toast.makeText(getApplicationContext(), "延时12秒后再次扫描", Toast.LENGTH_LONG).show();
            }else{
                //textView.setText("未发现设备");
            }
            if (!bluetoothAdapter.isDiscovering()){
                bluetoothAdapter.startDiscovery();
                buttonStart.setText("继续扫描中...");
            }
        }
    };

    private final TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Toast.makeText(getApplicationContext(), "查看是否再次启动扫描", Toast.LENGTH_LONG).show();
                    if(!bluetoothAdapter.isDiscovering()) {
                        ResetListItem();
                        bluetoothAdapter.startDiscovery();
                        //++searchTimes;
                        buttonStart.setText("第" + (++searchTimes) + "次扫描中...");
                    }
                }
            });
        }
    };

}
