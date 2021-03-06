package com.example.demo_3_1;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ConfigurationInfo;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {

    //声明 MyGLSurfaceView 类的引用
    MyGLSurfaceView mview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        Log.w("demo_MainActivity", "GLES Version " + Integer.toHexString(info.reqGlEsVersion));
        //设置为竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mview = new MyGLSurfaceView(this);
        mview.requestFocus(); //获取焦点
        mview.setFocusableInTouchMode(true); //设置为可触控
        setContentView(mview);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mview.onResume();//通过MyTDView类的对象调用onResume方法
        mview.stopRender(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mview.onPause();//通过MyTDView类的对象调用onPause方法
        mview.stopRender(false);
    }
}
