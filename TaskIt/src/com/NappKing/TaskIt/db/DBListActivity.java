package com.NappKing.TaskIt.db;

import java.util.List;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.GestureDetector.OnGestureListener;

import com.NappKing.TaskIt.R;
import com.NappKing.TaskIt.entities.Task;
import com.NappKing.TaskIt.widget.AnimationLayout;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

/**
 * Clase genérica para las ListActivity con acceso a la Base de Datos
 * @author pdiego
 */
public abstract class DBListActivity extends ListActivity implements AnimationLayout.Listener, OnGestureListener{
	private DBHelper _DBHelper;
	protected Dao<Task,Integer> _dao;
	protected GestureDetector detector;
    protected AnimationLayout animation;
	
	public static final String TAG = "TaskActivity";
	public static final String LIST = "List";
	public static final String TASK = "Task";
	
	public static final int LISTMEMBERS = 0;
	public static final int STARRED = 1;
	public static final int ALL = 2;
	public static final int TODAY = 3;
	public static final int TOMORROW = 4;
	public static final int NEXTDAYS = 5;
	public static final int EXPIRED = 6;
	public static final int WITHOUTEXPIRATION = 7;
	public static final int CONTACTS = 8;
	
	protected abstract void update();
	protected abstract String getCurrentTab();
    			
	protected DBHelper getHelper(){
		if(_DBHelper==null){
			_DBHelper = OpenHelperManager.getHelper(this, DBHelper.class);
		}
		return _DBHelper;
	}
	
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (_DBHelper != null) {
            OpenHelperManager.releaseHelper();
            _DBHelper = null;
        }
    }
  //WIDGET METHODS
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
  			Intent intent = new Intent(this, menu);
  			Bundle myBundle = new Bundle();
  			myBundle.putInt(TAG, position);
  			animation.setPosition(position);
  			intent.putExtras(myBundle);
  			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
  			startActivity(intent);		
  		}
  	}
  	
  	@Override
  	public boolean onCreateOptionsMenu(Menu menu) {
  		// Inflate the menu; this adds items to the action bar if it is present.
  		getMenuInflater().inflate(R.menu.menu, menu);
  		return true;
  	}

  	@Override
  	public void onBackPressed()	{
  		ActivityManager mngr = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
  		List<ActivityManager.RunningTaskInfo> taskList = mngr.getRunningTasks(5);
  		
  		if(taskList.get(0).numActivities == 1 &&
  		   taskList.get(0).topActivity.getClassName().equals(this.getClass().getName())) {
  			//sólo hay una actividad en la pila y su nombre coincide con el de ésta
  			AlertDialog.Builder alertDelete = new AlertDialog.Builder(this);  			
  			alertDelete.setTitle(this.getResources().getString(R.string.exitapp));  
  			alertDelete.setMessage(this.getResources().getString(R.string.askforconfirmation));            
  			alertDelete.setCancelable(true);  
  			alertDelete.setPositiveButton(this.getResources().getString(R.string.accept), new DialogInterface.OnClickListener() {  
  	            public void onClick(DialogInterface dialog, int id) { 
  	            	DBListActivity.super.onBackPressed();
  	            }  
  	        });  
  			alertDelete.setNegativeButton(this.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {  
  	            public void onClick(DialogInterface dialog, int id) {  
  	                dialog.dismiss();
  	            }  
  	        });            
  			alertDelete.show();			
  		}
  		else super.onBackPressed();
  	}

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
  	
  //MENU METHODS
  	/**
  	 * Acciones realizadas al pulsar algún item de la barra de menú
  	 */
  	@Override
  	public boolean onOptionsItemSelected(MenuItem item) {
  		switch (item.getItemId()){
  		//Menú inicio despliega la barra lateral con las opciones de navegación
  		case android.R.id.home:
  	        animation.toggleSidebar();
  			break;
  		}
  		return true;
  	}

	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
	    menu.getItem(0).setVisible(false);	//Settings 
	    return true;
	}
	
}