package com.example.administrator.essim.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.example.administrator.essim.R;
import com.example.administrator.essim.fragments.FragmentImageDetail;
import com.example.administrator.essim.response.IllustsBean;
import com.example.administrator.essim.download.DownloadTask;
import com.example.administrator.essim.download.SDDownloadTask;
import com.example.administrator.essim.utils.Common;
import com.sdsmdg.tastytoast.TastyToast;

import java.io.File;

public class ImageDetailActivity extends AppCompatActivity {

    public ViewPager mViewPager;
    public IllustsBean mIllustsBean;
    private Context mContext;
    private File parentFile, realFile;
    private DownloadTask mDownloadTask;
    private TextView mTextView, mTextView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        setContentView(R.layout.activity_image_detail);

        mContext = this;
        Intent intent = getIntent();
        mIllustsBean = (IllustsBean) intent.getSerializableExtra("illust");
        mTextView = findViewById(R.id.image_order);
        mTextView2 = findViewById(R.id.download_origin);
        mTextView2.setOnClickListener(view -> {
            parentFile = new File(Common.getLocalDataSet(mContext).getString("download_path", "/storage/emulated/0/PixivPictures"));
            if (!parentFile.exists()) {
                parentFile.mkdir();
                runOnUiThread(() -> TastyToast.makeText(mContext, "文件夹创建成功~",
                        TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show());
            }
            realFile = new File(parentFile.getPath(), mIllustsBean.getTitle() + "_" +
                    mIllustsBean.getId() + "_" + String.valueOf(mViewPager.getCurrentItem()) + ".jpeg");
            if (realFile.exists()) {
                runOnUiThread(() -> TastyToast.makeText(mContext, "该文件已存在~",
                        TastyToast.LENGTH_SHORT, TastyToast.CONFUSING).show());
            } else {
                mDownloadTask = new DownloadTask(realFile, mContext, mIllustsBean);
                if (mIllustsBean.getPage_count() == 1) {
                    if (Common.getLocalDataSet(mContext).getString("download_path", "/storage/emulated/0/PixivPictures").contains("emulated")) {
                        //下载至内置SD存储介质，使用传统文件模式;
                        mDownloadTask.execute(mIllustsBean.getMeta_single_page().getOriginal_image_url());
                    } else {//下载至可插拔SD存储介质，使用SAF 框架，DocumentFile文件模式;
                        new SDDownloadTask(realFile, mContext, mIllustsBean, Common.getLocalDataSet(mContext))
                                .execute(mIllustsBean.getMeta_single_page().getOriginal_image_url());
                    }
                } else {
                    if (Common.getLocalDataSet(mContext).getString("download_path", "/storage/emulated/0/PixivPictures").contains("emulated")) {
                        //下载至内置SD存储介质，使用传统文件模式;
                        mDownloadTask.execute(mIllustsBean.getMeta_pages().get(mViewPager.getCurrentItem()).getImage_urlsX().getOriginal());
                    } else {//下载至可插拔SD存储介质，使用SAF 框架，DocumentFile文件模式;
                        new SDDownloadTask(realFile, mContext, mIllustsBean, Common.getLocalDataSet(mContext))
                                .execute(mIllustsBean.getMeta_pages().get(mViewPager.getCurrentItem()).getImage_urlsX().getOriginal());
                    }
                }
                mDownloadTask = null;
            }
        });
        mViewPager = findViewById(R.id.view_pager);
        mViewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return FragmentImageDetail.newInstance(position);
            }

            @Override
            public int getCount() {
                return mIllustsBean.getPage_count();
            }
        });
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == 2) {
                    mTextView.setText(String.format("%s/%s", String.valueOf(mViewPager.getCurrentItem() + 1),
                            mIllustsBean.getPage_count()));
                }
            }
        });
        mTextView.setText(String.format("%s/%s", String.valueOf(mViewPager.getCurrentItem() + 1),
                mIllustsBean.getPage_count()));
    }
}