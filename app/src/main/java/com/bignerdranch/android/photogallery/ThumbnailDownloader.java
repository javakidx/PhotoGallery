package com.bignerdranch.android.photogallery;

import android.media.session.MediaSession.Token;
import android.os.HandlerThread;
import android.util.Log;

/**
 * Created by 安軻 on 2015/8/11.
 */
public class ThumbnailDownloader<Token> extends HandlerThread
{
    private static final String TAG = "ThumbnailDownloader";

    public ThumbnailDownloader()
    {
        super(TAG);
    }

    public void queueThumbnail(Token token, String url)
    {
        Log.i(TAG, "Got an URL: " + url);
    }
}
