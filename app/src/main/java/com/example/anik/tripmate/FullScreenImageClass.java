package com.example.anik.tripmate;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class FullScreenImageClass extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_screen_image);

        Intent intent = getIntent();
        String i = intent.getStringExtra("image");

        ImageView img = (ImageView) findViewById(R.id.imageView);
        Glide.with(this).load(Uri.parse(i)).placeholder(R.drawable.userimage).into(img);
    }
}
