package com.puddlesmanagment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SendPicActivity extends AppCompatActivity {
    private ProgressDialog pDialog;
    private static final String TAG_SUCCESS = "success";
    JSONParser jsonParser = new JSONParser();
    StringBuilder Addressresult;
    Button b;
    String glo_pic;
    String filename;
    //int serverResponseCode=0;
    SqliteController controller;
    String username="";
    TextView messageText;
    Button uploadButton;
    int serverResponseCode = 0;
    ProgressDialog dialog = null;

    String upLoadServerUri = null;

    //String url_upload = ServerConfig.serverurl + "uploadImage.php";


    //Button btnShowLocation;
    // GPSTracker class
    GPSTracker gps;
    Context mContext;
    double latitude,longitude;

    private static final String IMAGE_DIRECTORY = "/POTHOLE";
    private int GALLERY = 1, CAMERA = 2;
    String uploadFilePath = "";
    String uploadFileName = "";
    Bitmap bitmap = null;

    RadioGroup rdnGroup;
    String pothole_length="";

    /*final String uploadFilePath = "/storage/3036-6436/WPSystem/SharedData/MediaMetaFiles/DeviceMediaStore/Art/";
    final String uploadFileName = "03000a0e.jpg";*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_pic);

        controller=new SqliteController(this);
        username=controller.returnUsername();

        b=(Button)findViewById(R.id.btnSelectPhoto);
        rdnGroup=(RadioGroup)findViewById(R.id.rdngrp);

        rdnGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(radioGroup.getCheckedRadioButtonId()==R.id.rdnsixandbelow){
                    pothole_length="Less than 6 inch";
                }
                else if(radioGroup.getCheckedRadioButtonId()==R.id.rdnbetweensixandtwelve){
                    pothole_length="Between 6 inch and 12 inch";
                }
                else if(radioGroup.getCheckedRadioButtonId()==R.id.rdnabovetwelve){
                    pothole_length="Above 12 inch";
                }
            }
        });

        //viewImage=(ImageView)findViewById(R.id.viewImage);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(pothole_length.equals("")) {
                    final AlertDialog alertDialog=new AlertDialog.Builder(SendPicActivity.this).create();
                    alertDialog.setMessage("Please Select Length of Pot Hole");
                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            alertDialog.dismiss();
                        }
                    });
                    alertDialog.show();
                }
                else {
                    selectImage();
                }
            }
        });
        //GPS GETTING
        mContext = this;

        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SendPicActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        } else {
            gps = new GPSTracker(mContext, SendPicActivity.this);

            // Check if GPS enabled
            if (gps.canGetLocation()) {

                 latitude = gps.getLatitude();
                 longitude = gps.getLongitude();
                getAddress(latitude,longitude);
                //getAddress(19.2276576,72.8579102);
            } else {
                // Can't get location.
                // GPS or network is not enabled.
                // Ask user to enable GPS/network in settings.
                gps.showSettingsAlert();
            }
        }
        //GPS GETTING
        uploadButton = (Button)findViewById(R.id.uploadButton);
        messageText  = (TextView)findViewById(R.id.messageText);
        //messageText.setText("Uploading file path :-"+glo_pic);

        /************* Php script path ****************/
        upLoadServerUri = ServerConfig.serverurl + "UploadToServer.php";
        uploadButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog = ProgressDialog.show(SendPicActivity.this, "", "Uploading file...", true);

                new Thread(new Runnable() {
                    public void run() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                messageText.setText("uploading started.....");
                            }
                        });
                        //uploadFile(glo_pic);
                        uploadFile(uploadFilePath+""+uploadFileName);
                    }
                }).start();
            }
        });
    }
    private String getAddress(double latitude, double longitude) {
        Addressresult = new StringBuilder();
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);

                Addressresult.append(address.getAddressLine(0)).append("\n");
                Addressresult.append(address.getPostalCode()).append("\n");
                Addressresult.append(address.getSubLocality()).append("\n");
                Addressresult.append(address.getCountryCode()).append("\n");
                Addressresult.append(address.getCountryName());
            }
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }
        //Log.d("sameen " , String.valueOf(Addressresult));
        return Addressresult.toString();

    }

/* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds options to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }*/
@Override
public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    switch (requestCode) {
        case 1: {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was granted, yay! Do the

                // contacts-related task you need to do.

                gps = new GPSTracker(mContext, SendPicActivity.this);

                // Check if GPS enabled
                if (gps.canGetLocation()) {

                     latitude = gps.getLatitude();
                     longitude = gps.getLongitude();

                    // \n is for new line
                    //Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                } else {
                    // Can't get location.
                    // GPS or network is not enabled.
                    // Ask user to enable GPS/network in settings.
                    gps.showSettingsAlert();
                }

            } else {

                // permission denied, boo! Disable the
                // functionality that depends on this permission.

                //Toast.makeText(mContext, "You need to grant permission", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        case 12:{
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                takePhotoFromCamera();
                //Toast.makeText(getActivity(),"Permission Granted for Camera.Please Select Farm Photo",Toast.LENGTH_SHORT).show();

            } else {

                // permission denied, boo! Disable the
                // functionality that depends on this permission.
            }
            return;
        }
        case 13:{
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                saveImage(bitmap);
                //Toast.makeText(getActivity(),"Permission Granted for Camera.Please Select Farm Photo",Toast.LENGTH_SHORT).show();

            } else {

                // permission denied, boo! Disable the
                // functionality that depends on this permission.
            }
            return;
        }
    }
}
    private void selectImage() {
        final CharSequence[] options = { "Take Photo","Choose from Gallery", "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(SendPicActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo"))
                {
                    if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                        // only for gingerbread and newer versions
                        if (ContextCompat.checkSelfPermission(SendPicActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(SendPicActivity.this,new String[]{Manifest.permission.CAMERA},12);
                        } else {
                            takePhotoFromCamera();
                        }
                    }
                    else{
                        takePhotoFromCamera();
                    }

                    /*Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(Environment.getExternalStorageDirectory(), "temp.jpg");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(intent, 1);*/
                }
                else if (options[item].equals("Choose from Gallery"))
                {

                    choosePhotoFromGallary();
                    //Intent intent = new   Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    //startActivityForResult(intent, 2);

                }
                else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    public void choosePhotoFromGallary() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(galleryIntent, GALLERY);
    }

    private void takePhotoFromCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                Uri contentURI = data.getData();

                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), contentURI);
                    //String path = saveImage(bitmap);
                    if (new ConnectionDetector(SendPicActivity.this).isConnectingToInternet()) {
                        if (ContextCompat.checkSelfPermission(SendPicActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                           ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},13);
                        } else {
                            saveImage(bitmap);
                        }

                    } else {
                        Toast.makeText(SendPicActivity.this, "Please Connect to Working Internet", Toast.LENGTH_SHORT).show();
                    }
                    //imgUserImage.setImageBitmap(bitmap);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } else if (requestCode == CAMERA) {
            bitmap = (Bitmap) data.getExtras().get("data");

            //imgUserImage.setImageBitmap(bitmap);
            if (new ConnectionDetector(SendPicActivity.this).isConnectingToInternet()) {
                if (ContextCompat.checkSelfPermission(SendPicActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},13);
                } else {

                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    //myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    File wallpaperDirectory = new File(Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
                    // have the object build the directory structure, if needed.
                    if (!wallpaperDirectory.exists()) {
                        wallpaperDirectory.mkdirs();
                    }

                    saveImage(bitmap);
                }
            } else {
                Toast.makeText(SendPicActivity.this, "Please Connect to Working Internet", Toast.LENGTH_SHORT).show();
            }

        }
    }

    public String saveImage(Bitmap myBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        //myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        File wallpaperDirectory = new File(Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        // have the object build the directory structure, if needed.
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs();
        }
        try {
            String flname=""+ Calendar.getInstance().getTimeInMillis();
            File f = new File(wallpaperDirectory, flname + ".jpg");
            wallpaperDirectory.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            fo.close();

            String temp1 = f.getAbsolutePath();
            uploadFilePath = temp1.substring(0, temp1.lastIndexOf("/")) + "/";
            uploadFileName = temp1.substring(temp1.lastIndexOf("/") + 1);

            return f.getAbsolutePath();
        } catch (IOException e1) {
            Toast.makeText(SendPicActivity.this, e1.getMessage() + "", Toast.LENGTH_LONG).show();
        }
        return "";
    }

    public int uploadFile(String sourceFileUri) {
        String fileName = sourceFileUri;
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);

        if (!sourceFile.isFile()) {

            dialog.dismiss();

            Log.e("uploadFile", "Source File not exist :"
                    +glo_pic);

            runOnUiThread(new Runnable() {
                public void run() {
                    messageText.setText("Source File not exist :" +glo_pic);
                }
            });

            return 0;

        }
        else
        {
            try {
                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);
                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);
                dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name='uploaded_file';filename='"
                        + fileName + "'" + lineEnd);
                dos.writeBytes(lineEnd);
                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];
                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);
                if(serverResponseCode == 200){
                    runOnUiThread(new Runnable() {
                        public void run() {
                            String msg = "File Upload Completed.";
                            messageText.setText(msg);
                            Toast.makeText(SendPicActivity.this, "File Upload Complete.",
                                    Toast.LENGTH_SHORT).show();

                            if (new ConnectionDetector(SendPicActivity.this).isConnectingToInternet()) {
                                new Uploadfilename().execute();

                            } else {
                                Toast.makeText(SendPicActivity.this, "Please Connect to Internet", Toast.LENGTH_SHORT).show();
                            }

                        }

                    });

                }
                fileInputStream.close();
                dos.flush();
                dos.close();
            } catch (MalformedURLException ex) {

                dialog.dismiss();
                ex.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        messageText.setText("MalformedURLException Exception : check script url.");
                        Toast.makeText(SendPicActivity.this, "MalformedURLException",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                dialog.dismiss();
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        messageText.setText("Got Exception : see logcat ");
                        Toast.makeText(SendPicActivity.this, "Got Exception : see logcat ",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("UtoserverExcion","En:" + e.getMessage(), e);
            }
            dialog.dismiss();
            return serverResponseCode;
        } // End else block
    }
    class Uploadfilename extends AsyncTask<String,String ,String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SendPicActivity.this);
            pDialog.setMessage("Uploading To Server.. Please Wait..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... args) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            String temp=uploadFilePath+""+uploadFileName;
            filename=temp.substring(temp.lastIndexOf("/")+1);
            params.add(new BasicNameValuePair("path",filename));
            params.add(new BasicNameValuePair("email",username));
            params.add(new BasicNameValuePair("latitude",""+latitude));
            params.add(new BasicNameValuePair("longitude",""+longitude));
            params.add(new BasicNameValuePair("address",""+Addressresult));
            params.add(new BasicNameValuePair("potholesize",""+pothole_length));
            //Log.d("sameen param",""+params);

            String url= ServerConfig.serverurl + "uploadPhoto.php";
            JSONObject json = jsonParser.makeHttpRequest(url, "POST", params);
            Log.d("Create Response", json.toString());
            try {
                int success = json.getInt(TAG_SUCCESS);
                final String message = json.getString("message");
                if (success == 1) {
                    Handler handler = new Handler(SendPicActivity.this.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(SendPicActivity.this, message, Toast.LENGTH_SHORT).show();
                            //sqliteController.funLogout();
                            Intent i = new Intent(SendPicActivity.this,HomePage.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            finish();
                        }
                    });
                } else {
                    Toast.makeText(SendPicActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            pDialog.dismiss();
        }
    }
    public boolean isPermissionGranted() {

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("TAG", "Permission is granted");
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.CAMERA
                }, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("TAG", "Permission is granted");
            return true;
        }

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
