package com.storyteller_f.reca.widget;

import android.graphics.Bitmap;

public class DesktopApplication extends Application {
    private final int type;
    private final String className;
    public DesktopApplication(Bitmap image, String label, String packageName, String className, int type) {
        super(image, label, packageName);
        this.type = type;
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public int getType() {
        return type;
    }
}
