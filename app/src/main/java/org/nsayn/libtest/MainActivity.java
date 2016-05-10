package org.nsayn.libtest;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;

import java.util.List;

import kr.bsinc.android.library.manager.PreferenceManager;
import kr.bsinc.service.BackgroundService;
import kr.bsinc.service.Constants;

public class  MainActivity extends AppCompatActivity {

    private BackgroundService mBackgoundService;
    public EditText etIdValue;
    private EditText etDelayTime;
    private SeekBar sbDelayTime;

    private ListView listView;

    private final static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        etDelayTime  = (EditText) findViewById(R.id.etDelayTime);
        sbDelayTime = (SeekBar) findViewById(R.id.sbDelayTime);
        listView = (ListView)findViewById(R.id.listView);

        etDelayTime.setText(String.valueOf(PreferenceManager.getInstance(getApplicationContext()).getFrequencyDelaySet()));
        etDelayTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                try {
                    sbDelayTime.setProgress(Integer.parseInt(s.toString()));
                }catch (Exception e)
                {
                    sbDelayTime.setProgress(0);
                }
                int pos = etDelayTime.getText().length();
                if(start+1 == pos )
                    etDelayTime.setSelection(pos);
                else if(before <= count)
                {
                    try {
                        etDelayTime.setSelection(start+1);
                    }catch (Exception e)
                    {
                        etDelayTime.setSelection(0);
                    }
                }
                else
                    etDelayTime.setSelection(start);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });



        sbDelayTime.setProgress(Integer.parseInt(etDelayTime.getText().toString()));
        sbDelayTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                etDelayTime.setText(String.valueOf(arg1));
                int pos = etDelayTime.getText().length();
                etDelayTime.setSelection(pos);
            }
        });

        serviceBinding();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    // 필요할 경우 Service Connection 이벤트 처리
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            BackgroundService.LocalBinder binder = (BackgroundService.LocalBinder) service;
            mBackgoundService = binder.getService();
            notifyToService(Constants.ACTION_STOP_BGSERVICE);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBackgoundService = null;
        }
    };

    // Background Sercvice 구동 및 바인딩
    private void serviceBinding() {
        Intent intent = new Intent(this, BackgroundService.class);
        if (startService(intent) != null) {
            //bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
            ;
        }
        Intent intentOut = new Intent(Constants.ACTION_STOP_BGSERVICE);
        sendBroadcast(intentOut);
    }

    private void notifyToService(String action) {

        Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();

        PreferenceManager.getInstance(getApplicationContext()).setFrequencyDelaySet(Integer.parseInt(etDelayTime.getText().toString()));

        // Service 중지 및 언바인딩 처리
        //unbindService(mServiceConnection);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                killApp();
            default:
        }
        return  false;
    }

    private  void  killApp(){
        notifyToService(Constants.ACTION_START_BGSERVICE);
        finish();
    }

    public void onWifiClicked(View v) {
         final WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> configurations = wifiMgr.getConfiguredNetworks();
        if (configurations != null) {
            for (final WifiConfiguration config : configurations) {
                // 설정 정보에서 networkID와 SSID를 로그에 출력
                int id = config.networkId;
                String ssid = config.SSID;
                Log.i(TAG, String.format("Network ID:%3d SSID:%s", id, ssid));
            }
        }
        if (!wifiMgr.isWifiEnabled()) {
            //현재 wifi가 off 상태
            Log.i(TAG, String.format("wifi off -> on"));
            wifiMgr.setWifiEnabled(true);
        }

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                List<ScanResult> results = wifiMgr.getScanResults();
                Log.i(TAG, String.format("%d network found", results.size()));
                for (ScanResult result : results) {
                    Log.i(TAG, String.format("SSID:%s BSSID:%s", result.SSID, result.BSSID));
                }
            }
        }, new IntentFilter(wifiMgr.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiMgr.startScan();
    }


}
