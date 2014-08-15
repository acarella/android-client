package com.mifos.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;


/**
 * @author ishankhanna
 * Class To Block User Interface Safely for Asynchronous Network Calls
 * and/or Heavy Operations
 */
public class SafeUIBlockingUtility {



    public static String utilityTitle = "Working";
    public static String utilityMessage = "Message";

    private ProgressDialog progressDialog;

    private Context context;

    public void safelyBlockUI(){
        progressDialog.show();
    }

    public SafeUIBlockingUtility(Context context){
        this.context = context;
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(utilityMessage);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle(utilityTitle);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }

    public void safelyBlockUI(String uniqueMessage, String uniqueTitle){

        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(uniqueMessage);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle(uniqueTitle);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
    }

    public void safelyUnBlockUI(){
        progressDialog.dismiss();
    }

    public static String getUtilityTitle() {
        return utilityTitle;
    }

    public static void setUtilityTitle(String utilityTitle) {
        SafeUIBlockingUtility.utilityTitle = utilityTitle;
    }

    public static String getUtilityMessage() {
        return utilityMessage;
    }

    public static void setUtilityMessage(String utilityMessage) {
        SafeUIBlockingUtility.utilityMessage = utilityMessage;
    }

    public void safelyUnblockUIForFailure(String tag, String message){

        progressDialog.dismiss();
        Toast.makeText(context, "Some Problem Executing Request", Toast.LENGTH_SHORT).show();
        Log.i(tag,message);

    }




}
