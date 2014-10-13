package org.unipd.nbeghin.climbtheworld.adapters;

import java.util.List;

import org.unipd.nbeghin.climbtheworld.ClimbApplication;
import org.unipd.nbeghin.climbtheworld.R;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class PagerAdapter extends FragmentPagerAdapter {
	private List<Fragment>	fragments;

	public PagerAdapter(FragmentManager manager, List<Fragment> fragments) {
		super(manager);
		this.fragments = fragments;
	}

	@Override
	public Fragment getItem(int position) {
		return this.fragments.get(position);
	}

	@Override
	public int getCount() {
		return this.fragments.size();
	}

	@Override
	public String getPageTitle(int position) {
		switch (position) {
			case 0:
				return ClimbApplication.getContext().getString(R.string.buildings);
			case 1:
				return ClimbApplication.getContext().getString(R.string.tours);
			case 2:
				return ClimbApplication.getContext().getString(R.string.notifications);
			case 3:
				return ClimbApplication.getContext().getString(R.string.trophies);
			default:
				return ClimbApplication.getContext().getString(R.string.undefined);
		}
	}
}
