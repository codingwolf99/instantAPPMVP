package com.simple.top.autocalc;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.simple.top.autocalc.dialog.CommonDialogs;
import com.simple.top.autocalc.utils.StatusBarUtils;
import com.simple.top.autocalc.widgets.MyTitleBar;

public class AboutActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_about);

    StatusBarUtils.setLightMode(this);

    MyTitleBar title_bar = findViewById(R.id.title_bar);
    title_bar.setTitle(getTitle());
    title_bar.setLeftIconOnClickListener((View v) -> finish());

    findViewById(R.id.btn_ok).setOnClickListener((v) -> {
      finish();
    });
    findViewById(R.id.btn_help).setOnClickListener((v) -> {
      CommonDialogs.showHelp(this);
    });
  }
}
