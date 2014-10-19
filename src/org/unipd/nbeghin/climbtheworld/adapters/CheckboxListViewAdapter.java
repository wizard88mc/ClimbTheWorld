package org.unipd.nbeghin.climbtheworld.adapters;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.unipd.nbeghin.climbtheworld.ClimbApplication;
import org.unipd.nbeghin.climbtheworld.FBPickFriendActivity;
import org.unipd.nbeghin.climbtheworld.R;

import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CheckboxListViewAdapter extends ArrayAdapter<JSONObject> implements Filterable {

    private final Object mLock = new Object();
    
	private List<JSONObject> currentInvitableFriends;
	private Context context;
	private ImageView profilePicView;
    private ItemsFilter mFilter;

	public CheckboxListViewAdapter(Context context, List<JSONObject> invitableFriends) {
		super(context, R.layout.checkbox_listview, invitableFriends);
		this.context = context;
		this.currentInvitableFriends = invitableFriends;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
			ImageView bmImage;

			public ImageDownloader(ImageView bmImage) {
				this.bmImage = bmImage;
			}

			protected Bitmap doInBackground(String... urls) {
				String url = urls[0];
				Bitmap mIcon = null;
				try {
					InputStream in = new java.net.URL(url).openStream();
					mIcon = BitmapFactory.decodeStream(in);
				} catch (Exception e) {
					Log.e("Error", e.getMessage());
				}
				return mIcon;
			}

			protected void onPostExecute(Bitmap result) {
				bmImage.setImageBitmap(result);
				scaleImage(bmImage, 100);

			}
			
			private void scaleImage(ImageView view, int boundBoxInDp)
			{
			    // Get the ImageView and its bitmap
			    Drawable drawing = view.getDrawable();
			    Bitmap bitmap = ((BitmapDrawable)drawing).getBitmap();

			    // Get current dimensions
			    int width = bitmap.getWidth();
			    int height = bitmap.getHeight();

			    // Determine how much to scale: the dimension requiring less scaling is
			    // closer to the its side. This way the image always stays inside your
			    // bounding box AND either x/y axis touches it.
			    float xScale = ((float) boundBoxInDp) / width;
			    float yScale = ((float) boundBoxInDp) / height;
			    float scale = (xScale <= yScale) ? xScale : yScale;

			    // Create a matrix for the scaling and add the scaling data
			    Matrix matrix = new Matrix();
			    matrix.postScale(scale, scale);

			    // Create a new bitmap and convert it to a format understood by the ImageView
			    Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
			    BitmapDrawable result = new BitmapDrawable(scaledBitmap);
			    width = scaledBitmap.getWidth();
			    height = scaledBitmap.getHeight();

			    // Apply the scaled bitmap
			    view.setImageDrawable(result);

			    // Now change ImageView's dimensions to match the scaled image
			    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
			    params.width = width;
			    params.height = height;
			    view.setLayoutParams(params);
			}

			private int dpToPx(int dp)
			{
			    float density = context.getResources().getDisplayMetrics().density;
			    return Math.round((float)dp * density);
			}
		}

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View listItemView = inflater.inflate(R.layout.checkbox_listview, parent, false);

		profilePicView = (ImageView) listItemView.findViewById(R.id.imageProfileView);
		TextView nameView = (TextView) listItemView.findViewById(R.id.nameText);
		CheckBox checkBox = (CheckBox) listItemView.findViewById(R.id.checkBoxItem);

		JSONObject currentUser = currentInvitableFriends.get(position);

		JSONObject pictureJson = currentUser.optJSONObject("picture").optJSONObject("data");
		new ImageDownloader(profilePicView).execute(pictureJson.optString("url"));
		String name = currentUser.optString("first_name") + " " + currentUser.optString("last_name");
		nameView.setText(name);

		checkBox.setEnabled(false);
		if(FBPickFriendActivity.idsToInvite.contains(currentUser.optString("id")))
			checkBox.setChecked(true);
		else
			checkBox.setChecked(false);
		

		return listItemView;

	}
	
	@Override
    public int getCount() {
        return currentInvitableFriends.size();
    }
    @Override
    public JSONObject getItem(int position) {
        return currentInvitableFriends.get(position);
    }
    @Override
    public int getPosition(JSONObject item) {
        return currentInvitableFriends.indexOf(item);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    
  
    
    /**
     * Custom Filter implementation for the items adapter.
     *
     */
    private class ItemsFilter extends Filter {
        protected FilterResults performFiltering(CharSequence prefix) {
            // Initiate our results object
            FilterResults results = new FilterResults();
            // If the adapter array is empty, check the actual items array and use it
            if (currentInvitableFriends == null) {
                synchronized (mLock) { // Notice the declaration above
                		currentInvitableFriends = ClimbApplication.invitableFriends;
                }
            }
            // No prefix is sent to filter by so we're going to send back the original array
            if (prefix == null || prefix.length() == 0) {
                synchronized (mLock) {
                    results.values = ClimbApplication.invitableFriends;
                    results.count = ClimbApplication.invitableFriends.size();
                }
            } else {
                    // Compare lower case strings
                String prefixString = prefix.toString().toLowerCase();
                // Local to here so we're not changing actual array
                final ArrayList<JSONObject> items = (ArrayList<JSONObject>) currentInvitableFriends;
                final int count = items.size();
                final ArrayList<JSONObject> newItems = new ArrayList<JSONObject>(count);
                for (int i = 0; i < count; i++) {
                    final JSONObject item = items.get(i);
                    String name = item.optString("first_name")+ " " + item.optString("last_name");
                    final String itemName = name.toLowerCase();
                    // First match against the whole, non-splitted value
                    if (itemName.contains(prefixString)) {
                        newItems.add(item);
                    } else {} /* This is option and taken from the source of ArrayAdapter
                        final String[] words = itemName.split(" ");
                        final int wordCount = words.length;
                        for (int k = 0; k < wordCount; k++) {
                            if (words[k].startsWith(prefixString)) {
                                newItems.add(item);
                                break;
                            }
                        }
                    } */
                }
                // Set and return
                results.values = newItems;
                results.count = newItems.size();
            }
            return results;
        }
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence prefix, FilterResults results) {
            //noinspection unchecked
            currentInvitableFriends = (ArrayList<JSONObject>) results.values;
            // Let the adapter know about the updated list
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
}
    
    /**
     * Implementing the Filterable interface.
     */
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ItemsFilter();
        }
        return mFilter;
    }
    
    
}
