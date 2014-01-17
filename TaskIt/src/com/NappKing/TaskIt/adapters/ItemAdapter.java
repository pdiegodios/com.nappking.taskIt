package com.NappKing.TaskIt.adapters;

import com.NappKing.TaskIt.entities.Item;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Adaptador para mostrar los item de los menús contextuales
 * @author pdiego
 */
public class ItemAdapter extends ArrayAdapter<Item>{
	private Item[] _items;
	private Context _context;

	public ItemAdapter(Context context, int resource,int textViewResourceId, Item[] items) {
        super(context, resource, textViewResourceId, items);
        this._items = items;
        this._context = context;
    }
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		//Super clase de usuario para crear la vista
        View v = super.getView(position, convertView, parent);
        TextView tv = (TextView)v.findViewById(android.R.id.text1);

        //Coloca el icono en el textview
        tv.setCompoundDrawablesWithIntrinsicBounds(_items[position].icon, 0, 0, 0);

        //Añade margen entre imagen y texto (soporta diferentes densidades de pantalla)
        int dp5 = (int) (5 * _context.getResources().getDisplayMetrics().density + 0.5f);
        tv.setCompoundDrawablePadding(dp5);

        return v;
	}
}