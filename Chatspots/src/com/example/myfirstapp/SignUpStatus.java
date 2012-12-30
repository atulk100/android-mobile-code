package com.example.myfirstapp;

import java.net.MalformedURLException;

import org.json.JSONException;

import android.R.layout;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class SignUpStatus extends Activity {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_status);

    }
    
	public void login_page(View view) throws MalformedURLException, JSONException {
	        
	    	Intent intent = new Intent(this, Main.class);
	    	startActivity(intent);
	    }
}
