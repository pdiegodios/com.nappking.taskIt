package com.NappKing.TaskIt.activities;


import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Base64;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.NappKing.TaskIt.R;
import com.NappKing.TaskIt.adapters.ItemAdapter;
import com.NappKing.TaskIt.db.DBActivity;
import com.NappKing.TaskIt.entities.Contact;
import com.NappKing.TaskIt.entities.Item;
import com.NappKing.TaskIt.entities.Task;
import com.NappKing.TaskIt.entities.Tasklist;
import com.NappKing.TaskIt.service.ReminderService;
import com.NappKing.TaskIt.widget.AnimationLayout;
import com.j256.ormlite.dao.Dao;

public class TaskInfoActivity extends DBActivity implements AnimationLayout.Listener, OnGestureListener{
	
	static final int PICK_CONTACT = 0;
	public static final String TASK = "Task";
	
	private Task _task = null;
	private EditText txName;
	private TextView txNote,txReminder,txExpiration,txAssociate,txColorPriority;
	private LinearLayout lineNote,lineReminder,lineExpiration,linePriority;
	private LinearLayout lineAssociate;
	private Spinner selectList;
	private CheckBox chkStarred;
	private CheckBox chkCompleted;
	private GestureDetector detector;
	private DateFormat _dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm",Locale.getDefault());
	private DateFormat _dateParse = new SimpleDateFormat("dd/MM/yyyy HH:mm",Locale.getDefault());
	private AnimationLayout animation;
	

	//LIFETIME
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.task_info);
		animation = (AnimationLayout) findViewById(R.id.animation_layout);
		animation.setListener(this);
		detector = new GestureDetector(this,this);
    	getActionBar().setDisplayShowHomeEnabled(true);
    	getActionBar().setHomeButtonEnabled(true);
		Bundle myBundle = getIntent().getExtras();
		_task = (Task) myBundle.getSerializable(TASK);
		boolean isNotified= myBundle.getBoolean(ReminderService.REMINDER, false);
		if (isNotified){
			//En caso de lanzarse desde notificación, anulamos el recordatorio y guardamos, para evitar cancelación.
			_task.setReminder(null);
			loadViews();
			setListeners();
			update();
			save();
		}
		else{
			//Cargamos campos, agregamos listeners y actualizamos datos sobre los campos
			loadViews();
			setListeners();
			update();
		}
	}		
	
	@Override
	protected void onResume(){
		animation.setPressed(false);
		super.onResume();
	}	
	

	//AUXILIAR METHODS
	private void loadViews(){
		txName = (EditText)findViewById(R.id.txCurrentName);
		txNote = (TextView)findViewById(R.id.txResumeNote);
		txReminder = (TextView)findViewById(R.id.txResumeRemember);
		txColorPriority = (TextView)findViewById(R.id.txColourPriority);
		txExpiration = (TextView)findViewById(R.id.txResumeExpiration);
		txAssociate = (TextView)findViewById(R.id.txResumeAssociate);
		selectList = (Spinner)findViewById(R.id.selectList);
		chkStarred = (CheckBox)findViewById(R.id.chkStarred);
		chkCompleted = (CheckBox)findViewById(R.id.chkCompleted);
		lineNote = (LinearLayout)findViewById(R.id.notes);
		lineReminder = (LinearLayout)findViewById(R.id.remember);
		lineExpiration = (LinearLayout)findViewById(R.id.expiration);
		lineAssociate = (LinearLayout)findViewById(R.id.associate);	
		linePriority = (LinearLayout)findViewById(R.id.priority);
	}
	
	@SuppressWarnings("unchecked")
	private void update(){
		try {
			Dao<Tasklist, Integer> tasklistDao = getHelper().getTasklistDAO();
			ArrayList<Tasklist> lista = (ArrayList<Tasklist>) tasklistDao.queryForAll();
			ArrayAdapter<Tasklist> adapter = new ArrayAdapter<Tasklist>(this,android.R.layout.simple_spinner_item,lista);
			selectList.setAdapter(adapter);
			if (_task!=null){
				setTitle(_task.getName());
				txName.setText(_task.getName());
				String note=_task.getNote();
				if(note!=null){
					if(note.length()>20)	//Si la nota es grande mostramos sólo el inicio
						note = note.substring(0, 20) +"...";
				}
				txNote.setText(note);
				if(_task.getReminder()!=null && !_task.getReminder().isEmpty()){
					try {
						txReminder.setText(_dateParse.format(_dateFormat.parse(_task.getReminder())));
					} catch (ParseException e) {
						e.printStackTrace();
					}	
				}
				else txReminder.setText(_task.getReminder()); 
				
				if(_task.getExpiration()!=null && !_task.getExpiration().isEmpty()){
					try {
						txExpiration.setText(_dateParse.format(_dateFormat.parse(_task.getExpiration())));
					} catch (ParseException e) {
						e.printStackTrace();
					}	
				}
				else txExpiration.setText(_task.getExpiration());
				
				txColorPriority.setBackgroundResource(_task.getLevel());
				if (_task.getAssociated()!=null){
					txAssociate.setText(_task.getAssociated().getName());
				}	
				else{
					txAssociate.setText(null);
				}
				chkStarred.setChecked(_task.isStarred());
				chkCompleted.setChecked(_task.isCompleted());
				adapter = (ArrayAdapter<Tasklist>) selectList.getAdapter();
				Tasklist tasklist = _task.getList();
				int position=-1;
				for(int i=0;i<adapter.getCount();i++){
					Tasklist tasklistInAdapter = adapter.getItem(i);
					if(tasklistInAdapter.getId()==tasklist.getId()){
						position = i;
						break;
					}
				}
				selectList.setSelection(position);
			}
		} catch (SQLException e) {
			Log.e(TAG, "Error al acceder a la BD");
		}
	}
	
	private void setListeners(){
		chkCompleted.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				_task.setCompleted(isChecked);
			}
		});

		chkStarred.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				_task.setStarred(isChecked);
			}
		});
		

		final Item[] priorities = {
				new Item("Urgente", R.drawable.red),
				new Item("Muy Alta", R.drawable.orange),
				new Item("Alta", R.drawable.yellow),
				new Item("Normal", R.drawable.green)
		};
		
		final ItemAdapter priorityAdapter = new ItemAdapter(this,android.R.layout.select_dialog_item,android.R.id.text1,priorities);
		
		//Abre diálogo para elegir color de prioridad
		linePriority.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {       	
				//final View view = v;
				new AlertDialog.Builder(v.getContext())
					.setTitle("Nivel")
					.setCancelable(true)
					.setAdapter(priorityAdapter, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch(which){
							case 0: _task.setLevel(R.color.red); break;
							case 1: _task.setLevel(R.color.orange); break;
							case 2: _task.setLevel(R.color.yellow); break;
							case 3: _task.setLevel(R.color.green); break;
							default:break;
						}
						dialog.dismiss();	
						update();
					}	
				}).show();				
			}
		});
		
		//Abre diálogo para escribir notas sobre la tarea.
		lineNote.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				final Dialog dialog = new Dialog(v.getContext());
				dialog.setContentView(R.layout.note_dialog);
				dialog.setTitle("Anotaciones");
				// set the custom dialog components - text, image and button
				final EditText txNote = (EditText) dialog.findViewById(R.id.note);
				Button okButton = (Button) dialog.findViewById(R.id.btnGuardar);
				txNote.setText(_task.getNote());
				
				okButton.setOnClickListener(new OnClickListener() {						
					@Override
					public void onClick(View view) {
						String newNote = txNote.getText().toString();
						_task.setNote(newNote);	
						dialog.dismiss();	
						update();
					}
				});					
				dialog.show();
			}
		});

		//Abrir selección fecha y hora para seleccionar recordatorio
		lineReminder.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Dialog dialog = new Dialog(v.getContext());
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setContentView(R.layout.reminder);
				// set the custom dialog components - text, image and button
				final DatePicker date = (DatePicker) dialog.findViewById(R.id.datePicker);
				final TimePicker time = (TimePicker) dialog.findViewById(R.id.timePicker);
				Button okButton = (Button) dialog.findViewById(R.id.setDate);
				Button cancelButton = (Button) dialog.findViewById(R.id.cancel);
				Button nullButton = (Button) dialog.findViewById(R.id.setNull);
				String reminder= _task.getReminder();
				if(reminder!=null){		
					if(!reminder.isEmpty()){
						try {
							Date currentReminder = _dateFormat.parse(reminder);
							Calendar myCalendar = Calendar.getInstance();
							myCalendar.setTime(currentReminder);
							date.updateDate(myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), 
									myCalendar.get(Calendar.DAY_OF_MONTH));
							time.setCurrentHour(myCalendar.get(Calendar.HOUR_OF_DAY));
							time.setCurrentMinute(myCalendar.get(Calendar.MINUTE));
						} catch (ParseException e) {
							Log.e(TAG, "Error al recuperar fecha");
						}
					}
				}	
				okButton.setOnClickListener(new OnClickListener() {						
					@Override
					public void onClick(View view) {
						Calendar reminder = Calendar.getInstance();
						int year = date.getYear();
						int month = date.getMonth();
						int day = date.getDayOfMonth();
						int hour = time.getCurrentHour();
						int minute = time.getCurrentMinute();
						
						reminder.set(Calendar.HOUR_OF_DAY, hour);
						reminder.set(Calendar.MINUTE, minute);
						reminder.set(Calendar.SECOND, 0);
						reminder.set(Calendar.MILLISECOND, 0);
						reminder.set(Calendar.DAY_OF_MONTH, day);
						reminder.set(Calendar.MONTH, month);
						reminder.set(Calendar.YEAR, year);
						_task.setReminder(_dateFormat.format(reminder.getTime()));
						dialog.dismiss();	
						update();
					}
				});		
				cancelButton.setOnClickListener(new OnClickListener() {						
					@Override
					public void onClick(View view) {
						dialog.dismiss();	
					}
				});					
				nullButton.setOnClickListener(new OnClickListener() {						
					@Override
					public void onClick(View view) {
						_task.setReminder("");
						dialog.dismiss();	
						update();
					}
				});								
				dialog.show();
			}				
		});
		
		//Abrir selección fecha y hora para seleccionar fecha de vencimiento
		lineExpiration.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				final Dialog dialog = new Dialog(v.getContext());
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setContentView(R.layout.reminder);
				// set the custom dialog components - text, image and button
				final DatePicker date = (DatePicker) dialog.findViewById(R.id.datePicker);
				final TimePicker time = (TimePicker) dialog.findViewById(R.id.timePicker);
				Button okButton = (Button) dialog.findViewById(R.id.setDate);
				Button cancelButton = (Button) dialog.findViewById(R.id.cancel);
				Button nullButton = (Button) dialog.findViewById(R.id.setNull);
				String expiration= _task.getExpiration();
				if(expiration!=null) {	
					if(!expiration.isEmpty()){
						try {
							Date currentReminder = _dateFormat.parse(expiration);
							Calendar myCalendar = Calendar.getInstance();
							myCalendar.setTime(currentReminder);
							date.updateDate(myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), 
									myCalendar.get(Calendar.DAY_OF_MONTH));
							time.setCurrentHour(myCalendar.get(Calendar.HOUR_OF_DAY));
							time.setCurrentMinute(myCalendar.get(Calendar.MINUTE));
						} catch (ParseException e) {
							Log.e(TAG, "Error al recuperar fecha");
						}
					}
				}	
				okButton.setOnClickListener(new OnClickListener() {						
					@Override
					public void onClick(View view) {
						Calendar expiration = Calendar.getInstance();
						expiration.set(Calendar.HOUR_OF_DAY, time.getCurrentHour());
						expiration.set(Calendar.MINUTE, time.getCurrentMinute());
						expiration.set(Calendar.SECOND, 0);
						expiration.set(Calendar.MILLISECOND, 0);
						expiration.set(Calendar.DAY_OF_MONTH,date.getDayOfMonth());
						expiration.set(Calendar.MONTH,date.getMonth());
						expiration.set(Calendar.YEAR,date.getYear());
						_task.setExpiration(_dateFormat.format(expiration.getTime()));
						dialog.dismiss();	
						update();
					}
				});		
				cancelButton.setOnClickListener(new OnClickListener() {						
					@Override
					public void onClick(View view) {
						dialog.dismiss();	
					}
				});					
				nullButton.setOnClickListener(new OnClickListener() {						
					@Override
					public void onClick(View view) {
						_task.setExpiration("");
						dialog.dismiss();	
						update();
					}
				});								
				dialog.show();
			}				
		});

		final Item[] items = {
				new Item("Ver Contacto", R.drawable.info),
				new Item("Borrar", R.drawable.delete_icon)
		};
		
		final ItemAdapter adapter = new ItemAdapter(this,android.R.layout.select_dialog_item,android.R.id.text1,items);			
		
		
		//Abrir selección de cliente
		lineAssociate.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {  
				final View view = v;
				final Contact contact = _task.getAssociated();
				if(contact==null){
					Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
					startActivityForResult(intent, PICK_CONTACT);
				}
				else {
					new AlertDialog.Builder(v.getContext())
						.setCancelable(true)
						.setAdapter(adapter, new DialogInterface.OnClickListener() {							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								switch(which){
									case 0: 
										//Ver Contacto
										openContact(contact);
										break;
									case 1: 
										//Desligamos el contacto de la tarea
										_task.setAssociated(null);										
										try {
											Dao<Task,Integer> daoTask = getHelper().getTaskDAO();
											daoTask.update(_task);
											//Si el contacto no está presente en ninguna tarea, lo eliminamos
											List<Task> tasks = contact.getTasks(view.getContext());
											if(tasks.isEmpty()){	
													Dao<Contact,Integer> daoContact = getHelper().getContactDAO();
													daoContact.delete(contact);
											}		
										} catch (SQLException e) {
											e.printStackTrace();
										}
										update();
										break;
									default:break;
								}
							}
						}).show();
				}
				update();
			}
		});		
	}
	
	private void save(){
		try{
			_task.setName(txName.getText().toString());
			_task.setStarred(chkStarred.isChecked());
			_task.setCompleted(chkCompleted.isChecked());
			Tasklist lista = (Tasklist) selectList.getSelectedItem();
			_task.setList(lista);
			if(_task.getReminder()!=null){
				if(!_task.getReminder().isEmpty()){
					Date date = _dateFormat.parse(_task.getReminder());
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(date);
					//Set Notification with ID
					_task.startAlarm(calendar,this);
				}
				else _task.stopAlarm(this);
			}
			else _task.stopAlarm(this);
			Dao<Task,Integer> dao = getHelper().getTaskDAO();
			dao.update(_task);
		}catch(SQLException ex){
			Log.e(TAG, "Error al actualizar Tarea");
			Toast.makeText(this.getBaseContext(), "No se ha actualizado la tarea", Toast.LENGTH_SHORT).show();
		} catch (ParseException e) {
			Log.e(TAG, "Error al parsear fecha");
		}

		Bundle myBundle = getIntent().getExtras();
		boolean isNotified= myBundle.getBoolean(ReminderService.REMINDER, false);
		if(!isNotified){
			Toast.makeText(this.getBaseContext(), "Tarea Actualizada", Toast.LENGTH_SHORT).show();
			finish();
		}	
	}
	
	
	//BACK BUTTON METHOD
	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this)
			.setTitle("Guardar")
			.setMessage("¿Guardar cambios antes de salir?")
			.setCancelable(true)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					save();
					finish();
				}
			})
			.setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					finish();
				}
			})
			.show();		
	}
		
	
	//RESULT METHOD
    @Override 
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data); 
    	switch(requestCode) { 
        	case (PICK_CONTACT) :  //Tras obtener el contacto elegido en la agenda
        		if (resultCode == RESULT_OK) {
        	        Uri contactData = data.getData();
        	        Cursor cursor =  getContentResolver().query(contactData, null, null, null, null);
        	        String lookUpKey, contactID, name=null, mobile=null, phoneWork=null, phoneHome=null;
        	        String photo = null;
        			Dao<Contact, Integer> daoContact;
					try {
						daoContact = getHelper().getContactDAO();
	        	        if (cursor.moveToFirst()) {
	        	        	//Aquí recuperamos informacion que nos interese respecto al contacto asociado.

	          	          	//lookupKey
	          	          	lookUpKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));

		                    //Consultamos si hay contacto con la lookupkey obtenida
		        			List<Contact> contacts = daoContact.queryForEq(Contact.LOOKUPKEY, lookUpKey);
		        			
		        			Contact associated=null;
		        			
		        			if(!contacts.isEmpty()){
		        				//EXISTE EL CONTACTO, y sólo puede haber uno pues lookupkey es única
		        				associated = contacts.get(0);
		        			}
		        			else{
		        				//NO EXISTE EL CONTACTO: Recuperamos valores para crearlo
		        				
		        	        	//ID
		          	          	contactID = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
		          	          	
		          	          	//name
		          	          	name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
		          	          	
		          	          	//photo
		          	          	InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(),
		                              ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.valueOf(contactID)));           
			                    if (inputStream != null) {
			                    	Bitmap bm = BitmapFactory.decodeStream(inputStream);
			                        ByteArrayOutputStream baos = new ByteArrayOutputStream();  
			                        bm.compress(Bitmap.CompressFormat.PNG, 100, baos); //bm is the bitmap object   
			                        byte[] b = baos.toByteArray();
			                        photo = Base64.encodeToString(b, Base64.DEFAULT);
			                    }    
			                    
			                    // Using the contact ID now we will get contact phone number
			                    // mobile
			                    cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
			                            new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},	             
			                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
			                                    ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
			                                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,	             
			                            new String[]{contactID},
			                            null);	             
			                    if (cursor.moveToFirst()) {
			                        mobile = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			                    }
			                    
			                    //phoneWork
			                    cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
			                            new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},	             
			                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
			                                    ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
			                                    ContactsContract.CommonDataKinds.Phone.TYPE_WORK,	             
			                            new String[]{contactID},
			                            null);	 	             
			                    if (cursor.moveToFirst()) {
			                    	phoneWork = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			                    }	   
			                    
			                    //phoneHome
			                    cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
			                            new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},	             
			                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
			                                    ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
			                                    ContactsContract.CommonDataKinds.Phone.TYPE_HOME,	             
			                            new String[]{contactID},
			                            null);	 	             
			                    if (cursor.moveToFirst()) {
			                    	phoneHome = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			                    }	   
			                    cursor.close();
			                    
			        			// se crea el contacto
				                associated = new Contact();
				                associated.setLookupKey(lookUpKey);
				                associated.setMobile(mobile);
				                associated.setPhoneHome(phoneHome);
				                associated.setPhoneWork(phoneWork);
				                associated.setName(name);
				                associated.setPhoto(photo);
			          	        daoContact.create(associated);
			        		}
		        			_task.setAssociated(associated);
		        			Dao<Task,Integer> daoTask = getHelper().getTaskDAO();
		        			daoTask.update(_task);
	        	        }
					} catch (SQLException e) {
						//ERROR con la BD
						e.printStackTrace();
					}
				}
	        	break; 
	        	
	        // Si hay otros case irían aquí
	        
        	default:break;
        } 
        update();
    }        
     
    
    //WIDGET METHODS
	@Override
	public boolean onTouchEvent(MotionEvent me){
		this.detector.onTouchEvent(me);
		return super.onTouchEvent(me);
	}
	
	@Override 
	public boolean dispatchTouchEvent(MotionEvent me){ 
		this.detector.onTouchEvent(me);
		return super.dispatchTouchEvent(me); 
	}
	
	@Override 
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        //desplazamiento mínimo para desplegar o recoger animationlayout
		float sensitivity = 100;
        //desplazamiento hacia la izquierda
        if((e1.getX() - e2.getX()) > sensitivity){
            animation.closeSidebar();
        //desplazamiento hacia la derecha
        }else if((e2.getX() - e1.getX()) > sensitivity){
            animation.openSidebar();
        }
        return true;
	}
    
	@Override
	public void onSidebarOpened() {}

	@Override
	public void onSidebarClosed() {}

	@Override
	public boolean onDown(MotionEvent e) {return false;}

	@Override
	public void onLongPress(MotionEvent e) {}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {return false;}

	@Override
	public void onShowPress(MotionEvent e) {}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {return false;}

	@Override
	public boolean onContentTouchedWhenOpening() {
        //Si la zona de contenido es tocada cuando la barra lateral está abierta, se cerrará
        animation.closeSidebar();
        return true;
	}	 

  	@Override
  	public void onNavigationTo(Class<?> menu, int position){
  		//Navegación según click en el menú lateral (sidebar)
  		if (menu!=null){
  			if (menu!=this.getClass()){
  				Intent intent = new Intent(this, menu);
  				Bundle myBundle = new Bundle();
  				myBundle.putInt(TAG, position);
  				intent.putExtras(myBundle);
  				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
  				startActivity(intent);				
  			}
  		}
  	}
    	
  	
  	//MENU METHODS 
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case android.R.id.home:
				animation.toggleSidebar();
				break;
			case R.id.save:	
				save();
				break;
			case R.id.delete:	
				new AlertDialog.Builder(this)
				.setTitle(this.getResources().getString(R.string.option_delete)+" "+_task.getName())
				.setMessage("¿"+this.getResources().getString(R.string.askfordeletetask)+" "+_task.getName()+"?")
				.setCancelable(true)
				.setPositiveButton(this.getResources().getString(R.string.accept), new DialogInterface.OnClickListener() {				
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						_task.stopAlarm(TaskInfoActivity.this);		
						try {				
							Dao<Task, Integer> dao = getHelper().getTaskDAO();
							dao.delete(_task);
						} catch (SQLException e) {
							e.printStackTrace();
						}
						finish();
					}
				})
				.setNegativeButton(this.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {				
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
				.show();
				break;
			default: 
				break;
		}	
		return true;
	}	
	
	/**
	 * items visibles en la barra de menú dentro de la actividad según el tipo de estrategia
	 * @item0: lists; @item1: save;
	 */
	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {    		
		return true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menuedit, menu);
		return true;
	}	 
	
	
	//AUXILIAR METHODS
	public void openContact(Contact contact){
		Intent myIntent = new Intent(this, ContactActivity.class);
		Bundle myBundle = new Bundle();
		myBundle.putSerializable(Task.ASSOCIATED, contact);
		myIntent.putExtras(myBundle);
		startActivity(myIntent);
	}	
	
}