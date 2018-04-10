package br.com.projetouspeyesvr.eyesvr;
import android.net.wifi.p2p.*;
import android.net.wifi.*;
import android.net.NetworkInfo;
import android.content.*;
import android.widget.Toast;
import android.os.Looper;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.net.InetAddress;
public class ConnManager {
	public void setup(Context ctx, Looper l) {
		topctx = ctx;
		ifilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		ifilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		ifilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		ifilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		mgr = (WifiP2pManager) ctx.getSystemService(Context.WIFI_P2P_SERVICE);
		chan = mgr.initialize(ctx, l, null);
	}

	private void connect() {
		WifiP2pDevice p = ps.get(0);
		WifiP2pConfig c = new WifiP2pConfig();
		c.deviceAddress = p.deviceAddress;
		c.wps.setup = WpsInfo.PBC;
		mgr.connect(chan, c, new WifiP2pManager.ActionListener() {
			@Override
			public void onSuccess() {
				// Connected successfully
			}
			@Override
			public void onFailure(int reason) {
				// do something about it
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
	private List<WifiP2pDevice> ps = new ArrayList<WifiP2pDevice>();
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
							// Peer discovery has been successfully started
							toast("Peer discovery has been started");
						}
						@Override
						public void onFailure(int reason) {
							// do something about it
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
						toast("Peer list changed");
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
							} else if(ci.groupFormed) {
							}
						}
					});
				}
			} else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
			}
		}
	};
}

