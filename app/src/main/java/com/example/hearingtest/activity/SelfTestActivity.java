package com.example.hearingtest.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hearingtest.R;
import com.example.hearingtest.base.BaseActivity;
import com.example.hearingtest.utils.ArraysUtils;

import com.example.hearingtest.utils.MediaRecorderDemo;

import com.example.hearingtest.view.BrokenLineView;
import com.example.hearingtestlibrary.AudioTrackManager;
import com.example.hearingtestlibrary.PlaySound;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SelfTestActivity extends BaseActivity {
    private static final String TAG = "SelfTestActivity";
    private static final int MY_PERMISSIONS_REQUEST = 1000;
    private String[] permissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.BLUETOOTH
    };
    private boolean isPermissionGranted =false;
    // 声明一个集合，在后面的代码中用来存储用户拒绝授权的权
    private List<String> mDeniedPermissionList = new ArrayList<>();

    private Button mBtnNoiseTest;
    private TextView mTvMaxValue;
    private TextView mTvAvgValue;
    private TextView mTvMinValue;
    private LinearLayout mLlChartView;
    private TextView mTvExplain;
    private Button mBtnDevTest;
    private Button mBtnEnterHearingTest;
    private Button mBtnStopDevTest;
    private EditText mEtDB;
    private EditText mEtFz;

    private AudioTrackManager mAudioTrackManager;
    private float volume =0;

    /** 检测时间最大时间  */
    private long checkTime = 15 * 1000;
    /** 更新噪音标志  */
    private static final int UPDATE_NOISE_VALUE = 1;
    /** 检测噪音的开始时间  */
    private long startTime=0;
    /** 检测噪音工具类  */
    private MediaRecorderDemo mMediaRecorderDemo;
    private BrokenLineView mBrokenLine;

    private double maxVolume = 0.0;
    private double minVolume = 99990.0;
    /** 检测到的所有噪音分贝值  */
    private List<Double> allVolume = new ArrayList<Double>();
    /** 噪音分贝值 的说明文字  */
    private String[] dbExplain;
    private boolean isFinished = false;
    private boolean isStartDevTest = false;

    @SuppressLint("HandlerLeak")
    private Handler mHandler =new Handler(){
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case UPDATE_NOISE_VALUE:
                    double db =(Double) msg.obj;
                    long time = System.currentTimeMillis()-startTime;
                    if(time>checkTime){
                        //检测完成
                        mMediaRecorderDemo.stopRecord();
                        showDialog();
                    }
                    updateNoise(db);
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        requestPermissions();
        registerHeadsetPlugReceiver();
        //devTestThread.start();
    }


    @Override
    protected void showLongToast(String message) {
        super.showLongToast(message);
    }

    @Override
    public void initView() {
        ((TextView) findViewById(R.id.tv_head_title)).setText("个人听力测试");
        mBtnNoiseTest = (Button)findViewById(R.id.btn_noise_test);
        mTvMaxValue = (TextView)findViewById(R.id.tv_max_value);
        mTvAvgValue = (TextView)findViewById(R.id.tv_avg_value);
        mTvMinValue = (TextView)findViewById(R.id.tv_min_value);
        mLlChartView = (LinearLayout)findViewById(R.id.ll_chart_view);
        mTvExplain = (TextView)findViewById(R.id.tv_db_explain);
        mBrokenLine = new BrokenLineView(SelfTestActivity.this);
        mLlChartView.addView(mBrokenLine.execute(), new FrameLayout.LayoutParams
                (FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        mBtnDevTest = (Button)findViewById(R.id.btn_dev_test);
        mBtnEnterHearingTest = (Button)findViewById(R.id.btn_enter_test);
        mBtnStopDevTest = (Button)findViewById(R.id.btn_stop_dev_test);
        mEtDB = (EditText)findViewById(R.id.et_db);
        mEtFz = (EditText)findViewById(R.id.et_fz);
        mBtnDevTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int status = getheadsetStatsu();
                /*if(status ==1 || status==2 ){*/
                    //连接上了耳机，1为有线耳机，2为蓝牙耳机
              //  Log.d(TAG, "isFirst: "+isFirst);

                  isStartDevTest = true;

              /*  }else{
                    //请先连接耳机
                    Toast.makeText(SelfTestActivity.this,"请先佩戴耳机",Toast.LENGTH_SHORT).show();
                }*/
                if(!mEtDB.getText().toString().isEmpty() && !mEtFz.getText().toString().isEmpty() ){
                    int db =Integer.parseInt(mEtDB.getText().toString());
                    int f = Integer.parseInt(mEtFz.getText().toString());
                  //  startPlay(1000, db, true);
                    startPlay2(f,db);
                }else{
                    startPlay2(1000,90);
                }



            }
        });

        mBtnStopDevTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStartDevTest= false;
            }
        });

        mBtnNoiseTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 if(isPermissionGranted){
                     mHandler.post(checkNoise);
                 }
            }
        });

        mBtnEnterHearingTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterHearingTest();
            }
        });


    }

    @Override
    public void initData() {
        dbExplain = getResources().getStringArray(R.array.db_explain_arr);
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    /*    volume = (float) mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);// 获取系统音量
        Log.d(TAG, "system volume"+volume);*/
        float musicVolume = (float) mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        Log.d(TAG, "musicVolume "+musicVolume);
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,maxVolume,0);
        Log.d(TAG, "maxVolume "+maxVolume);//15
       /* mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,10,0);//标准分贝为音量10或11的时候？
        float music2Volume = (float) mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        Log.d(TAG, "music2Volume "+music2Volume);
        AudioTrackManager.setVolume(volume);*/
    }

    @Override
    public void setContentView() {
        setContentView(R.layout.activity_selftest);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        if(mMediaRecorderDemo!=null){
            mMediaRecorderDemo.stopRecord();
        }
        unregisterReceiver(headsetPlugReceiver);
        isFinished = true;
        devTestThread.interrupt();

    }

    private void requestPermissions(){
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.M){
            mDeniedPermissionList.clear();
            for (int i = 0; i < permissions.length; i++) {
                if (ContextCompat.checkSelfPermission(SelfTestActivity.this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    mDeniedPermissionList.add(permissions[i]);
                }
            }
            if (mDeniedPermissionList.isEmpty()) {//未授予的权限为空，表示都授予了
                isPermissionGranted = true;
               // Toast.makeText(SelfTestActivity.this,"已经授权",Toast.LENGTH_LONG).show();
            } else {//请求权限方法
                String[] permissions = mDeniedPermissionList.toArray(new String[mDeniedPermissionList.size()]);//将List转为数组
                ActivityCompat.requestPermissions(SelfTestActivity.this, permissions, MY_PERMISSIONS_REQUEST);
            }
        }else{
            isPermissionGranted =true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==MY_PERMISSIONS_REQUEST){
            isPermissionGranted = true;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    //判断是否勾选禁止后不再询问
                    boolean showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(SelfTestActivity.this, permissions[i]);
                   /* if (showRequestPermission) {
                        showToast("权限未申请");
                    }*/
                    isPermissionGranted = false;
                    break;
                }
            }
        }
    }

    private Thread devTestThread = new Thread(){
        @Override
        public void run() {
            super.run();
            Log.d(TAG, "isFinished "+isFinished);
            while (!isFinished){
                if(isStartDevTest){
                    try {
                        // 左耳
                        Log.d(TAG, "run: play");
                        startPlay(1000, 94, true);
         /*           // sleep(600);
                    startPlay(1000, 50, true);
                    //  sleep(800);
                    startPlay(1000, 50, true);
                    // sleep(1000);
                    // 右耳
                    startPlay(1000, 50, false);
                    // sleep(600);
                    startPlay(1000, 50, false);
                    //   sleep(800);
                    startPlay(1000, 50, false);*/
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }

        }
    };


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

    private void startPlay2(int hz,int db){
        PlaySound playSound = new PlaySound();
        playSound.setFrequency(hz);
        playSound.setDecibel(db,hz);
        playSound.genTone();
        playSound.playSound(PlaySound.LEFT);
    }




    /**
     * 噪声检测
     */
    private Runnable checkNoise = new Runnable() {
        @Override
        public void run() {
            mMediaRecorderDemo = new MediaRecorderDemo(new MediaRecorderDemo.NoiseValueUpdateCallback() {
                @Override
                public void onUpdateNoiseValue(double noiseValue) {
                    Message msg = new Message();
                    msg.what = UPDATE_NOISE_VALUE;
                    msg.obj = noiseValue;
                    mHandler.sendMessage(msg);
                }
            });
            mMediaRecorderDemo.startRecord();
            startTime = System.currentTimeMillis();
        }
    };

    /**
     * 更新噪声显示
     * @param db
     */
    private void updateNoise(double db){
        if (db > maxVolume) {
            maxVolume = db;
            String maxdb =  "最高分贝:\n " + (int)maxVolume+ " dB";
            mTvMaxValue.setText (maxdb);
        }
        // 更新最小值
        if (db < minVolume && db != 0.0) {
            minVolume = db;
            String mindb =  "最低分贝:\n " + (int)minVolume + " dB";
            mTvMinValue.setText(mindb);
        }
        // 更新平均值
        if (db != 0.0) {
            allVolume.add(db);
            double  avgVolume = ArraysUtils.avg(allVolume);
            String avgdb = "平均分贝:\n " + (int)avgVolume + " dB";
            mTvAvgValue.setText(avgdb);
            String explain = dbExplain[(int)(avgVolume / 10)]+"\n"+dbExplain[(int)(avgVolume / 10)+1];
            mTvExplain.setText(explain);
        }

          mBrokenLine.updateDate(ArraysUtils.sub(allVolume,mBrokenLine.maxCacheNum));
    }

    private void showDialog(){
        // 平均噪音分贝 > 40dB
        if (ArraysUtils.avg(allVolume) > 40) {
         /*   AlertDialog(mContext)
                    .setScaleWidth(0.7)
                    .setMessage("您的监测环境不适合后面的测试，请您到较安静的环境下测试。")
                    .setLeftButton("取消", null)
                    .setRightButton("重新检测") { mActivity.onBackPressed() }
                    .show()*/

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("您的监测环境不适合后面的测试，请您到较安静的环境下测试。")
                    .setNegativeButton("取消", null)
                    .setPositiveButton("重新检测", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mHandler.post(checkNoise);
                        }
                    })
                    .create()
                    .show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("您的测试环境良好，可以继续后面测试。")
                    .setNegativeButton("取消", null)
                    .setPositiveButton("进入测试", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                           enterHearingTest();
                        }
                    })
                    .create()
                    .show();
        }
    }

    /**
     * 进入听力测试
     */
    private void enterHearingTest(){
        Intent intent = new Intent(SelfTestActivity.this,HearingTestActivity.class);
        startActivity(intent);
    }

    /**
     * 获取耳机的连接状态
     * @return 根据返回的int值进行自己的逻辑操作
     */
    public int getheadsetStatsu(){
        AudioManager audoManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE); //获取声音管理器

        if(audoManager.isWiredHeadsetOn()){  //有限耳机是否连接
            return 1;
        }

        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();  //蓝牙耳机
        if (ba == null){ //若蓝牙耳机无连接
            return -1;
        } else if(ba.isEnabled()) {
            int a2dp = ba.getProfileConnectionState(BluetoothProfile.A2DP);              //可操控蓝牙设备，如带播放暂停功能的蓝牙耳机
            int headset = ba.getProfileConnectionState(BluetoothProfile.HEADSET);        //蓝牙头戴式耳机，支持语音输入输出
            int health = ba.getProfileConnectionState(BluetoothProfile.HEALTH);          //蓝牙穿戴式设备

            //查看是否蓝牙是否连接到三种设备的一种，以此来判断是否处于连接状态还是打开并没有连接的状态
            int flag = -1;
            if (a2dp == BluetoothProfile.STATE_CONNECTED) {
                flag = a2dp;
            } else if (headset == BluetoothProfile.STATE_CONNECTED) {
                flag = headset;
            } else if (health == BluetoothProfile.STATE_CONNECTED) {
                flag = health;
            }
            //说明连接上了三种设备的一种
            if (flag != -1) {
                return 2;
            }
        }
        return -2;
    }

    /**
     * 注册监听
     */
    private void registerHeadsetPlugReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(headsetPlugReceiver, intentFilter);
        IntentFilter bluetoothFilter = new IntentFilter(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        registerReceiver(headsetPlugReceiver, bluetoothFilter);
    }

    private BroadcastReceiver headsetPlugReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                if(BluetoothProfile.STATE_DISCONNECTED == adapter.getProfileConnectionState(BluetoothProfile.HEADSET)) {
                    Toast.makeText(context, "蓝牙耳机断开连接了", Toast.LENGTH_SHORT).show();
                }else if(BluetoothProfile.STATE_CONNECTED == adapter.getProfileConnectionState(BluetoothProfile.HEADSET)){
                    Toast.makeText(context, "蓝牙耳机连接上了", Toast.LENGTH_SHORT).show();
                }
            } else if ("android.intent.action.HEADSET_PLUG".equals(action)) {
                if (intent.hasExtra("state")) {
                    if (intent.getIntExtra("state", 0) == 0) {
                        Toast.makeText(context, "有线拔出", Toast.LENGTH_SHORT).show();
                    }else if(intent.getIntExtra("state", 0) == 1){
                        Toast.makeText(context, "有线插入", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

    };


}