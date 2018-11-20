package com.example.anik.tripmate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.core.GeoHashQuery;

/**
 * Created by anik on 3/7/18.
 */

public class FullScreenViewActivity extends AppCompatActivity {

    //private GeoHashQuery.Utils utils;
    private FullScreenImageAdapter adapter;
    private ViewPager viewPager;
    Button deleteButtonToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_screen_image_view);

        viewPager = (ViewPager) findViewById(R.id.pager);

        Intent i = getIntent();
        int position = i.getIntExtra("position", 0);

        adapter = new FullScreenImageAdapter(FullScreenViewActivity.this,
                AlbumsAdapter.albumList);

        viewPager.setAdapter(adapter);

        // displaying selected image first
        viewPager.setCurrentItem(position);

        //int pos = viewPager.getCurrentItem();


    }
}