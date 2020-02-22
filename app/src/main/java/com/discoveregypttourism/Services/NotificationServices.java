package com.discoveregypttourism.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.discoveregypttourism.MainActivity;
import com.discoveregypttourism.Messenger.service.SensorRestarterBroadcastReceiver;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NotificationServices extends Service {
    public NotificationServices() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
           getNotification();
        return super.onStartCommand(intent, flags, startId);
    }

    /* Used to build and start foreground service. */
    private void pushNotification(String text)
    {
        // Create notification default intent.

        // Create notification builder.
        NotificationCompat.Builder mBuilder  = new NotificationCompat.Builder(this);

        // Make notification show big text.
        mBuilder.setSmallIcon(com.discoveregypttourism.R.drawable.ic_discover_logo);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources() , com.discoveregypttourism.R.drawable.messaage_fill));
        mBuilder.setContentTitle(getResources().getString(com.discoveregypttourism.R.string.app_name));
        mBuilder.setContentText(text);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
       // mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, mBuilder.build());
    }

    private void getNotification(){

        FirebaseDatabase.getInstance().getReference().child("profiles").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("username").getValue().toString();
                FirebaseDatabase.getInstance().getReference().child("message").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                            FirebaseDatabase.getInstance().getReference().child("message/" + dataSnapshot1.getKey())
                                    .limitToLast(1)
                                    .addChildEventListener(new ChildEventListener() {
                                        @Override
                                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                            if (dataSnapshot.child("idReceiver").getValue().equals(name) && dataSnapshot.child("notified").getValue()==null){
                                                getNameByIdAndPushNotification(dataSnapshot.child("idSender").getValue().toString(),
                                                        dataSnapshot.child("text").getValue().toString());
                                                FirebaseDatabase.getInstance().getReference().child("message/" + dataSnapshot1.getKey() + "/" + dataSnapshot.getKey() + "/" + "notified")
                                                        .setValue("yes");
                                            }

                                        }

                                        @Override
                                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                        }

                                        @Override
                                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                                        }

                                        @Override
                                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopSelf();
        Log.e("stopservice","stopServices");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onTaskRemoved(rootIntent);
    }

    private void getNameByIdAndPushNotification(String id , String message){
        FirebaseDatabase.getInstance().getReference().child("profiles").child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("username").getValue().toString();
                pushNotification(name + ": " + message);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent(this, SensorRestarterBroadcastReceiver.class);
        startService(new Intent(NotificationServices.this , NotificationServices.class));
        sendBroadcast(broadcastIntent);
    }
}