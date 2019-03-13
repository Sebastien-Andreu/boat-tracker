package com.example.boattrackerv2;

import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

public class Main7Activity extends AppCompatActivity {


    private Button buttonGetContainerPort;
    private Dialog getContainerPort;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main7);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        buttonGetContainerPort = findViewById(R.id.buttonGetContainerPort);

        getContainerPort = new Dialog(this);

        buttonGetContainerPort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogueGetCOntainerPort();
            }
        });


    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        getContainerPort.dismiss();
    }

    private void showDialogueGetCOntainerPort(){
        getContainerPort.setContentView(R.layout.popup_getcontainer_port);

        setViewDialog(getContainerPort);
        getContainerPort.show();
    }

    private void setViewDialog(Dialog dialog){
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        Window window = dialog.getWindow();
        layoutParams.copyFrom(window.getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);
    }

}
