package com.example.myfirstapp;

import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.socket.*;
import android.R.layout;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends Activity{
	String response = "";
	Intent intent;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    public void login(View view) throws MalformedURLException, JSONException {
    	intent = new Intent(this, Profile.class);
    	EditText username = (EditText) findViewById(R.id.username);
        String str_username = username.getText().toString();
        
        EditText password = (EditText) findViewById(R.id.password);
        String str_password = password.getText().toString();
        
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
					response = ((JSONObject)args[0]).get("RequestStatus").toString();
					if(response.equals("200"))
					{
						String userid = (((JSONObject)((JSONArray)((JSONObject)args[0]).get("Parameters")).get(0)).get("UserID")).toString();
						intent.putExtra("userid", userid);
						startActivity(intent);
					}
					else
					{
						runOnUiThread(new Runnable() {
						     public void run() {

						    	 TextView err = (TextView) findViewById( R.id.login_error );
							        err.setText("Invalid username or password!");
							        
							        EditText u = (EditText) findViewById( R.id.username );
							        u.setText("");
							        
							        EditText p = (EditText) findViewById( R.id.password );
							        p.setText("");
															    }
						});
						
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        });

        if(str_username.equals(""))
        {
			runOnUiThread(new Runnable() {
			     public void run() {

			    	 TextView err = (TextView) findViewById( R.id.sign_up_error );
				        err.setText("Missing username");
				   }
			});
        }
        else if(str_password.equals(""))
        {
			runOnUiThread(new Runnable() {
			     public void run() {

			    	 TextView err = (TextView) findViewById( R.id.sign_up_error );
				        err.setText("Missing pasword");
				   }
			});
        }
        else
        {
        socket.send(new JSONObject().put("Action", "AuthenticateUser").put("Parameters", 
        		new JSONArray().put(new JSONObject().put("UserName", str_username).put("Password", md5(str_password)))
        		));
        
        }
    }
        
    public void sign_up_page(View view) throws MalformedURLException, JSONException {
    
    	Intent intent = new Intent(this, SignUp.class);
    	startActivity(intent);
    }
    
    public String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}