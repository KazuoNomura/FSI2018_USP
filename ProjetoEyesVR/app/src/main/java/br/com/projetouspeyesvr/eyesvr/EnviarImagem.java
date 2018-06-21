package br.com.projetouspeyesvr.eyesvr;

import android.content.Context;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import android.os.Handler;

public class EnviarImagem implements Runnable {
    private OutputStream os;
    private Socket connection;
    private Context mContext;
    private MainActivity mActivityInstance;
    private Handler mHandler;
    private String clientIP;
    private int clientPort;

    public EnviarImagem(Context context, Socket s, Handler handler) {
        super();
        mContext=context;
        mHandler = handler;
        connection = s;
        mActivityInstance = (MainActivity)mContext;
    }

    @Override
    public void run() {
        clientIP = connection.getInetAddress().toString().replace("/", "");
        clientPort = connection.getPort();
        try {
            connection.setKeepAlive(true);
            os = connection.getOutputStream();
            while (true) {
                DataOutputStream dos = new DataOutputStream(os);
                dos.writeInt(4);
                dos.writeUTF("#@@#");
                dos.writeInt(mActivityInstance.showCamera.mFrameBuffer.size());
                dos.writeUTF("-@@-");
                dos.flush();
                System.out.println(mActivityInstance.showCamera.mFrameBuffer.size());
                dos.write(mActivityInstance.showCamera.mFrameBuffer.toByteArray());
                dos.flush();
                Thread.sleep(1000 / 15);
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (os != null)
                    os.close();

            } catch (Exception e2) {
                e.printStackTrace();
            }
        }
    }
}
