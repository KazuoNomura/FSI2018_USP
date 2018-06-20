package br.com.projetouspeyesvr.eyesvr;
import android.app.Activity;
import android.app.Application;
import android.content.*;
import android.net.NetworkInfo;
import android.net.wifi.*;
import android.net.wifi.p2p.*;
import android.os.AsyncTask;
import android.os.Looper;
import android.os.NetworkOnMainThreadException;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.app.AlertDialog;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
public class ConnManager {
	private final ListView pl;
	private SocketListener sockcb;
	private ServerSocket ss;
	private Context topctx;
	private WifiP2pManager.Channel chan;
	private WifiP2pManager mgr;
	private ArrayAdapter<WifiP2pDevice> ps;
	private final IntentFilter ifilter = new IntentFilter();

	ConnManager(Activity ctx, Looper l, SocketListener sl) {
		sockcb = sl; // callback; see below
		topctx = ctx; // need this for several things
		ifilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		ifilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		ifilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		ifilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		mgr = (WifiP2pManager) ctx.getSystemService(Context.WIFI_P2P_SERVICE);
		chan = mgr.initialize(ctx, l, null);

		// build potential connectees list
		ps = new ArrayAdapter<WifiP2pDevice>(ctx, R.layout.list_element, new ArrayList<WifiP2pDevice>());
		pl = (ListView) ctx.findViewById(R.id.peerlist);
		pl.setAdapter(ps);
		pl.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
				// when the user chooses a peer, connect to it
				connect(ps.getItem(pos));
			}
		});
		toast("Started");
		unpause();
	}

	private void connect(WifiP2pDevice p) {
		WifiP2pConfig c = new WifiP2pConfig();
		c.deviceAddress = p.deviceAddress;
		c.wps.setup = WpsInfo.PBC;
		mgr.connect(chan, c, new WifiP2pManager.ActionListener() {
			@Override
			public void onSuccess() {
				// the connection state will be changed and that will be noticed by the BroadcastReceiver below
				toast("Connection established successfully");
			}
			@Override
			public void onFailure(int reason) {
				toast("Connection attempt failed");
			}
		});
	}

	public void pause() {
		// must be called by onPause()
		topctx.unregisterReceiver(br);
	}
	public void unpause() {
		// must be called by onResume()
		topctx.registerReceiver(br, ifilter);
	}

	private void toast(String s) {
		// for debugging
		AlertDialog ad = new AlertDialog.Builder(topctx).create();
		ad.setTitle(s);
		ad.setMessage(s);
		ad.show();
	}

	private final BroadcastReceiver br = new BroadcastReceiver() {
		@Override
		public void onReceive(Context ctx, Intent i) {
			String action = i.getAction();
			if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
				if(i.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1) == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
					// if p2p has been enabled, find peers
					mgr.discoverPeers(chan, new WifiP2pManager.ActionListener() {
						@Override
						public void onSuccess() {
							//toast("Peer discovery has been started");
						}
						@Override
						public void onFailure(int reason) {
							//toast("Peer discovery couldn't be started");
						}
					});
				} else {
					//toast("Wifi P2P is disabled");
				}
			} else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
				// when peers have been found, ask for the list
				//toast("Going to request peer list now");
				try {
					mgr.requestPeers(chan, new WifiP2pManager.PeerListListener() {
						@Override
						public void onPeersAvailable(WifiP2pDeviceList l) {
							// when the list arrives, set ps to it so the user can choose
							// (the view should update automagically)
							try {
								Collection<WifiP2pDevice> npl = l.getDeviceList();
								ps.clear();
								ps.addAll(npl);
								//toast("Peer list retrieved");
							} catch(Exception e) {
								//toast("PERR LIST ERROR " + e.getMessage());
							}
						}
					});
				} catch(Exception e) {
					//toast("COULDN't request perr list: " + e.getMessage());
				}
			} else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
				NetworkInfo ni = (NetworkInfo) i.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
				if(ni.isConnected()) {
					// got a connection! now we need a socket
					toast("Connection attempt will begin");
					mgr.requestConnectionInfo(chan, new WifiP2pManager.ConnectionInfoListener() {
						@Override
						public void onConnectionInfoAvailable(final WifiP2pInfo ci) {
							// so many callbacks...
							toast("Connection established");
							final InetAddress growner = ci.groupOwnerAddress;
							Log.e("Sou o Proprietario",String.valueOf(ci.isGroupOwner));
							Log.e("Grupo Formado", String.valueOf(ci.groupFormed));
							if(ci.groupFormed && ci.isGroupOwner) {
								// I am a server
								toast("I am a server");
								new AsyncTask<Void, Void, Void>() {
									@Override
									protected Void doInBackground(Void... useless__) {
										// should be done in the background because it blocks
										try {
											// 2014 is the port, named after ACH2014
											ServerSocket ss = new ServerSocket(2014);
											//Asnctask anula contexto. Toast quebra o app.
											//toast("ServerSocket is ready");
											Socket s = ss.accept();
											//toast("Socket established");
											sockcb.onSocketReady(s);
										} catch(IOException e) {
											//toast("Socket not established");
											sockcb.onSocketFail(e);
										}
										return null;
									}
									@Override
									protected void onPostExecute(Void useless__) {
										// executed on main thread, can now call callback
										pl.setVisibility(View.INVISIBLE);
									}
								}.execute();
							} else if(ci.groupFormed && !ci.isGroupOwner) {
								toast("I am a client");
								new AsyncTask<Void, Void, Void>() {
									@Override
									protected Void doInBackground(Void... useless__) {
										try {
											// just connect and call the callback in a single step
											sockcb.onSocketReady(new Socket(growner, 2014));
										} catch(IOException e) {
											sockcb.onSocketFail(e);
										}
										return null;
									}
									@Override
									protected void onPostExecute(Void useless__) {
										pl.setVisibility(View.INVISIBLE);
									}
								}.execute();
							}
						}
					});
				} else {
					toast("Disconected");
					// disconnected
					if(ss != null) {
						try {
							ss.close();
						} catch(Exception e_) {
							/* can't do much now */
						}
						ss = null;
					}
				}
			} else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
				// what do I do here?
				// is it important?
			}
		}
	};
	/* me hates android :'( */
	// callback class, called when done
	public static abstract class SocketListener {
		protected abstract void onSocketReady(Socket s);
		protected abstract void onSocketFail(IOException e);
	}
}

