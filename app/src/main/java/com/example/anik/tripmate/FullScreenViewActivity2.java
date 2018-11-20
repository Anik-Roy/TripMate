package com.example.anik.tripmate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

/**
 * Created by anik on 3/7/18.
 */

public class FullScreenViewActivity2 extends AppCompatActivity {

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

        adapter = new FullScreenImageAdapter(FullScreenViewActivity2.this,
                ViewPagerAdapter.images);

        viewPager.setAdapter(adapter);

        // displaying selected image first
        viewPager.setCurrentItem(position);

        //int pos = viewPager.getCurrentItem();
    }
}
