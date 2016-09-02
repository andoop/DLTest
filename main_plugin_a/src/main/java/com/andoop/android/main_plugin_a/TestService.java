package com.andoop.android.main_plugin_a;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.ryg.dynamicload.DLBasePluginService;

public class TestService extends DLBasePluginService {
    public TestService() {
    }
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(">>>","0");
        Toast.makeText(TestService.this, "TestService onCreate :服务开启", Toast.LENGTH_SHORT).show();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(">>>","1");
        Toast.makeText(that, "TestService onStartCommand :服务已开启", Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public void onDestroy() {
        Log.e(">>>","2");
        Toast.makeText(that, "TestService onDestroy :服务关闭", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }
}
