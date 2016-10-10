package com.example.train912;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.TextView;

import com.example.train912.view.LockPatternView;

public class MainActivity extends AppCompatActivity implements LockPatternView.OnPatternChangeListener{

    private TextView mTv;
    private LockPatternView mLpv;
    private String passStr = "9845";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTv = (TextView) findViewById(R.id.tv);
        mLpv = (LockPatternView) findViewById(R.id.lpv);
        mLpv.setOnPatternChangeListener(this);
    }

    @Override
    public void patternChangeListener(String pass) {
        if(!TextUtils.isEmpty(pass)){
            mTv.setText(pass);
            if(passStr.equals(pass)){
                mTv.setText("密码正确");
            }else {
                mTv.setText("密码错误");
                mLpv.errorPoint();
            }
        }else {
            mTv.setText("至少绘制3个点");
        }
    }

    @Override
    public void patternStartListener() {
        mTv.setText("请绘制图案");
    }
}
