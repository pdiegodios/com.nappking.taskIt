package com.NappKing.TaskIt.db;

import java.sql.SQLException;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
 
import com.NappKing.TaskIt.entities.Contact;
import com.NappKing.TaskIt.entities.Task;
import com.NappKing.TaskIt.entities.Tasklist;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * Clase donde se crean los distintos DAO para acceder a las tablas de SQLite. 
 * En esta clase se especifica además el nombre y la versión de la base de datos
 */
public class DBHelper extends OrmLiteSqliteOpenHelper {

	private static final String DATABASE_NAME = "MyDB.db";
    private static final int DATABASE_VERSION = 1;
    
    //DAO's
    private Dao<Tasklist, Integer> _tasklistDAO;
    private Dao<Task, Integer> _taskDAO;     
    private Dao<Contact, Integer> _contactDAO;   
 
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    public static SQLiteDatabase getDatabaseConnection(Context c){
		return new DBHelper(c).getWritableDatabase();
	}
 
    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connection) {
        try {
        	//Creacion de las tablas de SQLite
            TableUtils.createTable(connection, Tasklist.class);
            TableUtils.createTable(connection, Contact.class);
            TableUtils.createTable(connection, Task.class);
            /*
             * Agregamos una lista por defecto: "Cajón desastre", donde irán todas las nuevas 
             * tareas creadas fuera de alguna lista.
             */
            ContentValues initialValues = new ContentValues();                        
            initialValues.put(Tasklist.NAME, Tasklist.DEFAULT);
            initialValues.put(Tasklist.STARRED, false);
            db.insert(Tasklist.TABLE, null, initialValues);
            
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
 
    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connection, int oldVersion, int newVersion) {
        Log.w(DBHelper.class.getName(),"Upgrading database from version " + oldVersion + " to "+ newVersion 
        		+ ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + Tasklist.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + Task.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + Contact.TABLE);
        onCreate(db, connection);
    }
    
    //Método para recuperar la tabla de Listas de Tareas
    public Dao<Tasklist, Integer> getTasklistDAO() throws SQLException {
        if (_tasklistDAO == null)	_tasklistDAO = getDao(Tasklist.class);
        return _tasklistDAO;
    }    
    
    //Método para recuperar la tabla de Tareas
    public Dao<Task, Integer> getTaskDAO() throws SQLException {
        if (_taskDAO == null)	_taskDAO = getDao(Task.class);
        return _taskDAO;
    }

    //Método para recuperar la tabla de Contactos
    public Dao<Contact, Integer> getContactDAO() throws SQLException {
        if (_contactDAO == null)	_contactDAO = getDao(Contact.class);
        return _contactDAO;
    }
    
    @Override
    public void close() {
    	//Se cierra la conexión con la BD
        super.close();
        _taskDAO 				= null;
        _tasklistDAO 			= null;
        _contactDAO 			= null;
    }
 
}
