package br.com.projetouspeyesvr.eyesvr;
import android.app.Activity;
import android.content.*;
import android.net.NetworkInfo;
import android.net.wifi.*;
import android.net.wifi.p2p.*;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
public class ConnManager {
	ConnManager(Activity ctx, Looper l) {
		topctx = ctx;
		ifilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		ifilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		ifilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		ifilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		mgr = (WifiP2pManager) ctx.getSystemService(Context.WIFI_P2P_SERVICE);
		chan = mgr.initialize(ctx, l, null);

		ps = new ArrayAdapter<WifiP2pDevice>(ctx, R.id.peerlist, new ArrayList<WifiP2pDevice>());
		final ListView pl = (ListView) ctx.findViewById(R.id.peerlist);
		pl.setAdapter(ps);
		pl.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
				connect(ps.getItem(pos));
			}
		});
	}

	private void connect(WifiP2pDevice p) {
		WifiP2pConfig c = new WifiP2pConfig();
		c.deviceAddress = p.deviceAddress;
		c.wps.setup = WpsInfo.PBC;
		mgr.connect(chan, c, new WifiP2pManager.ActionListener() {
			@Override
			public void onSuccess() {
				toast("Connection established successfully");
			}
			@Override
			public void onFailure(int reason) {
				toast("Connection attempt failed");
			}
		});
	}

	public void pause() {
		topctx.unregisterReceiver(br);
	}
	public void unpause() {
		topctx.registerReceiver(br, ifilter);
	}

	private void toast(String s) {
		Toast.makeText(topctx, s, Toast.LENGTH_LONG).show();
	}

	private Context topctx;
	private WifiP2pManager.Channel chan;
	private WifiP2pManager mgr;
	private ArrayAdapter<WifiP2pDevice> ps;
	private final IntentFilter ifilter = new IntentFilter();
	private final BroadcastReceiver br = new BroadcastReceiver() {
		@Override
		public void onReceive(Context ctx, Intent i) {
			String action = i.getAction();
			if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
				if(i.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1) == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
					mgr.discoverPeers(chan, new WifiP2pManager.ActionListener() {
						@Override
						public void onSuccess() {
							toast("Peer discovery has been started");
						}
						@Override
						public void onFailure(int reason) {
							toast("Peer discovery couldn't be started");
						}
					});
				} else {
					toast("Wifi P2P is disabled");
				}
			} else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
				mgr.requestPeers(chan, new WifiP2pManager.PeerListListener() {
					@Override
					public void onPeersAvailable(WifiP2pDeviceList l) {
						Collection<WifiP2pDevice> npl = l.getDeviceList();
						if(!npl.equals(ps)) {
							ps.clear();
							ps.addAll(l.getDeviceList());
						}
					}
				});
			} else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
				NetworkInfo ni = (NetworkInfo) i.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
				if(ni.isConnected()) {
					mgr.requestConnectionInfo(chan, new WifiP2pManager.ConnectionInfoListener() {
						@Override
						public void onConnectionInfoAvailable(final WifiP2pInfo ci) {
							toast("Connection established");
							InetAddress growner = ci.groupOwnerAddress;
							if(ci.groupFormed && ci.isGroupOwner) {
								toast("I am a server");
							} else if(ci.groupFormed) {
								toast("I am a client");
							}
						}
					});
				}
			} else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
			}
		}
	};
}

