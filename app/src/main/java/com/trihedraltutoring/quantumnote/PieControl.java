package com.trihedraltutoring.quantumnote;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.trihedraltutoring.quantumnote.view.PieItem;
import com.trihedraltutoring.quantumnote.view.PieMenu;
import com.trihedraltutoring.quantumnote.view.PieMenu.PieView.OnLayoutListener;
import com.trihedraltutoring.quantumnote.view.PieStackView;
import com.trihedraltutoring.quantumnote.view.PieStackView.OnCurrentListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for Quick Controls pie menu
 */
public class PieControl implements PieMenu.PieController, OnClickListener {

    protected Activity mActivity;
    protected PieMenu mPie;
    protected int mItemSize;
    protected TextView mTabsCount;
    private PieItem mBack;
    private PieItem mForward;
    private PieItem mRefresh;
    private PieItem mUrl;
    private PieItem mOptions;
    private PieItem mBookmarks;
    private PieItem mHistory;
    private PieItem mAddBookmark;
    private PieItem mNewTab;
    private PieItem mIncognito;
    private PieItem mClose;
    private PieItem mShowTabs;
    private PieItem mInfo;
    private PieItem mFind;
    private PieItem mShare;
    private PieItem mRDS;
    private TabAdapter mTabAdapter;

    public PieControl(Activity activity) {
        mActivity = activity;
        mItemSize = (int) activity.getResources().getDimension(R.dimen.qc_item_size);
    }


    protected void attachToContainer(FrameLayout container) {
        if (mPie == null) {
            mPie = new PieMenu(mActivity);
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
            mPie.setLayoutParams(lp);
            populateMenu();
            mPie.setController(this);
        }
        container.addView(mPie);
    }

    protected void removeFromContainer(FrameLayout container) {
        container.removeView(mPie);
    }

    protected void forceToTop(FrameLayout container) {
        if (mPie.getParent() != null) {
            container.removeView(mPie);
            container.addView(mPie);
        }
    }

    protected void setClickListener(OnClickListener listener, PieItem... items) {
        for (PieItem item : items) {
            item.getView().setOnClickListener(listener);
        }
    }

    protected void populateMenu() {

        mInfo = makeItem(android.R.drawable.ic_menu_info_details, 1);
        PieStackView stack = new PieStackView(mActivity);
        stack.setOnCurrentListener(mTabAdapter);
        stack.setAdapter(mTabAdapter);
        mShowTabs.setPieView(stack);
        setClickListener(this, mBack, mRefresh, mForward, mUrl, mFind, mInfo,
                mShare, mBookmarks, mNewTab, mIncognito, mClose, mHistory,
                mAddBookmark, mOptions, mRDS);
        // level 1
        mPie.addItem(mOptions);
        mOptions.addItem(mRDS);
        mOptions.addItem(makeFiller());
        mOptions.addItem(makeFiller());
        mOptions.addItem(makeFiller());
        mPie.addItem(mBack);
        mBack.addItem(mRefresh);
        mBack.addItem(mForward);
        mBack.addItem(makeFiller());
        mBack.addItem(makeFiller());
        mPie.addItem(mUrl);
        mUrl.addItem(mFind);
        mUrl.addItem(mShare);
        mUrl.addItem(makeFiller());
        mUrl.addItem(makeFiller());
        mPie.addItem(mShowTabs);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mShowTabs.addItem(makeFiller());
            mShowTabs.addItem(mClose);
        } else {
            mShowTabs.addItem(mClose);
            mShowTabs.addItem(mIncognito);
        }
        mShowTabs.addItem(mNewTab);
        mShowTabs.addItem(makeFiller());
        mPie.addItem(mBookmarks);
        mBookmarks.addItem(makeFiller());
        mBookmarks.addItem(makeFiller());
        mBookmarks.addItem(mAddBookmark);
        mBookmarks.addItem(mHistory);
    }

    protected PieItem makeItem(int image, int l) {
        ImageView view = new ImageView(mActivity);
        view.setImageResource(image);
        view.setMinimumWidth(mItemSize);
        view.setMinimumHeight(mItemSize);
        view.setScaleType(ScaleType.CENTER);
        LayoutParams lp = new LayoutParams(mItemSize, mItemSize);
        view.setLayoutParams(lp);
        return new PieItem(view, l);
    }

    protected PieItem makeFiller() {
        return new PieItem(null, 1);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onOpen() {
        return false;
    }

    @Override
    public void stopEditingUrl() {

    }

    static class TabAdapter extends BaseAdapter implements OnCurrentListener {

        LayoutInflater mInflater;
        private int mCurrent;

        @Override
        public void onSetCurrent(int index) {
            mCurrent = index;
        }

        @Override
        public int getCount() {
            return 0;
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
            return null;
        }
    }

}



//public class PieControl extends View{
//    public PieControl(Context context) {
//        super(context);
//    }
//    PieControl pieControl;
//
//    public void buttonOnLongPress(View view){
//        Button button = (Button) view;
//        ((Button) view).setText("Pie Control");
//    }
//}
