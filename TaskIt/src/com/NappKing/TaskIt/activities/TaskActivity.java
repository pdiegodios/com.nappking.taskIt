package com.NappKing.TaskIt.activities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;

import com.NappKing.TaskIt.R;
import com.NappKing.TaskIt.adapters.TaskAdapter;
import com.NappKing.TaskIt.db.DBListActivity;
import com.NappKing.TaskIt.entities.Task;
import com.NappKing.TaskIt.entities.Tasklist;
import com.NappKing.TaskIt.widget.AnimationLayout;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

public class TaskActivity extends DBListActivity implements OnKeyListener{
	
	final static int EXPIRATION = 0;
	final static int PRIORITY = 1;
	final static int NAME = 2;
	private int _type = 0;
	private Tasklist _tasklist = null;
	private ListView _list;
	private Boolean _completed;
	private int _orderBy=0;
	private DateFormat _dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
	private MenuItem tasksCompleted, tasksUncompleted;	
	
//LIFETIME
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.task_tab);
		Bundle myBundle = getIntent().getExtras();
		_type = myBundle.getInt(TAG);		
		animation = (AnimationLayout) findViewById(R.id.animation_layout);
		animation.setListener(this);
		animation.setPosition(_type);
		detector = new GestureDetector(this,this);
		if(getActionBar()!=null){
	    	getActionBar().setDisplayShowHomeEnabled(true);
	    	getActionBar().setHomeButtonEnabled(true);
		}
		_completed=false;
		_list = (ListView) findViewById(android.R.id.list);
		_tasklist = (Tasklist) myBundle.getSerializable(LIST);
		if(_tasklist!=null)
			setTitle(_tasklist.getName());
		EditText txNewTask = (EditText) findViewById(R.id.inputTask);
		txNewTask.setAlpha((float) 0.7);
		txNewTask.setOnKeyListener(this);
		setVisibility(txNewTask);
		update();
	}		
    
	@Override
	protected void onResume(){
		animation.setPosition(_type);
		super.onResume();
		update();
	}			
	
	
	
	
//INHERITED METHODS
	@Override
	protected void update(){
		ArrayList<Task> tasks = getTasks();
		TaskAdapter adapter = new TaskAdapter(this, R.layout.task_item, tasks);	
		_list.setAdapter(adapter);
	}

	@Override
	protected String getCurrentTab() {
		switch(_type){
			case TOMORROW: return getResources().getString(R.string.menu_tomorrow);
			case ALL: return getResources().getString(R.string.menu_all);
			case NEXTDAYS: return getResources().getString(R.string.menu_nextdays);
			case WITHOUTEXPIRATION: return getResources().getString(R.string.menu_noexpiration);
			case EXPIRED: return getResources().getString(R.string.menu_expired);
			case STARRED: return getResources().getString(R.string.menu_starred);
			case TODAY: return getResources().getString(R.string.menu_today);
			case LISTMEMBERS: return _tasklist.getName();
			default: return null;
		}
	}
	
	
	
	

//AUXILIAR METHODS
	private void setVisibility(View v){
		switch(_type){
		case ALL: 
			v.setVisibility(View.VISIBLE);
			break;
		case TOMORROW: 
			v.setVisibility(View.VISIBLE);
			break;
		case NEXTDAYS: 
			v.setVisibility(View.INVISIBLE);
			break;
		case WITHOUTEXPIRATION: 
			v.setVisibility(View.VISIBLE);
			break;
		case EXPIRED: 
			v.setVisibility(View.INVISIBLE);
			break;
		case STARRED: 
			v.setVisibility(View.VISIBLE);
			break;
		case CONTACTS: 
			v.setVisibility(View.VISIBLE);
			break;
		case LISTMEMBERS: 
			v.setVisibility(View.VISIBLE);
			break;
		default: break;
		}
		setTitle(getCurrentTab());
	}
	
	/**
	 * Devuelve las tareas correspondientes a cada pestaña, cuya definición es clara en la interfaz
	 * @return Listado de tareas según pestaña
	 */
	private ArrayList<Task> getTasks(){
		ArrayList<Task> tasks = new ArrayList<Task>();
		try{
			Dao<Task,Integer> dao = getHelper().getTaskDAO();
			QueryBuilder<Task, Integer> queryBuilder;
			Where<Task,Integer> where;
			PreparedQuery<Task> preparedQuery;
			queryBuilder = dao.queryBuilder();
			where = queryBuilder.where();
			if(tasksCompleted!=null){
				if(_completed){
					tasksCompleted.setVisible(true);
					tasksUncompleted.setVisible(false);
				}
				else{
					tasksCompleted.setVisible(false);
					tasksUncompleted.setVisible(true);						
				}
			}
			
			switch(_type){
			
				case ALL:		
					where.eq(Task.COMPLETED, _completed);
					switch(_orderBy){
						case EXPIRATION:
							where.and();
							where.isNotNull(Task.EXPIRATION);
							queryBuilder.orderBy(Task.EXPIRATION, true);
					    	preparedQuery = queryBuilder.prepare();
					    	tasks = (ArrayList<Task>) dao.query(preparedQuery);
					    	//Las que no tienen fecha de expiración las ponemos últimas
					    	where = queryBuilder.where();
					    	where.eq(Task.COMPLETED, _completed);
					    	where.and();
					    	where.isNull(Task.EXPIRATION);
					    	preparedQuery = queryBuilder.prepare();
					    	tasks.addAll((ArrayList<Task>) dao.query(preparedQuery));
							break;
						case PRIORITY:
							where.and();
							where.lt(Task.LEVEL, R.color.green);
							queryBuilder.orderBy(Task.LEVEL, false);
					    	preparedQuery = queryBuilder.prepare();
					    	tasks = (ArrayList<Task>) dao.query(preparedQuery);
					    	//Las de prioridad verde tienen un valor mayor pero tienen que ir últimas
					    	where = queryBuilder.where();
					    	where.eq(Task.COMPLETED, _completed);
					    	where.and();
					    	where.eq(Task.LEVEL, R.color.green);
					    	preparedQuery = queryBuilder.prepare();
					    	tasks.addAll((ArrayList<Task>) dao.query(preparedQuery));
							break;
						case NAME:
							queryBuilder.orderBy(Task.NAME, true);
							preparedQuery = queryBuilder.prepare();
							tasks = (ArrayList<Task>) dao.query(preparedQuery);							
							break;
						default:break;
					}
					break;
					
				case TOMORROW:
					//tomorrow
					Calendar tomorrow = Calendar.getInstance();
					tomorrow.set(Calendar.HOUR_OF_DAY, 0);
					tomorrow.set(Calendar.MINUTE, 0);
					tomorrow.set(Calendar.SECOND, 0);
					tomorrow.set(Calendar.MILLISECOND, 0);
					tomorrow.add(Calendar.DATE, 1);
					//afterTomorrow
					Calendar afterTomorrow = Calendar.getInstance();
					afterTomorrow.set(Calendar.HOUR_OF_DAY, 0);
					afterTomorrow.set(Calendar.MINUTE, 0);
					afterTomorrow.set(Calendar.SECOND, 0);
					afterTomorrow.set(Calendar.MILLISECOND, 0);
					afterTomorrow.add(Calendar.DATE, 2);
					queryBuilder = dao.queryBuilder();
					where = queryBuilder.where();
					where.eq(Task.COMPLETED, _completed);
					where.and();
					where.between(Task.EXPIRATION, _dateFormat.format(tomorrow.getTime()), 
							_dateFormat.format(afterTomorrow.getTime()));
					switch(_orderBy){
						case EXPIRATION:
							queryBuilder.orderBy(Task.EXPIRATION, true);
						    preparedQuery = queryBuilder.prepare();
							tasks = (ArrayList<Task>) dao.query(preparedQuery);
							break;
						case PRIORITY:
							where.and();
							where.lt(Task.LEVEL, R.color.green);
							queryBuilder.orderBy(Task.LEVEL, false);
					    	preparedQuery = queryBuilder.prepare();
					    	tasks = (ArrayList<Task>) dao.query(preparedQuery);
					    	//Las de prioridad verde tienen un valor mayor pero tienen que ir últimas
					    	where = queryBuilder.where();
					    	where.eq(Task.COMPLETED, _completed);
							where.and();
							where.between(Task.EXPIRATION, _dateFormat.format(tomorrow.getTime()), 
									_dateFormat.format(afterTomorrow.getTime()));
					    	where.and();
					    	where.eq(Task.LEVEL, R.color.green);
					    	preparedQuery = queryBuilder.prepare();
					    	tasks.addAll((ArrayList<Task>) dao.query(preparedQuery));
							break;
						case NAME:
							queryBuilder.orderBy(Task.NAME, true);
							preparedQuery = queryBuilder.prepare();
							tasks = (ArrayList<Task>) dao.query(preparedQuery);							
							break;
						default:break;
					}
					break;
					
				case NEXTDAYS:
					//today
					Calendar today = Calendar.getInstance();
					Calendar nextWeek = Calendar.getInstance();
					//set nextWeek to 7 days in advance
					nextWeek.set(Calendar.HOUR_OF_DAY, 0);
					nextWeek.set(Calendar.MINUTE, 0);
					nextWeek.set(Calendar.SECOND, 0);
					nextWeek.set(Calendar.MILLISECOND, 0);
					nextWeek.add(Calendar.DATE, 7);
					where.eq(Task.COMPLETED, _completed);
					where.and();
					where.between(Task.EXPIRATION, _dateFormat.format(today.getTime()), 
							_dateFormat.format(nextWeek.getTime()));
					switch(_orderBy){
						case EXPIRATION:
							queryBuilder.orderBy(Task.EXPIRATION, true);
						    preparedQuery = queryBuilder.prepare();
							tasks = (ArrayList<Task>) dao.query(preparedQuery);
							break;
						case PRIORITY:
							where.and();
							where.lt(Task.LEVEL, R.color.green);
							queryBuilder.orderBy(Task.LEVEL, false);
					    	preparedQuery = queryBuilder.prepare();
					    	tasks = (ArrayList<Task>) dao.query(preparedQuery);
					    	//Las de prioridad verde tienen un valor mayor pero tienen que ir últimas
					    	where = queryBuilder.where();
					    	where.eq(Task.COMPLETED, _completed);
							where.and();
							where.between(Task.EXPIRATION, _dateFormat.format(today.getTime()), 
									_dateFormat.format(nextWeek.getTime()));
					    	where.and();
					    	where.eq(Task.LEVEL, R.color.green);
					    	preparedQuery = queryBuilder.prepare();
					    	tasks.addAll((ArrayList<Task>) dao.query(preparedQuery));
							break;
						case NAME:
							queryBuilder.orderBy(Task.NAME, true);
							preparedQuery = queryBuilder.prepare();
							tasks = (ArrayList<Task>) dao.query(preparedQuery);							
							break;
						default:break;
					}
					break;
					
				case WITHOUTEXPIRATION:
					where.eq(Task.COMPLETED, _completed);
					where.and();
					where.isNull(Task.EXPIRATION);
					switch(_orderBy){
						case EXPIRATION:
							queryBuilder.orderBy(Task.EXPIRATION, true);
						    preparedQuery = queryBuilder.prepare();
							tasks = (ArrayList<Task>) dao.query(preparedQuery);
							break;
						case PRIORITY:
							where.and();
							where.lt(Task.LEVEL, R.color.green);
							queryBuilder.orderBy(Task.LEVEL, false);
					    	preparedQuery = queryBuilder.prepare();
					    	tasks = (ArrayList<Task>) dao.query(preparedQuery);
					    	//Las de prioridad verde tienen un valor mayor pero tienen que ir últimas
					    	where = queryBuilder.where();
					    	where.eq(Task.COMPLETED, _completed);
					    	where.and();
							where.isNull(Task.EXPIRATION);
					    	where.and();
					    	where.eq(Task.LEVEL, R.color.green);
					    	preparedQuery = queryBuilder.prepare();
					    	tasks.addAll((ArrayList<Task>) dao.query(preparedQuery));
							break;
						case NAME:
							queryBuilder.orderBy(Task.NAME, true);
							preparedQuery = queryBuilder.prepare();
							tasks = (ArrayList<Task>) dao.query(preparedQuery);							
							break;
						default:break;
					}
					break;
					
				case EXPIRED:
					Calendar now = Calendar.getInstance();
					where.eq(Task.COMPLETED, _completed);
					where.and();
					where.le(Task.EXPIRATION, _dateFormat.format(now.getTime()));
					switch(_orderBy){
						case EXPIRATION:
							queryBuilder.orderBy(Task.EXPIRATION, true);
						    preparedQuery = queryBuilder.prepare();
							tasks = (ArrayList<Task>) dao.query(preparedQuery);
							break;
						case PRIORITY:
							where.and();
							where.lt(Task.LEVEL, R.color.green);
							queryBuilder.orderBy(Task.LEVEL, false);
					    	preparedQuery = queryBuilder.prepare();
					    	tasks = (ArrayList<Task>) dao.query(preparedQuery);
					    	//Las de prioridad verde tienen un valor mayor pero tienen que ir últimas
					    	where = queryBuilder.where();
					    	where.eq(Task.COMPLETED, _completed);
							where.and();
							where.le(Task.EXPIRATION, _dateFormat.format(now.getTime()));
					    	where.and();
					    	where.eq(Task.LEVEL, R.color.green);
					    	preparedQuery = queryBuilder.prepare();
					    	tasks.addAll((ArrayList<Task>) dao.query(preparedQuery));
							break;
						case NAME:
							queryBuilder.orderBy(Task.NAME, true);
							preparedQuery = queryBuilder.prepare();
							tasks = (ArrayList<Task>) dao.query(preparedQuery);							
							break;
						default:break;
					}
					break;
					
				case STARRED:
					where.eq(Task.COMPLETED, _completed);
					where.and();
					where.eq(Task.STARRED, true);
					switch(_orderBy){
						case EXPIRATION:
							where.and();
							where.isNotNull(Task.EXPIRATION);
							queryBuilder.orderBy(Task.EXPIRATION, true);
					    	preparedQuery = queryBuilder.prepare();
					    	tasks = (ArrayList<Task>) dao.query(preparedQuery);
					    	//Las que no tienen fecha de expiración las ponemos últimas
					    	where = queryBuilder.where();
					    	where.eq(Task.COMPLETED, _completed);
							where.and();
							where.eq(Task.STARRED, true);
					    	where.and();
					    	where.isNull(Task.EXPIRATION);
					    	preparedQuery = queryBuilder.prepare();
					    	tasks.addAll((ArrayList<Task>) dao.query(preparedQuery));
							break;
						case PRIORITY:
							where.and();
							where.lt(Task.LEVEL, R.color.green);
							queryBuilder.orderBy(Task.LEVEL, false);
					    	preparedQuery = queryBuilder.prepare();
					    	tasks = (ArrayList<Task>) dao.query(preparedQuery);
					    	//Las de prioridad verde tienen un valor mayor pero tienen que ir últimas
					    	where = queryBuilder.where();
					    	where.eq(Task.COMPLETED, _completed);
							where.and();
							where.eq(Task.STARRED, true);
					    	where.and();
					    	where.eq(Task.LEVEL, R.color.green);
					    	preparedQuery = queryBuilder.prepare();
					    	tasks.addAll((ArrayList<Task>) dao.query(preparedQuery));
							break;
						case NAME:
							queryBuilder.orderBy(Task.NAME, true);
							preparedQuery = queryBuilder.prepare();
							tasks = (ArrayList<Task>) dao.query(preparedQuery);							
							break;
						default:break;
					}
					break;
					
				case TODAY:
					//from startOfToday
					Calendar startToday = Calendar.getInstance();
					startToday.set(Calendar.HOUR_OF_DAY, 0);
					startToday.set(Calendar.MINUTE, 0);
					startToday.set(Calendar.SECOND, 0);
					startToday.set(Calendar.MILLISECOND, 0);
					//until endOfToday
					Calendar startTomorrow = Calendar.getInstance();
					startTomorrow.set(Calendar.HOUR_OF_DAY, 0);
					startTomorrow.set(Calendar.MINUTE, 0);
					startTomorrow.set(Calendar.SECOND, 0);
					startTomorrow.set(Calendar.MILLISECOND, 0);
					startTomorrow.add(Calendar.DATE, 1);
					where.eq(Task.COMPLETED, _completed);
					where.and();
					where.between(Task.EXPIRATION, _dateFormat.format(startToday.getTime()), 
							_dateFormat.format(startTomorrow.getTime()));
					switch(_orderBy){
						case EXPIRATION:
							queryBuilder.orderBy(Task.EXPIRATION, true);
						    preparedQuery = queryBuilder.prepare();
							tasks = (ArrayList<Task>) dao.query(preparedQuery);
							break;
						case PRIORITY:
							where.and();
							where.lt(Task.LEVEL, R.color.green);
							queryBuilder.orderBy(Task.LEVEL, false);
					    	preparedQuery = queryBuilder.prepare();
					    	tasks = (ArrayList<Task>) dao.query(preparedQuery);
					    	//Las de prioridad verde tienen un valor mayor pero tienen que ir últimas
					    	where = queryBuilder.where();
					    	where.eq(Task.COMPLETED, _completed);
							where.and();
							where.between(Task.EXPIRATION, _dateFormat.format(startToday.getTime()), 
									_dateFormat.format(startTomorrow.getTime()));
					    	where.and();
					    	where.eq(Task.LEVEL, R.color.green);
					    	preparedQuery = queryBuilder.prepare();
					    	tasks.addAll((ArrayList<Task>) dao.query(preparedQuery));
							break;
						case NAME:
							queryBuilder.orderBy(Task.NAME, true);
							preparedQuery = queryBuilder.prepare();
							tasks = (ArrayList<Task>) dao.query(preparedQuery);							
							break;
						default:break;
					}
					break;
					
				case LISTMEMBERS:		
					where.eq(Task.LIST, _tasklist);
					where.and();
					where.eq(Task.COMPLETED, _completed);
					switch(_orderBy){
						case EXPIRATION:
							where.and();
							where.isNotNull(Task.EXPIRATION);
							queryBuilder.orderBy(Task.EXPIRATION, true);
					    	preparedQuery = queryBuilder.prepare();
					    	tasks = (ArrayList<Task>) dao.query(preparedQuery);
					    	//Las que no tienen fecha de expiración las ponemos últimas
					    	where = queryBuilder.where();
					    	where.eq(Task.COMPLETED, _completed);
							where.and();
							where.eq(Task.LIST, _tasklist);
					    	where.and();
					    	where.isNull(Task.EXPIRATION);
					    	preparedQuery = queryBuilder.prepare();
					    	tasks.addAll((ArrayList<Task>) dao.query(preparedQuery));
							break;
						case PRIORITY:
							where.and();
							where.lt(Task.LEVEL, R.color.green);
							queryBuilder.orderBy(Task.LEVEL, false);
					    	preparedQuery = queryBuilder.prepare();
					    	tasks = (ArrayList<Task>) dao.query(preparedQuery);
					    	//Las de prioridad verde tienen un valor mayor pero tienen que ir últimas
					    	where = queryBuilder.where();
					    	where.eq(Task.COMPLETED, _completed);
							where.and();
							where.eq(Task.LIST, _tasklist);
					    	where.and();
					    	where.eq(Task.LEVEL, R.color.green);
					    	preparedQuery = queryBuilder.prepare();
					    	tasks.addAll((ArrayList<Task>) dao.query(preparedQuery));
							break;
						case NAME:
							queryBuilder.orderBy(Task.NAME, true);
							preparedQuery = queryBuilder.prepare();
							tasks = (ArrayList<Task>) dao.query(preparedQuery);							
							break;
						default:break;
					}
					break;
				default:
					break;
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return tasks;
	}

	/**
	 * Se implementa la acción de crear una actividad al pulsar el botón enter. 
	 * OnKey es ejecutado cada vez que se interactúa con el teclado.
	 */
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		EditText txTask = (EditText) v;
		if(keyCode == EditorInfo.IME_ACTION_DONE||
				event.getAction() == KeyEvent.ACTION_DOWN &&
				event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
			if(!event.isShiftPressed()){
				String name = txTask.getText().toString();				
				try{
					Dao<Task,Integer> dao = getHelper().getTaskDAO();
					Task task = new Task();
					task.setName(name);
					task.setLevel(R.color.green);
					switch(_type){
						case TOMORROW:
							Calendar tomorrow = Calendar.getInstance();
							tomorrow.add(Calendar.DATE, 1);
							task.setExpiration(_dateFormat.format(tomorrow.getTime()));
							break;
						case TODAY:
							Calendar today = Calendar.getInstance();
							today.set(Calendar.HOUR_OF_DAY, 23);
							today.set(Calendar.MINUTE, 59);
							today.set(Calendar.SECOND, 59);
							today.set(Calendar.MILLISECOND, 999);
							task.setExpiration(_dateFormat.format(today.getTime()));
							break;
						case STARRED:
							task.setStarred(true);
							break;
						case LISTMEMBERS:
							task.setList(_tasklist);
						default: break;
					}
					task.setCompleted(false);
					task.setAssociated(null);
					task.setNote(null);
					if(_tasklist==null)
						task.setList(Tasklist.getDefault(v.getContext()));
					dao.create(task);
					update();
				}catch(Exception ex){
					ex.printStackTrace();
				}
				txTask.setText("");	
			}
		}
		return false;
	}
	
	
//MENU METHODS
	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {	
		if(!_completed){	 
		    menu.getItem(0).setVisible(true);   //Orden	 
			menu.getItem(1).setVisible(false);	//Terminadas  		
		    menu.getItem(2).setVisible(true);   //Sin Terminar 
		}
		else{ 	 
		    menu.getItem(0).setVisible(true);   //Orden	  
			menu.getItem(1).setVisible(true);	//Terminadas   		
		    menu.getItem(2).setVisible(false);  //Sin Terminar
		}
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case R.id.check:	
				_completed = false; 
				update();
				break;
			case R.id.unchecked:
				_completed = true; 
				update();
				break;
			case R.id.sortByExpiration:
				_orderBy = EXPIRATION; 
				update();
				break;
			case R.id.sortByPriority:
				_orderBy = PRIORITY; 
				update();
				break;
			case R.id.sortByName:
				_orderBy = NAME; 
				update();
				break;
			case android.R.id.home:
				animation.toggleSidebar();
				break;
			default: 
				break;
		}	
		return true;
	}	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menutasklist, menu);
		tasksCompleted = menu.getItem(1);
		tasksUncompleted = menu.getItem(2);
		return true;
	}	
	
}
