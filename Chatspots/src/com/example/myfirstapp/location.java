package com.example.myfirstapp;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class location extends Activity {
	Timer timer1;
	LocationManager lm;
	Location locationResult;
	boolean gps_enabled = false;
	boolean network_enabled = false;
	boolean GPS = false, NET = false;
	ProgressDialog progress_dialog;
	String msg;
	Context mContext;
	int userid;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 setContentView(R.layout.profile);
		 Intent intent_old = getIntent();
	     userid = Integer.parseInt(intent_old.getStringExtra("userid"));
	     timer1 = new Timer();
		 mContext = this.getApplicationContext();
		 progress_dialog = new ProgressDialog(this);
		 msg = getString(R.string.toast_location);
		 progress_dialog.setMessage(msg);
		 getLocation();
	}

	/**
	 * If only the GPS is disabled, this method is called to remind the user
	 * that the location won't be accurate. And send the user to the setting
	 * location to enable it, if the user wants.
	 */
	public void onGPS() {
		Log.e("checks", "NO GPS ");
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Location Manager");
		msg = getString(R.string.toast_noGps);
		builder.setMessage(msg);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Launch settings, allowing user to make a change
				Log.e("Before Setting", gps_enabled + " " + network_enabled);
				startActivityForResult(
						new Intent(
								android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS),
						1);

			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				getloca();
			}
		});
		builder.create().show();
	}

	/**
	 * This is called if the GPS and network are disabled It asks the user to
	 * enable any to be able to get the location, by sending the user to
	 * settings page if user agree's
	 */
	public void noLoca() {
		Log.e("Checks", "NO GPS and Location");
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Location Manager");
		msg = getString(R.string.toast_noGps_noNet);
		builder.setMessage(msg);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.e("Before Setting", gps_enabled + " " + network_enabled);
				startActivityForResult(
						new Intent(
								android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS),
						0);

			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// No location service, no Activity
				locationResult = null;
				getloca();
			}
		});
		builder.create().show();
	}

	/**
	 * This won't be called unless at least the GPS or network is enabled gets
	 * the location of the user by the service enabled it will set the timer to
	 * wait for 2000 miliseconds, incase non of the services where able to
	 * locate the user. To get then the latest saved location. It shows a
	 * progress dialog untill it gets the location
	 */
	public void getloca() {

		Log.e("after check", "getting location ");
		gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
		network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		if (!gps_enabled && !network_enabled) {
			
			msg = getString(R.string.toast_disabled);
			progress_dialog.setMessage(msg);
			progress_dialog.show();
			timer1.schedule(new GetLastLocation(), 1500);
		} else {
			progress_dialog.show();
			if (gps_enabled)
				lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
						locationListenerGps);
			if (network_enabled)
				lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0,
						0, locationListenerNetwork);

			Log.e("loaction class", "before set timer");
			timer1.schedule(new GetLastLocation(), 20000);
			Log.e("loaction class", "after schedule");
		}

	}

	/**
	 * gets the result from the location settings
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.e("After Setting", resultCode + " " + requestCode);
		// If the request went well (OK) and the request was
		// PICK_CONTACT_REQUEST
		if (resultCode == 0 && requestCode == 0) {

			String provider = Settings.Secure.getString(getContentResolver(),
					Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
			Log.e("After Setting", "Code 0 = " + provider);
			if (!provider.contains("gps"))
				onGPS();
			else
				getloca();

		}
		if (resultCode == 0 && requestCode == 1) {
			Log.e("After Setting", "Code 1");
			getloca();
		}
	}

	public void getLocation() {
		// I use LocationResult callback class to pass location value from
		// MyLocation to user code.
		// locationResult=result;
		if (lm == null)
			lm = (LocationManager) mContext
					.getSystemService(Context.LOCATION_SERVICE);

		// exceptions will be thrown if provider is not permitted.
		try {
			gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

		} catch (Exception ex) {
			Toast toast = Toast.makeText(mContext, R.string.exception_gps,
					Toast.LENGTH_LONG);
			toast.show();
			toast.setGravity(Gravity.CENTER, 0, 0);
			Log.e("GPS", "NOT enabled");

		}
		try {
			network_enabled = lm
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		} catch (Exception ex) {
			Toast toast = Toast.makeText(mContext, R.string.exception_net,
					Toast.LENGTH_LONG);
			toast.show();
			toast.setGravity(Gravity.CENTER, 0, 0);
			Log.e("Network", "NOT enabled");

		}

		// don't start listeners if no provider is enabled

		if (!gps_enabled && !network_enabled)
			noLoca();
		else if (!gps_enabled)
			onGPS();
		else
			getloca();

	}

	LocationListener locationListenerGps = new LocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			GPS = true;
			Log.e("GPS location",
					location.getLatitude() + " " + location.getLongitude());

			timer1.cancel();
			locationResult = location;
			Toast toast = Toast.makeText(
					mContext,
					"GPS location" + location.getLatitude() + " "
							+ location.getLongitude(), Toast.LENGTH_LONG);
			toast.show();
			toast.setGravity(Gravity.CENTER, 0, 0);
			lm.removeUpdates(this);
			lm.removeUpdates(locationListenerNetwork);
			result();
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};

	LocationListener locationListenerNetwork = new LocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			NET = true;
			Log.e("Network location",
					location.getLatitude() + " " + location.getLongitude());

			timer1.cancel();
			locationResult = location;

			Toast toast = Toast.makeText(mContext, "Network location"
					+ location.getLatitude() + " " + location.getLongitude(),
					Toast.LENGTH_LONG);
			toast.show();
			toast.setGravity(Gravity.CENTER, 0, 0);
			lm.removeUpdates(this);
			lm.removeUpdates(locationListenerGps);
			result();
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};

	class GetLastLocation extends TimerTask {
		@Override
		public void run() {
			Log.e("get Location", "last location");

			location.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					progress_dialog.dismiss();
					Toast.makeText(location.this,R.string.toast_getting_oldLoca,
							Toast.LENGTH_LONG).show();
					// x.show();
				}
			});

			lm.removeUpdates(locationListenerGps);
			lm.removeUpdates(locationListenerNetwork);

			Location net_loc = null, gps_loc = null;
	
			if (gps_enabled)
				gps_loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (network_enabled)
				net_loc = lm
						.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

			// if there are both values use the latest one
			if (gps_loc != null && net_loc != null) {
				if (gps_loc.getTime() > net_loc.getTime()) {
					GPS = true;
					locationResult = gps_loc;
				} else {
					NET = true;
					locationResult = net_loc;
				}
				Log.e("old by time location", net_loc.getLatitude() + " "
						+ net_loc.getLongitude());
				result();
				return;
			}

			if (gps_loc != null) {
				Log.e("old gps location",
						net_loc.getLatitude() + " " + net_loc.getLongitude());
				GPS = true;
				locationResult = gps_loc;
				result();
				return;
			}
			if (net_loc != null) {
				Log.e("old net location",
						net_loc.getLatitude() + " " + net_loc.getLongitude());
				NET = true;
				locationResult = net_loc;
				result();
				return;
			}
			Log.e("old location", null + "");
			locationResult = null;
			result();
		}

	}

	public void result() {
		// Log.e("in result", locationResult.toString());
		Intent mIntent = new Intent();
		if (locationResult == null) {
			Log.e("Result", "canceled");
			setResult(RESULT_CANCELED);
		} else {
			Log.e("Result", "ok lng= " + locationResult.getLongitude()
					+ "lat= " + locationResult.getLatitude() + " " + GPS + " "
					+ NET);
			Bundle bundle = new Bundle();
			bundle.putDouble("lng", locationResult.getLongitude());
			bundle.putDouble("lat", locationResult.getLatitude());
			
			//WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
			//WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			//String userIP =  Formatter.formatIpAddress(wifiInfo.getIpAddress());
			String userIP = "";
			try
			{
				URL whatismyip = new URL("http://automation.whatismyip.com/n09230945.asp");
			    URLConnection connection = whatismyip.openConnection();
			    connection.addRequestProperty("Protocol", "Http/1.1");
			    connection.addRequestProperty("Connection", "keep-alive");
			    connection.addRequestProperty("Keep-Alive", "1000");
			    connection.addRequestProperty("User-Agent", "Web-Agent");

			    BufferedReader in = 
			        new BufferedReader(new InputStreamReader(connection.getInputStream()));

			    userIP = in.readLine(); //you get the IP as a String
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			String user_phone = Build.BRAND+" "+Build.DEVICE;
			SocketIO socket=new SocketIO();
	        try {
	            socket = new SocketIO("http://chatspots.sytes.net:1333");
	        } catch (MalformedURLException e1) {
	            e1.printStackTrace();
	        }
	        socket.connect(new IOCallback() {
	            public void onMessage(JSONObject json, IOAcknowledge ack) {
	                try {

	                    System.out.println("Server said:" + json.toString(2));


	                } catch (JSONException e) {
	                    e.printStackTrace();
	                }
	            }

	            public void onMessage(String data, IOAcknowledge ack) {
	                System.out.println("Server said: " + data);
	            }

	            public void onError(SocketIOException socketIOException) {
	                System.out.println("an Error occured");

	                socketIOException.printStackTrace();
	            }

	            public void onDisconnect() {
	                System.out.println("Connection terminated.");
	            }

	            public void onConnect() {
	                System.out.println("Connection established");
	            }

	            public void on(String event, IOAcknowledge ack, Object... args) {
	                System.out.println("Server triggered event '" + event + "'");
	                
	            }
	        });

	        
	        try {
				socket.send(new JSONObject().put("Action", "UpdateUserInformation").put("Parameters", 
						new JSONArray().put(new JSONObject().put("UserID", userid+"").put("UserIP", userIP)
								.put("UserClient", "1").put("UserMobilePhone", user_phone)
								.put("UserLocationLat", ""+locationResult.getLatitude()).put("UserLocationLong", ""+locationResult.getLongitude()))
						));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (GPS) {
				bundle.putString("LocationBy", "GPS");
			} else {
				bundle.putString("LocationBy", "NET");
			}
			mIntent.putExtras(bundle);
			setResult(RESULT_OK, mIntent);
		}

		progress_dialog.dismiss();
		super.finish();

	}

}