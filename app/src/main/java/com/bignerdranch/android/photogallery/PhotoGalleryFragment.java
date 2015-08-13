package com.bignerdranch.android.photogallery;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.IOException;
import java.util.List;

/**
 * Created by bioyang on 15/8/11.
 */
public class PhotoGalleryFragment extends Fragment
{
    private static final String TAG = "PhotoGalleryFragment";

    private GridView mGridView;
    private List<GalleryItem> mItems;
    private ThumbnailDownloader<ImageView> mThumbnailThread;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        new FetchItemsTask().execute();

        //mThumbnailThread = new ThumbnailDownloader<>();
        mThumbnailThread = new ThumbnailDownloader<ImageView>(new Handler());

        mThumbnailThread.setListener(new ThumbnailDownloader.Listener<ImageView>()
        {
            @Override
            public void onThumbnailDownloaded(ImageView imageView, Bitmap bitmap)
            {
                if(isVisible())
                {
                    imageView.setImageBitmap(bitmap);
                }
            }
        });

        mThumbnailThread.start();
        mThumbnailThread.getLooper();
        Log.i(TAG, "Background thread started");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mGridView = (GridView)v.findViewById(R.id.gridView);

        setupAdapter();

        return v;
    }

//    private class FetchItemsTask extends AsyncTask<Void, Void, Void>
    private class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>>
    {

        @Override
//        protected Void doInBackground(Void... params)
        protected List<GalleryItem> doInBackground(Void... params)
        {
//            try
//            {
//                String result = new FlickrFetchr().getUrl("http://www.google.com");
//                Log.i(TAG, "Fetched contents of URL: " + result);
//            }
//            catch (IOException ioe)
//            {
//                Log.e(TAG, "Failed to fetch URL: ", ioe);
//            }
//            new FlickrFetchr().fetchItems();
            return new FlickrFetchr().fetchItems();
//            return null;
        }

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems)
        {
            mItems = galleryItems;
            setupAdapter();
        }
    }

    private class GalleryItemAdapter extends ArrayAdapter<GalleryItem>
    {
        public GalleryItemAdapter(List<GalleryItem> items)
        {
            super(getActivity(), 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null)
            {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.gallery_item, parent, false);
            }

            ImageView imageView = (ImageView)convertView.findViewById(R.id.gallery_item_imageView);
            imageView.setImageResource(R.drawable.mum_small);

            GalleryItem item = getItem(position);
            mThumbnailThread.queueThumbnail(imageView, item.getUrl());

            return convertView;
        }
    }

    private void setupAdapter()
    {
        if (getActivity() == null || mGridView == null)
        {
            return;
        }

        if (mItems != null)
        {
            /*mGridView.setAdapter(new ArrayAdapter<GalleryItem>(getActivity(),
                                                                android.R.layout.simple_gallery_item,
                                                                mItems));*/
            mGridView.setAdapter(new GalleryItemAdapter(mItems));
        }
        else
        {
            mGridView.setAdapter(null);
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mThumbnailThread.quit();

        Log.i(TAG, "Background thread destroyed");
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        mThumbnailThread.clearQueue();
    }
}
