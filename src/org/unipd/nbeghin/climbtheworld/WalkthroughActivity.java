package org.unipd.nbeghin.climbtheworld;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CirclePageIndicator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WalkthroughActivity extends Activity {

    private static final int MAX_VIEWS = 4;

    ViewPager mViewPager;
    //TextView nav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walkthrough);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(new WalkthroughPagerAdapter());
        WalkthroughPageChangeListener listener = new WalkthroughPageChangeListener();
        mViewPager.setOnPageChangeListener(listener);
//        nav = (TextView) findViewById(R.id.screen_navigation_button);
//        nav.setText("Page 1 of " + MAX_VIEWS);
        mViewPager.setOnPageChangeListener(new WalkthroughPageChangeListener());
        
        CirclePageIndicator cri= (CirclePageIndicator)findViewById(R.id.indicator);
        cri.setViewPager(mViewPager);
        cri.setOnPageChangeListener(listener);
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
            Typeface tf = Typeface.createFromAsset(ClimbApplication.getContext().getAssets(),"fonts/cake.ttf"); //sketch-me 
            ImageView imageView1 = (ImageView) imageViewContainer.findViewById(R.id.imageView1);
            ImageView imageView2 = (ImageView) imageViewContainer.findViewById(R.id.imageView2);
            ImageView imageView3 = (ImageView) imageViewContainer.findViewById(R.id.imageView3);
            TextView title = (TextView) imageViewContainer.findViewById(R.id.textView1);
            TextView text1 = (TextView) imageViewContainer.findViewById(R.id.textView2);
            TextView text2 = (TextView) imageViewContainer.findViewById(R.id.textView3);
            TextView text3 = (TextView) imageViewContainer.findViewById(R.id.textView4);
            TextView intro = (TextView) imageViewContainer.findViewById(R.id.textIntro);
            title.setTypeface(tf);
            text1.setTypeface(tf);
            text2.setTypeface(tf);
            text3.setTypeface(tf);
            intro.setTypeface(tf);
            intro.setVisibility(View.INVISIBLE);
            
            switch(position) {
            case 0:
            	intro.setVisibility(View.INVISIBLE);
            	title.setText(getString(R.string.title_1));
                imageView1.setImageResource(R.drawable.cards);
                imageView1.setPadding(0, 0, 15, 0);
//                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//                lp.setMargins(0, 0, 2, 0);
//                imageView1.setLayoutParams(lp);
                text1.setText(getString(R.string.text_1_1));
                imageView2.setImageResource(R.drawable.stairs);
                
                text2.setText(getString(R.string.text_1_2));
                int i = (String.valueOf(text2.getText())).indexOf("p1");
        		SpannableString ss = new SpannableString(text2.getText()); 
                Drawable d = getResources().getDrawable(R.drawable.play); 
                d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight()); 
                ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE); 
                ss.setSpan(span, i, i+"p1".length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE); 
                text2.setText(ss); 

                imageView3.setImageResource(R.drawable.climb);
                text3.setText(getString(R.string.text_1_3));
                break;

            case 1:
            	intro.setVisibility(View.VISIBLE);
            		title.setText(getString(R.string.title_2));
            		intro.setText(getString(R.string.intro_2));
                imageView1.setImageResource(R.drawable.help_friend);
                imageView1.setPadding(30, 30, 30, 30);
                text1.setText(getString(R.string.text_2_1));
                imageView2.setImageResource(R.drawable.competition);
                imageView2.setPadding(0, 0, 60, 0);
                text2.setText(getString(R.string.text_2_2));
                imageView3.setImageResource(R.drawable.team);
                text3.setText(getString(R.string.text_2_3));

                break;

            case 2:
            	intro.setVisibility(View.INVISIBLE);
            		title.setText(getString(R.string.title_3));
            		imageView1.setImageResource(R.drawable.swipe0);
            		text1.setText(getString(R.string.text_3_1));
            		imageView2.setImageResource(R.drawable.swipe1);
            		text2.setText(getString(R.string.text_3_2));
            		imageView3.setImageResource(R.drawable.swipe2);
            		text3.setText(getString(R.string.text_3_3));

                break;

            case 3:
            	intro.setVisibility(View.INVISIBLE);
            	title.setText(getString(R.string.title_4));
        		imageView1.setImageResource(R.drawable.stats);
        		imageView1.setPadding(10, 10, 10, 10);
        		text1.setText(getString(R.string.text_4_1));
        		
                i = (String.valueOf(text1.getText())).indexOf("p2");
        		ss = new SpannableString(text1.getText()); 
                d = getResources().getDrawable(R.drawable.profile); 
                d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight()); 
                span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE); 
                ss.setSpan(span, i, i+"p2".length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE); 
                text1.setText(ss); 
        		
        		imageView2.setImageResource(R.drawable.heart); 
        		imageView2.setPadding(50, 50, 50, 50);
        		text2.setText(getString(R.string.text_4_2));
        		ss = new SpannableString(text2.getText()); 
        		i = (String.valueOf(text2.getText())).indexOf("p2");
        		 d = getResources().getDrawable(R.drawable.profile); 
        		 d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight()); 
                 span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE); 
                ss.setSpan(span, i, i+"p2".length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE); 
                text2.setText(ss);
        		
        		imageView3.setImageResource(R.drawable.settigs);
        		imageView3.setPadding(20, 20, 20, 20);
        		text3.setText(getString(R.string.text_4_3));
                break;
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
            
          // nav.setText("Page " + (position + 1) + " of " + MAX_VIEWS);


        }

    }
}
