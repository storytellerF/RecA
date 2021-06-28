package com.storyteller_f.reca.widget;

import android.graphics.Bitmap;

public class ImageItem extends LabelItem {
    private final Bitmap image;

    public ImageItem(Bitmap image, String label) {
        super(label);
        this.image = image;
    }

    public Bitmap getImage() {
        return image;
    }
}
