package com.example.demo_5_1;

import android.opengl.Matrix;

/**
 * 存储系统矩阵状态的类
 */
public class MatrixState {
    private static float[] mProjMatrix = new float[16]; // 4x4 矩阵，投影用
    private static float[] mVMatrix = new float[16]; //摄像机朝向矩阵（相机投影矩阵），9参数矩阵
    private static float[] mMVPMatrix = null; //最终的总变换矩阵

    //设置摄像机的方法
    public static void setCamera(float cx, float cy, float cz, //Camera位置的X、Y、Z坐标
                                 float tx, float ty, float tz, //Target观察目标的X、Y、Z坐标
                                 float upx, float upy, float upz) //Camera up向量在X、Y、Z的分量
    {
        Matrix.setLookAtM(mVMatrix, //存储生成矩阵元素的float[]类型数组
                  0, //填充起始偏移量
                          cx, cy, cz,
                          tx, ty, tz,
                          upx, upy, upz);
    }

    //设置正交投影的方法
    public static void setProjectOrtho(float left, float right,
                                       float bottom, float top,
                                       float near, float far)
    {
        Matrix.orthoM(mProjMatrix, //存储生成矩阵元素的float[]类型数组
                0, //填充起始偏移量
                       left, right, //left、right面到视景体中心轴线的距离
                       bottom, top, //bottom、top面到视景体中心轴线的距离
                       near, far); //near、far面与视点的距离
    }

    //获取具体物体的总变换矩阵
    public static float[] getFinalMatrix(float[] spec) //spec具体物体的移动旋转矩阵，包括旋转、平移、缩放
    {
        mMVPMatrix = new float[16]; //创建用来存放最终变换矩阵的数组
        //将摄像机矩阵mVMatrix乘以变换矩阵spec
        Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, spec, 0);
        //将摄像机矩阵mProjMatrix乘以上一步结果矩阵mMVPMatrix，得到最终变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);
        return mMVPMatrix;
    }
}
