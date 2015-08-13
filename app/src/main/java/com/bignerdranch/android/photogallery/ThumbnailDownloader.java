package com.bignerdranch.android.photogallery;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.session.MediaSession;
import android.media.session.MediaSession.Token;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 安軻 on 2015/8/11.
 */
public class ThumbnailDownloader<Token> extends HandlerThread
{
    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;

    private Handler mHandler;
    private Handler mResponseHandler;
    private Listener<Token> mListener;

    public interface Listener<Token>
    {
        void onThumbnailDownloaded(Token token, Bitmap bitmap);
    }

    private Map<Token, String> mRequestMap = Collections.synchronizedMap(new HashMap<Token, String>());

    /*public ThumbnailDownloader()
    {
        super(TAG);
    }*/
    public ThumbnailDownloader(Handler responseHandler)
    {
        super(TAG);
        mResponseHandler = responseHandler;
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared()
    {
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg)
            {
                if (msg.what == MESSAGE_DOWNLOAD)
                {
                    @SuppressWarnings("unchecked")
                    Token token = (Token)msg.obj;
                    Log.i(TAG, "Got a request for url: " + mRequestMap.get(token));
                    handleRequest(token);
                }
            }
        };
    }

    public void queueThumbnail(Token token, String url)
    {
        Log.i(TAG, "Got an URL: " + url);
        mRequestMap.put(token, url);

        mHandler.obtainMessage(MESSAGE_DOWNLOAD, token)
                .sendToTarget();
    }

    private void handleRequest(final Token token)
    {
        try
        {
            final String url = mRequestMap.get(token);

            if(url == null)
            {
                return;
            }

            byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
            final Bitmap bitmp = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);

            Log.i(TAG, "Bitmap created");

            mResponseHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    if(mRequestMap.get(token) != url)
                    {
                        return;
                    }

                    mRequestMap.remove(token);
                    mListener.onThumbnailDownloaded(token, bitmp);
                }
            });

        } catch (IOException e)
        {
            Log.e(TAG, "Error downloading image", e);
        }
    }

    public void clearQueue()
    {
        mHandler.removeMessages(MESSAGE_DOWNLOAD);
        mRequestMap.clear();
    }

    public void setListener(Listener<Token> listener)
    {
        this.mListener = listener;
    }
}
