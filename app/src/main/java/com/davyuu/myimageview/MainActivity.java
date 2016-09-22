package com.davyuu.myimageview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MyImageView myImageView = (MyImageView)findViewById(R.id.myImageView);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cu_0001);
        myImageView.setImageBitmap(bitmap);
    }
}
