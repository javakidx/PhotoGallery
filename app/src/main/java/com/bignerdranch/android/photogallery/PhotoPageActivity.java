package com.bignerdranch.android.photogallery;

import android.support.v4.app.Fragment;

/**
 * Created by bioyang on 15/8/25.
 */
public class PhotoPageActivity extends SingleFragmentActivity
{
    @Override
    protected Fragment createFragment()
    {
        return new PhotoPageFragment();
    }
}
