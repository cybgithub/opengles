package com.example.demo_5_1;

import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * 六角星
 */
public class SixPointedStar {
    int mProgram; //自定义渲染管线着色器程序id
    int muMVPMatrixHandle; //总体变换矩阵引用
    int maPositionHandle; //顶点位置属性引用
    int maColorHandle; //顶点颜色属性引用
    String mVertexShaderStr; //顶点着色器脚本内容转换为字符串形式
    String mFragmentShaderStr; //片元着色器脚本内容转换为字符串形式

    /*static*/ float[] mMMatrix = new float[16]; //具体物体的3D变换矩阵，包含旋转、平移、缩放

    FloatBuffer mVertexBuffer; //顶点坐标数据缓冲
    FloatBuffer mColorBuffer; //顶点颜色数据缓冲

    int vCount = 0;
    float xAngle = 0; //绕X轴渲染的角度
    float yAngle = 0; //绕Y轴渲染的角度
    float zAngle = 0; //绕Y轴渲染的角度
    final float UNIT_SIZE = 1.0f;

    public SixPointedStar(MyGLSurfaceView mv, float r, float R, float z)
    {
        initVertexData(R, r, z);
        initShader(mv);
    }

    //初始化顶点数据
    //特别提示：由于不同平台字节顺序不同数据单元不是字节的一定要经过ByteBuffer
    //转换，关键是要通过ByteOrder设置nativeOrder()，否则有可能会出问题
    public void initVertexData(float R, float r, float z)
    {
        List<Float> flist = new ArrayList<Float>();
        float tempAngle = 360 / 6;
        //循环生成构成六角形各三角形的顶点坐标
        for(float angle= 0; angle < 360; angle += tempAngle)
        {
            //第一个三角形
            //第一个点的x、y、z坐标
            flist.add(0f);
            flist.add(0f);
            flist.add(z);
            //第二个点的x、y、z坐标
            flist.add((float) (R * UNIT_SIZE * Math.cos(Math.toRadians(angle))));
            flist.add((float) (R * UNIT_SIZE * Math.sin(Math.toRadians(angle))));
            flist.add(z);
            //第三个点的x、y、z坐标
            flist.add((float) (r * UNIT_SIZE * Math.cos(Math.toRadians(angle+tempAngle/2))));
            flist.add((float) (r * UNIT_SIZE * Math.sin(Math.toRadians(angle+tempAngle/2))));
            flist.add(z);

            //第二个三角形
            //第一个中心点的x、y、z坐标
            flist.add(0f);
            flist.add(0f);
            flist.add(z);
            //第二个点的x、y、z坐标
            flist.add((float) (r * UNIT_SIZE * Math.cos(Math.toRadians(angle+tempAngle/2))));
            flist.add((float) (r * UNIT_SIZE * Math.sin(Math.toRadians(angle+tempAngle/2))));
            flist.add(z);
            //第三个点的x、y、z坐标
            flist.add((float) (R * UNIT_SIZE * Math.cos(Math.toRadians(angle+tempAngle))));
            flist.add((float) (R * UNIT_SIZE * Math.sin(Math.toRadians(angle+tempAngle))));
            flist.add(z);
        }
        vCount = flist.size()/3;
        float[] vertexArray = new float[flist.size()];//顶点坐标数组
        for(int i = 0; i < vCount; i++)
        {
            vertexArray[i * 3] = flist.get(i * 3);
            vertexArray[i * 3 + 1] = flist.get(i * 3 + 1);
            vertexArray[i * 3 + 2] = flist.get(i * 3 + 2);
        }
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertexArray.length*4);
        vbb.order(ByteOrder.nativeOrder());	//设置字节顺序为本地操作系统顺序
        mVertexBuffer = vbb.asFloatBuffer();
        mVertexBuffer.put(vertexArray);//将顶点坐标数据放进缓冲
        mVertexBuffer.position(0);

        //顶点着色数据的初始化
        float[] colorArray = new float[vCount * 4];
        for(int i = 0; i < vCount; i++)
        {
            if(i % 3 == 0)
            {
                //中心点为白色，RGBA 4个通道[1,1,1,0]
                colorArray[i * 4] = 1;
                colorArray[i * 4 + 1] = 1;
                colorArray[i * 4 + 2] = 1;
                colorArray[i * 4 + 3] = 0;
            }
            else
            {
                //边上的点为淡蓝色，RGBA 4个通道[0.45,0.75,0.75,0]
                colorArray[i * 4] = 0.45f;
                colorArray[i * 4 + 1] = 0.75f;
                colorArray[i * 4 + 2] = 0.75f;
                colorArray[i * 4 + 3] = 0;
            }
        }
        ByteBuffer cbb = ByteBuffer.allocateDirect(colorArray.length * 4);
        cbb.order(ByteOrder.nativeOrder());	//设置字节顺序为本地操作系统顺序
        mColorBuffer = cbb.asFloatBuffer();
        mColorBuffer.put(colorArray);//将顶点颜色数据放进缓冲
        mColorBuffer.position(0);
    }

    //初始化着色器
    public void initShader(MyGLSurfaceView mv)
    {
        //加载顶点着色器脚本内容，转换为字符串形式，用于 glCreateProgram 时作为参数传入
        mVertexShaderStr = ShaderUtil.loadFromAssetsFile("vertex.sh", mv.getResources());
        //加载片元着色器脚本内容，转换为字符串形式，用于 glCreateProgram 时作为参数传入
        mFragmentShaderStr = ShaderUtil.loadFromAssetsFile("frag.sh", mv.getResources());
        //基于顶点与片元着色器创建程序，内部包含着色器的创建、编译以及与program的attach、link
        mProgram = ShaderUtil.createProgram(mVertexShaderStr, mFragmentShaderStr);
        //获取程序中顶点位置属性引用id
        maPositionHandle = GLES30.glGetAttribLocation(mProgram, "aPosition");
        //获取程序中顶点颜色属性引用id
        maColorHandle= GLES30.glGetAttribLocation(mProgram, "aColor");
        //获取程序中总变换矩阵引用id
        muMVPMatrixHandle = GLES30.glGetUniformLocation(mProgram, "uMVPMatrix");
    }

    //绘制六角星
    public void drawSelf()
    {
        Log.w("demo_draw", "angle " + xAngle + " " + yAngle +" " + zAngle);
        //指定着色器程序
        GLES30.glUseProgram(mProgram);
        //初始化变换矩阵
        Matrix.setRotateM(mMMatrix, 0, 0, 0, 1, 0);
        //设置沿Z轴正向位移1
        //Matrix.translateM(mMMatrix,0,0,0,1);
        //设置沿Z轴正负位移1
        Matrix.translateM(mMMatrix,0,0,0,-1);
        //设置绕x轴旋转xAngle度
        Matrix.rotateM(mMMatrix,0, xAngle,1,0,0);
        //设置绕y轴旋转yAngle度
        Matrix.rotateM(mMMatrix,0, yAngle,0,1,0);
        //设置绕z轴旋转zAngle度
        Matrix.rotateM(mMMatrix,0, zAngle,0,0,1);
        //将最终变换矩阵传入渲染管线对应的uniform变量uMVPMatrix
        GLES30.glUniformMatrix4fv(muMVPMatrixHandle, 1, false /*不转置*/, MatrixState.getFinalMatrix(mMMatrix), 0);

        //将顶点位置数据送入渲染管线
        GLES30.glVertexAttribPointer(maPositionHandle,
                                3,
                                GLES30.GL_FLOAT,
                                false,
                                3 * 4,
                                mVertexBuffer);
        //将顶点颜色数据送入渲染管线
        GLES30.glVertexAttribPointer(maColorHandle,
                                    4,
                                    GLES30.GL_FLOAT,
                                    false,
                                    4*4,
                                    mColorBuffer);
        //启用顶点位置数据数组
        GLES30.glEnableVertexAttribArray(maPositionHandle);
        //启用顶点颜色数据数组
        GLES30.glEnableVertexAttribArray(maColorHandle);
        //绘制六角星
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vCount);
    }
}
