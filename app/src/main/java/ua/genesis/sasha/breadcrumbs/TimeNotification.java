package ua.genesis.sasha.breadcrumbs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class TimeNotification extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {


            Toast.makeText(context, "Alarm", Toast.LENGTH_LONG).show();
        Log.i("Script","TimeNotificationCalss");
        // Этот метод будет вызываться по событию

    }
}
