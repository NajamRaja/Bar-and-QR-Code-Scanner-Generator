package com.example.barandqrcodescanner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.Writer;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

public class BarCodeGenerator extends AppCompatActivity {
    private EditText editTextProductId, barcodeEditText;
    private LinearLayout buttonGenerate, saveBarcodeButton;

    private ImageView imageViewResult,back;
    private Bitmap generatedBarcode;

    private static final int REQUEST_PERMISSION = 1;
    Handler handler;
    Runnable runnable;
    int check_act = 0;
    int counter = 0;
    Context context;
    Bitmap cache = null;
    int main_w;
    int main_h;

    private AdView mAdView;
    private NativeAd nativeAd;
    ShimmerFrameLayout NativeShimmer;
    RelativeLayout Laynative;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_code_generator);
        initView();
    }

    private void initView() {
        editTextProductId = findViewById(R.id.editTextProductId);
        barcodeEditText = findViewById(R.id.barcode_text_edit_text);
        imageViewResult = findViewById(R.id.imageViewResult);
        buttonGenerate = findViewById(R.id.buttonGenerate);
        saveBarcodeButton = findViewById(R.id.save_barcode_button);
        back=findViewById(R.id.back);

        nativead();

        Laynative = findViewById(R.id.Laynative);
        NativeShimmer = findViewById(R.id.shimmer_container_Native);

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {

            NativeShimmer.setVisibility(View.VISIBLE);
            NativeShimmer.startShimmer();

        } else {

            NativeShimmer.setVisibility(View.GONE);
            NativeShimmer.stopShimmer();

        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        buttonGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateBarcode(view);
            }
        });

        saveBarcodeButton.setOnClickListener(v->{

            BarCodeGenerator.this.check_act = 6;

            String barcodeData = editTextProductId.getText().toString().trim();
            if (TextUtils.isEmpty(barcodeData)) {
                Toast.makeText(this, "Generate the Barcode First", Toast.LENGTH_SHORT).show();
                return;
            }
            else
            {
                BarCodeGenerator.this.imageViewResult.setDrawingCacheEnabled(false);
                BarCodeGenerator.this.imageViewResult.setDrawingCacheEnabled(true);
                BarCodeGenerator.this.imageViewResult.buildDrawingCache();
                BarCodeGenerator.this.cache = BarCodeGenerator.this.imageViewResult.getDrawingCache();
                BarCodeGenerator.this.main_w = BarCodeGenerator.this.imageViewResult.getMeasuredWidth();
                BarCodeGenerator.this.main_h = BarCodeGenerator.this.imageViewResult.getMeasuredHeight();
                Bitmap viewCache = imageViewResult.getDrawingCache();
                Bitmap bitmap = viewCache.copy(viewCache.getConfig(), false);
                new BarCodeGenerator.SaveTask(false).execute(bitmap);
            }

            // saveBarcode();
        });

        this.handler = new Handler();
        this.runnable = new Runnable() {
            public void run() {
                BarCodeGenerator.this.handler.postDelayed(this, 10);
                BarCodeGenerator.this.counter++;
                if (BarCodeGenerator.this.counter == 12) {
                    BarCodeGenerator.this.handler.removeCallbacks(BarCodeGenerator.this.runnable);
                    BarCodeGenerator.this.counter = 0;

                    switch (BarCodeGenerator.this.check_act) {

                        case 6:
                            BarCodeGenerator.this.finish();
                            break;

                    }
                    BarCodeGenerator.this.check_act = 0;
                }
            }
        };

    }

    private void generateBarcode(View view) {
        try {
            String productId = editTextProductId.getText().toString();
            String barcodeText = barcodeEditText.getText().toString();

            if (!productId.isEmpty()) {
                Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<>();
                hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

                Writer codeWriter = new Code128Writer();
                BitMatrix byteMatrix = codeWriter.encode(productId, BarcodeFormat.CODE_128, 400, 200, hintMap);
                int width = byteMatrix.getWidth();
                int height = byteMatrix.getHeight();
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        bitmap.setPixel(i, j, byteMatrix.get(i, j) ? Color.BLACK : Color.WHITE);
                    }
                }

                generatedBarcode = bitmap;
                imageViewResult.setImageBitmap(generatedBarcode);
            } else {
                Toast.makeText(getApplicationContext(), "Enter barcode text", Toast.LENGTH_SHORT).show();
            }
        } catch (WriterException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private Uri getImageUri(String fileName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use MediaStore API for Android 10 and above
            ContentResolver contentResolver = getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

            return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        }
        else
        {
            // Fallback to the deprecated method for Android 9 and below
            String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
            File imageFile = new File(imagesDir, fileName);

            return Uri.fromFile(imageFile);
        }
    }

    private class SaveTask extends AsyncTask<Bitmap, Void, File> {
        private boolean isShare;
        private ProgressDialog mProgressDialog;

        public SaveTask(boolean b) {
            this.isShare = b;
        }

        protected void onPreExecute() {
            this.mProgressDialog = new ProgressDialog(BarCodeGenerator.this);
            this.mProgressDialog.setMessage(BarCodeGenerator.this.getString(R.string.saving));
            this.mProgressDialog.setIndeterminate(true);
            this.mProgressDialog.show();
        }

        protected void onPostExecute(final File result) {
            this.mProgressDialog.dismiss();
            if (result == null) {
                return;
            }
            if (this.isShare) {
                Intent sendIntent = new Intent("android.intent.action.SEND");
                sendIntent.putExtra("android.intent.extra.STREAM", Uri.fromFile(new File(result.getAbsolutePath())));
                sendIntent.setType("image/png*");
                BarCodeGenerator.this.startActivity(Intent.createChooser(sendIntent, "Share via"));
                return;
            }

            Snackbar.make(findViewById(android.R.id.content), BarCodeGenerator.this.getString(R.string.paint_saved), Snackbar.LENGTH_LONG).setAction("View", new View.OnClickListener() {
                public void onClick(View v) {
                    BarCodeGenerator.this.startActivity(new Intent(BarCodeGenerator.this, ImageFullSlider.class).putExtra("single_photo_path", result.getAbsolutePath()));
                }
            }).show();
        }

        protected File doInBackground(Bitmap... params) {
            Throwable th;
            File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Bar and QR Codes Scanner");
//            File folder = new File(Environment.getExternalStorageDirectory().toString() + File.separator + AppConstance.DIRECTORY_NAME);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            File result = new File(folder.toString(), new SimpleDateFormat("'QR'yyyy-MM-dd_HH-mm-ss.S'.png'").format(new Date()));
            FileOutputStream stream = null;
            try {
                FileOutputStream stream2 = new FileOutputStream(result);
                try {
                    if (params[0].compress(Bitmap.CompressFormat.PNG, 100, stream2)) {
                        BarCodeGenerator.this.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.fromFile(result)));
                    } else {
                        result = null;
                    }
                    if (stream2 != null) {
                        try {
                            stream2.close();
                        } catch (IOException e) {
                            stream = stream2;
                            result = null;
                            Thread.sleep(1000);
                            return result;
                        }
                    }
                    stream = stream2;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e2) {
                    }
                    return result;
                } catch (Throwable th2) {
                    th = th2;
                    stream = stream2;
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e3) {
                            result = null;
                            Thread.sleep(1000);
                            return result;
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                if (stream != null) {

                    try {
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                try {
                    throw th;
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
            return result;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Bitmap viewCache = imageViewResult.getDrawingCache();
                Bitmap bitmap = viewCache.copy(viewCache.getConfig(), false);
                new BarCodeGenerator.SaveTask(false).execute(bitmap);

               // saveImageToGallery(generatedBarcode);
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Permission denied. Cannot save barcode", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public  void populateNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        MediaView mediaView = adView.findViewById(R.id.ad_media);
        adView.setMediaView(mediaView);
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }
        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }
        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }
        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }
        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }
        if (nativeAd.getStarRating() == null || nativeAd.getStarRating() < 3) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }
        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }
        adView.setNativeAd(nativeAd);
    }
    private void nativead() {
        AdLoader.Builder builder = new AdLoader.Builder(this, getString(R.string.native_id));
        builder.forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
            @Override
            public void onNativeAdLoaded(NativeAd unifiedNativeAd) {

                if (nativeAd != null) {
                    nativeAd.destroy();
                }
                nativeAd = unifiedNativeAd;
                FrameLayout frameLayout = findViewById(R.id.fl_adplaceholderMain);
                @SuppressLint("InflateParams") NativeAdView adView = (NativeAdView) getLayoutInflater().inflate(R.layout.mediation_native_ad, null);

                populateNativeAdView(unifiedNativeAd, adView);
                frameLayout.removeAllViews();
                frameLayout.addView(adView);


                NativeShimmer.setVisibility(View.GONE);
                NativeShimmer.stopShimmer();

                frameLayout.setVisibility(View.VISIBLE);
            }
        }).build();
        NativeAdOptions adOptions = new NativeAdOptions.Builder().build();
        builder.withNativeAdOptions(adOptions);
        AdLoader adLoader = builder.withAdListener(new AdListener() {
            public void onAdFailedToLoad(int i) {

            }
        }).build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }
}
