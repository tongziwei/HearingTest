package com.example.hearingtest.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.hearingtest.R;
import com.example.hearingtest.activity.HearingExerciseActivity;
import com.example.hearingtest.activity.HearingTestActivity;
import com.example.hearingtestlibrary.AudioTrackManager;

import static java.lang.Thread.sleep;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DeviceTestFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DeviceTestFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Button mBtnEnterHearTest;
    private Button mBtnStartDevTest;
    //private Button mBtnStopDevTest;
    private Button mBtnEnterHearExercise;

    private AudioTrackManager mAudioTrackManager;
    private float volume =0;
    private boolean isFinished = false;
    private boolean isStartDevTest = false;
 //   private boolean isFirst = true;//首次开始设备测试

    public DeviceTestFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DeviceTestFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DeviceTestFragment newInstance(String param1, String param2) {
        DeviceTestFragment fragment = new DeviceTestFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        AudioManager mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        int maxVolume =  mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,10,0);  //标准分贝为音量10或11的时候？
       /* volume = (float) mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);// 获取系统音量
        AudioTrackManager.setVolume(volume);*/
        devTestThread.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_device_test, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBtnStartDevTest = (Button)view.findViewById(R.id.btn_dev_test);
     //   mBtnStopDevTest = (Button)view.findViewById(R.id.btn_stop_dev_test);

        mBtnStartDevTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int status = getheadsetStatsu();
                if(status ==1 || status==2 ){
                    //连接上了耳机，1为有线耳机，2为蓝牙耳机
               /*     if(isFirst){
                        devTestThread.start();
                        isStartDevTest = true;
                        isFirst = false;
                    }else{
                        if(!isStartDevTest){
                            isStartDevTest = true;
                        }
                    }*/
                    isStartDevTest = true;
                }else{
                    //请先连接耳机
                    Toast.makeText(getActivity(),"请先佩戴耳机",Toast.LENGTH_SHORT).show();
                }
               // isStartDevTest = true;
            }
        });

       /* mBtnStopDevTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStartDevTest= false;
            }
        });*/

        mBtnEnterHearTest = (Button)view.findViewById(R.id.btn_enter_hear_test);
        mBtnEnterHearTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), HearingTestActivity.class);
                startActivity(intent);
            }
        });

        mBtnEnterHearExercise = (Button)view.findViewById(R.id.btn_enter_hear_exercise);
        mBtnEnterHearExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), HearingExerciseActivity.class);
                startActivity(intent);
            }
        });
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mAudioTrackManager!=null){
            mAudioTrackManager.stop();
        }
        isFinished = true;
        devTestThread.interrupt();
    }

    /**
     * 获取耳机的连接状态
     * @return 根据返回的int值进行自己的逻辑操作
     */
    public int getheadsetStatsu(){
        AudioManager audoManager = (AudioManager)getActivity().getSystemService(Context.AUDIO_SERVICE); //获取声音管理器

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

    private Thread devTestThread = new Thread(){
        @Override
        public void run() {
            super.run();
            while (!isFinished){
                if(isStartDevTest){
                    try {
                        // 左耳
                        startPlay(1000, 40, true);
                        sleep(600);
                        startPlay(1000, 40, true);
                        sleep(800);
                        startPlay(1000, 40, true);
                        sleep(1000);
                        // 右耳
                        startPlay(1000, 40, false);
                        sleep(600);
                        startPlay(1000, 40, false);
                        sleep(800);
                        startPlay(1000, 40, false);
                        sleep(1000);
                        isStartDevTest= false;
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
}