package org.unipd.nbeghin.climbtheworld;

import org.unipd.nbeghin.climbtheworld.models.GameModeType;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CirclePageIndicator;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

public class WalkthroughActivity extends Activity {

    private static final int MAX_VIEWS = 4;

    ViewPager mViewPager;
    //TextView nav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walkthrough_ex);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(new WalkthroughPagerAdapter());
        WalkthroughPageChangeListener listener = new WalkthroughPageChangeListener();
        mViewPager.setOnPageChangeListener(listener);
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
        	LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        	View imageViewContainer = inflater.inflate(R.layout.walkthrough_page_ex, null);
        	Typeface tf = Typeface.createFromAsset(ClimbApplication.getContext().getAssets(),"fonts/cake.ttf");
        	
            ImageView image = (ImageView) imageViewContainer.findViewById(R.id.imageDemo);
           // TextView textUp = (TextView) imageViewContainer.findViewById(R.id.textUp);
            TextView textDown = (TextView) imageViewContainer.findViewById(R.id.textDown);
            TextView title = (TextView) imageViewContainer.findViewById(R.id.textTitle);
            textDown.setTypeface(tf);
           // textUp.setTypeface(tf);
            title.setTypeface(tf);
            
            switch (position) {
			case 0:
				title.setText(getString(R.string.demo1_welcome));
			//	textUp.setText("");
				image.setImageResource(R.drawable.logo);
				textDown.setText(getString(R.string.demo1_slide0));
				break;
			case 1:
				title.setText(getString(R.string.demo1_title));
			//	textUp.setText("Pick a building from 'Building' or 'Tour'");
				image.setImageResource(R.drawable.normal_building);
				textDown.setText(getString(R.string.demo1_slide1));
				TableRow tr = null;
				
				for(int i = 0; i < 4; i++){
					Button myButton = new Button(getApplicationContext());
					myButton.setTypeface(tf);
					switch (i) {
					case 0:
						myButton.setText("Solo Climb");
						myButton.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								Intent intent = new Intent(WalkthroughActivity.this, InnerWalkthroughActivity.class);
								intent.putExtra("game_mode", GameModeType.SOLO_CLIMB);
								startActivity(intent);
															
							}
						});
						break;	
					case 1:
						myButton.setText("Social Climb");

						myButton.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								Intent intent = new Intent(WalkthroughActivity.this, InnerWalkthroughActivity.class);
								intent.putExtra("game_mode", GameModeType.SOCIAL_CLIMB);
								startActivity(intent);
															
							}
						});
						break;
					case 2:
						myButton.setText("Social Challenge");
						myButton.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								Intent intent = new Intent(WalkthroughActivity.this, InnerWalkthroughActivity.class);
								intent.putExtra("game_mode", GameModeType.SOCIAL_CHALLENGE);
								startActivity(intent);
															
							}
						});
	
						break;
					case 3:
						myButton.setText("Team vs Team");
						myButton.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								Intent intent = new Intent(WalkthroughActivity.this, InnerWalkthroughActivity.class);
								intent.putExtra("game_mode", GameModeType.TEAM_VS_TEAM);
								startActivity(intent);
															
							}
						});
						break;

					}
					if(i < 2){
						tr = (TableRow) imageViewContainer.findViewById(R.id.tableRowBtn1);
					}else{
						tr = (TableRow) imageViewContainer.findViewById(R.id.tableRowBtn2);
					}
					tr.addView(myButton);
				}

				
				break;
			case 2:
				title.setText(getString(R.string.demo1_title));
			//	textUp.setText("Change your mind? Don\'t worry");
				image.setImageResource(R.drawable.back_building);
				textDown.setText(getString(R.string.demo1_slide2));
				break;
			case 3:
				title.setText(getString(R.string.demo1_title));
		//		textUp.setText("Check your profile and statistics");
				image.setImageResource(R.drawable.profilo);
				textDown.setText(getString(R.string.demo1_slide3));
				break;
			
			}


//            ImageView imageView1 = (ImageView) imageViewContainer.findViewById(R.id.imageView1);
//            ImageView imageView2 = (ImageView) imageViewContainer.findViewById(R.id.imageView2);
//            ImageView imageView3 = (ImageView) imageViewContainer.findViewById(R.id.imageView3);
//            TextView title = (TextView) imageViewContainer.findViewById(R.id.textView1);
//            TextView text1 = (TextView) imageViewContainer.findViewById(R.id.textView2);
//            TextView text2 = (TextView) imageViewContainer.findViewById(R.id.textView3);
//            TextView text3 = (TextView) imageViewContainer.findViewById(R.id.textView4);
//            TextView intro = (TextView) imageViewContainer.findViewById(R.id.textIntro);
//            title.setTypeface(tf);
//            text1.setTypeface(tf);
//            text2.setTypeface(tf);
//            text3.setTypeface(tf);
//            intro.setTypeface(tf);
//            intro.setVisibility(View.INVISIBLE);
//            
//            switch(position) {
//            case 0:
//            	intro.setVisibility(View.INVISIBLE);
//            	title.setText(getString(R.string.title_1));
//                imageView1.setImageResource(R.drawable.cards);
//                imageView1.setPadding(0, 0, 15, 0);
////                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
////                lp.setMargins(0, 0, 2, 0);
////                imageView1.setLayoutParams(lp);
//                text1.setText(getString(R.string.text_1_1));
//                imageView2.setImageResource(R.drawable.stairs);
//                
//                text2.setText(getString(R.string.text_1_2));
//                int i = (String.valueOf(text2.getText())).indexOf("p1");
//        		SpannableString ss = new SpannableString(text2.getText()); 
//                Drawable d = getResources().getDrawable(R.drawable.play); 
//                d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight()); 
//                ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE); 
//                ss.setSpan(span, i, i+"p1".length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE); 
//                text2.setText(ss); 
//
//                imageView3.setImageResource(R.drawable.climb);
//                text3.setText(getString(R.string.text_1_3));
//                break;
//
//            case 1:
//            	intro.setVisibility(View.VISIBLE);
//            		title.setText(getString(R.string.title_2));
//            		intro.setText(getString(R.string.intro_2));
//                imageView1.setImageResource(R.drawable.help_friend);
//                imageView1.setPadding(30, 30, 30, 30);
//                text1.setText(getString(R.string.text_2_1));
//                imageView2.setImageResource(R.drawable.competition);
//                imageView2.setPadding(0, 0, 60, 0);
//                text2.setText(getString(R.string.text_2_2));
//                imageView3.setImageResource(R.drawable.team);
//                text3.setText(getString(R.string.text_2_3));
//
//                break;
//
//            case 2:
//            	intro.setVisibility(View.INVISIBLE);
//            		title.setText(getString(R.string.title_3));
//            		imageView1.setImageResource(R.drawable.swipe0);
//            		text1.setText(getString(R.string.text_3_1));
//            		imageView2.setImageResource(R.drawable.swipe1);
//            		text2.setText(getString(R.string.text_3_2));
//            		imageView3.setImageResource(R.drawable.swipe2);
//            		text3.setText(getString(R.string.text_3_3));
//
//                break;
//
//            case 3:
//            	intro.setVisibility(View.INVISIBLE);
//            	title.setText(getString(R.string.title_4));
//        		imageView1.setImageResource(R.drawable.stats);
//        		imageView1.setPadding(10, 10, 10, 10);
//        		text1.setText(getString(R.string.text_4_1));
//        		
//                i = (String.valueOf(text1.getText())).indexOf("p2");
//        		ss = new SpannableString(text1.getText()); 
//                d = getResources().getDrawable(R.drawable.profile); 
//                d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight()); 
//                span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE); 
//                ss.setSpan(span, i, i+"p2".length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE); 
//                text1.setText(ss); 
//        		
//        		imageView2.setImageResource(R.drawable.heart); 
//        		imageView2.setPadding(50, 50, 50, 50);
//        		text2.setText(getString(R.string.text_4_2));
//        		ss = new SpannableString(text2.getText()); 
//        		i = (String.valueOf(text2.getText())).indexOf("p2");
//        		 d = getResources().getDrawable(R.drawable.profile); 
//        		 d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight()); 
//                 span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE); 
//                ss.setSpan(span, i, i+"p2".length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE); 
//                text2.setText(ss);
//        		
//        		imageView3.setImageResource(R.drawable.settigs);
//        		imageView3.setPadding(20, 20, 20, 20);
//        		text3.setText(getString(R.string.text_4_3));
//                break;
////
////            case 4:
////             //   imageView.setImageResource(R.drawable.help_friend);
////                break;
//            }

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
