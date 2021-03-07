package com.example.demo_5_1;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLSurfaceView extends GLSurfaceView {
    private static final String TAG = "MyGLSurfaceView_TAG";
    final float ANGLE_SPAN = 0.5f; //每个周期变化的角度

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
        SixPointedStar[] stars = new SixPointedStar[6];

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            Log.w(TAG, "onSurfaceCreated");
            //设置屏幕背景颜色RGBA
            GLES30.glClearColor(0, 0, 0, 1.0f);
            //创建六角星数组中的各个六角星
            for(int i = 0; i < stars.length;i++)
            {
                stars[i]=new SixPointedStar(MyGLSurfaceView.this,0.2f,0.5f,-0.1f * i);
            }
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
            //设置正交投影
            MatrixState.setProjectOrtho(-ratio, ratio, -1, 1, 1, 10);
            //设置摄像机
            MatrixState.setCamera(0, 0, 3f,
                                  0, 0, 0f,
                                  0f, 1.0f, 0.0f);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            Log.w(TAG, "onDrawFrame ");
            //清除颜色与深度缓冲区
            GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT | GLES30.GL_COLOR_BUFFER_BIT);
            //循环绘制各个六角星
            for(SixPointedStar s:stars)
            {
                s.drawSelf();
            }
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
                    //设置各个六角星绕x轴、y轴旋转的角度
                    for(SixPointedStar s:mRenderer.stars)
                    {
                        s.yAngle += ANGLE_SPAN;
                        s.xAngle += ANGLE_SPAN;
                        s.zAngle += ANGLE_SPAN;
                    }
                    try
                    {
                        Log.d(TAG, "RotateThread Sleep short");
                        Thread.sleep(16);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                    try {
                        //Log.d(TAG, "RotateThread Sleep long");
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {

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
