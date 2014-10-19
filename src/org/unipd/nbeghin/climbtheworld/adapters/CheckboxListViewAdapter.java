package org.unipd.nbeghin.climbtheworld.adapters;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.unipd.nbeghin.climbtheworld.ClimbApplication;
import org.unipd.nbeghin.climbtheworld.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class CheckboxListViewAdapter extends ArrayAdapter<JSONObject> {

	private List<JSONObject> invitableFriends;
	private Context context;
	private ImageView profilePicView;

	public CheckboxListViewAdapter(Context context, List<JSONObject> invitableFriends) {
		super(context, R.layout.checkbox_listview, invitableFriends);
		this.context = context;
		this.invitableFriends = invitableFriends;
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
			}
		}

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View listItemView = inflater.inflate(R.layout.checkbox_listview, parent, false);

		profilePicView = (ImageView) listItemView.findViewById(R.id.imageProfileView);
		TextView nameView = (TextView) listItemView.findViewById(R.id.nameText);
		CheckBox checkBox = (CheckBox) listItemView.findViewById(R.id.checkBoxItem);

		JSONObject currentUser = invitableFriends.get(position);

		JSONObject pictureJson = currentUser.optJSONObject("picture").optJSONObject("data");
		new ImageDownloader(profilePicView).execute(pictureJson.optString("url"));

		nameView.setText(currentUser.optString("first_name"));

		checkBox.setEnabled(false);

		

		return listItemView;

	}

}
