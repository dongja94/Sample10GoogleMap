package com.example.dongja94.samplegooglemap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.Map;

/**
 * Created by dongja94 on 2016-03-04.
 */
public class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    View infoView;
    TextView titleView, subtitleView, descriptionView;
    Map<Marker, POIItem> poiResolver;
    public MyInfoWindowAdapter(Context context, Map<Marker, POIItem> poiResolver) {
        infoView = LayoutInflater.from(context).inflate(R.layout.view_info_window, null);
        titleView = (TextView)infoView.findViewById(R.id.text_title);
        subtitleView = (TextView)infoView.findViewById(R.id.text_subtitle);
        descriptionView = (TextView)infoView.findViewById(R.id.text_description);
        this.poiResolver = poiResolver;
    }

    @Override
    public View getInfoWindow(Marker marker) {
//        POIItem item = poiResolver.get(marker);
//        titleView.setText(item.title);
//        subtitleView.setText(item.subtitle);
//        descriptionView.setText(item.description);
//        return infoView;
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        POIItem item = poiResolver.get(marker);
        titleView.setText(item.title);
        subtitleView.setText(item.subtitle);
        descriptionView.setText(item.description);
        return infoView;
//        return null;
    }
}
