package com.andoop.android.main_plugin_a;

import android.os.Bundle;

import com.ryg.dynamicload.DLBasePluginActivity;

public class SecondTestActivity extends DLBasePluginActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second_test);
    }
}
