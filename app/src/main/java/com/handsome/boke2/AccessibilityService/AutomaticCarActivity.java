package com.handsome.boke2.AccessibilityService;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.handsome.boke2.R;

import java.io.IOException;
import java.io.InputStream;

public class AutomaticCarActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnAS;
    private Button btnStartAutoCare;
    private Button btnSaveMsg;
    private Button btnFinish;
    private EditText tv_cargetStation;
    private EditText tv_backstation;

    //行驶里程
    private EditText tv_mileage;


    private int SCREEN_OFF_TIMEOUT = 30*1000;
    private PermissionUtil permissionUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qhb);
        permissionUtil = new PermissionUtil(this) {
            @Override
            protected void requestPermissionsSuccess() {
                Toast.makeText(getApplicationContext(), "设置成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void requestPermissionsFail() {

            }
        };
        permissionUtil.requestPermissions(1,
                new String[]{
                        Manifest.permission.WRITE_SETTINGS,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                });
        btnAS = (Button) findViewById(R.id.btnFuZhu);
        btnAS.setOnClickListener(this);

        btnFinish = (Button) findViewById(R.id.btnFinish);
        btnFinish.setOnClickListener(this);
        btnStartAutoCare = (Button) findViewById(R.id.btnStartAutoCare);
        btnStartAutoCare.setOnClickListener(this);
        btnSaveMsg = (Button) findViewById(R.id.btnSaveMsg);
        btnSaveMsg.setOnClickListener(this);

        tv_mileage = (EditText) findViewById(R.id.tv_mileage);
        tv_cargetStation = (EditText) findViewById(R.id.tv_cargetStation);
        tv_backstation = (EditText) findViewById(R.id.tv_backstation);
        setScreenOffTime(30 * 60 * 1000);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnFuZhu:
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
                break;
            case R.id.btnStartAutoCare:
                Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage("com.woasis.smp");
                startActivity(LaunchIntent);
                break;
            case R.id.btnSaveMsg:
                String mileage = tv_mileage.getText().toString().trim();
                if (!TextUtils.isEmpty(mileage)) {
                    AutomaticCarAccessibilityService.mileage = Integer.parseInt(mileage);
                }
                String text1 = tv_cargetStation.getText().toString();
                if(!TextUtils.isEmpty(text1)){
                   int index = text1.indexOf("区")+1;
                   AutomaticCarAccessibilityService.tv_cargetOregion = text1.substring(0,index);
                   AutomaticCarAccessibilityService.tv_cargetStation = text1.substring(index);
                }
//                AutomaticCarAccessibilityService.tv_cargetStation = tv_cargetStation.getText().toString();
                AutomaticCarAccessibilityService.tv_backstation = tv_backstation.getText().toString();
                break;
            case R.id.btnFinish:
                finish();
                System.exit(1);
                break;


        }
    }

    /**
     * 设置背光时间  毫秒
     */
    private void setScreenOffTime(int paramInt) {

        try {
//            Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, paramInt);
            int result = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
        } catch (Exception localException) {
            localException.printStackTrace();
            Log.e("tgl===", "localException catch:" + localException.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, AutomaticCarAccessibilityService.class));
        setScreenOffTime(SCREEN_OFF_TIMEOUT);
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionUtil.requestResponse(requestCode, permissions, grantResults);
    }

//
//    /**
//     * 根据资源id获得图片并压缩，返回bitmap用于显示
//     */
//    final public Bitmap getSmallBmpFromResource(Context context, int id, int targetW, int targetH) {
//        if (context == null || context.getResources() == null) {
//            return null;
//        }
//        InputStream inputStream = context.getResources().openRawResource(id);
//        final BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        options.inPurgeable = true;
//        options.inInputShareable = true;
//        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
////		BitmapFactory.decodeResource(context.getResources(), id, options);
//        BitmapFactory.decodeStream(inputStream, null, options);
//        // Calculate inSampleSize
//
//        options.inSampleSize = calculateInSampleSize(options, targetW, targetH);
//        // Decode bitmap with inSampleSize set
//        options.inJustDecodeBounds = false;
//        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
//        try {
//            inputStream.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return bitmap;
//    }
//
//    /** 计算图片的缩放值 */
//    final public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
//    {
//        final int height = options.outHeight;
//        final int width = options.outWidth;
//        int inSampleSize = 1;
//        if(height > reqHeight || width > reqWidth)
//        {
//            final int heightRatio = Math.round((float) height / (float) reqHeight);
//            final int widthRatio = Math.round((float) width / (float) reqWidth);
//            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
//        }
//        return inSampleSize;
//    }
}
