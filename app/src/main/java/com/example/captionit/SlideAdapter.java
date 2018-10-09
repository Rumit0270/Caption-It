package com.example.captionit;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Arsenal_PC on 7/22/2018.
 */

public class SlideAdapter extends PagerAdapter {

    private Context context;
    private LayoutInflater inflater;

    //list of images
    private int[] imagesList = {
            R.drawable.image_1,
            R.drawable.image_2,
            R.drawable.image_3,
            R.drawable.image_4
    };

    // list of titles
    private String[] titleList = {
            "ClickPhoto / UploadImage",
            "Snap / SelectPhoto",
            "Photo processing...",
            "Caption!"
    };


    private String[] descriptionList = {
            "Tap the option on how you would want to provide a picture",
            "Click a Picture from camera or select from gallery",
            "Image gets sent to server for processing",
            "Caption for the image is generated",
    };


    // list of background colors

    private int[] backgroundColorList = {
            Color.rgb(141,153,174),
            Color.rgb(228,179,99),
            Color.rgb(91,192,190),
            Color.rgb(224,223,213)
    };

    public SlideAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return titleList.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return (view == (LinearLayout) object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        //return super.instantiateItem(container, position);
        inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.slide,container,false);
        LinearLayout layoutslide = (LinearLayout) view.findViewById(R.id.slidelinearlayout);
        ImageView imgslide = (ImageView) view.findViewById(R.id.slideimg);
        TextView txttitle = (TextView) view.findViewById(R.id.txttitle);
        TextView description = (TextView) view.findViewById(R.id.txtdescription);

        layoutslide.setBackgroundColor(backgroundColorList[position]);
        imgslide.setImageResource(imagesList[position]);
        txttitle.setText(titleList[position]);
        description.setText(descriptionList[position]);
        container.addView(view);
        return view;
    }


    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        //super.destroyItem(container, position, object);

        container.removeView((LinearLayout) object);
    }
}
