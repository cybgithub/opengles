package com.example.demo_3_1;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLSurfaceView extends GLSurfaceView {
    private static final String TAG = "MyGLSurfaceView_TAG";
    final float ANGLE_SPAN = 0.375f; //每个周期变化的角度

    RotateThread rthread;
    SceneRender mRenderer; //自定义渲染器引用
    public boolean runFlag = true;

    public MyGLSurfaceView(Context context)
    {
        super(context);
        this.setEGLContextClientVersion(3);
        mRenderer = new SceneRender();
        this.setRenderer(mRenderer);
        this.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    //自定义渲染器实现
    public class SceneRender implements GLSurfaceView.Renderer
    {
        Triangle tle;

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            Log.w(TAG, "onSurfaceCreated");
            //设置屏幕背景颜色RGBA
            GLES30.glClearColor(0, 0, 0, 1.0f);
            //创建三角形对象
            tle = new Triangle(MyGLSurfaceView.this);
            //打开深度检测
            GLES30.glEnable(GLES30.GL_DEPTH_TEST);
            //启动旋转线程
            rthread = new RotateThread();
            rthread.start();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            Log.w(TAG, "onSurfaceChanged");
            //设置视图大小及位置
            GLES30.glViewport(0, 0, width, height);
            //计算GLSurfaceView的宽高比
            float ratio = (float)width / height;

            //计算产生透视投影矩阵
            Matrix.frustumM(Triangle.mProjMatrix,0, -ratio, ratio, -1, 1, 1, 10);
            //计算产生摄像机9参数位置矩阵
            Matrix.setLookAtM(Triangle.mVMatrix, 0, 0, 0, 3, 0f, 0f,0f,0f,1.0f,0.0f);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            Log.w(TAG, "onDrawFrame ");
            //清除颜色与深度缓冲区
            GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT | GLES30.GL_COLOR_BUFFER_BIT);
            //绘制三角形
            tle.drawSelf();
        }
    }

    //自定义内部线程类
    public class RotateThread extends Thread
    {
        @Override
        public void run()
        {
            while(true)
            {
                if(isRender())
                {
                    mRenderer.tle.xAngle = mRenderer.tle.xAngle + ANGLE_SPAN;
                    try {
                        Log.d(TAG, "RotateThread Sleep");
                        Thread.sleep(16);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    //线程是否在运行
    public boolean isRender()
    {
        synchronized(this) {
            return runFlag;
        }
    }

    //线程暂停与恢复
    public void stopRender(boolean flg)
    {
        synchronized(this) {
            runFlag = flg;
        }
    }

}
