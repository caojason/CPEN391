package com.cpen391.torch;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

public class OtherUtils {

    public static boolean stringIsNullOrEmpty(String str) {
        return (str == null || str.equals(""));
    }

    public static boolean checkStoreName(String storeName) {
        //check if the username is valid or not
        if (stringIsNullOrEmpty(storeName)) return false;
        return Pattern.matches("^[aA-zZ0-9_-]{3,15}$", storeName);
    }

    public static boolean checkValidEmail(String target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    /*
     * Encoding and decoding images for storing in shared preferences and send to server
     * */
    public static String encodeImage(Bitmap image) {
        if (image == null) {
            return "";
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] b = baos.toByteArray();

        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    public static Bitmap decodeImage(String encodedImg) {
        if (stringIsNullOrEmpty(encodedImg)) return null;
        byte[] decoded = Base64.decode(encodedImg, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
    }

    public static Bitmap scaleImage(Bitmap image, int width, int height) {
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    /*
     * upload strings, bitmap to the server
     * returns true if success, false otherwise
     * */
    public static void uploadToServer(String endpoint, String uid, String data) {

        String jsonStringToSend = createJsonString(uid, data);
        String response = "";
        String serverLink = "http://35.233.184.107:5000/" + endpoint;
        Log.d("upload", "uploading: " + jsonStringToSend);
        Log.d("upload", "upload to: "+ serverLink);
        try {
            URL url = new URL(serverLink);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(3000);
            conn.setDoOutput(true);
            conn.connect();
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(jsonStringToSend);
            wr.flush();
            wr.close();

            response = conn.getResponseMessage();
            conn.disconnect();
            Log.d("HTTP_POST", "response msg: " + response);
        } catch (Exception e) {
            retransmit(serverLink, jsonStringToSend);
            Log.d("error_message", "" + e.getMessage());
        }
    }

    private static void retransmit(String link, String data) {
        for (int tryTime = 0; tryTime < 10; tryTime++) {
            try {
                URL url = new URL(link);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "text/html");
                conn.setConnectTimeout(3000);
                conn.setDoOutput(true);
                conn.connect();
                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                wr.writeBytes(data);
                wr.flush();
                wr.close();

                String response = conn.getResponseMessage();
                conn.disconnect();
                Log.d("HTTP_POST", "response msg: " + response);
                return;
            } catch (Exception e) {
                Log.d("error_message", "" + e.getMessage());
            }
        }
    }

    private static String createJsonString(String uid, String data) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uid", uid);
            jsonObject.put("data", data);
        } catch (Exception e) {
            Log.d("other_utils", "create json object failed");
        }
        Log.d("other util", "create success: " + jsonObject.toString());
        return jsonObject.toString();
    }

    /**
     * get string content from the server,
     * on success, return the content retrieved from the server
     * on fail, return an empty string
     */
    public static String readFromURL(String urlLink) {
        String result = "";
        Log.d("html_get", "trying to get from: " + urlLink);
        try {
            URL url = new URL(urlLink);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
            StringBuilder stb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stb.append(line);
            }
            result = stb.toString();
            conn.disconnect();
            Log.d("html_get", "result: " + result);
        } catch (Exception e) {
            String eMessage = e.getMessage() + "";
            Log.d("html_exception", eMessage);
        }
        return result;
    }

    public static void deleteRequest(String params) {
        String urlLink = "" + params;
        Log.d("html_delete", "deleting: " + urlLink);
        try {
            URL url = new URL(urlLink);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setConnectTimeout(3000);
            conn.connect();
            int result = conn.getResponseCode();
            conn.disconnect();
            Log.d("html_delete", "result: " + result);
        } catch (Exception e) {
            String eMessage = e.getMessage() + "";
            Log.d("html_exception", eMessage);
        }
    }
}
