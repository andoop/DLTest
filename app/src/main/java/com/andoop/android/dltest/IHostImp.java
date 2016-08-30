package com.andoop.android.dltest;

import android.content.Context;

import com.andoop.android.interactlib.IHost;

/**
 * Created by 黄栋 on 2016/8/30.
 */
public class IHostImp implements IHost{
    @Override
    public String hostMethod(Context context,String data) {
        return data+">>我是host中字符串";
    }
}
