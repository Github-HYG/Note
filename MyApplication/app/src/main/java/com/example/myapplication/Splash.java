package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

public class Splash extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在setContentView(R.layout.activity_boot_animation);之前将状态栏和标题栏隐藏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.splash);

        //新建一个子线程
        Thread thread =new Thread(){
            @Override
            public void run() {
                try{
                    sleep(3000);//程序休眠三秒后启动MainActivity
                    Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(intent);
                    finish();//关闭当前活动，否则按返回键还能回到启动画面
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        thread.start();//启动线程
    }
}

