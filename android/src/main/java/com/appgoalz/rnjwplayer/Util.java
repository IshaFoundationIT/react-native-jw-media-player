package com.appgoalz.rnjwplayer;

import static com.google.android.exoplayer2.util.Util.toByteArray;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Patterns;
import android.webkit.URLUtil;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class Util {

    private static final String GOOGLE_PLAY_STORE_PACKAGE_NAME_OLD = "com.google.market";
    private static final String GOOGLE_PLAY_STORE_PACKAGE_NAME_NEW = "com.android.vending";

    public static byte[] executePost(String url, byte[] data, Map<String, String> requestProperties)
            throws IOException {
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(data != null);
            urlConnection.setDoInput(true);
            if (requestProperties != null) {
                for (Map.Entry<String, String> requestProperty : requestProperties.entrySet()) {
                    urlConnection.setRequestProperty(requestProperty.getKey(),
                            requestProperty.getValue());
                }
            }
            // Write the request body, if there is one.
            if (data != null) {
                OutputStream out = urlConnection.getOutputStream();
                try {
                    out.write(data);
                } finally {
                    out.close();
                }
            }
            // Read and return the response body.
            InputStream inputStream = urlConnection.getInputStream();
            try {
                return toByteArray(inputStream);
            } finally {
                inputStream.close();
            }
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    public static boolean isValidURL(String url){
        return URLUtil.isValidUrl(url) && Patterns.WEB_URL.matcher(url).matches();
    }

    private static boolean doesPackageExist(PackageManager packageManager, String targetPackage) {
        try {
            packageManager.getPackageInfo(targetPackage, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

    // Without the Google API's Chromecast won't work
    public static boolean isGoogleApiAvailable(Context context) {
        PackageManager packageManager = context.getPackageManager();
        boolean isOldPlayStoreInstalled = doesPackageExist(packageManager, GOOGLE_PLAY_STORE_PACKAGE_NAME_OLD);
        boolean isNewPlayStoreInstalled = doesPackageExist(packageManager, GOOGLE_PLAY_STORE_PACKAGE_NAME_NEW);

        boolean isPlaystoreInstalled = isNewPlayStoreInstalled||isOldPlayStoreInstalled;

        boolean isGoogleApiAvailable = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS;
        return isPlaystoreInstalled && isGoogleApiAvailable;
    }
}