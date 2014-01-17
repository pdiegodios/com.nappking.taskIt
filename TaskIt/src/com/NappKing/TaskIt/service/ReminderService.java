package com.NappKing.TaskIt.service;

import com.NappKing.TaskIt.R;
import com.NappKing.TaskIt.activities.TaskInfoActivity;
import com.NappKing.TaskIt.db.DBListActivity;
import com.NappKing.TaskIt.entities.Task;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

public class ReminderService extends IntentService {

	public static String REMINDER = "Reminder";
	
    public ReminderService(){
        super("ReminderService");
    }
    

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
      protected void onHandleIntent(Intent intent) {
    	Task task = (Task) intent.getSerializableExtra(DBListActivity.TASK);
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        long when = System.currentTimeMillis();         // notification time
        if(task!=null){
        	int priority = 0;
        	switch(task.getLevel()){
	        	case R.drawable.red: priority=4;break;
	        	case R.drawable.orange: priority=3;break;
	        	case R.drawable.yellow: priority=2;break;
	        	case R.drawable.green: priority=1;break;
	        	default:break;
        	}
            Intent notificationIntent = new Intent(this, TaskInfoActivity.class);
            notificationIntent.putExtra(DBListActivity.TASK, task);
            notificationIntent.putExtra(REMINDER, true);
            PendingIntent contentIntent = PendingIntent.getActivity(this, task.getId(), notificationIntent , 0);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
            	.setContentTitle(this.getResources().getString(R.string.txReminder)+": "+task.getName())
            	.setContentText(this.getResources().getString(R.string.txList)+": "+task.getList()+" | "+task.getReminder())
            	.setSmallIcon(R.drawable.alerts)
            	.setPriority(priority)
            	.setWhen(when)
            	.setContentIntent(contentIntent);
            Notification notification = builder.build();
            notification.defaults |= Notification.DEFAULT_SOUND;
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            nm.notify(task.getId(), notification);        	
        }
    }

}
