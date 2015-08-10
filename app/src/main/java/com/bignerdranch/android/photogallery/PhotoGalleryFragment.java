package com.bignerdranch.android.photogallery;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;

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

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        new FetchItemsTask().execute();
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

    private void setupAdapter()
    {
        if (getActivity() == null || mGridView == null)
        {
            return;
        }

        if (mItems != null)
        {
            mGridView.setAdapter(new ArrayAdapter<GalleryItem>(getActivity(),
                                                                android.R.layout.simple_gallery_item,
                                                                mItems));
        }
        else
        {
            mGridView.setAdapter(null);
        }
    }
}
