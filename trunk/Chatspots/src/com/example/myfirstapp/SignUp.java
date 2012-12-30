package com.example.myfirstapp;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.commons.net.ftp.FTPClient;

import android.R.layout;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.util.Patterns;

public class SignUp extends Activity {
	String response = "";
	Intent intent;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);
  
    }
    
    public void sign_up(View view) throws MalformedURLException, JSONException {
    	intent = new Intent(this, SignUpStatus.class);
		
    	EditText username = (EditText) findViewById(R.id.username);
        final String str_username = username.getText().toString();
        
        EditText password = (EditText) findViewById(R.id.password);
        String str_password = password.getText().toString();
        
        EditText confirm_password = (EditText) findViewById(R.id.confirm_password);
        String str_confirm_password = confirm_password.getText().toString();
        
        EditText email = (EditText) findViewById(R.id.email);
        String st_email = email.getText().toString();
        
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        
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
						
						FTPClient client = new FTPClient();
					    
					    try {
					        client.connect("chatspots.sytes.net");
					        client.login("chatspots", "P!CtuR3$");
					        client.makeDirectory(str_username);
					        
					        client.logout();
					    } catch (IOException e) {
					        e.printStackTrace();
					    }
						
						runOnUiThread(new Runnable() {
						     public void run() {

						    	TextView err = (TextView) findViewById( R.id.sign_up_error );
								System.out.println(err);
							    err.setText(" ");
															    }
						});
						startActivity(intent);
					}
					else if(response.equals("201"))
					{
						runOnUiThread(new Runnable() {
						     public void run() {

						    	 TextView err = (TextView) findViewById( R.id.sign_up_error );
							        err.setText("Username already exists!");
							        
							        EditText u = (EditText) findViewById( R.id.username );
							        u.setText("");
							        
							        						    }
						});
						
					}
					else
					{
						runOnUiThread(new Runnable() {
						     public void run() {

						    	 TextView err = (TextView) findViewById( R.id.sign_up_error );
							        err.setText("Sign up failed! An error occured");
							   }
						});
						
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        });

        if(str_username.equals("") || str_password.equals("") || st_email.equals(""))
        {
			runOnUiThread(new Runnable() {
			     public void run() {

			    	 TextView err = (TextView) findViewById( R.id.sign_up_error );
				        err.setText("Please fill all the fields!");
				   }
			});
        }
        else if(!str_password.equals(str_confirm_password))
        {
        	runOnUiThread(new Runnable() {
			     public void run() {

			    	 TextView err = (TextView) findViewById( R.id.sign_up_error );
				     err.setText("Passwords don't match!");
				     EditText password = (EditText) findViewById(R.id.password);
				     password.setText("");
				     EditText confirm_password = (EditText) findViewById(R.id.confirm_password);
				     confirm_password.setText("");
				   }
			});
        }
        else if(str_password.length()<6)
        {
        	runOnUiThread(new Runnable() {
			     public void run() {

			    	 TextView err = (TextView) findViewById( R.id.sign_up_error );
				     err.setText("Password too short! Minimum 6 characters");
				     EditText password = (EditText) findViewById(R.id.password);
				     password.setText("");
				     EditText confirm_password = (EditText) findViewById(R.id.confirm_password);
				     confirm_password.setText("");
				   }
			});
        }
        else if(!pattern.matcher(st_email).matches())
        {
        	runOnUiThread(new Runnable() {
			     public void run() {
			    	 TextView err = (TextView) findViewById( R.id.sign_up_error );
				     err.setText("Invalid email address!");
				   }
			});
        }
        else
        {
        socket.send(new JSONObject().put("Action", "CreateUserProfile").put("Parameters", 
        		new JSONArray().put(new JSONObject().put("UserName", str_username).put("Password", md5(str_password)).put("UserEmail", st_email))
        		));
        
        }
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
