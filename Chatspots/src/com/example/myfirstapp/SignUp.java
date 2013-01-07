package com.example.myfirstapp;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.commons.net.ftp.FTPClient;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import android.R.layout;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.util.Patterns;

public class SignUp extends Activity {
	String response = "";
	Intent intent;
	String str_username = "";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);
  
    }
    
    public void sign_up(View view) throws MalformedURLException, JSONException {
    	intent = new Intent(this, SignUpStatus.class);
		
    	EditText username = (EditText) findViewById(R.id.username);
        str_username = username.getText().toString();
        
        EditText password = (EditText) findViewById(R.id.password);
        String str_password = password.getText().toString();
        
        EditText confirm_password = (EditText) findViewById(R.id.confirm_password);
        String str_confirm_password = confirm_password.getText().toString();
        
        EditText email = (EditText) findViewById(R.id.email);
        String st_email = email.getText().toString();
        
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        
        final ProgressDialog pdia;
    	pdia = ProgressDialog.show(SignUp.this, "",
				"Loading.....", true);
        
        
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
                runOnUiThread(new Runnable() {
				     public void run() {

				    	 TextView err = (TextView) findViewById( R.id.sign_up_error);
					        err.setText("Error contacting server! Please try again later");
					        
				     	}
				});
                pdia.dismiss();
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
						//create_directory();
						runOnUiThread(new Runnable() {
						     public void run() {

						    	TextView err = (TextView) findViewById( R.id.sign_up_error );
								System.out.println(err);
							    err.setText(" ");
							    startActivity(intent);
						     }
						});
						
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
					pdia.dismiss();
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
			pdia.dismiss();
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
        	pdia.dismiss();
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
        	pdia.dismiss();
        }
        else if(!pattern.matcher(st_email).matches())
        {
        	runOnUiThread(new Runnable() {
			     public void run() {
			    	 TextView err = (TextView) findViewById( R.id.sign_up_error );
				     err.setText("Invalid email address!");
				   }
			});
        	pdia.dismiss();
        }
        else
        {
        socket.send(new JSONObject().put("Action", "CreateUserProfile").put("Parameters", 
        		new JSONArray().put(new JSONObject().put("UserName", str_username).put("Password", md5(str_password)).put("UserEmail", st_email))
        		));
        
        }
    }
    
    
    public void create_directory()
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
	        sftp.mkdir(str_username);
	    }
	    catch(Exception e){

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
