package com.NappKing.TaskIt.activities;

import java.util.List;

import com.NappKing.TaskIt.R;
import com.NappKing.TaskIt.adapters.TaskAdapter;
import com.NappKing.TaskIt.entities.Contact;
import com.NappKing.TaskIt.entities.Task;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


public class ContactActivity extends Activity{
	private Bundle myBundle;
	private Contact _contact;
	private ImageView iPhoto;
	private TextView mobileNumber, homeNumber, workNumber, empty, name;
	private ImageButton btnMobile, btnHome, btnWork;
	private ListView tasklist;
	
	
//LIFETIME
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_info);
		myBundle = getIntent().getExtras();
		loadViews();
		setValues();		
		update();
	}		
    
	protected void onResume(){
		super.onResume();
		update();
	}		
	
	private void update(){
        List<Task> tasks = _contact.getTasks(this);
        TaskAdapter adapter = new TaskAdapter(this, R.layout.task_item, tasks);
        if(!tasks.isEmpty())
        	empty.setVisibility(View.INVISIBLE);
        tasklist.setAdapter(adapter);
	}	
	
	private void loadViews(){
		_contact = (Contact) myBundle.getSerializable(Task.ASSOCIATED);
		iPhoto = (ImageView) findViewById(R.id.photo);
		mobileNumber = (TextView) findViewById(R.id.txMobile);
		homeNumber = (TextView) findViewById(R.id.txHome);
		workNumber = (TextView) findViewById(R.id.txWork);
		name = (TextView) findViewById(R.id.txName);
		empty = (TextView) findViewById(android.R.id.empty);
		btnMobile = (ImageButton) findViewById(R.id.btnMobile);
		btnHome = (ImageButton) findViewById(R.id.btnHome);
		btnWork = (ImageButton) findViewById(R.id.btnWork);
		tasklist = (ListView) findViewById(android.R.id.list);
    	btnMobile.setEnabled(false);
    	btnHome.setEnabled(false);
    	btnWork.setEnabled(false);
	}
	
	private void setValues(){    	
        final String mobile=_contact.getMobile();
        final String home=_contact.getPhoneHome();
        final String work=_contact.getPhoneWork();
        Bitmap photo = convertStringToBitmap(_contact.getPhoto());
        
        //Colocamos el nombre
        name.setText(_contact.getName());
        
        //Establecemos foto si no es null; en caso contrario se carga la foto por defecto 'unknown_contact'
        if(photo!=null)
        	iPhoto.setImageBitmap(photo);
        
        //Establecemos teléfonos, habilitamos botones y hacemos llamada en click
        //Con ACTION_DIAL tenemos opción de cancelar la llamada; ACTION_CALL llamaría directamente
        if(mobile!=null){
        	mobileNumber.setText(mobile);
        	btnMobile.setEnabled(true);
            btnMobile.setOnClickListener(new OnClickListener() {			
    			@Override
    			public void onClick(View v) {
    				Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mobile));
    				startActivity(intent);
    			}
    		});
        }
        if(home!=null){
        	homeNumber.setText(home);
        	btnHome.setEnabled(true);
            btnHome.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
    				Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + home));
    				startActivity(intent);
				}
			});
        }
        if(work!=null){
        	workNumber.setText(work);
        	btnWork.setEnabled(true);
            btnWork.setOnClickListener(new OnClickListener() {			
    			@Override
    			public void onClick(View v) {
    				Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + work));
    				startActivity(intent);
    			}
    		});
        }
	}
	
	//AUXILIAR
    private Bitmap convertStringToBitmap(String encodedImage){
    	if(encodedImage!=null){
	    	byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
	    	Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length); 
	    	return decodedByte;
    	}
    	else return null;
    }
	
}