package com.NappKing.TaskIt.activities;

import com.NappKing.TaskIt.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SplashActivity extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        
        //Vamos a declarar un nuevo thread
        Thread timer = new Thread(){
            //El nuevo Thread exige el metodo run
            public void run(){
                try{
                    sleep(2000);
                }catch(InterruptedException e){
                    //Si no puedo ejecutar el sleep muestro el error
                    e.printStackTrace();
                }finally{
                    Intent mainActivity = new Intent("com.NappKing.EasyTaskManager.TasklistTab");
                    startActivity(mainActivity);
                    finish();
                }                
            }
        };
        //ejecuto el thread
        timer.start();
    }
    
}