package com.andoop.android.main_plugin_b;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.andoop.android.interactlib.IHost;
import com.andoop.android.interactlib.IPlugin;
import com.andoop.android.interactlib.InterfaceManager;
import com.ryg.dynamicload.DLBasePluginFragmentActivity;

public class MainActivity extends DLBasePluginFragmentActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_plugin_b).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IHost host = InterfaceManager.getHost();
                if(host==null){
                    Toast.makeText(MainActivity.this.that,"hello", Toast.LENGTH_SHORT).show();
                    return;
                }
                String hostMethod = host.hostMethod(that,"在插件main_plugin_b中调用>>");
                Toast.makeText(MainActivity.this.that, hostMethod, Toast.LENGTH_SHORT).show();
            }
        });
        InterfaceManager.setPlugin(new IPlugin() {
            @Override
            public String pulginMethod(String s) {
                return s+"我是插件b";
            }
        });
    }
}
