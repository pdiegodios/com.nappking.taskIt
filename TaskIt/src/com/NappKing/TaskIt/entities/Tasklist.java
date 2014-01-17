package com.NappKing.TaskIt.entities;

import java.io.Serializable;
import java.util.ArrayList;

import android.content.Context;

import com.NappKing.TaskIt.db.DBHelper;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

public class Tasklist implements Serializable{
	
	private static final long serialVersionUID = -4939075897233953078L;

	//Tabla
	public static final String TABLE = "tasklist";
	
	//Lista inicial y por defecto
	public static final String DEFAULT = "Cajón Desastre";
	
	//Columnas 
	public static final String ROWID = "_id";
	public static final String NAME = "name";
	public static final String STARRED = "starred";

    //índices de la BD remota
    public static final String VERSION = "version";
    public static final String IDDEVICE = "idDevice";
    public static final String IDSQLITE = "idSQLite";
    public static final String IDSQL = "idSQL";
	
	//Campos
    @DatabaseField(generatedId = true, columnName = ROWID)
    private int _id;
    @DatabaseField(columnName = NAME)
    private String _nombre;
    @DatabaseField(columnName = STARRED)
    private boolean _destacado;
    @DatabaseField(columnName = VERSION)
    private int _version;
    @DatabaseField(columnName = IDDEVICE)
    private String _idDevice;
    @DatabaseField(columnName = IDSQLITE)
    private String _idSQLite;
    @DatabaseField(columnName = IDSQL)
    private String _idSQL;
    
    //getters
    public int getId()				{return _id;}    
    public String getName()			{return _nombre;}  
    public boolean isStarred()		{return _destacado;}  
    public int getVersion()			{return _version;}
    public String getIdDevice()		{return _idDevice;}
    public String getIdSQLite()		{return _idSQLite;}
    public String getIdSQL()		{return _idSQL;}
    
    public ArrayList<Task> getAllTasks(Context context){
    	ArrayList<Task> tasks = new ArrayList<Task>();
    	try{
    		DBHelper db = (DBHelper) OpenHelperManager.getHelper(context, DBHelper.class);
    		Dao<Task, Integer> dao = db.getTaskDAO();
    		tasks = (ArrayList<Task>) dao.queryForEq(Task.LIST, this);
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    	return tasks;
    }
    
    public ArrayList<Task> getCompletedTasks(Context context, boolean completed){
    	ArrayList<Task> tasks = new ArrayList<Task>();
    	try{
    		DBHelper db = (DBHelper) OpenHelperManager.getHelper(context, DBHelper.class);
    		Dao<Task, Integer> dao = db.getTaskDAO();
    		QueryBuilder<Task, Integer> queryBuilder = dao.queryBuilder();
			Where<Task, Integer> where = queryBuilder.where();
		    where.eq(Task.LIST, this);
		    where.and();
		    where.eq(Task.COMPLETED, completed);
		    PreparedQuery<Task> preparedQuery = queryBuilder.prepare();
		    tasks = (ArrayList<Task>) dao.query(preparedQuery);
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    	return tasks;
    }
    
    public static Tasklist getDefault(Context context){
    	Tasklist tasklist = null;
    	try{
    		DBHelper db = (DBHelper) OpenHelperManager.getHelper(context, DBHelper.class);
    		Dao<Tasklist, Integer> dao = db.getTasklistDAO();
    		tasklist = dao.queryForId(1);
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    	return tasklist;
    }
    
    //setters
    public void setId(int id)				{_id=id;}    
    public void setName(String name)		{_nombre=name;}    
    public void setStarred(boolean starred)	{_destacado=starred;} 
    public void setVersion(int version)		{_version=version;}
    public void setIdDevice(String device)	{_idDevice=device;}
    public void setIdSQLite(String idSQLite){_idSQLite=idSQLite;}
    public void setIdSQL(String idSQL)		{_idSQL=idSQL;}
    
    @Override
    public String toString(){
    	return _nombre;
    }
}
