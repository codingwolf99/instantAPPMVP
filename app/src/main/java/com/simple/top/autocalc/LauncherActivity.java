package com.simple.top.autocalc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

/*
 * author: huangkuncan
 * date: 2024/9/15
 * desc:
 */
public class LauncherActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        startActivity(new Intent(this,MainActivity.class));
    }
}
