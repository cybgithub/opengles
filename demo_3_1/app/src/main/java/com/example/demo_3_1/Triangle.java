package com.example.demo_3_1;

import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

//三角形绘制类
public class Triangle {
    public static float[] mProjMatrix = new float[16]; //4x4投影矩阵
    public static float[] mVMatrix = new float[16]; //相机位置朝向的参数矩阵
    public static float[] mMVPMatrix; //总变换矩阵

    int mProgram; //自定义渲染管线程序id
    int muMVPMatrixHandle; //总变换矩阵引用
    int maPositionHandle; //顶点位置属性引用
    int maColorHandle; //顶点颜色属性引用
    String mVertexShader;//顶点着色器代码脚本
    String mFragmentShader;//片元着色器代码脚本
    static float[] mMMatrix = new float[16]; //具体物体的移动旋转矩阵，包括旋转、平移、缩放

    FloatBuffer mVertexBuffer; //顶点坐标数据缓冲
    FloatBuffer mColorBuffer; //顶点颜色数据缓冲
    int vCount = 0;
    float xAngle = 0; //绕x轴旋转的角度

    public Triangle(MyGLSurfaceView mv)
    {
        //初始化顶点数据
        initVertexData();
        //初始化着色器
        initShader(mv);
    }

    //初始化顶点数据
    public void initVertexData()
    {
        //顶点坐标数据的初始化
        vCount = 3;
        final float UNIT_SIZE = 0.2f;
        //顶点坐标数组
        float vertices[] = new float[]
        {
           -4 * UNIT_SIZE, 0, 0,
           0, -4 * UNIT_SIZE, 0,
           4 * UNIT_SIZE, 0, 0,
        };

        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder()); //字节序设为本地操作系统顺序
        mVertexBuffer = vbb.asFloatBuffer(); //转换为浮点类型缓冲
        mVertexBuffer.put(vertices); //在缓冲区写入数据
        mVertexBuffer.position(0); //设置缓冲区起始位置

        //顶点颜色数组
        float colors[]=new float[]
        {
            1, 1, 1, 0, //白色
            0, 0, 1, 0, //蓝
            0, 1, 0, 0 //绿
        };

        ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * 4);
        cbb.order(ByteOrder.nativeOrder());//设置字节顺序为本地操作系统顺序
        mColorBuffer = cbb.asFloatBuffer();//转换为浮点(Float)型缓冲
        mColorBuffer.put(colors);//在缓冲区内写入数据
        mColorBuffer.position(0);//设置缓冲区起始位置
    }

    //初始化着色器
    public void initShader(MyGLSurfaceView mv)
    {
        //加载顶点着色器的脚本内容，转换成字符串形式
        mVertexShader = ShaderUtil.loadFromAssetsFile("vertex.sh", mv.getResources());
        //加载顶点着色器的脚本内容，转换成字符串形式
        mFragmentShader = ShaderUtil.loadFromAssetsFile("frag.sh", mv.getResources());
        //基于顶点着色器与片元着色器创建程序
        mProgram = ShaderUtil.createProgram(mVertexShader, mFragmentShader);
        //获取程序中顶点位置属性引用, aPosition 为着色器自定义统一变量
        maPositionHandle = GLES30.glGetAttribLocation(mProgram, "aPosition");
        //获取程序中顶点颜色属性引用, aColor 为着色器自定义统一变量
        maColorHandle = GLES30.glGetAttribLocation(mProgram, "aColor");
        //获取程序中总变换矩阵引用, uMVPMatrix 为着色器自定义统一变量
        muMVPMatrixHandle = GLES30.glGetUniformLocation(mProgram, "uMVPMatrix");
    }

    public void drawSelf()
    {
        //指定要使用的着色程序
        GLES30.glUseProgram(mProgram);
        //初始化变换矩阵
        Matrix.setRotateM(mMMatrix, 0, 0, 0, 1, 0);
        //设置沿z正向移动1
        Matrix.translateM(mMMatrix, 0, 0, 0, 1);
        //绕x轴旋转
        Matrix.rotateM(mMMatrix, 0, xAngle, 1, 0, 0);
        //绕y轴旋转
        Matrix.rotateM(mMMatrix, 0, xAngle, 0, 1, 0);
        //绕z轴旋转
        Matrix.rotateM(mMMatrix, 0, xAngle * 0.5f, 0, 0, 1);
        //将变换矩阵传入渲染管线
        GLES30.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, Triangle.getFianlMatrix(mMMatrix), 0);
        //将顶点位置数据传入渲染管线
        GLES30.glVertexAttribPointer(maPositionHandle,
                                 3,
                                     GLES30.GL_FLOAT,
                            false,
                                0, // 3 * 4
                                      mVertexBuffer);
        //将顶点颜色数据传送进渲染管线
        GLES30.glVertexAttribPointer(maColorHandle,
                                 4,
                                     GLES30.GL_FLOAT,
                            false,
                               0, // 4 * 4
                                     mColorBuffer);
        ///启用顶点位置数据
        GLES30.glEnableVertexAttribArray(maPositionHandle);
        //启动顶点着色数据
        GLES30.glEnableVertexAttribArray(maColorHandle);
        //绘制三角形
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vCount);
    }

    public static float[] getFianlMatrix(float[] spec)
    {
        mMVPMatrix=new float[16];
        Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, spec, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);
        return mMVPMatrix;
    }
}
