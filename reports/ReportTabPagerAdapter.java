package com.aconst.spinareg.profile;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.aconst.spinareg.calendar.SessionDayFragment;
import com.aconst.spinareg.calendar.SessionListFragment;
import com.aconst.spinareg.calendar.SessionMonthFragment;
import com.aconst.spinareg.calendar.SessionWeekFragment;

public class TabPagerAdapter extends FragmentPagerAdapter {
    private int tabCount;

    TabPagerAdapter(FragmentManager fm, int tabCount) {
        super(fm);
        this.tabCount = tabCount;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new Profile1Fragment();
            case 1:
                return new Profile2Fragment();
            case 2:
                return new Profile3Fragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }
}
