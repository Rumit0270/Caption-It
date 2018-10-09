package com.example.captionit;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
//import com.facebook.CallbackManager;
//import com.facebook.FacebookCallback;
//import com.facebook.FacebookException;
//import com.facebook.share.Sharer;
//import com.facebook.share.widget.ShareDialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BrowsePhoto extends AppCompatActivity {

   private static final String url = "http://192.168.43.100:8000/";
 //   private static final String url = "https://d2429ddc.ngrok.io";

    private RequestQueue requestQueue;
    private StringRequest stringRequest;

    private TextToSpeech toSpeech;
    private int result;

    private ProgressDialog progressDialog;

    private TextView resultTextView;


    private String caption;

    //image for sharing
    private Bitmap shareImage;


//    private CallbackManager callbackManager;
//    private ShareDialog shareDialog;


    // root content for screenshot
    private LinearLayout rootContent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_browse_photo);

        rootContent = (LinearLayout) findViewById(R.id.rootcontentBrowsePhoto);
        resultTextView = (TextView) findViewById(R.id.textView_browseResult);


        //invoke the image gallery using an implicit intent
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);

        //where do we want to find the data
        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = pictureDirectory.getPath();

        //finally get a URI representation
        Uri data = Uri.parse(pictureDirectoryPath);

        //set the data and type

        photoPickerIntent.setDataAndType(data, "image/*");
        startActivityForResult(photoPickerIntent, 95);

        toSpeech = new TextToSpeech(BrowsePhoto.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    result = toSpeech.setLanguage(Locale.US);
                } else {
                    Toast.makeText(getApplicationContext(), "Feature not supported in your device", Toast.LENGTH_SHORT).show();
                }
            }
        });


//        // initialize share dialog
//        callbackManager = CallbackManager.Factory.create();
//        shareDialog = new ShareDialog(this);
//
//        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
//            @Override
//            public void onSuccess(Sharer.Result result) {
//                Toast.makeText(BrowsePhoto.this, "Successful", Toast.LENGTH_LONG).show();
//            }
//
//            @Override
//            public void onCancel() {
//                Toast.makeText(BrowsePhoto.this, "Cancelled", Toast.LENGTH_LONG).show();
//            }
//
//            @Override
//            public void onError(FacebookException error) {
//                Toast.makeText(BrowsePhoto.this, error.getMessage(), Toast.LENGTH_LONG).show();
//            }
//        });

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

//        callbackManager.onActivityResult(requestCode, resultCode, data);
        ImageView imageView = (ImageView) findViewById(R.id.imageView_browseImage);

        if (resultCode == RESULT_OK) {

            if (requestCode == 95) {
                //the address of image in sd card
                Uri returnImageUri = data.getData();
                //declare a stream to read data form sd card
                InputStream inputStream;
                /* we are getting an input stream based on URI of the image */
                try {
                    inputStream = getContentResolver().openInputStream(returnImageUri);
                    //image is loaded to the program
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    imageView.setImageBitmap(bitmap);

                    //send this image for processing
                    sendRequestAndDisplayResponse(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    //show a message to the user indicating error
                    Toast.makeText(this, "Unable to open image", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            finish();
        }
    }

    private void sendRequestAndDisplayResponse(final Bitmap bitmap) {
        final TextView mTextView = (TextView) findViewById(R.id.textView_browseResult);
        requestQueue = Volley.newRequestQueue(this);

        final String encodedImage = getStringImage(bitmap);

        stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(final String response) {
                System.out.println(response);

                caption = response;
                progressDialog.dismiss();

                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                //render the label
                resultTextView.setText(response);
                resultTextView.startAnimation(animation);

                new CountDownTimer(5000, 1000) {
                    public void onFinish() {

                        speak(response);
                    }

                    public void onTick(long millisUntilFinished) {
                        // millisUntilFinished    The amount of time until finished.
                    }
                }.start();


                //speak(response);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bounce);

                resultTextView.setText("Error connecting to the server.");
                resultTextView.startAnimation(animation);

                speak("Error connecting to the server");


            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("image", encodedImage);
                //params.put("demo", "helloooo");
                return params;
            }
        };

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


    public void speakAgain(View view) {
        speak(caption);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (toSpeech != null) {
            toSpeech.stop();
            toSpeech.shutdown();
        }
    }

    //method to share image
    public void shareImage(View view) {
//        takeScreenShot();
//
//
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
//

        // take the screenshot
        Bitmap b = null;
        b = ScreenshotUtils.getScreenShot(rootContent);

        //If bitmap is not null
        if (b != null) {

            File saveFile = ScreenshotUtils.getMainDirectoryName(this);//get the path to save screenshot
            File file = ScreenshotUtils.store(b, "screenshot" + ".jpg", saveFile);//save the screenshot to selected path
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
