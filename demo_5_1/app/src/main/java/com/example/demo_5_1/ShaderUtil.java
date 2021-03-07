package com.example.demo_5_1;

import android.content.res.Resources;
import android.opengl.GLES30;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * 加载顶点shader和片元shader的工具类
 */
public class ShaderUtil {
    private static final String TAG = "ShaderUtil_TAG";
    //加载指定类型shader
    public static int loadShader(int type, String source)
    {
        //创建一个新的shader
        int shader = GLES30.glCreateShader(type);
        if(shader != 0)
        {
            //加载shader源码并编译
            GLES30.glShaderSource(shader, source);
            GLES30.glCompileShader(shader);

            //查询是否编译成功
            int[] compiled_ret = new int[1];
            GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled_ret, 0);
            if (compiled_ret[0] == 0)
            {
                //若编译失败则显示错误日志并删除此shader
                Log.e(TAG, "Could not compile shader " + type + ":");
                Log.e(TAG, GLES30.glGetShaderInfoLog(shader));
                GLES30.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    //创建shader程序
    public static int createProgram(String vertexSrc, String fragmentSrc)
    {
        //加载顶点着色器
        int vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexSrc);
        if(vertexShader == 0)
        {
            Log.e(TAG, "load vertexShader error");
            return 0;
        }

        //加载顶点着色器
        int pixelShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentSrc);
        if(pixelShader == 0)
        {
            Log.e(TAG, "load pixelShader error");
            return 0;
        }

        //创建程序
        int program = GLES30.glCreateProgram();
        if(program != 0)
        {
            //向程序中加入顶点着色器和片元着色器
            GLES30.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader Vertex");
            GLES30.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader Fragment");
            //链接程序
            GLES30.glLinkProgram(program);
            //查询链接结果
            int[] linkStatus = new int[1];
            GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0);
            //若链接失败则报错并删除程序
            if (linkStatus[0] != GLES30.GL_TRUE)
            {
                Log.e(TAG, "Could not link program: ");
                Log.e(TAG, GLES30.glGetProgramInfoLog(program));
                GLES30.glDeleteProgram(program);
                program = 0;
            }
        }

        return program;
    }

    //从sh脚本加载shader内容的方法
    public static  String loadFromAssetsFile(String fname, Resources r)
    {
        String result=null;
        try
        {
            InputStream in=r.getAssets().open(fname);
            int ch=0;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while((ch=in.read())!=-1)
            {
                baos.write(ch);
            }
            byte[] buff=baos.toByteArray();
            baos.close();
            in.close();
            result=new String(buff,"UTF-8");
            result=result.replaceAll("\\r\\n","\n");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return result;
    }

    //检查每一步操作是否有错误的方法
    //@SuppressLint("NewApi")
    public static void checkGlError(String op)
    {
        int error;
        while ((error = GLES30.glGetError()) != GLES30.GL_NO_ERROR)
        {
            Log.e(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }
}
