package com.NappKing.TaskIt.entities;

/**
 * Item es un objeto sencillo para crear menus contextuales que contengan un icono y descripci�n
 * @author pdiego
 */
public class Item{
	public final String text;
	public final int icon;
	
	/**
	 * @param text = descripci�n
	 * @param icon = Referencia en R.drawable.* o android.R.drawable.* al icono del men� contextual.
	 */
	public Item(String text, Integer icon){
		this.text = text;
		this.icon = icon;
	}	
	
	@Override
	public String toString(){
		return text;
	}
}
