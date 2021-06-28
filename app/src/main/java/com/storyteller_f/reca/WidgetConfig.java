package com.storyteller_f.reca;

import android.content.Intent;

import androidx.annotation.NonNull;

/**
 * @author faber
 */
public class WidgetConfig {
    public static final int count=6;
    public int col;
    public int row;
    public boolean isUsedTime;
    /**
     * 天 1
     * 周 2
     * 月 3
     */
    public int type;
    public boolean excludeToday;
    public boolean excludeSelf;

    public WidgetConfig(int col, int row) {
        this.col = col;
        this.row = row;
    }

    public static int toInt(String s) {
        return Integer.parseInt(s);
    }

    /*
     * 需要6个长度
     */
    public static WidgetConfig getConfig(String[] split) {
        WidgetConfig config = new WidgetConfig(toInt(split[0]), toInt(split[1]));
        config.isUsedTime = split[2].equals("true");
        config.type = toInt(split[3]);
        config.excludeToday = Boolean.parseBoolean(split[4]);
        config.excludeSelf = Boolean.parseBoolean(split[5]);
        return config;
    }

    public static String save(String col, String row, boolean isUsedTime, int type, boolean excludeSelf, boolean excludeToday) {
        return col + "-" + row + "-" + isUsedTime + "-" + type + "-" + excludeToday + "-" + excludeSelf;
    }

    @NonNull
    @Override
    public String toString() {
        return col + "-" + row + "-" + isUsedTime + "-" + type + "-" + excludeToday + "-" + excludeSelf;
    }

    public static WidgetConfig from(Intent intent) {
        int col = intent.getIntExtra("col",2);
        int row=intent.getIntExtra("row",2);
        WidgetConfig widgetConfig=new WidgetConfig(col,row);
        widgetConfig.type = intent.getIntExtra("type", 1);
        widgetConfig.isUsedTime = intent.getBooleanExtra("isUsedTime", false);
        widgetConfig.excludeToday = intent.getBooleanExtra("excludeToday",false);
        widgetConfig.excludeSelf = intent.getBooleanExtra("excludeSelf", false);
        return widgetConfig;
    }

    public static void putToIntent(Intent intent, WidgetConfig config) {
        intent.putExtra("col",config.col);
        intent.putExtra("row", config.row);
        intent.putExtra("type", config.type);
        intent.putExtra("isUsedTime", config.isUsedTime);
        intent.putExtra("excludeToday",config.excludeSelf);
        intent.putExtra("excludeSelf",config.excludeSelf);
    }

}
