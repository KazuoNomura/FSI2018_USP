package br.com.projetouspeyesvr.eyesvr;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import br.com.projetouspeyesvr.eyesvr.ConnManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ConnManager cm = new ConnManager();
        cm.setup(this, getMainLooper());
        cm.unpause();
    }
}
