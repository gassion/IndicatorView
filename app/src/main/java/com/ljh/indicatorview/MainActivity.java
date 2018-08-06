package com.ljh.indicatorview;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.ljh.indicatorview.adapter.MyViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import cn.junhua.android.view.IndicatorView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ViewPager viewpager;
    private IndicatorView indicator_view, indicator_view1, indicator_view2, indicator_view3, indicator_view4, indicator_view5;
    private List<Integer> resList;
    private MyViewPagerAdapter myViewPagerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewpager = findViewById(R.id.viewpager);
        indicator_view = findViewById(R.id.indicator_view);
        indicator_view1 = findViewById(R.id.indicator_view1);
        indicator_view2 = findViewById(R.id.indicator_view2);
        indicator_view3 = findViewById(R.id.indicator_view3);
        indicator_view4 = findViewById(R.id.indicator_view4);
        indicator_view5 = findViewById(R.id.indicator_view5);

        resList = new ArrayList<>();
        resList.add(R.mipmap.images1);
        resList.add(R.mipmap.images2);
        resList.add(R.mipmap.images3);
        resList.add(R.mipmap.images4);
        resList.add(R.mipmap.images5);
        resList.add(R.mipmap.images6);

        List<ImageView> imageViewList = new ArrayList<>();
        ImageView imageView;
        for (int res : resList) {
            imageView = new ImageView(this);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageResource(res);
            imageViewList.add(imageView);
        }


        myViewPagerAdapter = new MyViewPagerAdapter(imageViewList);
        viewpager.setAdapter(myViewPagerAdapter);

        indicator_view.setCount(resList.size());
        indicator_view1.setCount(resList.size());
        indicator_view2.setCount(resList.size());
        indicator_view3.setCount(resList.size());
        indicator_view4.setCount(resList.size());
        indicator_view5.setCount(resList.size());

        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.d(TAG, "onPageScrolled() called with: position = [" + position + "], positionOffset = [" + positionOffset + "], positionOffsetPixels = [" + positionOffsetPixels + "]");
            }

            @Override
            public void onPageSelected(int position) {
                indicator_view.setSelect(position);
                indicator_view1.setSelect(position);
                indicator_view2.setSelect(position);
                indicator_view3.setSelect(position);
                indicator_view4.setSelect(position);
                indicator_view5.setSelect(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }
}
