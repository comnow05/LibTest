package org.nsayn.libtest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

import kr.bsinc.android.library.Constants;

/**
 * Created by admin on 2016-05-02.
 */
public class BroadcastListener extends BroadcastReceiver {
    final static String TAG = "BroadcastListener";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        String toastText = ""  ;


        if(action.equals(Constants.ACTION_DEVICE_SCAN))     //디바이스 스캔
        {
            String sMacAddress = intent.getStringExtra("macaddress");
            int iRSSi = intent.getIntExtra("rssi", 0);
            toastText = "Device Scan = MacAddres : " + sMacAddress + ", RSSi : " + iRSSi;
            //return;   //찾는 속도보다 느려서 막아둠
        }

        if(action.equals(Constants.ACTION_AROUND_DEVICE))   //카운터 근처
        {
            toastText = "카운터 근처";
            //return;   //찾는 속도보다 느려서 막아둠
        }


        switch (action)
        {
            case Constants.ACTION_SOUND_DETECT:
                toastText = "사운드 접촉";
                WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
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
                    wifiMgr.setWifiEnabled(true);
                }
                break;
            case Constants.ACTION_SOUND_BREAK:
                toastText = "사운드 끊김";
                break;
            case Constants.ACTION_DEVICE_CONNECT:
                toastText = "디바이스와 연결 - TAB";
                break;
            case Constants.ACTION_SHOW_RECEIPT:
                toastText = "영수증 수신";
                break;
            case Constants.ACTION_SHOW_STAMP:
                toastText = "스탬프 수신";
                break;
        }
        Log.d("BroadcastListener", toastText);
        //Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();
    }

    public BroadcastListener() {
        super();
    }
}
