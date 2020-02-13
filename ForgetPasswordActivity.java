package com.puddlesmanagment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ForgetPasswordActivity extends AppCompatActivity {
    private ProgressDialog pDialog;
    JSONParser jsonParser = new JSONParser();
    private static String url_forgetpassword = ServerConfig.serverurl + "forgetPassword.php";
    EditText editEmailMobile;
    public static String EmailID = "";
    Button butnfSubmit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        editEmailMobile = (EditText) findViewById(R.id.editEmailMobile);
        butnfSubmit = (Button) findViewById(R.id.butnfSubmit);
        butnfSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EmailID = editEmailMobile.getText().toString().trim();

                if (EmailID.length() <= 0) {
                    Toast.makeText(ForgetPasswordActivity.this, "Enter Email ID OR Mobile Number", Toast.LENGTH_SHORT).show();
                    editEmailMobile.requestFocus();
                }
                else {
                    if (new ConnectionDetector(ForgetPasswordActivity.this).isConnectingToInternet()) {
                        if (new ConnectionDetector(ForgetPasswordActivity.this).isConnectingToInternet()) {
                            new SendMail().execute();

                        } else {
                            Toast.makeText(ForgetPasswordActivity.this, "Please Connect to Internet", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(ForgetPasswordActivity.this, "Please Connect to Internet", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

    }
    @Override
    public void onStart() {
        super.onStart();
    }
    class SendMail extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ForgetPasswordActivity.this);
            pDialog.setMessage("Please Wait..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }
        protected String doInBackground(String... args) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("EmailID", EmailID));
            JSONObject json = jsonParser.makeHttpRequest(url_forgetpassword, "POST", params);
            try {
                int success = json.getInt("success");
                final String message = json.getString("message");
                if (success == 1) {
                    Handler handler = new Handler(ForgetPasswordActivity.this.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(ForgetPasswordActivity.this, message, Toast.LENGTH_SHORT).show();

                        }
                    });
                    Intent i = new Intent(ForgetPasswordActivity.this, LoginActivity.class);
                    startActivity(i);
                    finish();
                } else {
                    Handler handler = new Handler(ForgetPasswordActivity.this.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(ForgetPasswordActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
