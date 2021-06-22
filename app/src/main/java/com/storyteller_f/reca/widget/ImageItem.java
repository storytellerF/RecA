package com.storyteller_f.reca.widget;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public class ImageItem extends LabelItem {
    public Bitmap getImage() {
        return image;
    }

    private final Bitmap image;

    public ImageItem(Bitmap image, String label) {
        super(label);
        this.image=image;
    }
}
