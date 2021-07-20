package com.example.hearingtest.activity;

import android.annotation.SuppressLint;
import android.content.Context;
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
import android.widget.Toast;

import com.example.hearingtest.R;
import com.example.hearingtest.base.BaseActivity;
import com.example.hearingtestlibrary.AudioTrackManager;


public class HearingExerciseActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "HearingExerciseActivity";
    private static final int UPDATE_UI_EAR_CHANGE = 21;
    private static final int EXERCISE_FINISHED =22;
    private ImageView mIvEarphone;
    private Button mBtnHearingYes;
    private Button mBtnStartTest;
    private Button mBtnEnterHearingTest;

    private AudioTrackManager mAudioTrackManager;
    private float volume =0;
    private boolean isFinished = false;
    private boolean isHeared = false;//是否听到;
    private boolean isLeft = true; //是否是左耳;
    private int curDB = 40; //初始分贝为40dB;
    private int desStepDB = 20; //每次下降的DB步长
    private int incStepDB = 10;//每次增加的DB步长
    private boolean isIncreasing = false;
    private int leftMinDB;
    private int rightMinDB;
    private boolean isStartExercise = false;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case UPDATE_UI_EAR_CHANGE:
                    if(isLeft){
                        mIvEarphone.setImageResource(R.mipmap.left_earphone);
                    }else{
                        mIvEarphone.setImageResource(R.mipmap.right_earphone);
                    }
                    break;
                case EXERCISE_FINISHED:
                    mBtnHearingYes.setVisibility(View.INVISIBLE);
                    mBtnStartTest.setVisibility(View.VISIBLE);
                    mBtnStartTest.setText("重新开始练习");
                    Toast.makeText(HearingExerciseActivity.this,"左耳："+leftMinDB+"dB, 右耳："+rightMinDB+"dB",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        exerciseThread.start();
    }

    @Override
    public void initView() {
        ((TextView) findViewById(R.id.tv_head_title)).setText("个人听力测试练习");
        mIvEarphone = (ImageView)findViewById(R.id.iv_exercise_earphone);
        mBtnHearingYes = (Button) findViewById(R.id.btn_exercise_hearing_yes);
        mBtnStartTest = (Button)findViewById(R.id.btn_exercise_start_test);
        mBtnEnterHearingTest = (Button)findViewById(R.id.btn_exercise_enter_test);

        mBtnStartTest.setOnClickListener(this);
        mBtnHearingYes.setOnClickListener(this);
        mBtnEnterHearingTest.setOnClickListener(this);

    }

    @Override
    public void initData() {
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,10,0);
        volume = (float) mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);// 获取系统音量
        AudioTrackManager.setVolume(volume);
    }

    @Override
    public void setContentView() {
        setContentView(R.layout.activity_hearing_exercise);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_exercise_start_test:
                Log.d(TAG, "start onClick: ");
                mBtnHearingYes.setVisibility(View.VISIBLE);
                mBtnStartTest.setVisibility(View.INVISIBLE);
                if(isLeft){
                    mIvEarphone.setImageResource(R.mipmap.left_earphone);
                }else{
                    mIvEarphone.setImageResource(R.mipmap.right_earphone);
                }
                isStartExercise = true;
                break;
            case R.id.btn_exercise_hearing_yes:
                isHeared = true;
                break;
            case R.id.btn_exercise_enter_test:
                Intent intent = new Intent(HearingExerciseActivity.this,HearingTestActivity.class);
                startActivity(intent);
                break;
            default:
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mAudioTrackManager!=null){
            mAudioTrackManager.stop();
        }
        isFinished = true;
        exerciseThread.interrupt();

    }

    /**
     * @param hz 频率
     * @param db 分贝
     * @param isLeft 是否是左耳
     */
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
    }

    //
    private Thread exerciseThread = new Thread(){
        @Override
        public void run() {
            super.run();
            while (!isFinished){
                if(isStartExercise){
                    Log.d(TAG, "curDB"+curDB);
                    startPlay(1000,curDB,isLeft);
                    try {
                        sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "isHeard"+isHeared);
                    if(isHeared){
                        if(isIncreasing){
                            if(isLeft){         //左耳练习完成
                                leftMinDB = curDB; //记录左耳分贝值
                                isLeft = false;  //切换到右耳
                                updateUIShow(UPDATE_UI_EAR_CHANGE);
                            }else{              //右耳练习完成
                                rightMinDB = curDB; //记录右耳分贝值
                                isStartExercise = false; //结束练习
                                isLeft = true;
                                updateUIShow(EXERCISE_FINISHED);
                            }
                            curDB = 40;
                            isIncreasing=false;
                        }else{
                            curDB= curDB-desStepDB;//以20dB为一档降低纯音级，直至不再作出反应
                            isIncreasing = false;
                        }
                        isHeared = false;
                    }else{
                        curDB = curDB+incStepDB; //以10dB为一档增加纯音级，直至不再作出反应
                        isIncreasing = true;
                    }
                }

            }
        }
    };

    public void updateUIShow(int changeId){
        Message msg = new Message();
        msg.what = changeId;
        mHandler.sendMessage(msg);
    }

}