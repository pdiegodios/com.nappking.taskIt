package com.NappKing.TaskIt.activities;

import java.sql.SQLException;
import java.util.ArrayList;

import com.NappKing.TaskIt.R;
import com.NappKing.TaskIt.activities.TaskActivity;
import com.NappKing.TaskIt.adapters.TasklistAdapter;
import com.NappKing.TaskIt.db.DBListActivity;
import com.NappKing.TaskIt.entities.Task;
import com.NappKing.TaskIt.entities.Tasklist;
import com.NappKing.TaskIt.widget.AnimationLayout;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TasklistActivity extends DBListActivity implements OnKeyListener{
		
	private ListView _list;
		
//LIFETIME
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tasklist_tab);
		_list = (ListView) findViewById(android.R.id.list);
		registerForContextMenu(_list);
		animation = (AnimationLayout) findViewById(R.id.animation_layout);
		animation.setListener(this);
		animation.setPosition(0);
		detector = new GestureDetector(this,this);
    	getActionBar().setDisplayShowHomeEnabled(true);
    	getActionBar().setHomeButtonEnabled(true);
		EditText txNewTask = (EditText) findViewById(R.id.inputTasklist);
		txNewTask.setAlpha((float) 0.7);
		txNewTask.setOnKeyListener(this);
		update();
	}	
    
	@Override
	protected void onResume(){
		animation.setPosition(0);
		super.onResume();
		update();
	}		
	
//KEYBOARD METHODS
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		EditText txTask = (EditText) v;
		if(keyCode == EditorInfo.IME_ACTION_DONE||
				event.getAction() == KeyEvent.ACTION_DOWN &&
				event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
			if(!event.isShiftPressed()){
				String name = txTask.getText().toString();				
				try{
					Dao<Tasklist,Integer> dao = getHelper().getTasklistDAO();
					Tasklist tasklist = new Tasklist();
					tasklist.setName(name);
					tasklist.setStarred(false);
					dao.create(tasklist);
					update();
				}catch(Exception ex){
					ex.printStackTrace();
				}
				txTask.setText("");	
			}
		}
		return false;
	}

//INHERITED METHODS	
	@Override
	protected void update(){
		ArrayList<Tasklist> tasklists = new ArrayList<Tasklist>();
		try{
			Dao<Tasklist,Integer> dao = getHelper().getTasklistDAO();
			QueryBuilder<Tasklist, Integer> queryBuilder = dao.queryBuilder();
			queryBuilder.orderBy(Tasklist.NAME, true);
		    PreparedQuery<Tasklist> preparedQuery = queryBuilder.prepare();
			tasklists = (ArrayList<Tasklist>) dao.query(preparedQuery);
		}catch (Exception ex){
			Log.e(TAG, "Error al recuperar listas");
			Toast.makeText(this, "Error al recuperar listas", Toast.LENGTH_SHORT).show();
		}
		TasklistAdapter adapter = new TasklistAdapter(this, R.layout.tasklist_item, tasklists);	
		_list.setAdapter(adapter);
	}
	

	
//LIST METHOD
	@Override
	public void onListItemClick(ListView l, View v, int position, long id){
		Tasklist tasklist = (Tasklist) l.getAdapter().getItem(position);
		openList(tasklist);
	}	
	
//AUXILIAR METHODS
	public void openList(Tasklist tasklist){
		Intent myIntent = new Intent(this, TaskActivity.class);
		Bundle myBundle = new Bundle();
		myBundle.putInt(TAG, LISTMEMBERS);
		myBundle.putSerializable(LIST, tasklist);
		myIntent.putExtras(myBundle);
		startActivity(myIntent);
	}	
	
//MENU METHOD    
    /**
     * Agregamos las opciones disponibles en el menú contextual.
     */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v.getId()==android.R.id.list) {
			super.onCreateContextMenu(menu, v, menuInfo);
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
			Tasklist tasklist = (Tasklist) _list.getAdapter().getItem(info.position);
			menu.setHeaderTitle(tasklist.getName());
			//Abrir
			menu.add(0,v.getId(),0, getString(R.string.option_open));
			//Editar Nombre
			menu.add(0,v.getId(),0, getString(R.string.option_editname));
			//Eliminar
			if (tasklist.getId()!=1) //Cajón Desastre no se elimina
				menu.add(0,v.getId(),0, getString(R.string.option_delete));
		}	  
	}	
	
	/**
	 * Acciones en el menú contextual
	 */
	public boolean onContextItemSelected(MenuItem item) {
	    AdapterView.AdapterContextMenuInfo info =(AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		final Tasklist tasklist = (Tasklist) _list.getAdapter().getItem(info.position);	    
	    
		String option = item.getTitle().toString();
		
	    //abrir
	    if (option == getString(R.string.option_open)) {
	    	openList(tasklist);
	    
	    //Editar Nombre
	    }else if (option == getString(R.string.option_editname)) {
	    	final EditText input = new EditText(this);
	    	input.setText(tasklist.getName(), TextView.BufferType.EDITABLE);
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this)
	    	.setTitle(this.getResources().getString(R.string.txName))
	    	.setMessage(this.getResources().getString(R.string.txNewNameTask))
	    	.setPositiveButton(RESULT_OK, new OnClickListener() {
	    		@Override
	    	    public void onClick(DialogInterface dialog, int which) {
	    	    	tasklist.setName(input.getText().toString());
	    	    	Dao<Tasklist, Integer> dao;
					try {
						dao = getHelper().getTasklistDAO();
		    	    	dao.update(tasklist);
					} catch (SQLException e) {
						e.printStackTrace();
					}
	    	    	update();
	    	    	dialog.dismiss();
	    	    }
	    	});
	    	AlertDialog dialog = builder.create();
	    	dialog.setView(input, 10, 0, 10, 0); // 10 spacing, left and right
	    	dialog.show(); 
	    
	    //Eliminar	    	
	    }else if (option == getString(R.string.option_delete)) {
			AlertDialog.Builder alertDelete = new AlertDialog.Builder(this);  
			alertDelete.setTitle(this.getResources().getString(R.string.option_delete)+" "+tasklist.toString());  
			alertDelete.setMessage("¿"+this.getResources().getString(R.string.askfordeletelist)+" "+tasklist.toString()+"?");            
			alertDelete.setCancelable(true);  
			alertDelete.setPositiveButton(this.getResources().getString(R.string.accept), new DialogInterface.OnClickListener() {  
				public void onClick(DialogInterface dialog, int id) {  
	            	try{
	            		_dao=getHelper().getTaskDAO();
	            		ArrayList<Task> tasksToMove= (ArrayList<Task>)_dao.queryForEq(Task.LIST, tasklist);
	            		Dao<Tasklist,Integer> daoTasklist = getHelper().getTasklistDAO();
	            		Tasklist disasterBox = daoTasklist.queryForId(1);
	            		for(int i=0;i<tasksToMove.size();i++){
	            			Task task = tasksToMove.get(i);
	            			task.setList(disasterBox);
	            			_dao.update(task);
	            		}
	            		daoTasklist=getHelper().getTasklistDAO(); 	
			            daoTasklist.delete(tasklist);		            	 
			            Toast.makeText(getBaseContext(), getResources().getString(R.string.txList)+" "+
			            		tasklist.toString()+" "+getResources().getString(R.string.txDeleted),Toast.LENGTH_SHORT).show();
			            update();
		            }catch (Exception ex){
		            	ex.printStackTrace();
		            }
		            dialog.dismiss();
	            }  
	        });  
			alertDelete.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {  	        
				public void onClick(DialogInterface dialog, int id) {  
		            dialog.dismiss();
		        }  
			});            
			alertDelete.show();				
	    }
	    else return false;
	    update();
	    return true;
	}

	@Override
	protected String getCurrentTab() {
		return "Listas";
	}
}
