 package com.example.myapplication;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.SearchView;
import android.content.SharedPreferences;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.service.autofill.VisibilitySetterAction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.myapplication.Alarm.AlarmReceiver;
import com.example.myapplication.Alarm.EditAlarmActivity;
import com.example.myapplication.Alarm.Plan;
import com.example.myapplication.Alarm.PlanAdapter;
import com.example.myapplication.Alarm.PlanDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static android.view.View.GONE;
import android.util.Log;
import static android.content.ContentValues.TAG;
 public class MainActivity extends BaseActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

     private NoteDatabase dbHelper;
     private PlanDatabase planDbHelper;

     private DisplayMetrics metrics;
     private NoteAdapter adapter;
     private List<Note> noteList = new ArrayList<Note>();
     private List<Plan> planList = new ArrayList<Plan>();
     private Toolbar my_toolbar;

     private SharedPreferences sharedPreferences;
     private Switch content_switch;
     private FloatingActionButton fab;
     private FloatingActionButton fab_alarm;
     private ListView lv;

     private LinearLayout lv_layout;
     private Context context = this;

     private LinearLayout lv_plan_layout;
     private ListView lv_plan;
     private TextView mEmptyView;
     private AlarmManager alarmManager;
     private PlanAdapter planAdapter;

     private PopupWindow popupWindow; // 左侧弹出菜单
     private PopupWindow popupCover; // 菜单蒙版
     private LayoutInflater layoutInflater;
     private RelativeLayout main;
     private ViewGroup customView;
     private ViewGroup coverView;
     private WindowManager wm;

     private TagAdapter tagAdapter;

     private TextView setting_text;
     private ImageView setting_image;
     private ListView lv_tag;
     private TextView add_tag;
     private BroadcastReceiver myReceiver;
     private TextView mEmptyPlan;


     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
              //achievement = new Achievement(context);
         alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
         initView();


     }

     private void initView() {
         initPrefs();  //THEMEsetting
//======================Alram=======================
         lv_plan = findViewById(R.id.lv_plan);
         lv_layout = findViewById(R.id.lv_layout);
         lv_plan_layout = findViewById(R.id.lv_plan_layout);
         fab_alarm = findViewById(R.id.fab_alarm);

         planAdapter = new PlanAdapter(getApplicationContext(), planList);
         lv_plan.setAdapter(planAdapter);
         lv_plan.setOnItemClickListener(this);
         lv_plan.setOnItemLongClickListener(this);
         Log.d(TAG,"fab alarm");
         fab_alarm.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Log.d(TAG,"NEW ALARM");
                 Intent intent = new Intent(MainActivity.this, EditAlarmActivity.class);
                 intent.putExtra("mode", 2); // MODE of 'new plan'
                 startActivityForResult(intent, 1);
                 overridePendingTransition(R.anim.in_righttoleft, R.anim.no);
             }
         });

//======================Alram=======================

//----------------------Setting Toolbar-----------------------
         my_toolbar = findViewById(R.id.my_toolbar);//工具栏
         setSupportActionBar(my_toolbar);
         getSupportActionBar().setHomeButtonEnabled(true);
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);//设置toolbar取代octtionbar
         initPopupView();
//----------------------Setting Toolbar---------------------
         my_toolbar.setNavigationIcon(R.drawable.ic_menu_black_24dp);
         my_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 showPopUpWindow();
             }
         });

         fab = findViewById(R.id.fab);
         fab.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(MainActivity.this, EditActivity.class);//从packagecontext：第一个类this类跳转到第二个参数类.class
                 intent.putExtra("mode", 4);     // MODE of 'new note' putExtra往Intent里赋值
                 startActivityForResult(intent, 1);      //collect data from edit启动跳转并获取结果
                 overridePendingTransition(R.anim.in_righttoleft, R.anim.out_righttoleft);
             }

         });


         refreshLvVisibility();

         lv = findViewById(R.id.lv);
         lv_layout = findViewById(R.id.lv_layout);

         content_switch = findViewById(R.id.content_switch);// note or plan
         adapter = new NoteAdapter(getApplicationContext(), noteList);//初始化一个Adapter
         refreshListView();
         lv.setAdapter(adapter);


         mEmptyView = findViewById(R.id.emptyView);

         lv.setEmptyView(mEmptyView);
                                        // lv == NULL :  show  mEmptyView data
         lv.setOnItemClickListener(this);
         lv.setOnItemLongClickListener(this);
//================================================================================
         boolean temp = sharedPreferences.getBoolean("content_switch", false);
         content_switch.setChecked(temp);//判断是看note还是plan
         content_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
             @Override
             public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 SharedPreferences.Editor editor = sharedPreferences.edit();
                 editor.putBoolean("content_switch" ,isChecked);
                 editor.commit();
                 refreshLvVisibility();
             }
         });
//=-================================================================================

     }




     private void initPopupView() {
         //instantiate the popup.xml layout file
         layoutInflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         customView = (ViewGroup) layoutInflater.inflate(R.layout.setting_layout, null);
         coverView = (ViewGroup) layoutInflater.inflate(R.layout.setting_cover, null);

         main = findViewById(R.id.main_layout);
         //instantiate popup window
         wm = getWindowManager();
         DisplayMetrics metrics = new DisplayMetrics();     ////BUG_Maybe
         wm.getDefaultDisplay().getMetrics(metrics);
     }


     private void showPopUpWindow() {

         int mScreenWidth= getWindowManager().getDefaultDisplay().getWidth();
         int mScreenHeight = getWindowManager().getDefaultDisplay().getHeight();
         //PopupWindow左右弹出的效果
         popupCover = new PopupWindow(coverView, mScreenWidth, mScreenHeight, false);       //盖住背景 ，颜色半透明，变暗
         popupWindow = new PopupWindow(customView, (int) (mScreenWidth * 0.7), (mScreenHeight), true);
        // if (isNightMode()) popupWindow.setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));
         popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
         popupWindow.setAnimationStyle(R.style.AnimationFade);
         popupCover.setAnimationStyle(R.style.AnimationCover);
         findViewById(R.id.main_layout).post(new Runnable() {//等待main_layout加载完，再show popupwindow
                                                 @Override
                                                 public void run() {
                                                     popupCover.showAtLocation(main, Gravity.NO_GRAVITY, 0, 0);
                                                     popupWindow.showAtLocation(main, Gravity.NO_GRAVITY, 0, 0);

                                                     setting_text = customView.findViewById(R.id.setting_settings_text);
                                                     setting_image = customView.findViewById(R.id.setting_settings_image);

                                                     lv_tag = customView.findViewById(R.id.lv_tag);    //tagList
                                                     refreshTagList();
                                                     Log.d(TAG, "line 5 is OK@@@@@@");
                                                     add_tag = customView.findViewById(R.id.add_tag); //添加新Tag


                                                     add_tag.setOnClickListener(new View.OnClickListener() {  //按下taglistItem
                                                         @Override
                                                         public void onClick(View v) {
                                                             if(sharedPreferences.getString("tagListString","").split("_").length<8){
                                                                 final EditText tag_et = new EditText(context);
                                                                 new AlertDialog.Builder(MainActivity.this)
                                                                         .setMessage("输入新标签名字")
                                                                         .setView(tag_et)
                                                                         .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                                             @Override
                                                                             public void onClick(DialogInterface dialog, int which) {
                                                                                 List<String> tagList = Arrays.asList(sharedPreferences.getString("tagListString",null).split("_"));

                                                                                 String name = tag_et.getText().toString();
                                                                                 if(!tagList.contains(name)){
                                                                                     //========tagList中加入新标签=========
                                                                                     SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                                                                                     String oldTagListString = sharedPreferences.getString("tagListString", null);
                                                                                     String newTagListString = oldTagListString + "_" + name;

                     //但是不知道这样重新运行了了taglistString会不会更新   -------->>>>//：：UPDATE SharedPreferences是一个轻量级数存储文件，APP关闭后不会被清空
                                                                                     SharedPreferences.Editor editor = sharedPreferences.edit();
                                                                                     editor.putString("tagListString", newTagListString);
                                                                                     editor.commit();
                                                                                     refreshTagList();      //记得自己写一个function！！
                                                                                     //========tagList中加入新标签=========

                                                                                 }
                                                                                 else
                                                                                     Toast.makeText(context, "该标签已存在", Toast.LENGTH_SHORT).show();
                                                                             }
                                                                         });

                                                             }
                                                             else{
                                                                 Toast.makeText(context, "自定义标签太多了哦~", Toast.LENGTH_SHORT).show();
                                                             }

                                                         }
                                                     });
                                                     //final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                                                     List<String> tagList = Arrays.asList(sharedPreferences.getString("tagListString", null).split("_")); //获取tags
                                                     tagAdapter = new TagAdapter(context, tagList, numOfTagNotes(tagList));
                                                     lv_tag.setAdapter(tagAdapter);

                                                     Log.d(TAG, "line 245 is OK@@@@@@");

                                                     lv_tag.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                         @Override
                                                         public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                             List<String> tagList = Arrays.asList(sharedPreferences.getString("tagListString", null).split("_")); //获取tags
                                                             int tag = position + 1;
                                                             List<Note> temp = new ArrayList<>();
                                                             for (int i = 0; i < noteList.size(); i++) {
                                                                 if (noteList.get(i).getTag() == tag) {
                                                                     Note note = noteList.get(i);
                                                                     temp.add(note);
                                                                 }
                                                             }
                                                             NoteAdapter tempAdapter = new NoteAdapter(context, temp);
                                                             lv.setAdapter(tempAdapter);
                                                             my_toolbar.setTitle(tagList.get(position));
                                                             popupWindow.dismiss();
                                                             Log.d(TAG, position + "");
                                                         }
                                                     });
                                                     //点击自动关闭popupWindow     //点击自动关闭popupWindow
                                                     coverView.setOnTouchListener(new View.OnTouchListener() {
                                                         @Override
                                                         public boolean onTouch(View v, MotionEvent event) {
                                                             popupWindow.dismiss();
                                                             return true;
                                                         }
                                                     });
                                                     //撤掉popupCover
                                                     popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                                                         @Override
                                                         public void onDismiss() {
                                                             popupCover.dismiss();
                                                         }
                                                     });
                                                 }
                                             });
//         kan看一下empty怎么弄
//         UPDATA_______empty不用管了，就是正常用法，底层方法具体实现就行
//                 明天实现一下编辑界面的TAG选择，然后考虑主界面的tagList

     }


     //========================================================
     //==================OnActivityResult==========================
     //==================================================================

     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);

         int returnMode;
         long note_Id;          //从哪个ID的note返回

         returnMode = data.getIntExtra("mode", -1);

         note_Id = data.getLongExtra("id", 0);

         if (returnMode == 1) {                         //Update a note
             String content = data.getStringExtra("content");
             String time = data.getStringExtra("time");
             long id = data.getLongExtra("id", 0);
             int tag = data.getIntExtra("tag", 1);

             Note note = new Note(content, time, tag);
             note.setId(id);
             CRUD op = new CRUD(context);
             op.open();
             op.updateNote(note);
             op.close();
         } else if (returnMode == 2) {//delete current note
             //删除操作只需要一个ID值即可
             Note curNote = new Note();
             curNote.setId(note_Id);
             CRUD op = new CRUD(context);
             op.open();
             op.removeNote(curNote);
             op.close();
          //   achievement.deleteNote();
         } else if (returnMode == 0) {
             Log.d(TAG,"RETURN O");   // create new note
             String content = data.getStringExtra("content");
             String time = data.getStringExtra("time");
             int tag = data.getExtras().getInt("tag", 1);
             Note newNote = new Note(content, time, tag);
             CRUD op = new CRUD(context);
             op.open();
             op.addNote(newNote);
             op.close();
            // achievement.addNote(content);
         } else if (returnMode == 11){//edit plan
             String title = data.getStringExtra("title");
             String content = data.getStringExtra("content");
             String time = data.getStringExtra("time");
             Log.d(TAG, time);
             Plan plan = new Plan(title, content, time);
             plan.setId(note_Id);
             com.example.myapplication.Alarm.CRUD op = new com.example.myapplication.Alarm.CRUD(context);
             op.open();
             op.updatePlan(plan);
             op.close();
         }else if (returnMode == 12){//delete existing plan
             Plan plan = new Plan();
             plan.setId(note_Id);
             com.example.myapplication.Alarm.CRUD op = new com.example.myapplication.Alarm.CRUD(context);
             op.open();
             op.removePlan(plan);
             op.close();
         }else if (returnMode == 10){//create new plan
             String title = data.getStringExtra("title");
             String content = data.getStringExtra("content");
             String time = data.getStringExtra("time");
             Plan newPlan = new Plan(title, content, time);
             com.example.myapplication.Alarm.CRUD op = new com.example.myapplication.Alarm.CRUD(context);
             op.open();
             op.addPlan(newPlan);
             Log.d(TAG, "onActivityResult: "+ time);
             op.close();
         }else{}
         refreshListView();
         super.onActivityResult(requestCode, resultCode, data);
     }


     @Override
     public boolean onCreateOptionsMenu(Menu menu) {


         getMenuInflater().inflate(R.menu.main_menu, menu);
         //Setting SearchView
         //=========================搜索=============================
         MenuItem mSearch = menu.findItem(R.id.action_search);
         SearchView mSearchView = (SearchView) mSearch.getActionView();
         mSearchView.setIconifiedByDefault(true);
         mSearchView.setQueryHint("Search");
         mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
             @Override
             public boolean onQueryTextSubmit(String query) {
                 return false;
             }

             @Override
             //搜索结果动态显示
             public boolean onQueryTextChange(String query) {
                 if(content_switch.isChecked())
                   planAdapter.getFilter().filter(query);
                 else
                     adapter.getFilter().filter(query);
                 return false;
             }
         });
         //=========================搜索=============================
         final int mode = (content_switch.isChecked()? 2 : 1);
         final String itemName = (mode == 1 ? "notes" : "plans");


         final View view = findViewById(R.id.menu_clear);
         return super.onCreateOptionsMenu(menu);
     }

     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()){
             case R.id.menu_clear:
                 if(!content_switch.isChecked()) {
                     new AlertDialog.Builder(MainActivity.this)//
                             .setMessage("确认删除所有笔记")//确认框

                             //设置按钮为YES反应
                             .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                 @Override
                                 public void onClick(DialogInterface dialog, int which) {
                                     //CRUD加入了新函数removeAllNotes
                                     dbHelper = new NoteDatabase(context);
                                     SQLiteDatabase db = dbHelper.getWritableDatabase();
                                     CRUD op = new CRUD(context);
                                     db.delete("notes", null, null);//delete data in table NOTES
                                     db.execSQL("update sqlite_sequence set seq=0 where name='notes'"); //reset id to 1
                                     refreshListView();
                                 }

                             })
                             .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                 @Override
                                 public void onClick(DialogInterface dialog, int which) {
                                     dialog.dismiss();                                   //cancel
                                 }
                             }).create().show();
                     //.create().show();  //.create().show(); //
                 }
                 else{
                         new AlertDialog.Builder(MainActivity.this)
                                 .setMessage("Delete All Plans ?")
                                 .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                     @Override
                                     public void onClick(DialogInterface dialog, int which) {
                                         planDbHelper = new PlanDatabase(context);
                                         SQLiteDatabase db = planDbHelper.getWritableDatabase();
                                         db.delete("plans", null, null);//delete data in table NOTES
                                         db.execSQL("update sqlite_sequence set seq=0 where name='plans'"); //reset id to 1
                                         refreshListView();
                                     }
                                 }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                 dialog.dismiss();
                             }
                         }).create().show();
                     }
                     break;
             case R.id.refresh:
                 my_toolbar.setTitle("All Notes");
                 lv.setAdapter(adapter);
                 break;

         }

         return super.onOptionsItemSelected(item);
     }

     private void initPrefs() {
         sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
         SharedPreferences.Editor editor = sharedPreferences.edit();

         //tag  分类
         if (!sharedPreferences.contains("tagListString")) {
             String s = "no tag_life_study_work_play";
             editor.putString("tagListString", s);
             editor.commit();
         }

//         if(!sharedPreferences.contains("noteTitle")){          //不用了，反而麻烦
//             editor.putBoolean("noteTitle", true);
//             editor.commit();
//         }
         //默认 note default
         if(!sharedPreferences.contains("content_switch")) {
             editor.putBoolean("content_switch", false);
             editor.commit();
         }

         if (!sharedPreferences.contains("nightMode")) {
             editor.putBoolean("nightMode", false);
             editor.commit();
         }
         if (!sharedPreferences.contains("reverseSort")) {
             editor.putBoolean("reverseSort", false);
             editor.commit();
         }
     }


    // 修改笔记，点击已存在的Item
     @Override
     public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
         Log.d(TAG, "onItemClick233333");
         switch (parent.getId()) {
             case R.id.lv:
                 Log.d(TAG, "open listItem");
                 Note curNote = (Note) parent.getItemAtPosition(position);//固定写法 position通过点击确定
                 Intent intent = new Intent(MainActivity.this, EditActivity.class);
                 intent.putExtra("content", curNote.getContent());
                 intent.putExtra("time", curNote.getTime());
                 intent.putExtra("id", curNote.getId());
                 intent.putExtra("mode", 3);                //修改的mode为3，openMode用
                 intent.putExtra("tag", curNote.getTag());
                 startActivityForResult(intent, 1);
                 overridePendingTransition(R.anim.in_righttoleft, R.anim.out_righttoleft);
                 break;
             case R.id.lv_plan:
                 Plan curPlan = (Plan) parent.getItemAtPosition(position);
                 Intent intent1 = new Intent(MainActivity.this, EditAlarmActivity.class);
                 intent1.putExtra("title", curPlan.getTitle());
                 intent1.putExtra("content", curPlan.getContent());
                 intent1.putExtra("time", curPlan.getTime());
                 intent1.putExtra("mode", 1);
                 intent1.putExtra("id", curPlan.getId());
                 startActivityForResult(intent1, 1);
                 break;

         }
     }
     //longclick item in listView
     @Override
     public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id){
         switch (parent.getId()){
             case R.id.lv:
                 final Note note = noteList.get(position);
                 new AlertDialog.Builder(MainActivity.this)
                         .setMessage("Do you want to delete this note ?")
                         .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                 CRUD op = new CRUD(context);
                                 op.open();
                                 op.removeNote(note);
                                 op.close();
                                 refreshListView();
                             }
                         }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         dialog.dismiss();
                     }
                 }).create().show();
                 break;
             case R.id.lv_plan:
                 final Plan plan = planList.get(position);
                 new AlertDialog.Builder(MainActivity.this)
                         .setMessage("Do you want to delete this plan ?")
                         .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int which) {

                                 com.example.myapplication.Alarm.CRUD op = new com.example.myapplication.Alarm.CRUD(context);
                                 op.open();
                                 op.removePlan(plan);
                                 op.close();
                                 refreshListView();
                             }
                         }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         dialog.dismiss();
                     }
                 }).create().show();
                 break;
         }
         return true;
     }


     //刷新listview
     public void refreshListView() {

//         int fabColor = sharedPreferences.getInt("fabColor", -500041);
//         chooseFabColor(fabColor);
//         int fabPlanColor = sharedPreferences.getInt("fabPlanColor", -500041);
//         chooseFabPlanColor(fabPlanColor);
         //initialize CRUD
         SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
         CRUD op = new CRUD(context);
         op.open();
         // set adapter
         if (noteList.size() > 0) noteList.clear();
         noteList.addAll(op.getAllNotes());
         //     if (sharedPreferences.getBoolean("reverseSort", false)) sortNotes(noteList, 2);
         //     else sortNotes(noteList, 1);
         op.close();
         adapter.notifyDataSetChanged();

         com.example.myapplication.Alarm.CRUD op1 = new com.example.myapplication.Alarm.CRUD(context);
         op1.open();
         if(planList.size() > 0) {
             cancelAlarms(planList);//删除所有闹钟
             planList.clear();
         }
         planList.addAll(op1.getAllPlans());
         startAlarms(planList);//添加所有新闹钟
//         if (sharedPreferences.getBoolean("reverseSort", false)) sortPlans(planList, 2);
//         else sortPlans(planList, 1);
         op1.close();
         planAdapter.notifyDataSetChanged();

     }


     private void refreshLvVisibility() {
         //决定应该现实notes还是plans
         boolean temp = sharedPreferences.getBoolean("content_switch", false);
         if(temp){
             lv_layout.setVisibility(GONE);
             lv_plan_layout.setVisibility(View.VISIBLE);
             fab_alarm.show();
             fab.hide();

         }
         else{
             lv_layout.setVisibility(View.VISIBLE);
             lv_plan_layout.setVisibility(GONE);
             fab_alarm.hide();
             fab.show();


         }
         if(temp) my_toolbar.setTitle("Plan");
         else my_toolbar.setTitle("Note");
     }

     //统计不同标签的笔记数
     //统计不同标签的笔记数
     //统计不同标签的笔记数
     //统计不同标签的笔记数     //统计不同标签的笔记数
     //统计不同标签的笔记数
     //统计不同标签的笔记数
     //统计不同标签的笔记数
     //统计不同标签的笔记数
     public List<Integer> numOfTagNotes(List<String> noteStringList){
         Integer[] numbers = new Integer[noteStringList.size()];
         for(int i = 0; i < numbers.length; i++) numbers[i] = 0;
         for(int i = 0; i < noteList.size(); i++){
             numbers[noteList.get(i).getTag() - 1] ++;
         }
         return Arrays.asList(numbers);
     }
     //统计不同标签的笔记数     //统计不同标签的笔记数     //统计不同标签的笔记数
     //统计不同标签的笔记数     //统计不同标签的笔记数     //统计不同标签的笔记数
     //统计不同标签的笔记数     //统计不同标签的笔记数
     //统计不同标签的笔记数
     //统计不同标签的笔记数
     //统计不同标签的笔记数     //统计不同标签的笔记数




     private void refreshTagList() {
         SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
         List<String> tagList = Arrays.asList(sharedPreferences.getString("tagListString", null).split("_")); //获取tags
         tagAdapter = new TagAdapter(context, tagList, numOfTagNotes(tagList));
         lv_tag.setAdapter(tagAdapter);
         tagAdapter.notifyDataSetChanged();
     }

//==========================================
     //==========================================
     //==========================================
     //==========================================
     //==========================================


     //设置提醒
     private void startAlarm(Plan p) {
         Calendar c = p.getPlanTime();
         if(!c.before(Calendar.getInstance())) {
             Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
             intent.putExtra("title", p.getTitle());
             intent.putExtra("content", p.getContent());
             intent.putExtra("id", (int)p.getId());
             PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) p.getId(), intent, 0);
             alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);//bug
             //alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
         }
     }

     //设置很多提醒
     private void startAlarms(List<Plan> plans){
         for(int i = 0; i < plans.size(); i++) startAlarm(plans.get(i));
     }

     //取消提醒
     private void cancelAlarm(Plan p) {
         Intent intent = new Intent(this, AlarmReceiver.class);
         PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int)p.getId(), intent, 0);
         alarmManager.cancel(pendingIntent);
     }

     //取消很多提醒
     private void cancelAlarms(List<Plan> plans){
         for(int i = 0; i < plans.size(); i++) cancelAlarm(plans.get(i));
     }


 }