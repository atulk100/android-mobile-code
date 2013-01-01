package com.example.myfirstapp;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.MalformedURLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;


import android.R.layout;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Profile extends Activity {
    String userid;
    Intent intent;
    LocationManager locationManager;
    String provider;
    SocketIO socket=new SocketIO();
    
    String username = "";
    String usermail = "";
    String fname = "";
    String lname = "";
    String bdate = "";
    String gender = "";
    String msisdn = "";
    String picture = "";
    ImageView img;
    Bitmap bitmap;
    String flag  = "false";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        // Get the message from the intent
        Intent intent_old = getIntent();
        userid = intent_old.getStringExtra("userid");
        
        intent = new Intent(this, EditProfile.class);
        intent.putExtra("userid", userid);
        fetch_profile();
    }
    
    public void fetch_profile()
    {
    	final ProgressDialog pdia;
    	pdia = ProgressDialog.show(Profile.this, "",
				"Loading.....", true);
    	System.out.println("in fetch profile");
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
                try {
					String response = ((JSONObject)args[0]).get("RequestStatus").toString();
					String params = ((JSONObject)args[0]).get("Parameters").toString();
					if(response.equals("200"))
					{
						System.out.println("response 200");
						username = (((JSONObject)((JSONArray)((JSONObject)args[0]).get("Parameters")).get(0)).get("UserName")).toString();
				        usermail = (((JSONObject)((JSONArray)((JSONObject)args[0]).get("Parameters")).get(0)).get("UserEmail")).toString();
				        if(params.contains("UserFirstName"))
				        {
					        fname = (((JSONObject)((JSONArray)((JSONObject)args[0]).get("Parameters")).get(0)).get("UserFirstName")).toString();
					        lname = (((JSONObject)((JSONArray)((JSONObject)args[0]).get("Parameters")).get(0)).get("UserLastName")).toString();
					        bdate = (((JSONObject)((JSONArray)((JSONObject)args[0]).get("Parameters")).get(0)).get("UserBirthDate")).toString();
					        gender = (((JSONObject)((JSONArray)((JSONObject)args[0]).get("Parameters")).get(0)).get("UserGender")).toString();
					        msisdn = (((JSONObject)((JSONArray)((JSONObject)args[0]).get("Parameters")).get(0)).get("UserMSISDN")).toString();
				        }
				        //picture = (((JSONObject)((JSONArray)((JSONObject)args[0]).get("Parameters")).get(0)).get("UserPicture")).toString();
				        runOnUiThread(new Runnable() {
						     public void run() {
						    	 display_profile();
						    	 download_image();
								}
						});
				        pdia.dismiss();
				        
					}
					else
					{
						runOnUiThread(new Runnable() {
						     public void run() {

						    	 Toast.makeText(getApplicationContext(),
					                        "Error fetching User Profile", Toast.LENGTH_SHORT).show();
								}
						});
						
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        });

        
        try {
			socket.send(new JSONObject().put("Action", "RetrieveProfile").put("Parameters", 
					new JSONArray().put(new JSONObject().put("UserID", userid))
					));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    
    public void display_profile()
    {
    	TextView u = (TextView) findViewById( R.id.welcome );
        u.setText("Welcome "+username+"!");
        intent.putExtra("username", username);
        
        TextView m = (TextView) findViewById( R.id.display_email );
        m.setText("Email: "+usermail);
        intent.putExtra("usermail", usermail);
        
        TextView f = (TextView) findViewById( R.id.fname );
        f.setText("First Name: "+fname);
        intent.putExtra("fname", fname);
        
        TextView l = (TextView) findViewById( R.id.lname );
        l.setText("Last Name: "+lname);
        intent.putExtra("lname", lname);
        
        TextView b = (TextView) findViewById( R.id.display_username );
        b.setText("Birth Date: "+bdate);
        intent.putExtra("bdate", bdate);
        
        TextView g = (TextView) findViewById( R.id.gender );
        intent.putExtra("gender", gender);
        
        TextView ms = (TextView) findViewById( R.id.msisdn );
        ms.setText("Mobile: "+msisdn);
        intent.putExtra("msisdn", msisdn);
        
        
        img = (ImageView) findViewById(R.id.profile_pic);
        if(gender.equals("F"))
        {
        	g.setText("Gender: Female");
        	img.setImageResource(R.drawable.female);
        }
        else
        {
        	g.setText("Gender: Male");
        	img.setImageResource(R.drawable.male);
        }
    }
    
    
    public void edit_profile(View view) throws MalformedURLException, JSONException {
    	intent.putExtra("flag", flag);
        startActivity(intent);
    }
    
    public static void upload_image(File f)
	{
		Session session ;
	     Channel channel = null;
	    ChannelSftp sftp;
	    JSch ssh = new JSch();
	   try {
	        session =ssh.getSession("chatspots", "chatspots.sytes.net");
	        System.out.println("JSch JSch JSch Session created.");
	        session.setPassword("P!CtuR3$");
	        java.util.Properties config = new java.util.Properties(); 
	        config.put("StrictHostKeyChecking", "no");
	        session.setConfig(config);
	        session.connect();
	        System.out.println("JSch JSch Session connected.");
	        System.out.println("Opening Channel.");
	        channel = session.openChannel("sftp"); 
	        channel.connect();
	        sftp= (ChannelSftp)channel;
	        //sftp.mkdir("dinaz");
	        sftp.cd("/home/chatspots/dinaz");
	        sftp.put(new FileInputStream(f), f.getName());

	    }
	    catch(Exception e){

	    }
	} 
	
	
	public void download_image()
	{
//		String SFTPHOST = "chatspots.sytes.net";
//		int    SFTPPORT = 22;
//		String SFTPUSER = "chatspots";
//		String SFTPPASS = "P!CtuR3$";
//		String SFTPWORKINGDIR = "//home/chatspots/dinaz/";
//		Session     session     = null;
//		Channel     channel     = null;
//		ChannelSftp channelSftp = null;
//		try{
//			JSch jsch = new JSch();
//			session = jsch.getSession(SFTPUSER,SFTPHOST,SFTPPORT);
//			session.setPassword(SFTPPASS);
//			java.util.Properties config = new java.util.Properties();
//			config.put("StrictHostKeyChecking", "no");
//			session.setConfig(config);
//			session.connect();
//			channel = session.openChannel("sftp");
//			channel.connect();
//			channelSftp = (ChannelSftp)channel;
//			channelSftp.cd(SFTPWORKINGDIR);
//			BufferedInputStream bis = new BufferedInputStream(channelSftp.get("dina.png"));
//			File newFile = new File("C:\\Documents and Settings\\dina.helal\\Desktop\\dina.png");
//			OutputStream os = new FileOutputStream(newFile);
//			BufferedOutputStream bos = new BufferedOutputStream(os);
//			bis.close();
//			bos.close();
//		}catch(Exception ex){
//
//			ex.printStackTrace();
//	
//		}
//
//		 
		
		Session session = null;
	    Channel channel = null;
	    ChannelSftp channelSftp = null;
	    boolean success = false;

	    try {
	        JSch jsch = new JSch();
	        session =jsch.getSession("chatspots", "chatspots.sytes.net");
	        System.out.println("JSch JSch JSch Session created.");
	        session.setPassword("P!CtuR3$");
	        
	        session.setConfig("StrictHostKeyChecking", "no");
	        session.connect();
	        
	        channel = session.openChannel("sftp");
	        channel.connect();
	        channelSftp = (ChannelSftp) channel;

	        
	        
	        File storagePath = new File(Environment.getExternalStorageDirectory() + "/chatspots/cache/"); 
	        storagePath.mkdirs(); 
	        File myImage = new File(storagePath, userid + ".png");
	        
//	        try { 
//	            FileOutputStream out = new FileOutputStream(myImage); 
//	            profile_pic.compress(Bitmap.CompressFormat.PNG, 80, out); 
//	            out.flush();    
//	            out.close();
//	        } catch (Exception e) { 
//	            e.printStackTrace(); 
//	        }   

	        flag = "true";
	        	        
	        //String destPath = "C:\\Documents and Settings\\dina.helal\\Desktop\\dina.png";        
	        channelSftp.cd("/home/chatspots/"+username+"/");
	        channelSftp.get(userid+".png" , Environment.getExternalStorageDirectory() + "/chatspots/cache/"); 
	        bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/chatspots/cache/"+userid+".png");
	        img.setImageBitmap(bitmap);

	    } catch (JSchException ex) {
	     
	    } catch (SftpException ex) {

	   }catch (Exception ex) {

	    }finally {
	        if (channelSftp.isConnected()) {
	            try {
	                session.disconnect();
	                channel.disconnect();
	                channelSftp.quit();
	            } catch (Exception ioe) {

	            }
	        }
	    }
	   
	}
    
public void Delete_profile(View view) throws MalformedURLException, JSONException {
	intent = new Intent(this, Main.class);    
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
            try {
				String response = ((JSONObject)args[0]).get("RequestStatus").toString();
				System.out.println(response);
				if(response.equals("200"))
				{
//					Toast.makeText(getApplicationContext(),
//	                        "Account deleted successfully", Toast.LENGTH_SHORT).show();
					
			    	startActivity(intent);
					
				}
				else
				{
					Toast.makeText(getApplicationContext(),
	                        "Account not deleted", Toast.LENGTH_SHORT).show();
//					runOnUiThread(new Runnable() {
//					     public void run() {
//
//					    	 TextView err = (TextView) findViewById( R.id.login_error );
//						        err.setText("Invalid username or password!");
//						        
//						        EditText u = (EditText) findViewById( R.id.username );
//						        u.setText("");
//						        
//						        EditText p = (EditText) findViewById( R.id.password );
//						        p.setText("");
//														    }
//					});
					
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    });

    
    socket.send(new JSONObject().put("Action", "DeleteProfile").put("Parameters", 
    		new JSONArray().put(new JSONObject().put("UserID", userid))
    		));
		
    }

		public void gps_location(View v) {
			Log.e("GPS", "IN gps");
			Intent intent = new Intent(this, location.class);
			intent.putExtra("userid", userid+"");
			startActivityForResult(intent, 0);
		
		}

}
