package com.puddlesmanagment;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class HomePage extends AppCompatActivity {

    TextView txtDescription;
    Button btnProceed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        txtDescription=(TextView) findViewById(R.id.txtDescription);
        btnProceed=(Button)findViewById(R.id.btnProceed);

        txtDescription.setText(Html.fromHtml("<b>PotSol App</b><br />Aim:<br/>The aim is to easily manage the entire process of pot hole management. Hereby managing and dealing with pot holes. To minimize the risk of human life.<br />Objective<br />1.To reduce risk of human life<br/>2.Good result in effective manner<br />Pothole creates a major issue in India. Every year in India nearly 3,597 deaths occur due to pothole. The major reason is the people facing the problem of pothole are unable to register this problem in a proper way. And sometimes registered problems are not heard.<br/>So our application \"PotSol\" has brought the solution to this problem.<br/>\"PotSol\" application enables you to upload the images of pothole you have stumbled on.<br/>The image will be reviewed by the concerned authority and status of the registered pothole will be updated. You can check whether your register problem is solved and thus by coming together we can solve this problem.<br/>Contact No:<b>1800 180 7777</b><br />Email ID:<b>dummy@potsol.com</b>"));
        btnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(HomePage.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}
