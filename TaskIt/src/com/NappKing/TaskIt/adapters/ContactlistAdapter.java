package com.NappKing.TaskIt.adapters;

import java.util.List;

import com.NappKing.TaskIt.R;
import com.NappKing.TaskIt.entities.Contact;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * Adaptador de apoyo para customizar nuestras Views. El adaptador está compuesto por Contacts
 * @author pdiego
 */

public class ContactlistAdapter extends ArrayAdapter<Contact>{
	private List<Contact> _contacts;
	private Context _context;
    
    public ContactlistAdapter(Context context, int textViewResourceId, List<Contact> contacts) {
        super(context, textViewResourceId, contacts);
        this._contacts = contacts;
        this._context = context;
    }
    
    public List<Contact> getList(){
    	return _contacts;
    }

    private void display(View v, final Contact contact){
    	TextView tname = (TextView) v.findViewById(R.id.name);
        TextView tphone = (TextView) v.findViewById(R.id.phone);
        ImageView icontact = (ImageView) v.findViewById(R.id.photo);        
        
        //Mostramos el móvil, teléfono de trabajo, o teléfono de casa (en este orden de preferencia)
        if(contact.getMobile()!=null){
        	if(!contact.getMobile().isEmpty())
        		tphone.setText(contact.getMobile());
        }
        else if (contact.getPhoneWork()!=null){
        	if(!contact.getPhoneWork().isEmpty())
        		tphone.setText(contact.getPhoneWork());
        }
        else if (contact.getPhoneHome()!=null){
        	if(!contact.getPhoneHome().isEmpty())
        		tphone.setText(contact.getPhoneHome());
        }
        
        tname.setText(contact.getName());
        Bitmap photo = convertStringToBitmap(contact.getPhoto());
        if(photo!=null)
        	icontact.setImageBitmap(photo);
        else 
        	icontact.setImageResource(R.drawable.unknown_contact);
    }  
    
    private Bitmap convertStringToBitmap(String encodedImage){
    	if(encodedImage!=null){
	    	byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
	    	Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length); 
	    	return decodedByte;
    	}
    	else return null;
    }
	 
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater layout = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layout.inflate(R.layout.contact_item, null);
        }
        Contact contact = (Contact) _contacts.get(position);
        if (contact != null) {
        	display(view,contact);	            
        }
        return view;
    }    
   
}
