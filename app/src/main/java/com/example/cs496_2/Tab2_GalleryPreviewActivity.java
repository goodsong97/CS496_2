package com.example.cs496_2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;

public class Tab2_GalleryPreviewActivity extends AppCompatActivity {

    ImageView GalleryPreviewImg;
    String path;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.tab2_gallery_preview);
        Intent intent = getIntent();
        path = intent.getStringExtra("path");
        GalleryPreviewImg = (ImageView) findViewById(R.id.tab2_GP_zoomageview);
        Glide.with(Tab2_GalleryPreviewActivity.this)
                .load(new File(path)) // Uri of the picture
                .into(GalleryPreviewImg);
    }
}