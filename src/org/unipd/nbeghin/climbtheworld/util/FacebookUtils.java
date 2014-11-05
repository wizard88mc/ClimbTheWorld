package org.unipd.nbeghin.climbtheworld.util;

import java.util.Collection;

import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.exceptions.NoFBSession;
import org.unipd.nbeghin.climbtheworld.models.Climbing;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;

public class FacebookUtils {
	private Context	context;

	public FacebookUtils(Context context) {
		this.context = context;
	}

//	private void showPublishResult(GraphObject result, FacebookRequestError error) {
//		if (error == null) {
//			Log.i(MainActivity.AppName, "Posted on Facebook wall");
//			Toast.makeText(context, "Posted on your wall", Toast.LENGTH_SHORT).show();
//		} else {
//			Log.e(MainActivity.AppName, "Unable to post on Facebook wall: " + error.getErrorMessage());
//			Toast.makeText(context, "Unable to post on your wall: " + error.getErrorMessage(), Toast.LENGTH_LONG).show();
//		}
//	}

	public static boolean isOnline(Activity activity) {
	    ConnectivityManager cm =
	        (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
	
	public static boolean isOnline(Context ctx) {
	    ConnectivityManager cm =
	        (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
	
	public static boolean isLoggedIn() {
	    Session session = Session.getActiveSession();
	    return (session != null && session.isOpened());
	}
	
	protected boolean hasPublishPermission() {
		Session session = Session.getActiveSession();
		return session != null && session.getPermissions().contains("publish_actions");
	}

	private void checkFBSession() throws NoFBSession {
		Session session = Session.getActiveSession();
		if (session == null || !session.isOpened()) throw new NoFBSession();
	}

	public void postToWall(Climbing climbing) throws NoFBSession {
		Log.i(MainActivity.AppName, "Posting on FB wall");
		Bundle params = new Bundle();
		params.putString("name", "ClimbTheWorld");
		params.putString("caption", "Climb the world: a serious game to promote physical activity");
		params.putString("description", climbing.getFBStatusMessage());
		params.putString("link", "https://developers.facebook.com/android");
		params.putString("picture", /*climbing.getBuilding().getPhoto()*/ "http://2.bp.blogspot.com/-aO8ILLDFKv4/UQb08_I2JkI/AAAAAAAAPEU/RvEo5lNHDvs/s1600/Victory.jpg");
		publishFeedDialog(params);
	}
	
	public void postUpdateToWall(Climbing climbing, int newSteps) throws NoFBSession{
		Log.i(MainActivity.AppName, "Posting on FB wall - update");
		Bundle params = new Bundle();
		params.putString("name", "ClimbTheWorld");
		params.putString("caption", "Climb the world: a serious game to promote physical activity");
		params.putString("description", "I'm climbing " + climbing.getBuilding().getName() + " and I've made " + (newSteps) + " more steps!!!!");
		params.putString("link", "https://developers.facebook.com/android");
		params.putString("picture", /*climbing.getBuilding().getPhoto()*/ "http://images.nationalgeographic.com/wpf/media-live/photos/000/234/cache/gunks-new-york-climb_23497_600x450.jpg");
		//params.putString("picture", "https://raw.github.com/fbsamples/ios-3.x-howtos/master/Images/iossdk_logo.png");
		publishFeedDialog(params);
	}
	
	public void inviteFriends(Collection<GraphUser> friends) throws NoFBSession {
		for(GraphUser friend: friends) {
			Bundle params = new Bundle();
			params.putString("name", "ClimbTheWorld");
			params.putString("caption", "Climb the world: a serious game to promote physical activity");
			params.putString("description", "Download ClimbTheWorld from Google Play (FREE)");
			params.putString("link", "https://developers.facebook.com/android");
			params.putString("to", friend.getId());
			params.putString("picture", "https://raw.github.com/fbsamples/ios-3.x-howtos/master/Images/iossdk_logo.png");
			publishFeedDialog(params);
		}
	}

	public void publishFeedDialog() throws NoFBSession {
		Bundle params = new Bundle();
		params.putString("name", "ClimbTheWorld");
		params.putString("caption", "Climb the world: a serious game to promote physical activity");
		params.putString("description", "Download ClimbTheWorld from Google Play (FREE)");
		params.putString("link", "https://developers.facebook.com/android");
		params.putString("picture", "https://raw.github.com/fbsamples/ios-3.x-howtos/master/Images/iossdk_logo.png");
		publishFeedDialog(params);
	}

	public void publishFeedDialog(Bundle params) throws NoFBSession {
		this.checkFBSession();
		WebDialog feedDialog = (new WebDialog.FeedDialogBuilder(context, Session.getActiveSession(), params)).setOnCompleteListener(new OnCompleteListener() {
			@Override
			public void onComplete(Bundle values, FacebookException error) {
				if (error == null) {
					final String postId = values.getString("post_id");
					if (postId != null) {
						Toast.makeText(context, "Posted successfully", Toast.LENGTH_SHORT).show();
					} else {
						// User clicked the Cancel button
						Toast.makeText(context.getApplicationContext(), "Publish cancelled", Toast.LENGTH_SHORT).show();
					}
				} else if (error instanceof FacebookOperationCanceledException) {
					// User clicked the "x" button
					Toast.makeText(context.getApplicationContext(), "Publish cancelled", Toast.LENGTH_SHORT).show();
				} else {
					// Generic, ex: network error
					Toast.makeText(context.getApplicationContext(), "Error posting story", Toast.LENGTH_SHORT).show();
				}
			}
		}).build();
		feedDialog.show();
	}
	
	 public static Bitmap getRoundedBitmap(Bitmap bitmap) {
	        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
	                .getHeight(), Bitmap.Config.ARGB_8888);
	        Canvas canvas = new Canvas(output);

	        final int color = 0xff424242;
	        final Paint paint = new Paint();
	        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
	        final RectF rectF = new RectF(rect);

	        paint.setAntiAlias(true);
	        canvas.drawARGB(0, 0, 0, 0);
	        paint.setColor(color);
	        canvas.drawOval(rectF, paint);

	        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
	        canvas.drawBitmap(bitmap, rect, rect, paint);

	        return output;
	    }
}
