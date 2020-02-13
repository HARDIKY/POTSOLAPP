package com.puddlesmanagment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
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

public class QueryDetailActivity extends AppCompatActivity {

    ProgressDialog pDialog;
    JSONObject json;
    SqliteController sqliteController;
    String status,img,Username;
    ImageView pic,updatedimg;
    TextView qurystatus,updatedstatus;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_detail);
        sqliteController=new SqliteController(this);
        Username=sqliteController.returnUsername();
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        status = bundle.getString("status");
        img = bundle.getString("img");

        pic = (ImageView) findViewById(R.id.pic);
        updatedimg = (ImageView) findViewById(R.id.updatedimg);

        qurystatus = (TextView) findViewById(R.id.qurystatus);
        updatedstatus = (TextView) findViewById(R.id.updatedstatus);


       // pic.setImageBitmap(ServerConfig.serverurl + "uploads/" +img);
        qurystatus.setText(status);
        updatedstatus.setText("After");
        if (new ConnectionDetector(this).isConnectingToInternet()) {
            new GetUpdates().execute();

        } else {
            Toast.makeText(this, "Please Connect to Internet", Toast.LENGTH_SHORT).show();
        }

    }
    class GetUpdates extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(QueryDetailActivity.this);
            pDialog.setMessage("Processing.. Please Wait..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }
        protected String doInBackground(String...args){
            List<NameValuePair> params= new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("emailid", Username));
            params.add(new BasicNameValuePair("pic", img));
            String url_profile_details = ServerConfig.serverurl + "getUpdetedresults.php";

            JSONParser jParser = new JSONParser();
            json = jParser.makeHttpRequest(url_profile_details, "POST", params);

            String jsondata=null;
            Log.d("doInBackground", "start");

            return jsondata;
        }
        protected void onPostExecute(String jsonarry){
            super.onPostExecute(jsonarry);
            // dismiss the dialog after getting all products

            Log.d("onPostExecute", "start");

            try{
                Log.d("onPostExecute try", "start");
                JSONArray user = json.getJSONArray("response");

                JSONObject jb= user.getJSONObject(0);
                String Id = jb.getString("ID");
                String updatepic = jb.getString("UpdatedPhoto");
                String imageUrls = ServerConfig.serverurlupdatepic +updatepic;
                Picasso.with(QueryDetailActivity.this).load(imageUrls).fit().into(updatedimg);
                Picasso.with(QueryDetailActivity.this).load(ServerConfig.serverurl+"uploads/"+img).into(pic);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            pDialog.dismiss();
        }

    }
}
