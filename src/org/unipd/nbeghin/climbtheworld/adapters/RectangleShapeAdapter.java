package org.unipd.nbeghin.climbtheworld.adapters;

import org.unipd.nbeghin.climbtheworld.R;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

public class RectangleShapeAdapter extends BaseAdapter {

	private Context mContext;

	private float scale=0f;
	
    public RectangleShapeAdapter(Context c) {
        mContext = c;
        
        scale=mContext.getResources().getDisplayMetrics().density;
        
    }
	
	
	@Override
	public int getCount() {
		return 24;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		TextView item_view = (TextView) convertView;
           
		if (item_view == null){
			item_view=new TextView(mContext);
		}		
		
		item_view.setBackgroundResource(R.drawable.rectangle_shape);
		item_view.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, dpToPx(75)));
		item_view.setText(Integer.toString(position)+":00-"+Integer.toString(position+1)+":00");
		item_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
		item_view.setGravity(Gravity.CENTER);
		
        return item_view;

	}
	
	
	private int dpToPx(int dp){		
		return (int) (dp * scale + 0.5f);		
	}

}
