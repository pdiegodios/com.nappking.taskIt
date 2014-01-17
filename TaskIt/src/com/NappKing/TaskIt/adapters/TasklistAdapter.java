package com.NappKing.TaskIt.adapters;

import java.util.List;

import com.NappKing.TaskIt.R;
import com.NappKing.TaskIt.entities.Tasklist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TasklistAdapter extends ArrayAdapter<Tasklist> {
	private List<Tasklist> _lists;
	private Context _context;
    
    public TasklistAdapter(Context context, int textViewResourceId, List<Tasklist> lists) {
        super(context, textViewResourceId, lists);
        this._lists = lists;
        this._context = context;
    }
    
    public List<Tasklist> getList(){
    	return _lists;
    }
        
    /*
     * Método para dibujar los iconos correspondientes de las diferentes entidades
     * y el estado de las mismas
     */
    private void display(View v, Tasklist list){
    	TextView txName = (TextView) v.findViewById(R.id.listName);
    	TextView txCount = (TextView) v.findViewById(R.id.countTasks);
    	TextView txTotal = (TextView) v.findViewById(R.id.totalTasks);
    	ImageView iDisaster = (ImageView) v.findViewById(R.id.disaster);
    	ProgressBar barCompleted = (ProgressBar) v.findViewById(R.id.progress);
        
    	iDisaster.setVisibility(View.INVISIBLE);
    	if (list.getId()==1 && iDisaster!=null){
    		iDisaster.setVisibility(View.VISIBLE);
    	}
        if (txName != null) {//Colocamos el nombre
        	txName.setText(list.getName());
        }                        
        if (txCount != null) {//Agregamos contador
        	txCount.setText(list.getCompletedTasks(v.getContext(),false).size()+"");
        }           
        if (txTotal != null) {//Agregamos contador
        	txTotal.setText(list.getAllTasks(v.getContext()).size()+"");
        }        
        if (barCompleted!=null){//Agregamos el icono al item
        	int completed = list.getCompletedTasks(v.getContext(), true).size();
        	int total = list.getAllTasks(v.getContext()).size();
        	barCompleted.setMax(total);
        	barCompleted.setProgress(completed);
        	if(completed==total&&total!=0)
        		v.setAlpha((float)0.6);
        	else v.setAlpha((float)1);
        }
    }  
	 
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater layout = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layout.inflate(R.layout.tasklist_item, null);
        }
        Tasklist tasklist = (Tasklist) _lists.get(position);
        if (tasklist != null) {
        	display(view,tasklist);	            
        }
        return view;
    }    
   
}