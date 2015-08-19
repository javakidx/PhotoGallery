package com.bignerdranch.android.photogallery;

import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SearchView;

import java.util.ArrayList;
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

        //加search menu item
        setHasOptionsMenu(true);

//        new FetchItemsTask().execute();
        updateItems();

        //start service
        //Intent i = new Intent(getActivity(), PollService.class);
        //getActivity().startService(i);
        PollService.setServiceAlarm(getActivity(), true);

        //mThumbnailThread = new ThumbnailDownloader<>();
        mThumbnailThread = new ThumbnailDownloader<ImageView>(new Handler());

        mThumbnailThread.setListener(new ThumbnailDownloader.Listener<ImageView>()
        {
            @Override
            public void onThumbnailDownloaded(ImageView imageView, Bitmap bitmap)
            {
                if (isVisible())
                {
                    imageView.setImageBitmap(bitmap);
                }
            }
        });

        mThumbnailThread.start();
        mThumbnailThread.getLooper();
        Log.i(TAG, "Background thread started");
    }

    public void updateItems()
    {
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
//            String query = "android"; //for testing
            Activity activity = getActivity();

            if(activity == null)
            {
                return new ArrayList<>();
            }

            String query = PreferenceManager.getDefaultSharedPreferences(activity)
                            .getString(FlickrFetchr.PREF_SEARCH_QUERY, null);

            if(query != null)
            {
                return new FlickrFetchr().search(query);
            }
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // pull out the SearchView
            MenuItem searchItem = menu.findItem(R.id.menu_item_search);
            SearchView searchView = (SearchView)searchItem.getActionView();

            // get the data from our searchable.xml as a SearchableInfo
            SearchManager searchManager = (SearchManager)getActivity()
                    .getSystemService(Context.SEARCH_SERVICE);
            ComponentName name = getActivity().getComponentName();
            SearchableInfo searchInfo = searchManager.getSearchableInfo(name);

            searchView.setSearchableInfo(searchInfo);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_item_search:
                getActivity().onSearchRequested();
                return true;

            case R.id.menu_item_clear:
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .edit()
                        .putString(FlickrFetchr.PREF_SEARCH_QUERY, null)
                        .commit();
                updateItems();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
