package com.puddlesmanagment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MenuesActivity extends AppCompatActivity {

    ProgressDialog pDialog;
    JSONObject json;
    LinearLayout linearVerified;
    TextView txtMonthYear;
    TextView txtUserName;
    ImageView imgProfile;

    String month_year="";
    String image="";
    String verified_username="";

    SqliteController controller;
    ListView lv;
    public static int[] icons = {R.drawable.ic_gllry, R.drawable.ic_status, R.drawable.ic_sett, R.drawable.ic_cen};
    public static String[] mnuList = {"Send Picture","Query Status","ChangePassword", "Logout"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menues);
        setTitle("Menu");

        linearVerified=(LinearLayout)findViewById(R.id.linearVerified);
        txtMonthYear=(TextView) findViewById(R.id.txtMonthYear);
        txtUserName=(TextView) findViewById(R.id.txtUserName);
        imgProfile=(ImageView)findViewById(R.id.imgProfile);

        if(new ConnectionDetector(this).isConnectingToInternet()){
            new GetVerifiedUser().execute();
        }
        else{
            Toast.makeText(this,"Please Connect to Working Internet",Toast.LENGTH_SHORT).show();
        }

        controller = new SqliteController(getApplicationContext());
        CustomAdapterMenu customAdapterMenu = new CustomAdapterMenu(MenuesActivity.this, mnuList, icons);
        lv = (ListView) findViewById(R.id.list_menueitem);
        lv.setAdapter(customAdapterMenu);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (mnuList[position].equals("Send Picture")) {
                    //Toast.makeText(MenuesActivity.this, mnuList[position], Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(MenuesActivity.this, SendPicActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                } else if (mnuList[position].equals("Query Status")) {
                    //Toast.makeText(MenuesActivity.this, mnuList[position], Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(MenuesActivity.this, ViewQueryActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                } else if (mnuList[position].equals("ChangePassword")) {
                    //Toast.makeText(MenuesActivity.this, mnuList[position], Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(MenuesActivity.this, ChangePasswordActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                } else if (mnuList[position].equals("Logout")) {
                    //Toast.makeText(MenuesActivity.this, mnuList[position], Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(MenuesActivity.this, HomePage.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    controller.funLogout();
                    finish();
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    class GetVerifiedUser extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MenuesActivity.this);
            pDialog.setMessage("Processing.. Please Wait..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }
        protected String doInBackground(String...args){
            List<NameValuePair> params= new ArrayList<NameValuePair>();

            String url_profile_details = ServerConfig.serverurl + "getVerifiedUser.php";

            JSONParser jParser = new JSONParser();
            json = jParser.makeHttpRequest(url_profile_details, "POST", params);

            return null;
        }
        protected void onPostExecute(String jsonarry){
            super.onPostExecute(jsonarry);

            try{
                JSONArray user = json.getJSONArray("response");
                if(user.length()>0) {
                    linearVerified.setVisibility(View.VISIBLE);
                    JSONObject jb = user.getJSONObject(0);
                    month_year = jb.getString("month_year");
                    verified_username = jb.getString("name");
                    if(jb.getString("profileimage").equals("")){
                        image = ServerConfig.serverurl_profilepic + "nouser.jpg";
                    }
                    else {
                        image = ServerConfig.serverurl_profilepic + jb.getString("profileimage");
                    }

                    txtMonthYear.setText(month_year);
                    txtUserName.setText(verified_username);
                    Picasso.with(MenuesActivity.this).load(image).fit().into(imgProfile);
                }
                else{
                    linearVerified.setVisibility(View.GONE);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            pDialog.dismiss();
        }

    }
}


