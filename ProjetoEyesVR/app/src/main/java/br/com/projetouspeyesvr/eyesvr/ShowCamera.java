package br.com.projetouspeyesvr.eyesvr;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ShowCamera extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback{
    private Camera camera;
    private SurfaceHolder holder;
    public ByteArrayOutputStream mFrameBuffer;
    private Context con;
    private int width;
    private int height;

    public ShowCamera (Context context, Camera c){
        super(context);
        con = context;
        camera = c;
        holder =getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Camera.Parameters parametros = camera.getParameters();
        parametros.set("orientation", "landscape");
        camera.setDisplayOrientation(0);
        parametros.setRotation(0);
        camera.setParameters(parametros);
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        try{
            camera.stopPreview();
        } catch (Exception e){
            e.printStackTrace();
        }
        try{
            //Configuration Camera Parameter(full-size)
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewSize(320,240);
            parameters.setPreviewFormat(ImageFormat.NV21);
            camera.setParameters(parameters);
            // mCamera.setDisplayOrientation(90);
            camera.setPreviewCallback(this);
            camera.startPreview();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        camera.setPreviewCallback(null);
        camera.release();
        camera = null;

    }

    public void onPreviewFrame(byte[] data,Camera camera){
        try{
            //convert YuvImage(NV21) to JPEG Image data
            YuvImage yuvimage=new YuvImage(data,ImageFormat.NV21,this.width,this.height,null);
            System.out.println("WidthandHeight"+yuvimage.getHeight()+"::"+yuvimage.getWidth());
            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            yuvimage.compressToJpeg(new Rect(0,0,this.width,this.height),100,baos);
            mFrameBuffer = baos;
        }catch(Exception e){
            Log.d("parse","errpr");
        }
    }
}
