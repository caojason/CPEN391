package com.cpen391.torch;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.Base64;
import android.util.Log;

import com.google.gson.JsonObject;

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
        return Pattern.matches("^[aA-zZ0-9_-]{3,15}$", storeName);
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
        byte[] decoded = Base64.decode(encodedImg, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
    }

    public static Bitmap scaleImage(Bitmap image) {
        //used for scaling image and making the image into a circle shape to fit in the profile image
        int radius = 200;
        Bitmap scaled = Bitmap.createScaledBitmap(image, 2 * radius, 2 * radius, true);
        Bitmap result = null;
        try {
            result = Bitmap.createBitmap(2 * radius, 2 * radius, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);

            Paint paint = new Paint();
            Rect rect = new Rect(0, 0, 2 * radius, 2 * radius);
            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(Color.WHITE);
            canvas.drawCircle(radius, radius, radius, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(scaled, rect, rect, paint);
        } catch (Exception e) {
            Log.d("Image", "Image scaling failed");
        }
        return result;
    }

    /*
     * upload strings, bitmap to the server
     * returns true if success, false otherwise
     * */
    public static void uploadToServer(String endpoint, String uid, String type, String data) {

        String jsonStringToSend = createJsonString(uid, type, data);
        String response = "";
        String serverLink = "http://quizzical.canadacentral.cloudapp.azure.com/upload" + endpoint;
        Log.d("upload", "uploading: " + jsonStringToSend);
        Log.d("upload", "upload to: "+ serverLink);
        try {
            URL url = new URL(serverLink);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
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
                conn.setRequestProperty("Content-Type", "application/json");
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

    private static String createJsonString(String uid, String type, String data) {
        JsonObject jsonObject = new JsonObject();
        try {
            jsonObject.addProperty("uid", uid);
            jsonObject.addProperty("type", type);
            jsonObject.addProperty("data", data);
        } catch (Exception e) {
            Log.d("other_utils", "create json object failed");
        }
        Log.d("other util", "create success: " + jsonObject.toString());
        return jsonObject.toString();
    }

    /*
     * function used for downloading images from url link
     * mostly used in creating quiz
     * may also be used for downloading user profile image
     * */
    public static Bitmap getBitmapFromUrl(String urlLink) {
        try {
            URL url = new URL(urlLink);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();

            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            Log.d("get_bitmap_err", e.getMessage() + "");
            return null;
        }
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
        String urlLink = "http://quizzical.canadacentral.cloudapp.azure.com/classes/delete?" + params;
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
