package com.example.barandqrcodescanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;

public class ImageFullSlider extends AppCompatActivity {
    ImageView imgview;
    LinearLayout  fabShare, fabDelete;
    Animation FabOpen, FabClose, FabRClockWise, FabRAntiClockWise;
    boolean isOpen = false;
    AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_full_slider);

        imgview = findViewById(R.id.singleImageVieww);

        Intent intent = getIntent();
        final String imageAblsolutePath = intent.getStringExtra("single_photo_path");

        adView = findViewById(R.id.adviewww);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        final File imgFile = new File(imageAblsolutePath);
        if (imgFile.exists()) {

            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            imgview.setImageBitmap(myBitmap);


            fabShare = findViewById(R.id.share_btn);
            fabShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    shareImage(imageAblsolutePath);

                }
            });

            fabDelete = findViewById(R.id.del_btn);
            fabDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    delete_image(imageAblsolutePath);
                }
            });
        }

    }

    private void shareImage(String imageAblsolutePath) {
        Uri uri = FileProvider.getUriForFile(getApplicationContext(), "com.example.barandqrcodescanner.provider", new File(imageAblsolutePath));
        Intent share = ShareCompat.IntentBuilder.from(ImageFullSlider.this)
                .setStream(uri) // uri from FileProvider
                .setType("text/html")
                .getIntent()
                .setAction(Intent.ACTION_SEND) //Change if needed
                .setDataAndType(uri, "image/*")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(share, "Share Image"));
    }

    // Retrieving the url to share
    private Uri getmageToShare(Bitmap bitmap) {
        File imagefolder = new File(getCacheDir(), "images");
        Uri uri = null;
        try {
            imagefolder.mkdirs();
            File file = new File(imagefolder, "shared_image.png");
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
            outputStream.flush();
            outputStream.close();
            uri = FileProvider.getUriForFile(this, "com.example.barandqrcodescanner", file);
        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return uri;

    }
    public void delete_image(String path) {
        File file = new File(path);
        if (file.exists()) {
            if (file.delete()) {
                this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(path))));
                Intent intent = new Intent(ImageFullSlider.this, CreationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else {
                return;
            }
        }
    }
}