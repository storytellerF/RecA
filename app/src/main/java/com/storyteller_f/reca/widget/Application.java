package com.storyteller_f.reca.widget;

import android.graphics.Bitmap;

public class Application extends ImageItem {
    public Application(Bitmap bitmap, String label, String packageName) {
        super(bitmap, label);
        this.packageName=packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    private final String packageName;
}
