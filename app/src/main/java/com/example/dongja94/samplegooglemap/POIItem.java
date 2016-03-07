package com.example.dongja94.samplegooglemap;

import com.google.gson.annotations.SerializedName;

/**
 * Created by dongja94 on 2016-03-04.
 */
public class POIItem {
    public String id;
    @SerializedName("name")
    public String title;
    public String telNo;
    public String frontLat;
    public String frontLon;
    public String noorLat;
    public String noorLon;
    public String upperAddrName;
    public String middleAddrName;
    public String lowerAddrName;
    public String detailAddrName;
    public String subtitle;
    @SerializedName("desc")
    public String description;

    public double latitude;
    public double longitude;

    @Override
    public String toString() {
        return title;
    }

    public void updatePOIData() {
        subtitle = upperAddrName + " " + middleAddrName + "\r\n" + lowerAddrName + " " + detailAddrName;
        latitude = ( Double.parseDouble(frontLat) + Double.parseDouble(noorLat) ) / 2;
        longitude = ( Double.parseDouble(frontLon) + Double.parseDouble(noorLon) ) / 2;
    }
}
