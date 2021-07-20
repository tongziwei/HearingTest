package com.example.hearingtest.fragment;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;


import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.hearingtest.R;
import com.example.hearingtest.activity.SelfTestActivity;
import com.example.hearingtest.activity.SelfTestPrepareActivity;
import com.example.hearingtest.utils.ArraysUtils;
import com.example.hearingtest.utils.MediaRecorderDemo;
import com.example.hearingtest.view.BrokenLineView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NoiseCheckFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NoiseCheckFragment extends Fragment {
    private static final String TAG = "NoiseCheckFragment";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private SelfTestPrepareActivity mSelfTestPrepareActivity;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Button mBtnNextStep;
    private Button mBtnNoiseTest;
    private LinearLayout mLlChartView;
    private TextView mTvMaxValue;
    private TextView mTvAvgValue;
    private TextView mTvMinValue;

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

    public NoiseCheckFragment() {
        // Required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NoiseCheckFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NoiseCheckFragment newInstance(String param1, String param2) {
        NoiseCheckFragment fragment = new NoiseCheckFragment();
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
        Log.d(TAG, "onCreate: ");
        requestPermissions();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_noise_check, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: ");
        mTvMaxValue = (TextView)view.findViewById(R.id.tv_max_value);
        mTvAvgValue = (TextView)view.findViewById(R.id.tv_avg_value);
        mTvMinValue = (TextView)view.findViewById(R.id.tv_min_value);
        mLlChartView = (LinearLayout)view.findViewById(R.id.ll_chart_view);
        mBrokenLine = new BrokenLineView(getActivity());
        mLlChartView.addView(mBrokenLine.execute(), new FrameLayout.LayoutParams
                (FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        mBtnNextStep = (Button)view.findViewById(R.id.btn_next_step);
        mBtnNextStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               enterDeviceTest();
            }
        });
        mBtnNoiseTest = (Button)view.findViewById(R.id.btn_noise_test);
        mBtnNoiseTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 if(isPermissionGranted){
                     mHandler.post(checkNoise);
                 }
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mMediaRecorderDemo!=null){
            mMediaRecorderDemo.stopRecord();
        }
    }

    private void enterDeviceTest(){
        mSelfTestPrepareActivity =(SelfTestPrepareActivity)getActivity();
        mSelfTestPrepareActivity.replaceFragment(new DeviceTestFragment());
    }

    private void requestPermissions(){
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.M){
            mDeniedPermissionList.clear();
            for (int i = 0; i < permissions.length; i++) {
                if (ContextCompat.checkSelfPermission(getActivity(), permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    mDeniedPermissionList.add(permissions[i]);
                }
            }
            if (mDeniedPermissionList.isEmpty()) {//未授予的权限为空，表示都授予了
                isPermissionGranted = true;
                // Toast.makeText(SelfTestActivity.this,"已经授权",Toast.LENGTH_LONG).show();
            } else {//请求权限方法
                String[] permissions = mDeniedPermissionList.toArray(new String[mDeniedPermissionList.size()]);//将List转为数组
                ActivityCompat.requestPermissions(getActivity(), permissions, MY_PERMISSIONS_REQUEST);
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
                    boolean showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permissions[i]);
                   /* if (showRequestPermission) {
                        showToast("权限未申请");
                    }*/
                    isPermissionGranted = false;
                    break;
                }
            }
        }
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
          /*  String explain = dbExplain[(int)(avgVolume / 10)]+"\n"+dbExplain[(int)(avgVolume / 10)+1];
            mTvExplain.setText(explain);*/
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

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("您的测试环境良好，可以继续后面测试。")
                    .setNegativeButton("取消", null)
                    .setPositiveButton("确定", null)
                    .create()
                    .show();
        }
    }

}