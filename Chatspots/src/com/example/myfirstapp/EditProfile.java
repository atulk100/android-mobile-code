package com.example.myfirstapp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Vector;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.ChannelSftp.LsEntry;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.OnEditorActionListener;
import android.util.Patterns;
import io.socket.*;

public class EditProfile extends Activity implements
OnEditorActionListener, OnItemClickListener, OnClickListener,
android.view.View.OnClickListener{
	String response = "";
	Intent intent;
	String userid;
	private ProgressDialog dialog;
	private EditText caption;
	private static final int GALLERY = 0;
	private static final int CAMERA = 1;
	Bitmap profile_pic;
	ImageView profile_pic_view;
	String byte_image = "";
	String username = "";
	InputStream in;
	String img_path = "";
	String file_name = "";
	boolean flag = false;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);
        
        Intent intent = getIntent();
        String flag = intent.getStringExtra("flag");
        
        userid = intent.getStringExtra("userid");
        username = intent.getStringExtra("username");
        
        EditText email = (EditText) findViewById(R.id.usermail);
        email.setText(intent.getStringExtra("usermail"));
        
        EditText fname = (EditText) findViewById(R.id.fname);
        fname.setText(intent.getStringExtra("fname"));
        
        EditText lname = (EditText) findViewById(R.id.lname);
        lname.setText(intent.getStringExtra("lname"));
        
        EditText msisdn = (EditText) findViewById(R.id.msisdn);
        msisdn.setText(intent.getStringExtra("msisdn"));
        
        profile_pic_view = (ImageView) findViewById(R.id.profile_pic);
        //pic.setText(intent.getStringExtra("fname"));
        
        ImageView img = (ImageView) findViewById(R.id.profile_pic);
        if(flag.equals("true"))
        {
        	String file_name = intent.getStringExtra("file_name");
        	Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/chatspots/cache/"+file_name);
	        img.setImageBitmap(bitmap);
        }
        String gender = intent.getStringExtra("gender");
        if(gender.equals("F"))
        {
        	RadioButton r = (RadioButton) findViewById(R.id.female);
        	if(flag.equals("false"))
        		img.setImageResource(R.drawable.female);
        	r.setChecked(true);
        }
        else
        {
        	RadioButton r = (RadioButton) findViewById(R.id.male);
        	if(flag.equals("false"))
	            img.setImageResource(R.drawable.male);
        	r.setChecked(true);
        }
        
        String [] bd = intent.getStringExtra("bdate").split("/");
        if(bd.length > 1)
        {
	        int day = Integer.parseInt(bd[0]);
	        int month = Integer.parseInt(bd[1]) - 1;
	        int year = Integer.parseInt(bd[2]);
	        DatePicker bdate = (DatePicker) findViewById(R.id.bdate);
	        bdate.updateDate(year, month, day);
        }
        
        
        //upload = (Button) findViewById(R.id.upload);
        //caption = (EditText) findViewById(R.id.picture);

//        upload.setOnClickListener(new View.OnClickListener() {
//        	
//        	 
//        	
//        public void onClick(View v) {
//            if (bitmap == null) {
//                Toast.makeText(getApplicationContext(),
//                        "Please select image", Toast.LENGTH_SHORT).show();
//            } else {
//                dialog = ProgressDialog.show(EditProfile.this, "Uploading",
//                        "Please wait...", true);
//                new ImageUploadTask().execute();
//            }
//        }
//        });
    }

	
	public void update_profile(View view) throws MalformedURLException, JSONException {
		final ProgressDialog pdia;
    	pdia = ProgressDialog.show(EditProfile.this, "",
				"Loading.....", true);
        
    	intent = new Intent(this, Profile.class);
    	intent.putExtra("userid", userid);
    	Pattern pattern = Patterns.EMAIL_ADDRESS;
        
        EditText password = (EditText) findViewById(R.id.password);
        String str_password = password.getText().toString();
        
        EditText confirm_password = (EditText) findViewById(R.id.confirm_password);
        String str_confirm_password = confirm_password.getText().toString();
        
        
        EditText email = (EditText) findViewById(R.id.usermail);
        String str_email = email.getText().toString();
        
        EditText fname = (EditText) findViewById(R.id.fname);
        String str_fname = fname.getText().toString();
        
        EditText lname = (EditText) findViewById(R.id.lname);
        String str_lname = lname.getText().toString();
        
     //   EditText pic = (EditText) findViewById(R.id.picture);
     //   String str_pic = pic.getText().toString();
        String ch_gender;
        RadioButton gender = (RadioButton) findViewById(R.id.female);
        if(gender.isChecked())
        	ch_gender = "F";
        else
        	ch_gender = "M";
       
        DatePicker bdate = (DatePicker) findViewById(R.id.bdate);
        String str_bdate = bdate.getDayOfMonth()+"/"+(bdate.getMonth()+1)+"/"+bdate.getYear();
      
        System.out.println(bdate.toString());
        
        EditText msisdn = (EditText) findViewById(R.id.msisdn);
        String str_msisdn = msisdn.getText().toString();
        
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

				    	 TextView err = (TextView) findViewById( R.id.update_profile_error );
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
						runOnUiThread(new Runnable() {
						     public void run() {
						    	 intent.putExtra("file_name", file_name);
						    	 startActivity(intent);
							   }
						});
					}
					else
					{
						runOnUiThread(new Runnable() {
						     public void run() {

						    	 TextView err = (TextView) findViewById( R.id.update_profile_error );
							        err.setText("Update Profile failed!");
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

        if(!str_password.equals(str_confirm_password))
        {
        	runOnUiThread(new Runnable() {
			     public void run() {

			    	 TextView err = (TextView) findViewById( R.id.update_profile_error );
				     err.setText("Passwords don't match!");
				     EditText password = (EditText) findViewById(R.id.password);
				     password.setText("");
				     EditText confirm_password = (EditText) findViewById(R.id.confirm_password);
				     confirm_password.setText("");
				   }
			});
        	pdia.dismiss();
        }
        else if(str_confirm_password.equals("") || str_password.equals("") || str_fname.equals("") ||str_lname.equals(""))
        {
        	runOnUiThread(new Runnable() {
			     public void run() {

			    	 TextView err = (TextView) findViewById( R.id.update_profile_error );
				        err.setText("Please fill all the fields!");
				   }
			});
        	pdia.dismiss();
        }
        else if(str_password.length()<6)
        {
        	runOnUiThread(new Runnable() {
			     public void run() {

			    	 TextView err = (TextView) findViewById( R.id.update_profile_error );
				     err.setText("Password too short! Minimum 6 characters");
				     EditText password = (EditText) findViewById(R.id.password);
				     password.setText("");
				     EditText confirm_password = (EditText) findViewById(R.id.confirm_password);
				     confirm_password.setText("");
				   }
			});
        	pdia.dismiss();
        }
        else if(!pattern.matcher(str_email).matches())
        {
        	runOnUiThread(new Runnable() {
			     public void run() {
			    	 TextView err = (TextView) findViewById( R.id.update_profile_error );
				     err.setText("Invalid email address!");
				   }
			});
        	pdia.dismiss();
        }	
        else
        {
        	runOnUiThread(new Runnable() {
			     public void run() {
			    	 if(flag)
							upload_image(in);
						
				   }
			});

	        socket.send(new JSONObject().put("Action", "UpdateUserProfile").put("Parameters", 
	        		new JSONArray().put(new JSONObject().put("Password", md5(str_password)).put("UserEmail", str_email).put("UserID", userid+"")
	        				.put("UserFirstName", str_fname).put("UserLastName", str_lname).put("UserPicture", img_path)
	        				.put("UserGender", ch_gender).put("UserBirthDate", str_bdate).put("UserMSISDN", str_msisdn))
	        		));
        
        }
    }

	
	public void chooseImageSource(View v) {
		System.gc();
		final CharSequence[] items = { getString(R.string.gallery),
				getString(R.string.camera) };

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.choose_image_source);
		builder.setItems(items, this);
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	protected void startCamera() {
		Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(camIntent, CAMERA);
	}


	protected void startGallery() {
		Intent gallIntent = new Intent(Intent.ACTION_GET_CONTENT);
		gallIntent.setType("image/*");
		startActivityForResult(gallIntent, GALLERY);
	}
	
	private Bitmap getImageFromURI(Uri data) {
		try {
			flag = true;
			in = getContentResolver().openInputStream(data);
			return Media.getBitmap(getContentResolver(), data);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == GALLERY || requestCode == CAMERA) {
			if (resultCode == RESULT_OK) {
				System.out.println("onActivityResult");
				profile_pic = getImageFromURI(data.getData());
				System.out.println(profile_pic);
				
				File storagePath = new File(Environment.getExternalStorageDirectory() + "/chatspots/cache/"); 
		        storagePath.mkdirs(); 
		        file_name = Long.toString(System.currentTimeMillis()) + ".png";
		        File myImage = new File(storagePath, file_name);

		        try { 
		            FileOutputStream out = new FileOutputStream(myImage); 
		            profile_pic.compress(Bitmap.CompressFormat.PNG, 80, out); 
		            out.flush();    
		            out.close();
		        } catch (Exception e) { 
		            e.printStackTrace(); 
		        }   

				
				//ByteArrayOutputStream stream = new ByteArrayOutputStream();
				//profile_pic.compress(Bitmap.CompressFormat.PNG, 100, stream);
//				byte[] byteArray = stream.toByteArray();
//				for(int i = 0; i <byteArray.length; i++)
//					byte_image+=byteArray[i];
				if (profile_pic != null) {
					System.out.println("null image");
					profile_pic_view.setImageBitmap(profile_pic);
				} else {
					Toast.makeText(getBaseContext(),
							getString(R.string.image_error), Toast.LENGTH_LONG)
							.show();
				}
			} else {
				if (resultCode != RESULT_CANCELED) {
					Toast.makeText(getBaseContext(), getString(R.string.error),
							Toast.LENGTH_LONG).show();
				}
			}
		}
	}

	
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(DialogInterface dialog, int item) {
		if (item == GALLERY) {
			startGallery();
		} else {
			if (item == CAMERA) {
				startCamera();
			} else {
				Toast.makeText(getApplicationContext(),
						getString(R.string.error), Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
		// TODO Auto-generated method stub
		return false;
	}
	
    public void upload_image(InputStream f)
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
	        img_path = "/home/chatspots/"+username;
	        try
            {
                sftp.cd("/home/chatspots/"+username);
            }
            catch ( SftpException e )
            {
                sftp.mkdir( username );
                sftp.cd( "/home/chatspots/"+username );
            }
	        Vector<LsEntry> list = sftp.ls("*.png");
	        if(list.size() > 0)
	        {
	        	for(LsEntry l:list)
	        		sftp.rm(l.getFilename());
	        }
	        sftp.put(f, file_name);
	        
	        
	        File file = new File(Environment.getExternalStorageDirectory(), file_name );
	        Drawable d = Drawable.createFromStream(in,file_name);
	        Bitmap bitmap = ((BitmapDrawable)d).getBitmap();
	        FileOutputStream outStream;
	        try {

	            outStream = new FileOutputStream(file);
	            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream); 
	            /* 100 to keep full quality of the image */
	            outStream.flush();
	            outStream.close();
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
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
