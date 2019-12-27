package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class TagAdapter extends BaseAdapter {

    private Context context;
    private List<String> tagList;
    private List<Integer> numList; //number of each tag

    public TagAdapter(Context context, List<String> tagList, List<Integer> numList) {
        this.context = context;
        this.tagList = tagList;
        this.numList = numList;

    }

    @Override
    public int getCount() {
        return tagList.size();
    }

    @Override
    public Object getItem(int position) {
        return tagList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        for(int i = 0; i < numList.size(); i++) Log.d("tag", numList.get(i).toString());
        Log.d("tag", "getView: " + numList.size());
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        context.setTheme((sharedPreferences.getBoolean("nightMode", false)? R.style.NightTheme: R.style.DayTheme));
        View v = LayoutInflater.from(context).inflate(R.layout.tag_layout, null);
        TextView blank_tag = (TextView)v.findViewById(R.id.blank_tag);
        TextView text_tag = (TextView)v.findViewById(R.id.text_tag);
        ImageView delete_tag = v.findViewById(R.id.delete_tag);

        String tagnum = numList.get(position).toString();
        text_tag.setText(tagList.get(position));
        blank_tag.setText(tagnum);


        return v;
    }
}
