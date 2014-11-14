package org.unipd.nbeghin.climbtheworld;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class WalkthroughActivity extends Activity {

    private static final int MAX_VIEWS = 3;

    ViewPager mViewPager;
    TextView nav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walkthrough);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(new WalkthroughPagerAdapter());
        mViewPager.setOnPageChangeListener(new WalkthroughPageChangeListener());
        nav = (TextView) findViewById(R.id.screen_navigation_button);
        nav.setText("Page 1 of " + MAX_VIEWS);
        mViewPager.setOnPageChangeListener(new WalkthroughPageChangeListener());
    }
    
    @Override
    protected void onResume() {
      super.onResume();
      ClimbApplication.activityResumed();
    }

    @Override
    protected void onPause() {
      super.onPause();
      ClimbApplication.activityPaused();
    }


    class WalkthroughPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return MAX_VIEWS;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == (View) object;
        }

        @Override
        public Object instantiateItem(View container, int position) {
            Log.e("walkthrough", "instantiateItem(" + position + ");");
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View imageViewContainer = inflater.inflate(R.layout.walkthrough_page, null);
            ImageView imageView1 = (ImageView) imageViewContainer.findViewById(R.id.imageView1);
            ImageView imageView2 = (ImageView) imageViewContainer.findViewById(R.id.imageView2);
            ImageView imageView3 = (ImageView) imageViewContainer.findViewById(R.id.imageView3);
            TextView title = (TextView) imageViewContainer.findViewById(R.id.textView1);
            TextView text1 = (TextView) imageViewContainer.findViewById(R.id.textView2);
            TextView text2 = (TextView) imageViewContainer.findViewById(R.id.textView3);
            TextView text3 = (TextView) imageViewContainer.findViewById(R.id.textView4);
            
            switch(position) {
            case 0:
            		title.setText("How it works (also offline)");
                imageView1.setImageResource(R.drawable.phone);
                text1.setText("1. Choose a building to climb");
                imageView2.setImageResource(R.drawable.man_stairs);
                text2.setText("2. Pick yout phone with you and make stairs");
                imageView3.setImageResource(R.drawable.lock_win);
                text3.setText("3. Win points, badge....and glory!!!!");
                break;

            case 1:
            		title.setText("Connect to facebook");
                imageView1.setImageResource(R.drawable.help_friend);
                text1.setText("You can ask your friends to help you climbing the building faster");
                imageView2.setImageResource(R.drawable.competition);
                text2.setText("Challenge you friends: who will arrive first????");
                imageView3.setImageResource(R.drawable.team);
                text3.setText("Pick a challenger, create a team to reach the top first!!!!");

                break;

            case 2:
            		title.setText("Monitor your results");
            		imageView1.setImageResource(R.drawable.stats);
            		text1.setText("See your profile and progress in the game");
            		imageView2.setImageResource(R.drawable.heart);
            		text2.setText("Get fit with our stairs counter");
            		imageView3.setImageResource(R.drawable.cup);
            		text3.setText("Admire your throphies!!!!");

                break;

//            case 3:
//             //   imageView.setImageResource(R.drawable.competition);
//                break;
//
//            case 4:
//             //   imageView.setImageResource(R.drawable.help_friend);
//                break;
            }

            ((ViewPager) container).addView(imageViewContainer, 0);
            return imageViewContainer;
        }
        

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager)container).removeView((View)object);
        }
    }


    class WalkthroughPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageSelected(int position) {
            // Here is where you should show change the view of page indicator
            switch(position) {

            case MAX_VIEWS - 1:
                break;

            default:

            }
            
            nav.setText("Page " + (position + 1) + " of " + MAX_VIEWS);


        }

    }
}
