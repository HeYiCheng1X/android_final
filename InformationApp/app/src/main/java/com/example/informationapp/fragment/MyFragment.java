package com.example.informationapp.fragment;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;

import com.example.informationapp.R;
import com.example.informationapp.activity.CollectActivity;
import com.example.informationapp.activity.HomeActivity;
import com.example.informationapp.activity.LoginActivity;

public class MyFragment extends BaseFragment implements View.OnClickListener {
    private ImageView imgHeader;
    private RelativeLayout rlCollect, rlLogout;

    public MyFragment() {
    }

    public static MyFragment newInstance() {
        MyFragment fragment = new MyFragment();
        return fragment;
    }

    @Override
    protected void initView() {
        imgHeader = mRootView.findViewById(R.id.img_myHeader);
        rlCollect = mRootView.findViewById(R.id.rl_collect);
        rlLogout = mRootView.findViewById(R.id.rl_logout);
    }

    @Override
    protected void initData() {
        imgHeader.setOnClickListener(this::onClick);
        rlCollect.setOnClickListener(this::onClick);
        rlLogout.setOnClickListener(this::onClick);
    }

    @Override
    protected int initLayout() {
        return R.layout.fragment_my;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_myHeader:
                showToast("个人信息界面待完善");
                break;
            case R.id.rl_collect:
                navigateTo(CollectActivity.class);
                break;
            case R.id.rl_logout:
                removeByKey("logged");
                navigateToWithFlag(LoginActivity.class,
                        Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);//跳转到登陆界面 清除其他页面
                break;
        }
    }
}
