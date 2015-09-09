package com.bignerdranch.android.photogallery;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.List;

/**
 * Created by bioyang on 15/8/19.
 */
public class PollService extends IntentService
{
    private static final String TAG = "PollService";
    //private static final int POLL_INTERVAL = 1000 * 15; // 15 secs
    private static final int POLL_INTERVAL = 1000 * 60 * 5;

    public static final String PREF_IS_ALARM_ON = "isAlarmOn";

    public static final String ACTION_SHOW_NOTIFICATION = "com.bignerdranch.android.photogallery.SHOW_NOTIFICATION";

    public static final String PERM_PRIVATE = "com.bignerdranch.android.photogallery.PRIVATE";

    public static Intent newIntent(Context context)
    {
        return new Intent(context, PollService.class);
    }

    public PollService()
    {
        super(TAG);
    }

    private boolean isNetworkAvailableAndConnected()
    {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);

        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;

        boolean isNetworkConnected = isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();

        return isNetworkConnected;
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
//        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
//
//        @SuppressWarnings("deprecation")
//        boolean isNetworkAvailable = cm.getBackgroundDataSetting() && cm.getActiveNetworkInfo() != null;
//
//        if (!isNetworkAvailable)
        if (!isNetworkAvailableAndConnected())
        {
            return;
        }
        //Log.i(TAG, "Receive an intent: " + intent);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String query = preferences.getString(FlickrFetchr.PREF_SEARCH_QUERY, null);
        String lastResultId = preferences.getString(FlickrFetchr.PREF_LAST_RESULT_ID, null);

        List<GalleryItem> items;
        if(query != null)
        {
            items = new FlickrFetchr().search(query);
        }
        else
        {
            items = new FlickrFetchr().fetchItems();
        }

        if(items.size() == 0)
        {
            return;
        }

        String resultId = items.get(0).getId();
        if (!resultId.equals(lastResultId))
        {
            Log.i(TAG, "Got a new result: " + resultId);

            //按通知啟動APP
            Resources resources = getResources();

            PendingIntent pi = PendingIntent.getActivity(this, 0, PhotoGalleryActivity.newIntent(this), 0);

            Notification notification = new NotificationCompat.Builder(this)
                    .setTicker(resources.getString(R.string.new_pictures_title))
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle(resources.getString(R.string.new_pictures_title))
                    .setContentText(resources.getString(R.string.new_pictures_text))
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .build();

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(0, notification);
//            NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
//            notificationManager.notify(0, notification);

            //sendBroadcast(new Intent(ACTION_SHOW_NOTIFICATION));
//            sendBroadcast(new Intent(ACTION_SHOW_NOTIFICATION), PERM_PRIVATE);
            //showBackgroundNotification(0, notification);
        }
        else
        {
            Log.i(TAG, "Got an old result: " + resultId);
        }

        preferences.edit()
                .putString(FlickrFetchr.PREF_LAST_RESULT_ID, lastResultId)
                .commit();
    }

    public static void setServiceAlarm(Context context, boolean isOn)
    {
        //Intent i = new Intent(context, PollService.class);
        Intent i = newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        if (isOn)
        {
            alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), POLL_INTERVAL, pi);
        }
        else
        {
            alarmManager.cancel(pi);
            pi.cancel();
        }

        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PollService.PREF_IS_ALARM_ON, isOn)
                .commit();
    }

    public static boolean isServiceAlarmOn(Context context)
    {
        //Intent i = new Intent(context, PollService.class);
        Intent i = PollService.newIntent(context);

        /*
            PendingIntent.FLAG_NO_CREATE says that if the PendingIntent dose not already exist,
            return null instead of creating it.
         */
        PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);

        return pi != null;
    }

    void showBackgroundNotification(int requestCode, Notification notification)
    {
        Intent i = new Intent(ACTION_SHOW_NOTIFICATION);
        i.putExtra("REQUEST_CODE", requestCode);
        i.putExtra("NOTIFICATION", notification);

        sendOrderedBroadcast(i, PERM_PRIVATE, null, null, Activity.RESULT_OK, null, null);
    }
}
