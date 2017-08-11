package com.handsome.boke2.AccessibilityService;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.handsome.boke2.R;

import java.io.IOException;

public class AutomaticCarActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnAS;
    private Button btnStartAutoCare;
    private EditText tv_cargetStation;
    private EditText tv_backstation;


    private int SCREEN_OFF_TIMEOUT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qhb);
        btnAS = (Button) findViewById(R.id.btnFuZhu);
        btnAS.setOnClickListener(this);

        btnStartAutoCare = (Button) findViewById(R.id.btnStartAutoCare);
        btnStartAutoCare.setOnClickListener(this);

        tv_cargetStation = (EditText) findViewById(R.id.tv_cargetStation);
        tv_backstation = (EditText) findViewById(R.id.tv_backstation);

        setScreenOffTime(30*60*1000);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnFuZhu:
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
                break;
            case R.id.btnStartAutoCare:
                AutomaticCarAccessibilityService.tv_cargetStation = tv_cargetStation.getText().toString();
                AutomaticCarAccessibilityService.tv_backstation = tv_backstation.getText().toString();
                Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage("com.woasis.smp");
                startActivity(LaunchIntent);
                break;
        }
    }

    /**
     * 设置背光时间  毫秒
     */
    private void setScreenOffTime(int paramInt){

        try{
            SCREEN_OFF_TIMEOUT  = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, paramInt);
            int  result  = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
            Log.e("tgl===", "result last= " + result);
        }catch (Exception localException){
            localException.printStackTrace();
            Log.e("tgl===", "localException catch:"+localException.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this,AutomaticCarAccessibilityService.class));
        setScreenOffTime(SCREEN_OFF_TIMEOUT);
        super.onDestroy();
    }
}
