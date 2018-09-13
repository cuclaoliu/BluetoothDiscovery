package edu.cuc.stephen.bluetoothstrength;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {
    private Button buttonSwitch;
    //private TextView textView;
    private ListView listViewResult;
    private BluetoothAdapter bluetoothAdapter;
    private List<Map<String, Object>> listItem;
    private SimpleAdapter simpleAdapter;
    private Button buttonStart;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(getApplicationContext(), "收到广播", Toast.LENGTH_LONG).show();
            String action = intent.getAction();
            //当设备开始扫描时
            if (BluetoothDevice.ACTION_FOUND.equals((action))){
                //从Intent得到blueDevice对象
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getBondState() != BluetoothDevice.BOND_BONDED){
                    //信号强度
                    short rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("bt_name", device.getName());
                    map.put("bt_address", device.getAddress());
                    map.put("bt_rssi", rssi);
                    listItem.add(map);
                    simpleAdapter.notifyDataSetChanged();
                }
            }else{
                //textView.setText("未发现设备");
            }
            if (!bluetoothAdapter.isDiscovering()){
                bluetoothAdapter.startDiscovery();
                buttonStart.setText("继续扫描中...");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonSwitch = findViewById(R.id.main_switch);
        listViewResult = findViewById(R.id.list_view);
        buttonStart = findViewById(R.id.start_scan);
        //textView = findViewById(R.id.text_info);

        //获取蓝牙适配器
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter.isEnabled()){
            buttonSwitch.setText("关闭蓝牙");
        }
        //设置过滤器，过滤因远程蓝牙设备被找到而发送的广播
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        //设置广播接收器和安装过滤器
        this.registerReceiver(mReceiver, filter);

        listItem = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("bt_name", "名称");
        map.put("bt_address", "地址");
        map.put("bt_rssi", "强度");
        listItem.add(map);
        simpleAdapter = new SimpleAdapter(this, listItem, R.layout.list,
                new String[]{"bt_name", "bt_address", "bt_rssi"},
                new int[]{R.id.bt_name, R.id.bt_address, R.id.bt_rssi});
        listViewResult.setAdapter(simpleAdapter);
    }


    public void startScan(View view) {
        if(bluetoothAdapter.isEnabled()){
            if(!bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.startDiscovery();
                buttonStart.setText("扫描中...");
            }else{
                buttonStart.setText("等待中...");
            }
        }else{
            Toast.makeText(getApplicationContext(), "蓝牙未打开", Toast.LENGTH_LONG).show();
        }
    }

    public void switchBT(View view) {
        if (!bluetoothAdapter.isEnabled()){
            bluetoothAdapter.enable();
            Toast.makeText(getApplicationContext(), "蓝牙已开启", Toast.LENGTH_LONG).show();
            buttonSwitch.setText("关闭蓝牙");
        }else{
            bluetoothAdapter.disable();
            Toast.makeText(getApplicationContext(), "蓝牙已关闭", Toast.LENGTH_LONG).show();
            buttonSwitch.setText("开启蓝牙");
        }
    }
}
