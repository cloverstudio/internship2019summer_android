package com.example.summerschoolapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.summerschoolapp.dialog.ProgressDialog;


public class BaseActivity extends AppCompatActivity {

    // start: global progress handle
    private ProgressDialog progress;
    private BroadcastReceiver stopProgressFromError = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            hideProgress();
        }
    };

    public void setProgressStatus(String text) {

        if (progress != null && progress.isShowing()) {
            progress.handleStatus(text);
        }
    }

    public void showProgress() {
        showProgress(true);
    }

    public void showProgress(boolean isCancelable) {

        try {

            if (progress == null || !progress.isShowing()) {

                progress = ProgressDialog.ShowProgressDialog(this, isCancelable);
                progress.show();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void hideProgress() {

        try {

            if (progress != null && progress.isShowing()) {
                progress.dismiss();
            }

            progress = null;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    // hide: global progress handle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}