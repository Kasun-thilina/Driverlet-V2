package com.example.safe.drivelert.Utility;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.RequiresApi;

import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;

public class Utils {

    static AlertDialog alertDialog;

    public static boolean isInternetAvailable(Context context) {
        boolean flag = false, mobile = false, wifi = false;

        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activenetwork = manager.getActiveNetworkInfo();
        if (activenetwork != null) {
            mobile = activenetwork.getType() == ConnectivityManager.TYPE_MOBILE;
            wifi = activenetwork.getType() == ConnectivityManager.TYPE_WIFI;
            if (activenetwork.isConnected() || activenetwork.isConnectedOrConnecting()) {
                if (wifi || mobile) {
                    flag = true;
                }
            }
        }

        return flag;
    }

    public static void showCityChangeDialogue(final Context context, final String city, String title, String message) {

        if (alertDialog != null && alertDialog.isShowing())
            alertDialog.dismiss();
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title + city);
        builder.setMessage(message);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog = builder.show();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                alertDialog.dismiss();
            }
        }, 5000);

    }


    public static String calculateHash(String message)
    {
        String hash = Hashing
                .sipHash24()
                .hashString(message
                        , StandardCharsets.UTF_8).toString();

        return hash;
    }
}
