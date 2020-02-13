package com.puddlesmanagment;

import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {
    private ProgressDialog pDialog;
    private static String url_create_user = ServerConfig.serverurl + "addRegister.php";

    // JSON Node names
    //private static final String TAG_SUCCESS = "success";
    EditText name ,email ,mobileno ,password ,retype;
    Button btnsubmit;
    public static String Name = "";
    public static String MobileNo = "";
    public static String EmailID = "";
    public static String Password = "";
    public static String rePassword = "";

    Button btnSelectPhoto;

    private static final String IMAGE_DIRECTORY = "/POTHOLE";
    private int GALLERY = 1, CAMERA = 2;
    String uploadFilePath = "";
    String uploadFileName = "";
    Bitmap bitmap = null;
    String upLoadServerUri = ServerConfig.serverurl + "UploadProfileImage.php";
    ProgressDialog dialog = null;
    int serverResponseCode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle("Register");
       name = (EditText) findViewById(R.id.name);
        email = (EditText) findViewById(R.id.email);
        mobileno = (EditText) findViewById(R.id.mobileno);
        password = (EditText) findViewById(R.id.password);
        retype = (EditText) findViewById(R.id.retype);
        btnsubmit = (Button) findViewById(R.id.btnsubmit);

        btnSelectPhoto=(Button)findViewById(R.id.btnSelectPhoto);
        btnSelectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        btnsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Name = name.getText().toString().trim();
                MobileNo = mobileno.getText().toString().trim();
                EmailID = email.getText().toString().trim();
                Password = password.getText().toString().trim();
                rePassword = retype.getText().toString().trim();
                if (Name.length() <= 0) {
                    Toast.makeText(RegisterActivity.this, "Enter Name", Toast.LENGTH_SHORT).show();
                    name.requestFocus();
                } else if (EmailID.length() <= 0) {
                    Toast.makeText(RegisterActivity.this, "Enter Email ID", Toast.LENGTH_SHORT).show();
                    email.requestFocus();
                }
                else if (MobileNo.length() <= 0) {
                    Toast.makeText(RegisterActivity.this, "Enter Mobile No", Toast.LENGTH_SHORT).show();
                    mobileno.requestFocus();
                }
                else if (Password.length() <= 0) {
                    Toast.makeText(RegisterActivity.this, "Enter Password", Toast.LENGTH_SHORT).show();
                    password.requestFocus();
                }else if (rePassword.length() <= 0) {
                    Toast.makeText(RegisterActivity.this, "Retype  Password", Toast.LENGTH_SHORT).show();
                    retype.requestFocus();
                }
                else if(!Password.equals(rePassword)){
                        Toast.makeText(RegisterActivity.this, "Retype  Password does not Match!!", Toast.LENGTH_SHORT).show();
                        retype.requestFocus();
                }
                else{
                    if (new ConnectionDetector(RegisterActivity.this).isConnectingToInternet()) {
                        if(uploadFileName.equals("")){
                            Toast.makeText(RegisterActivity.this, "Please Select Image for Profile", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            dialog = ProgressDialog.show(RegisterActivity.this, "", "Uploading file...", true);

                            new Thread(new Runnable() {
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            //messageText.setText("uploading started.....");
                                        }
                                    });
                                    //uploadFile(glo_pic);
                                    uploadFile(uploadFilePath + "" + uploadFileName);
                                }
                            }).start();
                        }
                        //new CreateNewUser().execute();

                    } else {
                        Toast.makeText(RegisterActivity.this, "Please Connect to Internet", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void selectImage() {
        final CharSequence[] options = { "Take Photo","Choose from Gallery", "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo"))
                {
                    if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                        // only for gingerbread and newer versions
                        if (ContextCompat.checkSelfPermission(RegisterActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(RegisterActivity.this,new String[]{android.Manifest.permission.CAMERA},12);
                        } else {
                            takePhotoFromCamera();
                        }
                    }
                    else{
                        takePhotoFromCamera();
                    }
                }
                else if (options[item].equals("Choose from Gallery")) {
                    choosePhotoFromGallary();
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
                    if (new ConnectionDetector(RegisterActivity.this).isConnectingToInternet()) {
                        if (ContextCompat.checkSelfPermission(RegisterActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},13);
                        } else {
                            saveImage(bitmap);
                        }

                    } else {
                        Toast.makeText(RegisterActivity.this, "Please Connect to Working Internet", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } else if (requestCode == CAMERA) {
            bitmap = (Bitmap) data.getExtras().get("data");
            if (new ConnectionDetector(RegisterActivity.this).isConnectingToInternet()) {
                if (ContextCompat.checkSelfPermission(RegisterActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},13);
                } else {

                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    File wallpaperDirectory = new File(Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
                    // have the object build the directory structure, if needed.
                    if (!wallpaperDirectory.exists()) {
                        wallpaperDirectory.mkdirs();
                    }
                    saveImage(bitmap);
                }
            } else {
                Toast.makeText(RegisterActivity.this, "Please Connect to Working Internet", Toast.LENGTH_SHORT).show();
            }

        }
    }

    public String saveImage(Bitmap myBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
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
            Toast.makeText(RegisterActivity.this, e1.getMessage() + "", Toast.LENGTH_LONG).show();
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

            runOnUiThread(new Runnable() {
                public void run() {
                    //messageText.setText("Source File not exist :" +glo_pic);
                    Toast.makeText(RegisterActivity.this,"File Not Found",Toast.LENGTH_SHORT).show();
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
                            //messageText.setText(msg);
                            Toast.makeText(RegisterActivity.this, "File Upload Complete.",
                                    Toast.LENGTH_SHORT).show();

                            if (new ConnectionDetector(RegisterActivity.this).isConnectingToInternet()) {
                                new CreateNewUser().execute();

                            } else {
                                Toast.makeText(RegisterActivity.this, "Please Connect to Internet", Toast.LENGTH_SHORT).show();
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
                        //messageText.setText("MalformedURLException Exception : check script url.");
                        Toast.makeText(RegisterActivity.this, "MalformedURLException",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                dialog.dismiss();
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        //messageText.setText("Got Exception : see logcat ");
                        Toast.makeText(RegisterActivity.this, "Got Exception : see logcat ",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("UtoserverExcion","En:" + e.getMessage(), e);
            }
            dialog.dismiss();
            return serverResponseCode;
        } // End else block
    }

    class CreateNewUser extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RegisterActivity.this);
            pDialog.setMessage("Registering User.. Please Wait..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }
        protected String doInBackground(String... args) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("Name", Name));
            params.add(new BasicNameValuePair("MobileNo", MobileNo));
            params.add(new BasicNameValuePair("EmailID", EmailID));
            params.add(new BasicNameValuePair("Password", Password));
            params.add(new BasicNameValuePair("Image",uploadFileName));

            JSONParser jsonParser = new JSONParser();
            JSONObject json = jsonParser.makeHttpRequest(url_create_user, "POST", params);

            try {
                int success = json.getInt("success");
                final String message = json.getString("message");
                if (success == 1) {
                    Handler handler = new Handler(RegisterActivity.this.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();

                        }
                    });
                    Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(i);
                    finish();
                } else {

                    Handler handler = new Handler(RegisterActivity.this.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    });
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 12:{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhotoFromCamera();

                } else {
                }
                return;
            }
            case 13:{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveImage(bitmap);

                } else {
                }
                return;
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
