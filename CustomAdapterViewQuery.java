package com.puddlesmanagment;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dell on 01/09/2018.
 */

public class CustomAdapterViewQuery extends ArrayAdapter<String> {
   /* private final Activity context;
    private final String[] statusList;
    private final String[] addressList;*/
   Activity mContext;
    LayoutInflater inflater;
    private List<SearchQuery> dataarraylist = null;
    private ArrayList<SearchQuery> arraylist;
    private String[] data;

    String []icon;
    String []addressList;
    String []statusList;
    String []timeperiod;

    //public CustomAdapterViewQuery(Context a, List<SearchQuery> dataarraylist){
    public CustomAdapterViewQuery(Activity a, String [] icon,String [] addressList,String [] statusList,String []timeperiod){
        //super(a, R.layout.cust_query_list,dataarraylist);
        super(a,R.layout.cust_query_list,icon);
       /* this.context = context;
        this.addressList= addressList;
        this.statusList= statusList;
        data=d;*/
        mContext = a;
        this.icon=icon;
        this.addressList=addressList;
        this.statusList=statusList;
        this.timeperiod=timeperiod;
        /*this.dataarraylist = dataarraylist;
        inflater = LayoutInflater.from(mContext);
        this.arraylist = new ArrayList<SearchQuery>();
        this.arraylist.addAll(dataarraylist);*/

    }
    public class ViewHolder {
        TextView title;
        TextView subtitle;
        //TextView population;
        ImageView icon;
    }

    /*@Override
    public int getCount() {
        return dataarraylist.size();
    }*/

    /*@Override
    public SearchQuery getItem(int position) {
        return dataarraylist.get(position);
    }*/

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = mContext.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.cust_query_list, null, true);
        ImageView icon1= (ImageView) rowView.findViewById(R.id.imgView);
        TextView addresslist= (TextView) rowView.findViewById(R.id.txtaddress);
        TextView list= (TextView) rowView.findViewById(R.id.status);
        String path = ServerConfig.serverurl + "uploads/"+icon[position];
        //icon.setImageResource(icons[position]);
        Glide.with(mContext).load(path)
                .thumbnail(0.5f)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(icon1);
        addresslist.setText(addressList[position]);
        list.setText(statusList[position]+"\nTime Period:"+timeperiod[position]);

        return rowView;
    }
}
