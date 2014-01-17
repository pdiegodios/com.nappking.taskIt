package com.NappKing.TaskIt.widget;


import com.NappKing.TaskIt.R;
import com.NappKing.TaskIt.activities.ContactlistActivity;
import com.NappKing.TaskIt.activities.TaskActivity;
import com.NappKing.TaskIt.activities.TasklistActivity;
import com.NappKing.TaskIt.adapters.ItemAdapter;
import com.NappKing.TaskIt.entities.Item;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class AnimationLayout extends ViewGroup {

    public final static int DURATION = 100; //0.12 seconds

    protected boolean mPlaceLeft = true;
    protected boolean mOpened;
    protected View mSidebar;
    protected View mContent;
    protected int mSidebarWidth = 100; 

    protected Animation mAnimation;
    protected OpenListener mOpenListener;
    protected CloseListener mCloseListener;
    protected Listener mListener;
    
    protected ListView sidebar_list;
    
    protected boolean mPressed = false;

    public AnimationLayout(Context context) {
        this(context, null);
    }

    public AnimationLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        mSidebar = findViewById(R.id.sidebar);
        mContent = findViewById(R.id.content);
        sidebar_list = (ListView) findViewById(R.id.sidebar_list);
        final Item[] items = {
			new Item(getResources().getString(R.string.menu_lists), R.drawable.list),				//pos 0
			new Item(getResources().getString(R.string.menu_starred), R.drawable.starred_icon),		//pos 1
			new Item(getResources().getString(R.string.menu_all), R.drawable.all),					//pos 2
			new Item(getResources().getString(R.string.menu_today), R.drawable.today),				//pos 3
			new Item(getResources().getString(R.string.menu_tomorrow), R.drawable.tomorrow),			//pos 4
			new Item(getResources().getString(R.string.menu_nextdays), R.drawable.nextdays),			//pos 5
			new Item(getResources().getString(R.string.menu_expired), R.drawable.expired),			//pos 6
			new Item(getResources().getString(R.string.menu_noexpiration), R.drawable.noexpiration),	//pos 7
			new Item(getResources().getString(R.string.menu_contacts), R.drawable.customer_icon)		//pos 8
			//,new Item(getResources().getString(R.string.menu_map), R.drawable.maps_icon)			//pos 9
		};		
        
		final ItemAdapter adapter = new ItemAdapter(this.getContext(),android.R.layout.simple_list_item_activated_1,android.R.id.text1,items);
		
		if (sidebar_list == null)
			throw new NullPointerException("no view id = animation_sidebar");
        
        if (mSidebar == null)
            throw new NullPointerException("no view id = animation_sidebar");

        if (mContent == null)
            throw new NullPointerException("no view id = animation_content");

        sidebar_list.setAdapter(adapter);
        sidebar_list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mOpenListener = new OpenListener(mSidebar, mContent);
        mCloseListener = new CloseListener(mSidebar, mContent);
        sidebar_list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				toggleSidebar();
				Class<?> selected = null;
				switch (position){
					case 0: selected = TasklistActivity.class; break;			//Lista Tareas
					case 8: selected = ContactlistActivity.class; break;			//Lista de Contactos con tareas
					default:selected = TaskActivity.class; break;			//Tareas
				}		
				mListener.onNavigationTo(selected,position);
			}
		});
    }

    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        /* the title bar assign top padding, drop it */
        int sidebarLeft = l;
        if (!mPlaceLeft) {
            sidebarLeft = r - mSidebarWidth;
        }
        mSidebar.layout(sidebarLeft,
                0,
                sidebarLeft + mSidebarWidth,
                0 + mSidebar.getMeasuredHeight());

        if (mOpened) {
            if (mPlaceLeft) {
                mContent.layout(l + mSidebarWidth, 0, r + mSidebarWidth, b);
            } else  {
                mContent.layout(l - mSidebarWidth, 0, r - mSidebarWidth, b);
            }
        } else {
            mContent.layout(l, 0, r, b);
        }
    }

    @Override
    public void onMeasure(int w, int h) {
        super.onMeasure(w, h);
        super.measureChildren(w, h);
        mSidebarWidth = mSidebar.getMeasuredWidth();
    }

    @Override
    protected void measureChild(View child, int parentWSpec, int parentHSpec) {
        if (child == mSidebar) {
            int mode = MeasureSpec.getMode(parentWSpec);
            int width = (int)(getMeasuredWidth() * 0.8);
            super.measureChild(child, MeasureSpec.makeMeasureSpec(width, mode), parentHSpec);
        } else {
            super.measureChild(child, parentWSpec, parentHSpec);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isOpening()) {
            return false;
        }

        int action = ev.getAction();

        if (action != MotionEvent.ACTION_UP
                && action != MotionEvent.ACTION_DOWN) {
            return false;
        }

        /* if user press and release both on Content while
         * sidebar is opening, call listener. otherwise, pass
         * the event to child. */
        int x = (int)ev.getX();
        int y = (int)ev.getY();
        if (mContent.getLeft() < x
                && mContent.getRight() > x
                && mContent.getTop() < y
                && mContent.getBottom() > y) {
            if (action == MotionEvent.ACTION_DOWN) {
                mPressed = true;
            }

            if (mPressed
                    && action == MotionEvent.ACTION_UP
                    && mListener != null) {
                mPressed = false;
                return mListener.onContentTouchedWhenOpening();
            }
        } else {
            mPressed = false;
        }

        return false;
    }

    public void setListener(Listener l) {
        mListener = l;
    }

    /* to see if the Sidebar is visible */
    public boolean isOpening() {
        return mOpened;
    }

    public void toggleSidebar() {
        if (mContent.getAnimation() != null) {
            return;
        }
        if (mOpened) {
            /* opened, make close animation*/
            if (mPlaceLeft) {
                mAnimation = new TranslateAnimation(0, -mSidebarWidth, 0, 0);
            } else {
                mAnimation = new TranslateAnimation(0, mSidebarWidth, 0, 0);
            }
            mAnimation.setAnimationListener(mCloseListener);
        } else {
            /* not opened, make open animation */
            if (mPlaceLeft) {
                mAnimation = new TranslateAnimation(0, mSidebarWidth, 0, 0);
            } else {
                mAnimation = new TranslateAnimation(0, -mSidebarWidth, 0, 0);
            }
            mAnimation.setAnimationListener(mOpenListener);
        }
        mAnimation.setDuration(DURATION);
        mAnimation.setFillAfter(true);
        mAnimation.setFillEnabled(true);
        mContent.startAnimation(mAnimation);
    }

    public void openSidebar() {
        if (!mOpened) {
            toggleSidebar();
        }
    }

    public void closeSidebar() {
        if (mOpened) {
            toggleSidebar();
        }
    }
 
    public boolean isOpen(){
    	return mOpened;
    }
    
    public void setPosition(int position){
    	sidebar_list.setItemChecked(position, true);
    }

    class OpenListener implements Animation.AnimationListener {
        View iSidebar;
        View iContent;

        OpenListener(View sidebar, View content) {
            iSidebar = sidebar;
            iContent = content;
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationStart(Animation animation) {
            iSidebar.setVisibility(View.VISIBLE);
        }

        public void onAnimationEnd(Animation animation) {
            iContent.clearAnimation();
            mOpened = !mOpened;
            requestLayout();
            if (mListener != null) {
                mListener.onSidebarOpened();
            }
        }
    }

    class CloseListener implements Animation.AnimationListener {
        View iSidebar;
        View iContent;

        CloseListener(View sidebar, View content) {
            iSidebar = sidebar;
            iContent = content;
        }

        public void onAnimationRepeat(Animation animation) {}
        public void onAnimationStart(Animation animation) {}

        public void onAnimationEnd(Animation animation) {
            iContent.clearAnimation();
            iSidebar.setVisibility(View.INVISIBLE);
            mOpened = !mOpened;
            requestLayout();
            if (mListener != null) {
                mListener.onSidebarClosed();
            }
        }
    }

    public interface Listener {
        public void onSidebarOpened();
        public void onSidebarClosed();
        public void onNavigationTo(Class<?> Activity, int position);
        public boolean onContentTouchedWhenOpening();
    }
}
