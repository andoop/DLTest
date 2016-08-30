package com.andoop.android.main_plugin_a;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.ryg.dynamicload.DLBasePluginActivity;
import com.ryg.dynamicload.internal.DLIntent;

public class MainActivity extends DLBasePluginActivity implements View.OnClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_openSecond).setOnClickListener(this);
        findViewById(R.id.btn_openService).setOnClickListener(this);
        findViewById(R.id.btn_stopService).setOnClickListener(this);
    }

    public void openSecond(){
        startPluginActivity(new DLIntent(getPackageName(),SecondTestActivity.class));
    }

    private void openService() {
        startPluginService(new DLIntent(getPackageName(),TestService.class));
    }

    private void stopService() {
        stopPluginService(new DLIntent(getPackageName(),TestService.class));
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_openSecond:
                openSecond();
                break;
            case R.id.btn_openService:
                openService();
                break;
            case R.id.btn_stopService:
                stopService();
                break;
        }
    }


}
