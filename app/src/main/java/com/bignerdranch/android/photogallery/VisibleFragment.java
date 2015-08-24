package com.bignerdranch.android.photogallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by bioyang on 15/8/24.
 */
public class VisibleFragment extends Fragment
{
    public static final String TAG = "VisibleFragment";

    private BroadcastReceiver mOnShowNotification = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            /*Toast.makeText(getActivity(),
                            "Got a broadcast: " + intent.getAction(),
                            Toast.LENGTH_LONG)
                          .show();*/
            Log.i(TAG, "canceling notification");
            setResultCode(PhotoGalleryActivity.RESULT_CANCELED);
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();

        IntentFilter filter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);

        //getActivity().registerReceiver(mOnShowNotification, filter);
        getActivity().registerReceiver(mOnShowNotification, filter, PollService.PERM_PRIVATE, null);
    }

    @Override
    public void onPause()
    {
        super.onPause();

        getActivity().unregisterReceiver(mOnShowNotification);
    }
}
