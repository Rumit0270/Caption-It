package com.example.captionit;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.TestLooperManager;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
//import com.facebook.CallbackManager;
//import com.facebook.FacebookCallback;
//import com.facebook.FacebookException;
//import com.facebook.share.Sharer;
//import com.facebook.share.model.SharePhoto;
//import com.facebook.share.model.SharePhotoContent;
//import com.facebook.share.widget.ShareDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TakePhoto extends AppCompatActivity {

    public static final int REQUEST_CODE = 66;

    private static final String url = "http://192.168.43.100:8000/";
    //private static  final String url = "https://d2429ddc.ngrok.io";

    private RequestQueue requestQueue;
    private StringRequest stringRequest;

    private TextView resultTextView;

    private TextToSpeech toSpeech;
    private int result;

    private ProgressDialog progressDialog;

    private String caption;

    //image for sharing
    private Bitmap shareImage;

//    private CallbackManager callbackManager;
//    private ShareDialog shareDialog;

    // for screenshot
    private LinearLayout rootContent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_take_photo);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CODE);

        resultTextView = (TextView) findViewById(R.id.textView_cameraResult);
        rootContent = (LinearLayout) findViewById(R.id.rootcontentTakePhoto);

        toSpeech = new TextToSpeech(TakePhoto.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    result = toSpeech.setLanguage(Locale.US);
                } else {
                    Toast.makeText(getApplicationContext(), "Feature not supported in your device", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // initialize share dialog
//        callbackManager = CallbackManager.Factory.create();
//        shareDialog = new ShareDialog(this);

    }

    public void speak(String label) {
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Toast.makeText(getApplicationContext(), "Feature not supported", Toast.LENGTH_SHORT).show();
        } else {
            toSpeech.speak(label, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {

            if(requestCode == REQUEST_CODE) {

                Bitmap bitmap = (Bitmap)data.getExtras().get("data");
                ImageView cameraImageView = (ImageView) findViewById(R.id.imageView_cameraImage);
                cameraImageView.setImageBitmap(bitmap);


                // assign the image to shareimage
                shareImage = bitmap;


                /// send this image to the server for processing
                sendRequestAndDisplayResponse(bitmap);

            }
        } else {
            finish();
        }

    }

    private void sendRequestAndDisplayResponse(final Bitmap bitmap) {
        requestQueue = Volley.newRequestQueue(this);

        final String encodedImage = getStringImage(bitmap);

        stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(final String response) {
                progressDialog.dismiss();
                caption = response;

                System.out.println(response);
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                //render the label
                resultTextView.setText(response);
                resultTextView.startAnimation(animation);


                // convert text to speech
                new CountDownTimer(5000, 1000) {
                    public void onFinish() {

                        speak(response);
                    }

                    public void onTick(long millisUntilFinished) {
                        // millisUntilFinished    The amount of time until finished.
                    }
                }.start();


//                speak("Hello world");

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();

                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bounce);

                resultTextView.setText("Error connecting to the server.");
                resultTextView.startAnimation(animation);
                speak("Error connecting to the server.");
            }
        } ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("image", encodedImage);
                //params.put("demo", "helloooo");
                return params;
            }
        } ;

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(30 * 1000, 1, 1.0f));

        requestQueue.add(stringRequest);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Processing");
        progressDialog.setMessage("It may take a while...");
        progressDialog.setCancelable(false);
        progressDialog.show();

    }

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;

    }

    public void takePhotoAgain(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        resultTextView.setText("");
        startActivityForResult(intent, REQUEST_CODE);
    }

    public  void speakAgain(View view) {
        speak(caption);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(toSpeech != null) {
            toSpeech.stop();
            toSpeech.shutdown();
        }
    }


    public void shareImage(View view) {

//        takeScreenShot();
//        SharePhoto photo = new SharePhoto.Builder()
//                .setBitmap(shareImage)
//                .build();
//
//        if(ShareDialog.canShow(SharePhotoContent.class)) {
//            SharePhotoContent content = new SharePhotoContent.Builder()
//                    .addPhoto(photo)
//                    .build();
//            shareDialog.show(content);
//        }

        Bitmap b = null;
        b =ScreenshotUtils.getScreenShot(rootContent);

        //If bitmap is not null
        if (b != null) {

            File saveFile = ScreenshotUtils.getMainDirectoryName(this);//get the path to save screenshot
            File file = ScreenshotUtils.store(b, "screenshot" +  ".jpg", saveFile);//save the screenshot to selected path
            shareScreenshot(file);//finally share screenshot
        }
    }


    private void shareScreenshot(File file) {
        Uri uri = Uri.fromFile(file);//Convert file path into Uri for sharing
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.sharing_text));
        intent.putExtra(Intent.EXTRA_STREAM, uri);//pass uri here
        startActivity(Intent.createChooser(intent, getString(R.string.share_title)));
    }



    //method to take screenshot

//    public void takeScreenShot() {
//        View view = this.getWindow().getDecorView();
//        view.setDrawingCacheEnabled(true);
//        view.buildDrawingCache();
//        Bitmap b1 = view.getDrawingCache();
//        Rect frame = new Rect();
//        this.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
//        int statusBarHeight = frame.top;
//
//        //Find the screen dimensions to create bitmap in the same size.
//        int width = this.getWindowManager().getDefaultDisplay().getWidth();
//        int height = this.getWindowManager().getDefaultDisplay().getHeight();
//
//        Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height - statusBarHeight);
//        view.destroyDrawingCache();
//        shareImage = b;
//    }
}
