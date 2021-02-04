package com.cpen391.torch.data;

import android.graphics.Bitmap;

import com.cpen391.torch.OtherUtils;
import com.google.gson.Gson;

public class StoreInfo {

    private String storeName;
    private String storeOwnerId;
    private double latitude;
    private double longitude;
    private String macAddr;
    private String encodedLogo;

    public StoreInfo(String storeName, String storeOwnerId, double latitude, double longitude, String macAddr, String encodedLogo) {
        this.storeName = storeName;
        this.storeOwnerId = storeOwnerId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.macAddr = macAddr;
        this.encodedLogo = encodedLogo;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getStoreOwnerId() {
        return storeOwnerId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getMacAddr() {
        return macAddr;
    }

    public String getEncodedLogo() {
        return encodedLogo;
    }

    public Bitmap getLogo() {
        return OtherUtils.decodeImage(encodedLogo);
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
