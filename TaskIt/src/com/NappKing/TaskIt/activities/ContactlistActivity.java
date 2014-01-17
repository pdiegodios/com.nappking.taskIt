package com.NappKing.TaskIt.activities;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.NappKing.TaskIt.R;
import com.NappKing.TaskIt.adapters.ContactlistAdapter;
import com.NappKing.TaskIt.db.DBListActivity;
import com.NappKing.TaskIt.entities.Contact;
import com.NappKing.TaskIt.entities.Task;
import com.NappKing.TaskIt.widget.AnimationLayout;
import com.j256.ormlite.dao.Dao;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class ContactlistActivity extends DBListActivity{
	private List<Contact> _contacts = new ArrayList<Contact>(); 
	private ListView contactList;
    private EditText inputSearch;
	protected Dao<Contact,Integer> _daoContact;
	protected ArrayAdapter<Contact> adapter;
    //Inicializamos la lista de contactos resultado de la búsqueda en inputSearch
    private List<Contact> contact_sort = new ArrayList<Contact>();
    //Atributos para la búsqueda
    private int textlength=0;
    private String text;
	
	@Override
	protected void update() {
		try {
			_contacts.clear();
			_daoContact = getHelper().getContactDAO();
			_contacts = _daoContact.queryForAll();
			adapter = new ContactlistAdapter(this, R.layout.contact_item, _contacts);
			contactList.setAdapter(adapter);		
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//LIFETIME    
	@Override //CREATE
	public void onCreate (Bundle savedInstanceState){	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_tab);			
		//Selección de los elementos del layout
		contactList = (ListView) findViewById(android.R.id.list);
		inputSearch = (EditText) findViewById(R.id.inputSearch);
		animation = (AnimationLayout) findViewById(R.id.animation_layout);
		animation.setListener(this);
		animation.setPosition(9);
		detector = new GestureDetector(this,this);
    	getActionBar().setDisplayShowHomeEnabled(true);
    	getActionBar().setHomeButtonEnabled(true);
						
		/**
		 * Operaciones propias de despliegue del ListView en cada pestaña, realizadas en las clases hijas.
		 * Recuperamos las entidades y las colocamos en el adapter del view.
		 */
		update();	
		
		//Listener del campo de búsqueda dentro de la lista
		inputSearch.addTextChangedListener(new TextWatcher(){
			public void afterTextChanged(Editable s){}
			public void beforeTextChanged(CharSequence s, int start, int count, int after){}
			public void onTextChanged(CharSequence s, int start, int before, int count){
				CharSequence searched = inputSearch.getText();
				textlength = searched.length();
				text = searched.toString().toLowerCase();
				contact_sort.clear();
				for (Contact c:_contacts){
					String name="",mobile="",phoneWork="",phoneHome="";
					if(c.getName()!=null)
						name = c.getName().toLowerCase();
					if(c.getMobile()!=null)
						mobile = c.getMobile().toLowerCase();	
					if(c.getPhoneWork()!=null)
						phoneWork = c.getPhoneWork().toLowerCase();		
					if(c.getPhoneHome()!=null)
						phoneHome = c.getPhoneHome().toLowerCase();					
					//se hace la búsqueda sobre el nombre y el metadato (CIF/NIF o descripcion en caso de grupo)
					if((textlength <=name.length())||(textlength <=mobile.length())
							||(textlength <=phoneWork.length())||(textlength <=phoneHome.length())){
						if(name.contains(text)||mobile.contains(text)
								||phoneWork.contains(text)||phoneHome.contains(text))
							contact_sort.add(c);
					}
				}
				contactList.setAdapter(new ContactlistAdapter(getBaseContext(), R.layout.contact_item, contact_sort));
			}			
		});
	}	
	
	
	//LIST METHOD
	@Override
	public void onListItemClick(ListView l, View v, int position, long id){
		Contact contact = (Contact) l.getAdapter().getItem(position);
		openContact(contact);
	}	
		
	//AUXILIAR METHODS
	public void openContact(Contact contact){
		Intent myIntent = new Intent(this, ContactActivity.class);
		Bundle myBundle = new Bundle();
		myBundle.putSerializable(Task.ASSOCIATED, contact);
		myIntent.putExtras(myBundle);
		startActivity(myIntent);
	}	

	@Override
	protected String getCurrentTab() {
		return "Contactos";
	}

}
