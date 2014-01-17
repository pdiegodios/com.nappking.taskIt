package com.NappKing.TaskIt.entities;

import java.io.Serializable;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.NappKing.TaskIt.db.DBListActivity;
import com.NappKing.TaskIt.service.ReminderService;
import com.j256.ormlite.field.DatabaseField;

public class Task implements Serializable{

	private static final long serialVersionUID = 3759641160190240261L;

	//Tabla
	public static final String TABLE = "Tareas";
	
	//Columnas 
	public static final String ROWID = "_id";
	public static final String NAME = "name";
	public static final String LIST = "list";
	public static final String NOTE = "notes";
	public static final String EXPIRATION = "expiration";
	public static final String REMINDER = "reminder";
	public static final String ASSOCIATED = "contacto";
	public static final String STARRED = "starred";
	public static final String COMPLETED = "checked";
	public static final String LEVEL = "level";

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
    @DatabaseField(columnName = LIST,foreign=true,foreignAutoRefresh=true)
    private Tasklist _lista;
    @DatabaseField(columnName = NOTE)
    private String _nota;
    @DatabaseField(columnName = EXPIRATION)
    private String _vencimiento;
    @DatabaseField(columnName = REMINDER)
    private String _recordatorio;
    @DatabaseField(columnName = ASSOCIATED,foreign=true,foreignAutoRefresh=true)
    private Contact _contacto;
    @DatabaseField(columnName = STARRED)
    private boolean _destacado;
    @DatabaseField(columnName = COMPLETED)
    private boolean _hecho;
    @DatabaseField(columnName = LEVEL)
    private int _nivel;
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
    public Tasklist getList()			{return _lista;}    
    public String getNote()				{return _nota;}    
    public String getExpiration()		{return _vencimiento;}    
    public String getReminder()			{return _recordatorio;}       
    public boolean isStarred()			{return _destacado;}  
    public boolean isCompleted()		{return _hecho;}   
    public int getLevel()				{return _nivel;}  
    public int getVersion()				{return _version;}
    public String getIdDevice()			{return _idDevice;}
    public String getIdSQLite()			{return _idSQLite;}
    public String getIdSQL()			{return _idSQL;}
    public Contact getAssociated()		{return _contacto;}
    
    //setters
    public void setId(int id)					{_id=id;}    
    public void setName(String name)			{_nombre=name;}    
    public void setList(Tasklist list)			{_lista=list;}    
    public void setNote(String note)			{_nota=note;}    
    public void setExpiration(String expire)	{_vencimiento=expire;}    
    public void setReminder(String reminder)	{_recordatorio=reminder;}  
    public void setStarred(boolean starred)		{_destacado=starred;}  
    public void setCompleted(boolean completed)	{_hecho=completed;}  
    public void setLevel(int level)				{_nivel=level;}  
    public void setVersion(int version)			{_version=version;}
    public void setIdDevice(String device)		{_idDevice=device;}
    public void setIdSQLite(String idSQLite)	{_idSQLite=idSQLite;}
    public void setIdSQL(String idSQL)			{_idSQL=idSQL;}
    public void setAssociated(Contact contacto)	{_contacto=contacto;}
    
	/**
	 * Programamos alarma de aviso sobre fin de tiempo en lista según la cantidad de tiempo 
	 * de antelación que haya indicado el usuario (_reminderBefore). El servicio de notificación
	 * se lanza desde ReminderService.
	 * @param context: Actividad desde dónde se lanza la alarma
	 */
	public void startAlarm(Calendar calendar, Context context) {
	    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	    long when = calendar.getTimeInMillis();         // notification time
	    Bundle myBundle = new Bundle();
	    myBundle.putSerializable(DBListActivity.TASK, this);
	    Intent intent = new Intent(context, ReminderService.class);
	    intent.putExtras(myBundle);
	    PendingIntent pendingIntent = PendingIntent.getService(context, this.getId(), intent, 0);
	    alarmManager.set(AlarmManager.RTC, when, pendingIntent);
	}
	
	/**
	 * Anula la notificación sobre fin de tarea.
	 * @param context
	 */
	public void stopAlarm(Context context){
	    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	    Bundle myBundle = new Bundle();
	    myBundle.putSerializable(DBListActivity.TASK, this);
	    Intent intent = new Intent(context, ReminderService.class);
	    intent.putExtras(myBundle);
	    PendingIntent pendingIntent = PendingIntent.getService(context, this.getId(), intent, 0);
	    alarmManager.cancel(pendingIntent);
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(this.getId());
	}
}
