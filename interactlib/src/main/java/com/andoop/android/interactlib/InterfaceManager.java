package com.andoop.android.interactlib;

/**
 * Created by 黄栋 on 2016/8/29.
 */
public class InterfaceManager {
    private static IHost host;
    private static IPlugin plugin;


    public static IHost getHost() {
        return host;
    }

    public static IPlugin getPlugin() {
        return plugin;
    }

    public static void setHost(IHost host) {
        InterfaceManager.host = host;
    }

    public static void setPlugin(IPlugin plugin) {
        InterfaceManager.plugin = plugin;
    }
}
