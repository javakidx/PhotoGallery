package com.bignerdranch.android.photogallery;

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

   public PollService()
    {
        super(TAG);
    }
    @Override
    protected void onHandleIntent(Intent intent)
    {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        @SuppressWarnings("deprecation")
        boolean isNetworkAvailable = cm.getBackgroundDataSetting() && cm.getActiveNetworkInfo() != null;

        if (!isNetworkAvailable)
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

            PendingIntent pi = PendingIntent.getService(this, 0, new Intent(this, PhotoGalleryActivity.class), 0);

            Notification notification = new NotificationCompat.Builder(this)
                    .setTicker(resources.getString(R.string.new_pictures_title))
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle(resources.getString(R.string.new_pictures_title))
                    .setContentText(resources.getString(R.string.new_pictures_text))
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .build();

            NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(0, notification);
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
        Intent i = new Intent(context, PollService.class);
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
    }

    public static boolean isServiceAlarmOn(Context context)
    {
        Intent i = new Intent(context, PollService.class);
        PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);

        return pi != null;
    }
}
