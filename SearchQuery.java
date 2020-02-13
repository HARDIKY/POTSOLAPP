package com.puddlesmanagment;

/**
 * Created by dell on 22/09/2018.
 */

public class SearchQuery {

    private String icon;
    private String address;
    private String status;

    public SearchQuery(String icon,String address,String status) {
        this.address = address;
        this.status = status;
        this.icon = icon;
    }
    public String geticon() {
        return this.icon;
    }
    public String getaddress() {
        return this.address;
    }
    public String getstatus() {
        return this.status;
    }
}

