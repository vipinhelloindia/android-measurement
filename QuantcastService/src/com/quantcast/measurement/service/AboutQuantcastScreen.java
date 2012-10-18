package com.quantcast.measurement.service;

import android.app.Activity;
import android.app.ProgressDialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;

import com.quantcast.settings.GlobalControl;
import com.quantcast.settings.GlobalControlListener;
import com.quantcast.settings.GlobalControlProvider;

public class AboutQuantcastScreen extends Activity {
    
    Activity activity = this;
    CheckBox optOutCheckbox;
    
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        QuantcastClient.addActivity(this);
        
        optOutCheckbox = new CheckBox(activity);
        Button proceedButton = new Button(activity);
        View view = ResourceHelper.getDialogView(activity, proceedButton, optOutCheckbox);
        
        proceedButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                final boolean blockingEventCollection = optOutCheckbox.isChecked();
                final ProgressDialog progressDialog = ProgressDialog.show(activity, "", "Saving opt out status.");
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        QuantcastGlobalControlProvider.getProvider(activity).saveControl(new GlobalControl(blockingEventCollection));
                        progressDialog.dismiss();
                        activity.finish();
                    }
                    
                }).start();
            }

        });
        
        boolean canChangeIcon = requestWindowFeature(Window.FEATURE_LEFT_ICON);
        
        setContentView(view);
        
        if (canChangeIcon) {
            getWindow().setFeatureDrawable(Window.FEATURE_LEFT_ICON, ResourceHelper.getLogo(activity));
        }
        
        setTitle(ResourceHelper.DIALOG_TITLE);
    };
    
    @Override
    protected void onPause() {
        super.onPause();
        
        QuantcastClient.pauseSession();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        QuantcastClient.resumeSession();
        
        GlobalControlProvider globalControlProvider = QuantcastGlobalControlProvider.getProvider(activity);
        globalControlProvider.refresh();
        final ProgressDialog progressDialog = ProgressDialog.show(activity, "", "Retrieving current opt out status.");
        QuantcastGlobalControlProvider.getProvider(activity).getControl(new GlobalControlListener() {
            
            @Override
            public void callback(GlobalControl control) {
                optOutCheckbox.setChecked(control.blockingEventCollection);
                progressDialog.dismiss();
            }
            
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        QuantcastClient.endSession(activity);
    }

}