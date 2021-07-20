package com.example.hearingtest.base;

import android.app.Activity;
import android.os.Bundle;

import android.view.Window;
import android.widget.Toast;

import androidx.annotation.Nullable;

public abstract class BaseActivity extends Activity implements IBaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView();
        initView();
        initData();
    }

    protected void showLongToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    protected void showShortToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
