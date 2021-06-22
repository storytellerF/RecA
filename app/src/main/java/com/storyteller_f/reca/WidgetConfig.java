package com.storyteller_f.reca;

import androidx.annotation.NonNull;

/**
 * @author faber
 */
public class WidgetConfig {
    public int col;
    public int row;
    public boolean isUsedTime;

    public WidgetConfig(int col, int row) {
        this.col = col;
        this.row = row;
    }
    @NonNull
    @Override
    public String toString() {
        return col + "-" + row + "-" + isUsedTime;
    }
    public static int toInt(String s) {
        return Integer.parseInt(s);
    }
    public static WidgetConfig getConfig(String[] split) {
        WidgetConfig config = new WidgetConfig(toInt(split[0]),toInt(split[1]));
        config.isUsedTime=split[2].equals("true");
        return config;
    }
}
