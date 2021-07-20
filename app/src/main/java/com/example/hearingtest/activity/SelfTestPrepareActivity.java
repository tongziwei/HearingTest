package com.example.hearingtest.activity;




import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.hearingtest.R;
import com.example.hearingtest.fragment.NoiseCheckFragment;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


public class SelfTestPrepareActivity extends FragmentActivity {
    private static final String TAG = "SelfTestPrepareActivity";
    private NoiseCheckFragment mNoiseCheckFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self_test_prepare);
        initView();
        mNoiseCheckFragment =new NoiseCheckFragment();
        addFragment(mNoiseCheckFragment);
    }


    private void initView() {
        ((TextView) findViewById(R.id.tv_head_title)).setText("个人听力测试准备");


    }



    public void addFragment(Fragment fragment){
        Log.d(TAG, "addFragment: ");
        FragmentTransaction mFragmentTransaction = getSupportFragmentManager().beginTransaction();
        mFragmentTransaction.add(R.id.fl_container,fragment);
        mFragmentTransaction.commit();
    }

    public void replaceFragment(Fragment fragment){
        FragmentTransaction mFragmentTransaction =  getSupportFragmentManager().beginTransaction();
        mFragmentTransaction.replace(R.id.fl_container,fragment);
        mFragmentTransaction.addToBackStack(null);
        mFragmentTransaction.commit();
    }

    public void backFragment(){
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        if (supportFragmentManager.getBackStackEntryCount() > 0) {
            supportFragmentManager.popBackStack();
        } else {
            finish();
        }
    }


}