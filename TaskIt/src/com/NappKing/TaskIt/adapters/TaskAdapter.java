package com.NappKing.TaskIt.adapters;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.NappKing.TaskIt.R;
import com.NappKing.TaskIt.activities.ContactActivity;
import com.NappKing.TaskIt.activities.TaskInfoActivity;
import com.NappKing.TaskIt.db.DBHelper;
import com.NappKing.TaskIt.db.DBListActivity;
import com.NappKing.TaskIt.entities.Item;
import com.NappKing.TaskIt.entities.Task;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;


/**
 * Adaptador para visualización de Tareas
 * @author pdiego
 */
public class TaskAdapter extends ArrayAdapter<Task>{
	
	private DateFormat _dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
	private DateFormat _expirationFormat = new SimpleDateFormat("EEE dd/MM", Locale.getDefault());
	private List<Task> _tasks;
	private Context _context;
	private String _errorUpdate = this.getContext().getResources().getString(R.string.error_updatetask);
	private static String TAG = "TaskAdapter";
    
    public TaskAdapter(Context context, int textViewResourceId, List<Task> tasks) {
        super(context, textViewResourceId, tasks);
        this._tasks = tasks;
        this._context = context;
    }
    
    public List<Task> getList(){
    	return _tasks;
    }
    
    
    /**
     * cargamos los iconos en la vista según las características de la tarea para su 
     * correcta visualización. Además, se establecen las diferentes acciones sobre 
     * elementos del task_item.xml
     * @param v : Vista sobre la que se cargan las propiedades de la tarea
     * @param task : Tarea visualizada
     */
    private void display(View v, final Task task){
    	final View vista = v;
    	if(task==null)
    		v.setVisibility(View.INVISIBLE);
    	else{
    	ImageButton btnComplete = (ImageButton) v.findViewById(R.id.check);
    	ImageView iStar = (ImageView) v.findViewById(R.id.star);
    	ImageView iAlert = (ImageView) v.findViewById(R.id.alert);
    	ImageView iExpiration = (ImageView) v.findViewById(R.id.expiration_img);
    	ImageView iAssociated = (ImageView) v.findViewById(R.id.associated_img);
    	TextView txName = (TextView) v.findViewById(R.id.taskName);
    	TextView txAssociated = (TextView) v.findViewById(R.id.associated);
    	TextView txExpiration = (TextView) v.findViewById(R.id.expiration);
    	TextView txLevelColour = (TextView) v.findViewById(R.id.level);
    	ImageButton btnNote = (ImageButton) v.findViewById(R.id.notes);
    	LinearLayout lineData = (LinearLayout) v.findViewById(R.id.data);	
        
    	if (btnComplete != null) {
        	if(task.isCompleted()) {
        		btnComplete.setImageResource(R.drawable.checked);
        		v.setAlpha((float) 0.6);
        	}	
            else {
            	btnComplete.setImageResource(R.drawable.unchecked);
        		v.setAlpha((float) 1);
            }
        	btnComplete.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					if(task.isCompleted())
						task.setCompleted(false);
					else task.setCompleted(true);
					try{
						DBHelper helper = OpenHelperManager.getHelper(_context, DBHelper.class);
						Dao<Task, Integer> dao = helper.getTaskDAO();
						if(dao!=null)
							dao.update(task);
					}catch(Exception ex){
						Log.e(TAG, _errorUpdate);
						Toast.makeText(_context, _errorUpdate, Toast.LENGTH_SHORT).show();
					}
					display(vista,task);
				}
			});
    	
    	}
    	
    	if(txLevelColour!=null){
    		txLevelColour.setBackgroundResource(task.getLevel());
    	}
    	
        if (iStar!=null){//Agregamos el icono al item
        	if(task.isStarred()) 
            	iStar.setImageResource(R.drawable.starred_icon);
            else iStar.setImageResource(R.drawable.unstarred_icon);
        	iStar.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					if(task.isStarred())
						task.setStarred(false);
					else task.setStarred(true);
					try{
						DBHelper helper = OpenHelperManager.getHelper(_context, DBHelper.class);
						Dao<Task, Integer> dao = helper.getTaskDAO();
						if(dao!=null)
							dao.update(task);
					}catch(Exception ex){
						Log.e(TAG, _errorUpdate);
						Toast.makeText(_context, _errorUpdate, Toast.LENGTH_SHORT).show();
					}
					display(vista,task);
				}
			});
        }
        
        if (iAlert!=null){//Visible o no según si tiener recordatorio
        	if(task.getReminder()!=null){
        		if(!task.getReminder().isEmpty())
        			iAlert.setVisibility(View.VISIBLE);
        		else iAlert.setVisibility(View.INVISIBLE);
        	}
    		else iAlert.setVisibility(View.INVISIBLE);
        }
        
        if (txName != null) {//Colocamos el nombre
        	txName.setText(task.getName());
        }       
        
        if (txAssociated != null){//Agregamos el nombre del asociado, si existe
        	if(task.getAssociated()!=null){
        		String associated = task.getAssociated().getName();
        		txAssociated.setText(associated);
        	}
        }
        
        if (iAssociated != null){//Agregamos la imagen de asociado, si existe
        	if(task.getAssociated()!=null)
        		iAssociated.setVisibility(View.VISIBLE);
    		else iAssociated.setVisibility(View.INVISIBLE);
        }
        
        
        if (txExpiration != null){//Agregamos la fecha de expiracion, si existe
        	if(task.getExpiration()!=null){
        		String txDate = "";
        		try {
					Date date = _dateFormat.parse(task.getExpiration());		
					txDate = _expirationFormat.format(date);
				} catch (ParseException e) {
					Log.e(TAG, e.toString());
				}
        		txExpiration.setText(txDate);
        	}
        }
        
        if (iExpiration != null){//Agregamos la imagen de expiracion, si existe
        	if(task.getExpiration()!=null){
        		if(!task.getExpiration().isEmpty())
        			iExpiration.setVisibility(View.VISIBLE);
        		else iExpiration.setVisibility(View.INVISIBLE);
        	}
    		else iExpiration.setVisibility(View.INVISIBLE);
        }
        
        if (btnNote != null){//icono si hay o no notas y listener para actualizar estado tarea en BD
        	final String note = task.getNote();
        	if(note!=null){
        		if(note.equals(""))
                	btnNote.setImageResource(R.drawable.notes_false); 
        		else
        			btnNote.setImageResource(R.drawable.notes_true);
        	}	
        	else 
        		btnNote.setImageResource(R.drawable.notes_false);        	       	
        	btnNote.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View view) {
					//custom note dialog
					final Dialog dialog = new Dialog(_context);
					dialog.setContentView(R.layout.note_dialog);
					dialog.setTitle(view.getResources().getString(R.string.txNote));
					// set the custom dialog components - text, image and button
					final EditText txNote = (EditText) dialog.findViewById(R.id.note);
					Button okButton = (Button) dialog.findViewById(R.id.btnGuardar);
					txNote.setText(note);
					
					okButton.setOnClickListener(new OnClickListener() {						
						@Override
						public void onClick(View view) {
							String newNote = txNote.getText().toString();
							task.setNote(newNote);							
							try{
								DBHelper helper = OpenHelperManager.getHelper(_context, DBHelper.class);
								Dao<Task, Integer> dao = helper.getTaskDAO();
								if(dao!=null)
									dao.update(task);
							}catch(Exception ex){
								Log.e(TAG, _errorUpdate);
								Toast.makeText(_context, _errorUpdate, Toast.LENGTH_SHORT).show();
							}
							dialog.dismiss();	
							display(vista,task);
						}
					});					
					dialog.show();
				}
			});        	
        }
        
        //Abrimos la tarea al hacer click sobre ella
        lineData.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				openTask(task);
			}
		});
        
        //abrimos el menú contextual tras un click largo sobre el cuerpo de la tarea
        lineData.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				showOptionsMenu(vista,task);
				return true;
			}
		});
        
    	}
    }  
    

	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater layout = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layout.inflate(R.layout.task_item, null);
        }
        Task task = (Task) _tasks.get(position);
        if (task != null) {
        	display(view,task);	            
        }
        return view;
    }    

    /**
     * mostramos las diferentes opciones del menú contextual según tarea y controlamos 
     * acciones según selección
     * @param v : Vista sobre la que se realiza la acción
     * @param task : tarea representada en la vista
     */
  	private void showOptionsMenu(View v, Task task){
  		final View view = v;
  		final Task taskF = task;
  		ItemAdapter adapter = new ItemAdapter(this.getContext(),android.R.layout.select_dialog_item,android.R.id.text1,getItemsOptionsMenu(taskF));
  		new AlertDialog.Builder(this.getContext())
  			.setTitle(taskF.getName())
  			.setCancelable(true)
  			.setAdapter(adapter, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					boolean b=false;
					DBHelper db = OpenHelperManager.getHelper(_context, DBHelper.class);
					Dao<Task, Integer> dao;
					try {
						dao = db.getTaskDAO();
						switch(which){
							case 0: //abrir tarea
								openTask(taskF);
								break;
							case 1: //Eliminar tarea	
								taskF.stopAlarm(_context);
								dao.delete(taskF);
								b=true;								
								break;
							case 2: //Terminar-No Terminar	
								taskF.setCompleted(!taskF.isCompleted());
								dao.update(taskF);
								break;
							case 3: //Favorito - NoFavorito
								taskF.setStarred(!taskF.isStarred());
								dao.update(taskF);
								break;
							case 4: //QuitarRecordatorio ó Ver Asociado
								if(taskF.getReminder()==null){
					  				openAssociated(taskF);
								}
								else if(taskF.getReminder().isEmpty()){
						  			openAssociated(taskF);
								}
								else{
									taskF.setReminder(null);
									taskF.stopAlarm(_context);
									dao.update(taskF);
								}
								break;
							case 5: //Ver Asociado
								openAssociated(taskF);
								break;
							default:break;
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
					if(b) display(view,null);
					else display(view,taskF);
					dialog.dismiss();
				}
			}).show();
  	}

  	/**
  	 * Obtenemos los diferentes elementos del menú contextual según características
  	 * de la tarea
  	 * @param task : Tarea sobre la que se consulta su menú contextual
  	 * @return Item[] con las opciones realizables sobre la tarea
  	 */
  	private Item[] getItemsOptionsMenu(Task task){
  		int i = 4;
  		if(task.getAssociated()!=null) i=i+1;
  		if(task.getReminder()!=null){
  			if(!task.getReminder().isEmpty())i=i+1;
  		}	
  		Item[] items = new Item[i];
  		i=0;
  		items[i]= new Item(this.getContext().getResources().getString(R.string.option_open),R.drawable.info);i=i+1;
  		items[i]= new Item(this.getContext().getResources().getString(R.string.option_delete), R.drawable.delete_icon);i=i+1;
  		if(task.isCompleted()){
  			items[i]= new Item(this.getContext().getResources().getString(R.string.option_unfinished), R.drawable.unchecked);  			
  			i=i+1;
  		}
  		else {
  			items[i]= new Item(this.getContext().getResources().getString(R.string.option_finished), R.drawable.checked);
  			i=i+1;  			
  		}
  		if(task.isStarred()){
  			items[i]= new Item(this.getContext().getResources().getString(R.string.option_unstarred), R.drawable.unstarred_icon);
  			i=i+1;
  		}
  		else {
  			items[i]= new Item(this.getContext().getResources().getString(R.string.option_starred), R.drawable.starred_icon);
  			i=i+1;
  		}
  		if(task.getReminder()!=null){
  			if(!task.getReminder().isEmpty()){
  				items[i]= new Item(this.getContext().getResources().getString(R.string.option_reminder), R.drawable.withoutalert);
  				i=i+1;
  			}
  		}
  		if(task.getAssociated()!=null){
  			items[i]= new Item(this.getContext().getResources().getString(R.string.option_associated), R.drawable.search);
  			i=i+1;
  		}  		
  		return items;
  	}  	

  	/**
  	 * Abre la tarea para la visualización de sus detalles en la actividad TaskInfoActivity
  	 * @param task : tarea que se quiere mostrar, se remite a la nueva actividad como extra
  	 */
	public void openTask(Task task){
		Intent myIntent = new Intent(_context, TaskInfoActivity.class);
		Bundle myBundle = new Bundle();
		myBundle.putSerializable(DBListActivity.TASK, task);
		myIntent.putExtras(myBundle);
		_context.startActivity(myIntent);
	}
  	
	/**
	 * Abre contacto asociado a la tarea (Single o Group)
	 * @param task : Tarea sobre la que se quiere abrir su asociado
	 */
	public void openAssociated(Task task){
		Intent myIntent = new Intent(_context, ContactActivity.class);
		Bundle myBundle = new Bundle();
		myBundle.putSerializable(Task.ASSOCIATED, task.getAssociated());
		myIntent.putExtras(myBundle);
		_context.startActivity(myIntent);
	}

}
