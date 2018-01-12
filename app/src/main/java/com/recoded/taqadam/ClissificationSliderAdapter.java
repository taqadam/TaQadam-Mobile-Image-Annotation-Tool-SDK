package com.recoded.taqadam;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.recoded.taqadam.R;

/**
 * Created by Ahmad Siafaddin on 12/25/2017.
 */

public class ClissificationSliderAdapter extends PagerAdapter {
    public int [] imgs={R.drawable.human,R.drawable.tiger,R.drawable.car};

    static int po;
    private Context ctx;
    private LayoutInflater inflater;

    public ClissificationSliderAdapter(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public int getCount() {
        return imgs.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {

        return (view==(LinearLayout)object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        inflater=(LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View items=inflater.inflate(R.layout.classification_slider,container,false);
        ImageView myImages=(ImageView) items.findViewById(R.id.images);

        myImages.setImageResource(imgs[position]);
        container.addView(items);

        po=getItemPosition(position);
        Log.v("PO", String.valueOf(po));
        return items;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {


        container.removeView((LinearLayout)object);
    }
}