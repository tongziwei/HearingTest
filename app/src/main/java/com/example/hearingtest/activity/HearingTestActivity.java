package com.example.hearingtest.activity;

import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.hearingtest.R;
import com.example.hearingtest.adapter.FrequencyBean;
import com.example.hearingtest.adapter.HearingFrquencyAdapter;
import com.example.hearingtest.base.BaseActivity;

import com.example.hearingtest.view.CustomDialog;
import com.example.hearingtestlibrary.AudioTrackManager;
import com.example.hearingtestlibrary.PlaySound;

import java.util.ArrayList;
import java.util.List;

public class HearingTestActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "HearingTestActivity";
    private static final int UPDATE_UI_EAR_CHANGE = 11;
    private static final int UPDATE_UI_HZ_CHANGE = 12;
    private static final int UPDATE_UI_TEST_COMPLETE = 13;

    private ImageView mIvEarphone;
    private Button mBtnBackFrontStep;
    private Button mBtnHearingYes;
    private Button mBtnStartTest;
    private RecyclerView mRvFrequency;

    private List<FrequencyBean> mFrequencyBeanList;
    private HearingFrquencyAdapter mHearingFrquencyAdapter;

    private float volume =0;
    private static final int defCurDB = 0;
    private static final int defCurHZ = 0;

    private int[] hzArr = new int[]{1000, 2000, 4000, 8000,500,250};
    private int[] dBArr = new int[]{-10, -5, 0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90};
    private int[] lDBMinVal = new int[6];
    private int[] rDBMinVal = new int[6];
    private int curDB = defCurDB;// 当前分贝索引
    private int curHZ = defCurHZ;// 当前频率索引
    private boolean isFirst = true;// 是否第一次听到
    private boolean isLeft = true;// 是否测试左耳
    private boolean leftCheckOver = false;// 左耳测试完成
    private boolean rightCheckOver = false;// 右耳测试完成
    private boolean isHeared = false;//是否听到
    private boolean isBackFrontStep = false;
    private boolean isFinished =false;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case UPDATE_UI_EAR_CHANGE:
                    if(isLeft){
                        mIvEarphone.setImageResource(R.mipmap.left_earphone);
                    }else{
                        mIvEarphone.setImageResource(R.mipmap.right_earphone);
                    }
                    break;
                case UPDATE_UI_HZ_CHANGE:
                    if(curHZ!=0){
                        mBtnBackFrontStep.setVisibility(View.VISIBLE);
                    }else{
                        mBtnBackFrontStep.setVisibility(View.INVISIBLE);
                    }
                    int index = curHZ +1;
                    for(FrequencyBean frequencyBean:mFrequencyBeanList){
                        if(frequencyBean.getLabel()==index){
                            frequencyBean.setEnable(true);
                        }else{
                            frequencyBean.setEnable(false);
                        }
                    }
                    mHearingFrquencyAdapter.notifyDataSetChanged();
                    break;
                case UPDATE_UI_TEST_COMPLETE:
                   // mBtnBackFrontStep.setVisibility(View.INVISIBLE);
                   // showCompletedDialog();
                    checkResult();
                    finish();
                    break;
            }
        }
    };

    private AudioTrackManager mAudioTrackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initView() {
        ((TextView) findViewById(R.id.tv_head_title)).setText("个人听力测试");
        mIvEarphone = (ImageView)findViewById(R.id.iv_earphone);
        mBtnBackFrontStep = (Button)findViewById(R.id.btn_back_front_step);
        mBtnHearingYes = (Button)findViewById(R.id.btn_hearing_yes);
        mBtnStartTest = (Button)findViewById(R.id.btn_start_test);
        mRvFrequency = (RecyclerView)findViewById(R.id.rv_frequency);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        mRvFrequency.setLayoutManager(linearLayoutManager);

        mBtnBackFrontStep.setOnClickListener(this);
        mBtnHearingYes.setOnClickListener(this);
        mBtnStartTest.setOnClickListener(this);
    }

    @Override
    public void initData() {
        mFrequencyBeanList = new ArrayList<>();
        for(int i=0;i<hzArr.length;i++){
            FrequencyBean frequencyBean = new FrequencyBean();
            if(i==0){
              frequencyBean.setEnable(true);
            }else{
                frequencyBean.setEnable(false);
            }
            frequencyBean.setFrequency(hzArr[i]);
            frequencyBean.setLabel(i+1);
            mFrequencyBeanList.add(frequencyBean);
        }
        mHearingFrquencyAdapter = new HearingFrquencyAdapter(mFrequencyBeanList);
        mRvFrequency.setAdapter(mHearingFrquencyAdapter);

        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,maxVolume,0);
    }

    @Override
    public void setContentView() {
        setContentView(R.layout.activity_hearing_test);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_start_test:
                //开始听力测试
                mBtnHearingYes.setVisibility(View.VISIBLE);
                mBtnStartTest.setVisibility(View.INVISIBLE);
                hearingTestThread.start();
                break;
            case R.id.btn_back_front_step:
                //返回上一步
                isBackFrontStep = true;
                break;
            case R.id.btn_hearing_yes:
                //听到声音
                isHeared = true;
                break;
            default:
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        isFinished = true;
        hearingTestThread.interrupt();//只是改变中断状态，不会中断一个正在运行的线程
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mAudioTrackManager!=null){
            mAudioTrackManager.stop();
        }
    }

/*    *//**
     * @param hz 频率
     * @param db 分贝
     * @param isLeft 是否是左耳
     *//*
    private void startPlay(int hz,int db,boolean isLeft){
        if(mAudioTrackManager!=null){
            mAudioTrackManager.stop();
        }
        mAudioTrackManager = new AudioTrackManager();
        mAudioTrackManager.setRate(hz, db);// 设置频率
        if (isLeft) {
            mAudioTrackManager.start(AudioTrackManager.LEFT);
        } else {
            mAudioTrackManager.start(AudioTrackManager.RIGHT);
        }
        mAudioTrackManager.play();
    }*/

    /**
     * @param hz 频率
     * @param db 分贝
     * @param isLeft 是否是左耳
     */
    private void startPlay(int hz,int db,boolean isLeft){
        PlaySound playSound = new PlaySound();
        playSound.setFrequency(hz);
        playSound.setDecibel(db);
        playSound.genTone();
        if(isLeft){
            playSound.playSound(PlaySound.LEFT);
        }else{
            playSound.playSound(PlaySound.RIGHT);
        }

    }


    private Thread hearingTestThread = new Thread(){
        @Override
        public void run() {
            super.run();
            while (!isFinished){
                //左耳
                if(!leftCheckOver){
                    for(int i=0;i<hzArr.length;i++){  //频率
                        if(isFinished){
                            break;
                        }
                        isHeared = false;
                        isBackFrontStep = false;
                        updateUIShow(UPDATE_UI_HZ_CHANGE);
                        curHZ = i;
                        for(int j=0;j<dBArr.length;j++){//分贝
                            if(isFinished){
                                break;
                            }
                            startPlay(hzArr[i],dBArr[j],isLeft);
                            Log.d(TAG, "left play,Hz:"+hzArr[i]+" db:"+dBArr[j]);
                            try {
                                sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if(isHeared){
                                curDB = j;
                                lDBMinVal[curHZ]=dBArr[curDB];
                                Log.d(TAG, "left: "+"curHz:"+hzArr[curHZ]+"curDB:"+dBArr[curDB]);
                                if(isBackFrontStep && i!=0){
                                    i=i-2;
                                }
                                break;
                            }
                            if(isBackFrontStep && i!=0){
                                i=i-2;
                                break;
                            }
                        }
                    }
                    leftCheckOver =true;
                    isLeft =false;
                    updateUIShow(UPDATE_UI_EAR_CHANGE);
                }
               if(!rightCheckOver){
                   //右耳
                   for(int i=0;i<hzArr.length;i++){  //频率
                       if(isFinished){
                           break;
                       }
                       isHeared = false;
                       isBackFrontStep = false;
                       curHZ = i;
                       updateUIShow(UPDATE_UI_HZ_CHANGE);
                       for(int j=0;j<dBArr.length;j++){//分贝
                           if(isFinished){
                               break;
                           }
                           startPlay(hzArr[i],dBArr[j],isLeft);
                           Log.d(TAG, "right play,Hz:"+hzArr[i]+" db:"+dBArr[j]);
                           try {
                               sleep(2000);
                           } catch (InterruptedException e) {
                               e.printStackTrace();
                           }
                           if(isHeared){
                               curDB = j;
                               rDBMinVal[curHZ]=dBArr[curDB];
                               Log.d(TAG, "right: "+"curHz:"+hzArr[curHZ]+"curDB:"+dBArr[curDB]);
                               if(isBackFrontStep && i!=0){
                                   i=i-2;
                               }
                               break;
                           }
                           if(isBackFrontStep && i!=0){
                               i=i-2;
                               break;
                           }
                       }

                   }
                   rightCheckOver =true;
               }

                String ldb ="";
                String rdb ="";
                for(int m =0;m<6;m++){
                    ldb=ldb+hzArr[m]+" "+ lDBMinVal[m]+", ";
                    rdb=rdb+hzArr[m]+" "+ rDBMinVal[m]+", ";
                }
                Log.d(TAG,ldb);
                Log.d(TAG, rdb);
                if(leftCheckOver && rightCheckOver){
                    isFinished = true;
                    updateUIShow(UPDATE_UI_TEST_COMPLETE);
                  /*  try {
                        sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/
                }
            }

        }
    };


    public void updateUIShow(int changeId){
        Message msg = new Message();
        msg.what = changeId;
        mHandler.sendMessage(msg);
    }

    private void checkResult(){
        Bundle bundle =new Bundle();
        bundle.putIntArray("LeftDB",lDBMinVal);
        bundle.putIntArray("RightDB",rDBMinVal);
        Intent intent = new Intent(HearingTestActivity.this,HearingResultActivity.class);
        intent.putExtra("HearingResult",bundle);
        startActivity(intent);
    }

    /**
     * 显示测试完成弹窗
     */
    private void showCompletedDialog(){
        CustomDialog.Builder builder = new CustomDialog.Builder(this);
        builder.setTitle("测试完成")
                .setMessage("听力测试已经完成，保存数据退出测试或丢弃数据重新测试")
                .setPositiveButton("返回重测", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Log.d(TAG, "onClick: 返回重测");
                        isLeft = true;
                        mIvEarphone.setImageResource(R.mipmap.left_earphone);
                        leftCheckOver = false;
                        rightCheckOver = false;
                    }
                })
                .setNegativeButton("保存退出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                         dialog.dismiss();

                    }
                });
        CustomDialog customDialog= builder.create();
        customDialog.setCancelable(false);
        customDialog.show();
    }

}