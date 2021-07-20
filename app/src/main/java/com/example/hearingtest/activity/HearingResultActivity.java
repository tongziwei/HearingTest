package com.example.hearingtest.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.hearingtest.R;
import com.example.hearingtest.base.BaseActivity;
import com.example.hearingtest.view.HearingResultView;

public class HearingResultActivity extends BaseActivity {
    private static final String TAG = "HearingResultActivity";
   // private int[] hzArr = new int[]{1000, 2000, 4000, 8000,500,125,};
    private int[] lDBMinVal = new int[6];
    private int[] rDBMinVal = new int[6];

    private LinearLayout mLlLeftResult;
    private LinearLayout mLlRightResult;
    private HearingResultView mLeftResultView;
    private HearingResultView mRightResultView;
    private TextView mTvLeftEarRank;
    private TextView mTvRightEarRank;
    private TextView mTvLeftEarAnalysis;
    private TextView mTvRightEarAnalysis;
    private Button mBtnRestartTest;
    private Button mBtnEnterSelfAdaption;

    private String[] mHearingRank;
    private String[] mPropose;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void initView() {
        ((TextView) findViewById(R.id.tv_head_title)).setText("听力测试报告");
        mLlLeftResult = (LinearLayout)findViewById(R.id.ll_left_res);
        mLlRightResult = (LinearLayout)findViewById(R.id.ll_right_res);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        mLeftResultView = new HearingResultView(this,HearingResultView.ONLY_LEFT);
        mLeftResultView.setYScope(90, -10);
        mLlLeftResult.addView(mLeftResultView,layoutParams);

        mRightResultView = new HearingResultView(this,HearingResultView.ONLY_RIGHT);
        mRightResultView.setYScope(90,-10);
        mLlRightResult.addView(mRightResultView,layoutParams);

        mTvLeftEarRank = (TextView)findViewById(R.id.tv_left_ear_rank);
        mTvRightEarRank = (TextView)findViewById(R.id.tv_right_ear_rank);
        mTvLeftEarAnalysis = (TextView)findViewById(R.id.tv_left_ear_analysis);
        mTvRightEarAnalysis = (TextView)findViewById(R.id.tv_right_ear_analysis);
        mBtnRestartTest = (Button)findViewById(R.id.btn_restart_test);
        mBtnEnterSelfAdaption =(Button)findViewById(R.id.btn_enter_self_adaption);

        mBtnRestartTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HearingResultActivity.this,HearingTestActivity.class);
                startActivity(intent);
                finish();
            }
        });

        mBtnEnterSelfAdaption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public void initData() {
        Bundle bundle = getIntent().getBundleExtra("HearingResult");
        lDBMinVal = bundle.getIntArray("LeftDB");
        rDBMinVal = bundle.getIntArray("RightDB");
  /*      lDBMinVal = new int[]{50,10,10,20,10,50};
        rDBMinVal = new int[]{40,80,60,70,60,50};*/
        String ldb ="";
        String rdb ="";
        for(int m =0;m<6;m++){
            ldb=ldb+ lDBMinVal[m]+", ";
            rdb=rdb+ rDBMinVal[m]+", ";
        }
        Log.d(TAG,ldb);
        Log.d(TAG, rdb);
        //更新左耳数据
        for(int i=0;i<lDBMinVal.length;i++){
            mLeftResultView.updateData(i,lDBMinVal[i],true);
        }
        //更新右耳数据
        for(int i=0;i<rDBMinVal.length;i++){
            mRightResultView.updateData(i,rDBMinVal[i],false);
        }

        //计算听力等级
        //(1000Hz测试的结果+2000Hz测试得到结果+500Hz测试得到结果）/3
        // 给出平均听力=（500Hz测试得到结果+1000Hz测试的结果+2000Hz测试得到结果+4000Hz）/4
        double leftAvg = (lDBMinVal[0]+lDBMinVal[1]+lDBMinVal[2]+lDBMinVal[4])/4;
        double rightAvg = (rDBMinVal[0]+rDBMinVal[1]+rDBMinVal[2]+rDBMinVal[4])/4;
        int leftRank = computeRank(leftAvg);
        int rightRank = computeRank(rightAvg);
        mHearingRank = getResources().getStringArray(R.array.hearing_rank_arr);
        mPropose = getResources().getStringArray(R.array.propose_arr);
        mTvLeftEarRank.setText(mHearingRank[leftRank]);
        mTvRightEarRank.setText(mHearingRank[rightRank]);
        mTvLeftEarAnalysis.setText(mPropose[leftRank]);
        mTvRightEarAnalysis.setText(mPropose[rightRank]);
    }

    @Override
    public void setContentView() {
        setContentView(R.layout.activity_hearing_result);
    }

    /**
     * 根据平均听力计算听力等级
     */
    private int computeRank(double dous) {
        int rank = 0;
        if (dous <= 25) {
            rank = 0;
        } else if (dous > 25 && dous <= 40) {// 轻度
            rank = 1;
        } else if (dous > 40 && dous <= 55) {// 中度
            rank = 2;
        } else if (dous > 55 && dous <= 70) {// 中重度
            rank = 3;
        } else if (dous > 70 && dous <= 90) {// 重度
            rank = 4;
        } else if (dous > 90) {// 极重度
            rank = 5;
        }
        return rank;
    }
}