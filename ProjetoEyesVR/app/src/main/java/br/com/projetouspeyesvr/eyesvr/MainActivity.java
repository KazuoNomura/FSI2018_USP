package br.com.projetouspeyesvr.eyesvr;

import android.Manifest;
import android.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.io.IOException;
import java.net.SocketException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    public static final String ALLOW_KEY = "ALLOWED";
    public static final String CAMERA_PREF = "camera_pref";

    private ConnManager cm;
    private Button button_camera;
    private Camera camera;
    public ShowCamera showCamera;
    private FrameLayout preview;
    private Socket connection;
    private ImageView mCameraView;
    public Bitmap mLastFrame;

    private Handler sendhandler = new Handler();
    private final Handler receivehandler = new myHandler(this);
    private String myIP;
    private int myPort;
    private PointF tmp_point = new PointF();
    private Paint tmp_paint = new Paint();



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        releaseCameraAndPreview();

        button_camera = (Button) findViewById(R.id.buttonCamera);
        button_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(connection != null){
                    button_camera.setVisibility(View.GONE);
                    camera = getCameraInstance();
                    myIP = getLocalIpAddress();
                    criarPreview();
                }
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (getFromPref(this, ALLOW_KEY)) {
                showSettingsAlert();
            } else if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA)

                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.CAMERA)) {
                    showAlert();
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_CAMERA);
                }
            }
        }

        cm = new ConnManager(this, getMainLooper(), new ConnManager.SocketListener() {
            @Override
            protected void onSocketReady(Socket s) {
                // connection is ok, s is our channel
                connection = s;
                /*AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Connection");
                alertDialog.setMessage("We now connected are.");
                alertDialog.show();*/
            }
            @Override
            protected void onSocketFail(IOException e) {
                // something went wrong
                /*try {
                    connection.close();
                } catch (IOException f){

                }*/
                /*AlertDialog a = new AlertDialog.Builder(MainActivity.this).create();
                a.setTitle("Connection");
                a.setMessage("We connected are not; something wrong went.\n" + e.getMessage());
                a.show();*/
            }
        });
    }

    public static Boolean getFromPref(Context context, String key) {
        SharedPreferences myPrefs = context.getSharedPreferences(CAMERA_PREF,
                Context.MODE_PRIVATE);
        return (myPrefs.getBoolean(key, false));
    }
    private void showSettingsAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the Camera.");

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DONT ALLOW",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                for (int i = 0, len = permissions.length; i < len; i++) {
                    String permission = permissions[i];

                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        boolean
                                showRationale =
                                ActivityCompat.shouldShowRequestPermissionRationale(
                                        this, permission);

                        if (showRationale) {
                            showAlert();
                        } else if (!showRationale) {
                            saveToPreferences(MainActivity.this, ALLOW_KEY, true);
                        }
                    }
                }
            }
        }
    }
    public static void saveToPreferences(Context context, String key, Boolean allowed) {
        SharedPreferences myPrefs = context.getSharedPreferences(CAMERA_PREF,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putBoolean(key, allowed);
        prefsEditor.commit();
    }

    private void showAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the Camera.");

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DONT ALLOW",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ALLOW",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                MY_PERMISSIONS_REQUEST_CAMERA);
                    }
                });
        alertDialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cm.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cm.unpause();
    }

    protected Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return c;
    }
    private void releaseCameraAndPreview() {

        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()&& inetAddress instanceof Inet4Address) { return inetAddress.getHostAddress().toString(); }
                }
            }
        } catch (SocketException ex) {
            Log.e("ServerActivity", ex.toString());
        }
        return null;
    }

    private void criarPreview(){
        if(camera != null) {
            showCamera = new ShowCamera(this, camera);
            //mCameraView = (ImageView) findViewById(R.id.camera_preview);
            //mCameraView.setVisibility(View.VISIBLE);
            preview = (FrameLayout) findViewById(R.id.sua_camera);
            preview.setVisibility(View.VISIBLE);
            //mCameraView.setVisibility(View.INVISIBLE);
            preview.addView(showCamera);
            /*final Thread enviarThread = new Thread(new EnviarImagem(this,connection,sendhandler));
            enviarThread.start();
            try {
                Thread receberThread = new Thread(new ReceberImagem(connection, receivehandler));
                receberThread.start();
            }catch(IOException e){
                e.printStackTrace();
            }
            final Thread putImage = (new Thread(new Runnable(){
                @Override
                public void run() {
                    try {
                        receivehandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (mLastFrame!=null){

                                    Bitmap mutableBitmap = mLastFrame.copy(Bitmap.Config.RGB_565, true);
                                    Canvas canvas = new Canvas(mutableBitmap);
                                    mCameraView.setImageBitmap(mutableBitmap);
                                }
                            }
                        }); //this function can change value of mInterval.
                    } finally {
                        // 100% guarantee that this always happens, even if
                        // your update method throws an exception
                        receivehandler.postDelayed(mStatusChecker, 1000/15);
                    }
                }
            }));
            putImage.start();
            final Thread Socketdown = (new Thread(new Runnable(){
                @Override
                public void run() {
                    if(connection == null){
                        preview = (FrameLayout) findViewById(R.id.sua_camera);
                        preview.setVisibility(View.VISIBLE);
                        mCameraView.setVisibility(View.INVISIBLE);
                        preview.addView(showCamera);
                    }
                }
            }));
            Socketdown.start();*/
            //Thread cirno = new Thread(mStatusChecker());

            //preview = (FrameLayout) findViewById(R.id.camera_preview);
            //preview.addView(showCamera);

            /*try {
                cameraPassar = connection.getOutputStream();
                buffer = dados da ShowCamera(em bitmap);
                os.write(buffer);
                //os.flush();
                //os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try{
                cameraCorrigida = connection.getInputStream();
                frame = Drawable.createFromStream(cameraCorrigida, "frame.jpg")
                preview.setDrawable(frame);

            } catch (IOException e) {
                e.printStackTrace();
            }*/
        }
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        if (source != null){
            Bitmap retVal;

            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            retVal = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
            source.recycle();
            return retVal;
        }
        return null;
    }

    private Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                receivehandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mLastFrame!=null){

                            Bitmap mutableBitmap = mLastFrame.copy(Bitmap.Config.RGB_565, true);
                            Canvas canvas = new Canvas(mutableBitmap);
                            mCameraView.setImageBitmap(mutableBitmap);
                        }
                    }
                }); //this function can change value of mInterval.
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                receivehandler.postDelayed(mStatusChecker, 1000/15);
            }
        }
    };
}
