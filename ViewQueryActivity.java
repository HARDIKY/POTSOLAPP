package com.puddlesmanagment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ViewQueryActivity extends AppCompatActivity {
    ListView list;
    CustomAdapterViewQuery adapter;
    private static String url_list = ServerConfig.serverurl + "getQueryList.php";

    public static String lid[];
    public static String licon[];
    public static String laddress[];
    public static String lstatus[];
    private ProgressDialog pDialog;
    public static String timePeriod[];

    SqliteController sqliteController;
    String Username,Status;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_query);
        setTitle("your queries");
        list = (ListView) findViewById(R.id.list);
        sqliteController=new SqliteController(ViewQueryActivity.this);
        Username=sqliteController.returnUsername();
        if (new ConnectionDetector(this).isConnectingToInternet()) {
            new GetMyQueryList().execute();

        } else {
            Toast.makeText(this, "Please Connect to Internet", Toast.LENGTH_SHORT).show();
        }
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //Status = list.getItemAtPosition(list.getSelectedItemPosition()).toString();
                String status = lstatus[position];
                if(status.equalsIgnoreCase("Pending")){
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ViewQueryActivity.this);
                    alertDialogBuilder.setMessage("Current status of your query is Pending!!");
                    alertDialogBuilder.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    //Toast.makeText(MainActivity.this,"You clicked yes button",Toast.LENGTH_LONG).show();
                                   // finish();
                                }
                            });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
                else{
                    String img =licon[position];
                    Intent intent=new Intent(ViewQueryActivity.this,QueryDetailActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("status", "Before");
                    intent.putExtra("img", img);
                    startActivity(intent);
                }
            }
        });
    }
    class GetMyQueryList extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ViewQueryActivity.this);
            pDialog.setMessage("Processing.. Please Wait..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... args) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("emailid", Username));
            JSONParser jParser = new JSONParser();
            JSONObject json = jParser.makeHttpRequest(url_list, "POST", params);

            try {
                JSONArray mylist = json.getJSONArray("response");

                lid = new String[mylist.length()];
                licon = new String[mylist.length()];
                laddress=new String[mylist.length()];
                lstatus = new String[mylist.length()];
                timePeriod = new String[mylist.length()];
                for (int i = 0; i < mylist.length(); i++) {
                    JSONObject jsonObject = mylist.getJSONObject(i);

                    lid[i] = jsonObject.getString("ID");
                    licon[i] = jsonObject.getString("Photo");
                    laddress[i] = jsonObject.getString("Address");
                    lstatus[i] = jsonObject.getString("Status");
                    timePeriod[i]=jsonObject.getString("timeperiod");

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String file_url) {
            if (licon.length > 0) {
                adapter = new CustomAdapterViewQuery(ViewQueryActivity.this,  licon, laddress,lstatus,timePeriod);
               // Log.d("imageuri", "" + imageUrls);
                list.setAdapter(adapter);
            }
            pDialog.dismiss();
        }

    }
}
