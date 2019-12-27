package com.example.myapplication;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.SearchView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class EditActivity extends AppCompatActivity {


    private NoteDatabase dbHelper;
    private Context context = this;

    public  Intent intent = new Intent();
    private int openMode=0;
    private EditText et;
    private Toolbar my_toolbar;
    private String time;
    private String content;

    private String old_content = "";
    private String old_time = "";
    private int old_tag = 1;
    private long id = 0;
    private int tag = 1;
    private boolean tagChange=false;
    private FloatingActionButton fab_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_layout);
        et = findViewById(R.id.et);//定位联系到edit.XML中id

        //----------------------Setting Toolbar-----------------------
        my_toolbar = findViewById(R.id.my_toolbar);//工具栏
        setSupportActionBar(my_toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//设置toolbar取代octtionbar
        //----------------------Setting Toolbar-----------------------

        //----------------------Setting TagList-----------------------
        Spinner mySpinner = (Spinner)findViewById(R.id.spinner);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        List<String> tagList = Arrays.asList(sharedPreferences.getString("tagListString", null).split("_")); //获取tags
        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, tagList);   //标签位置定位联系到tagList
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);                           //部署形式
        mySpinner.setAdapter(myAdapter);                                                                            //部署Adapter

        mySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tag = (int)id + 1;  //从下标1开始记起
                tagChange = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        initView();

        //----------------------Setting TagList-----------------------
        my_toolbar.setNavigationIcon(R.drawable.ic_keyboard_arrow_left_black_24dp);    //设置返回键样式
        my_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AutoSetMode();
                setResult(RESULT_OK, intent);
                finish();
            }
        });
//
//
//        //============Nothing OR Get Old_content=================
        Intent getintent = getIntent();
        openMode = getintent.getIntExtra("mode",0);
        if(openMode == 3){                                               //点击了已存在的笔记
            id = getintent.getLongExtra("id", 0);
            old_content = getintent.getStringExtra("content");
            old_time = getintent.getStringExtra("time");
            old_tag  = getintent.getIntExtra("tag", 1);
            et.setText(old_content);
            et.setSelection(old_content.length());
            mySpinner.setSelection(old_tag - 1);        //因为下标从1开始
        }


    }

    public void initView() {
        fab_image = findViewById(R.id.fab_image);

        fab_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent getImage = new Intent(Intent.ACTION_GET_CONTENT);// ACTION_GET_CONTENT 简单来说就是让用户选择一种特殊的数据并得到它。
                getImage.addCategory(Intent.CATEGORY_OPENABLE);         //        ACTION_GET_CONTENT可以让用户在运行的程序中取得数据
                // addCategory是要增加一个分类，增加一个什么分类呢？就是增加CATEGORY_OPENABLE，从字面意思值是增加一个可以打开的分类，也即是取得的uri要可以被ContentResolver解析，注意这里的分类即是执行的附加条件。
                getImage.setType("image/*");//        setType就是设置取得的数据类型为image，也即是取照片。
                startActivityForResult(getImage,1);

            }
        });


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        ContentResolver resolver = getContentResolver();
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1:
                    //获得图片的uri
                    Uri originalUri = intent.getData();
                    Bitmap bitmap = null;
                    try {
                        Bitmap originalBitmap = BitmapFactory.decodeStream(resolver.openInputStream(originalUri));
                        int mScreenWidth= getWindowManager().getDefaultDisplay().getWidth();

                        bitmap = resizeImage(originalBitmap, (int)(mScreenWidth*0.5), (int)(mScreenWidth*0.5));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (bitmap != null) {
                        //根据Bitmap对象创建ImageSpan对象
                        ImageSpan imageSpan = new ImageSpan(EditActivity.this, bitmap);
                        //创建一个SpannableString对象，以便插入用ImageSpan对象封装的图像
                        SpannableString spannableString = new SpannableString("[local]" + 1 + "[/local]");
                        //  用ImageSpan对象替换face
                        spannableString.setSpan(imageSpan, 0, "[local]1[local]".length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        //将选择的图片追加到EditText中光标所在位置
                        int index = et.getSelectionStart(); //获取光标所在位置
                        Editable edit_text = et.getEditableText();
                        if (index < 0 || index >= edit_text.length()) {
                            edit_text.append(spannableString);
                        } else {
                            edit_text.insert(index, spannableString);
                        }
                    } else {
                        Toast.makeText(EditActivity.this, "获取图片失败", Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;
            }
        }
    }
    /**
     * 图片缩放
     * @param originalBitmap 原始的Bitmap
     * @param newWidth 自定义宽度
     * @return 缩放后的Bitmap
     */
    private Bitmap resizeImage(Bitmap originalBitmap, int newWidth, int newHeight){
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();
        //定义欲转换成的宽、高
//            int newWidth = 200;
//            int newHeight = 200;
        //计算宽、高缩放率
        float scanleWidth = (float)newWidth/width;
        float scanleHeight = (float)newHeight/height;
        //创建操作图片用的matrix对象 Matrix
        Matrix matrix = new Matrix();
        // 缩放图片动作
        matrix.postScale(scanleWidth,scanleHeight);
        //旋转图片 动作
        //matrix.postRotate(45);
        // 创建新的图片Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(originalBitmap,0,0,width,height,matrix,true);
        return resizedBitmap;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if( keyCode== KeyEvent.KEYCODE_HOME){
            return true;
        }
        else if( keyCode== KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            AutoSetMode();
            setResult(RESULT_OK, intent);
            finish();
            return true;


            //setResult后，要调用finish()销毁当前的Activity，否则无法返回到原来的Activity，
            // 就无法执行原来Activity的onActivityResult函数，看到当前的Activity没反应。
    }
        return super.onKeyDown(keyCode, event);
}

    private void AutoSetMode() {
        Log.d("tag", "TAG123 is: "+ tag);
        if(openMode == 4){                                  //新建笔记
            if(et.getText().toString().length() == 0){      //   BUG:no editing
                intent.putExtra("mode", -1); //nothing new happens.
            }
            else{
                intent.putExtra("mode", 0); // new one note
                                    // MODE!4----->0
                intent.putExtra("content", et.getText().toString());
                intent.putExtra("time", dateToStr());
                intent.putExtra("tag", tag);
                Log.d("tag", "a new note！");
            }
        }
        else if(openMode == 3){
            if (et.getText().toString().equals(old_content) && !tagChange){ //判断是否有修改
                intent.putExtra("mode", -1); // edit nothing
                Log.d("tag", "edited the content！and TAG is: "+ tag);
            }
            else {
                intent.putExtra("mode", 1); //edited the content
                                    // MODE!3----->1
                intent.putExtra("content", et.getText().toString());
                intent.putExtra("time", dateToStr());
                intent.putExtra("id", id);                      //为什么传ID:要保持id前后一致；跟新笔记不同。
                intent.putExtra("tag", tag);
                Log.d("tag", "edited the content！and TAG is: "+ tag);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){                              //控件ID
            case R.id.delete:
                new AlertDialog.Builder(EditActivity.this)//
                .setMessage("确认删除")//确认框

                 //设置按钮为YES反应
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(openMode == 4){                                     //新添加笔记 do nothing
                                   intent.putExtra("mode", -1); // delete the note
                                   setResult(RESULT_OK, intent);
                        }
                        else {
                                    intent.putExtra("mode", 2); // delete the note
                                    intent.putExtra("id", id);
                                    setResult(RESULT_OK, intent);
                        }
                        finish();
                    }

                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();                                   //cancel
                    }
                }).create().show();
                //.create().show();  //.create().show(); //.create().show();  //.create().show();
            break;
        }

        //此数据传回MainActivity的OnAcitvityResult中
        return super.onOptionsItemSelected(item);
    }


    public String dateToStr(){
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(date);
    }
}

