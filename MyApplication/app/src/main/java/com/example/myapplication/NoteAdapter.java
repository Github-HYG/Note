package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/*
* 每个适配器Adapter都要继承BaseAdapter
*   Filterable方便排序
* */
public class NoteAdapter extends BaseAdapter implements Filterable {

    private Context mContext;
    private List<Note> backList;//用来备份原始数据,这个数据是会改变的，所以要有个变量来备份一下原始数据
    private List<Note> noteList;//核心 从数据库读入后传到此LIST
    private MyFilter mFilter;

    public NoteAdapter(Context mContext, List<Note> noteList) {
        this.mContext = mContext;
        this.noteList = noteList;
        backList = noteList;
    }


    @Override
    public int getCount() {
        return noteList.size();
    }

    @Override
    public Object getItem(int position) {
        return noteList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        //mContext.setTheme((sharedPreferences.getBoolean("nightMode", false) ? R.style.NightTheme : R.style.DayTheme));
        mContext.setTheme(R.style.DayTheme);
        //View v = View.inflate(mContext,, null);
        View v = LayoutInflater.from(mContext).inflate(R.layout.note_layout, null);
        //定义TextView与note_layout中List对应
        TextView tv_content = (TextView) v.findViewById(R.id.tv_content);
        TextView tv_time = (TextView) v.findViewById(R.id.tv_time);
        //List预览中的 第一行content 第二行Time
//
//        //Set text for TextView
        String allText = noteList.get(position).getContent();
        String time    = noteList.get(position).getTime();
//        if (sharedPreferences.getBoolean("noteTitle" ,true))
//            tv_content.setText(allText.split("\n")[0]);
//        else{
        tv_content.setText(allText.split("\n")[0]);//只显示第一行 标题
//              }
        tv_time.setText(time);
//
//        //Save note id to tag
        v.setTag(noteList.get(position).getId());

        return v;
    }


/////=============实现搜索界面===================
    class MyFilter extends Filter {
        //我们在performFiltering(CharSequence charSequence)这个方法中定义过滤规则
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults result = new FilterResults();
            List<Note> list;
            if (TextUtils.isEmpty(charSequence)) {//当过滤的关键字为空的时候，我们则显示所有的数据
                list = backList;
            } else {//否则把符合条件的数据对象添加到集合中
                list = new ArrayList<>();
                for (Note note : backList) {
                    if (note.getContent().contains(charSequence)|| note.getTime().contains(charSequence)) {  //内容是否匹配
                        list.add(note);
                    }
                }
            }
            result.values = list; //将得到的集合保存到FilterResults的value变量中
            result.count = list.size();//将集合的大小保存到FilterResults的count变量中

            return result;
        }

    //在publishResults方法中告诉适配器更新界面
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            noteList = (List<Note>)filterResults.values;
            if (filterResults.count>0){
                notifyDataSetChanged();//通知数据发生了改变
            }else {
                notifyDataSetInvalidated();//通知数据失效
            }
        }
    }

    @Override
    public Filter getFilter() {
        if (mFilter ==null){
            mFilter = new MyFilter();
        }
        return mFilter;
    }
}

