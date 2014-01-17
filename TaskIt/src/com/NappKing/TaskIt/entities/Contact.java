package com.NappKing.TaskIt.entities;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.NappKing.TaskIt.db.DBHelper;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

public class Contact implements Serializable{
	private static final long serialVersionUID = -8368592012001035507L;

	//Tabla
	public static final String TABLE = "Contactos";
	
	//Columnas 
	public static final String ROWID = "_id";
	public static final String NAME = "name";
	public static final String MOBILE = "mobile";
	public static final String PHONE_WORK = "phone_work";
	public static final String PHONE_HOME = "phone_home";
	public static final String LOOKUPKEY = "lookupKEY";
	public static final String PHOTO = "photo";
	public static final String EMAIL = "email";
	public static final String ADDRESS = "address";

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
    @DatabaseField(columnName = MOBILE)
    private String _movil;
    @DatabaseField(columnName = PHONE_WORK)
    private String _telefonoTrabajo;
    @DatabaseField(columnName = PHONE_HOME)
    private String _telefonoCasa;
    @DatabaseField(columnName = LOOKUPKEY)
    private String _lookupkey;	
    @DatabaseField(columnName = PHOTO)
    private String _photo;	
    @DatabaseField(columnName = EMAIL)
    private String _email;	
    @DatabaseField(columnName = ADDRESS)
    private String _address;	
    @DatabaseField(columnName = VERSION)
    private int _version;
    @DatabaseField(columnName = IDDEVICE)
    private String _idDevice;
    @DatabaseField(columnName = IDSQLITE)
    private String _idSQLite;
    @DatabaseField(columnName = IDSQL)
    private String _idSQL;
    
    //getters
    public int getId()					{return _id;}    
    public String getName()				{return _nombre;}    
    public String getMobile()			{return _movil;}    
    public String getPhoneWork()		{return _telefonoTrabajo;}  
    public String getPhoneHome()		{return _telefonoCasa;}       
    public String getLookupKey()		{return _lookupkey;}        
    public String getPhoto()			{return _photo;} 
    public int getVersion()				{return _version;}
    public String getIdDevice()			{return _idDevice;}
    public String getIdSQLite()			{return _idSQLite;}
    public String getIdSQL()			{return _idSQL;}
    public List<Task> getTasks(Context context){
    	List<Task> tasks = new ArrayList<Task>();
		DBHelper db = OpenHelperManager.getHelper(context, DBHelper.class);
		try {
			Dao<Task,Integer> daoTask = db.getTaskDAO();
			QueryBuilder<Task, Integer> queryBuilder;
			queryBuilder = daoTask.queryBuilder();
			Where<Task,Integer> where;
			PreparedQuery<Task> preparedQuery;
			
			//Consulta
			where = queryBuilder.where();
			where.eq(Task.ASSOCIATED, this);
			queryBuilder.orderBy(Task.COMPLETED, true);
			queryBuilder.orderBy(Task.EXPIRATION, true);
		    preparedQuery = queryBuilder.prepare();
			tasks = (ArrayList<Task>) daoTask.query(preparedQuery);			
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	return tasks;
    }
    
    //setters
    public void setId(int id)					{_id=id;}    
    public void setName(String name)			{_nombre=name;}
    public void setMobile(String mobile)		{_movil=mobile;}    
    public void setPhoneWork(String phoneWork)	{_telefonoTrabajo=phoneWork;}    
    public void setPhoneHome(String phoneHome)	{_telefonoCasa=phoneHome;}  
    public void setLookupKey(String lookupkey)	{_lookupkey=lookupkey;}      
    public void setPhoto(String photo)			{_photo=photo;}  
    public void setVersion(int version)			{_version=version;}
    public void setIdDevice(String device)		{_idDevice=device;}
    public void setIdSQLite(String idSQLite)	{_idSQLite=idSQLite;}
    public void setIdSQL(String idSQL)			{_idSQL=idSQL;}
    
}
