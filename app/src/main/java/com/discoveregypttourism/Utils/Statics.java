package com.discoveregypttourism.Utils;

import android.content.Context;
import android.graphics.Typeface;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Statics {

    public static Typeface regular (Context context){
        return Typeface.createFromAsset(context.getAssets(), "Roboto-Regular.ttf");
    }

    public static DatabaseReference firebase_reference = FirebaseDatabase.getInstance().getReference();
}
