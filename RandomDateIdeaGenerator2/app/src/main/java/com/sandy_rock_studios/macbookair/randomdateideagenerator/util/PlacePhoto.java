package com.sandy_rock_studios.macbookair.randomdateideagenerator.util;

import android.graphics.Bitmap;

public class PlacePhoto {
    Bitmap myBitmap;
    CharSequence myAttribution;
    public PlacePhoto(Bitmap bitmap, CharSequence attribution){
        myBitmap = bitmap;
        myAttribution = attribution;
    }

    public Bitmap getBitmap(){
        return myBitmap;
    }

    public CharSequence getAttribution() {
        return myAttribution;
    }
}
