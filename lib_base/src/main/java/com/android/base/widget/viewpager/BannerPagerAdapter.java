package com.android.base.widget.viewpager;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.base.imageloader.ImageLoaderFactory;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.viewpager.widget.PagerAdapter;

public abstract class BannerPagerAdapter extends PagerAdapter {

    private String mTransitionName;
    private Context mContext;
    private List<String> mEntities;
    private OnPageClickListener mClickListener;
    private boolean mIsLooper;

    void setTransitionName(String transitionName) {
        mTransitionName = transitionName;
    }

    void setContext(Context context) {
        mContext = context;
    }

    void setEntities(List<String> entities) {
        mEntities = entities;
        mIsLooper = mEntities.size() > 1;
    }

    void setOnBannerClickListener(OnPageClickListener clickListener) {
        this.mClickListener = clickListener;
    }

    @Override
    public int getCount() {
        return mEntities.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        ImageView imageView = createBannerPagerView(container, mContext, position);
        setTransitionName(imageView);
        callImageClicked(position, imageView);
        String url = mEntities.get(position);
        ImageLoaderFactory.getImageLoader().display(imageView, url);
        container.addView(imageView, 0);
        return imageView;
    }

    protected final void callImageClicked(int position, ImageView imageView) {
        if (mClickListener != null) {
            mClickListener.onClick(imageView, mIsLooper ? position - 1 : position);
        }
    }

    protected abstract ImageView createBannerPagerView(@NonNull ViewGroup container, @NonNull Context context, int position);

    private void setTransitionName(ImageView bannerLayout) {
        if (!TextUtils.isEmpty(mTransitionName)) {
            ViewCompat.setTransitionName(bannerLayout, mTransitionName);
        }
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

}