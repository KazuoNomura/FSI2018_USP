package br.com.projetouspeyesvr.eyesvr;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import br.com.projetouspeyesvr.eyesvr.ConnManager;

import java.net.Socket;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    public static final String ALLOW_KEY = "ALLOWED";
    private ConnManager cm;
    Button button_camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button_camera = (Button) findViewById(R.id.button_camera);
        button_camera.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                //openCamera();
            }
        });

        cm = new ConnManager(this, getMainLooper(), new ConnManager.SocketListener() {
            @Override
            protected void onSocketReady(Socket s) {
                // connection is ok, s is our channel
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
	        alertDialog.setTitle("Connection");
                alertDialog.setMessage("We now connected are.");
                alertDialog.show();
            }
            @Override
            protected void onSocketFail(IOException e) {
                // something went wrong
                AlertDialog a = new AlertDialog.Builder(MainActivity.this).create();
                a.setTitle("Connection");
                a.setMessage("We connected are not; something wrong went.\n" + e.getMessage());
                a.show();
            }
        });
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
}
