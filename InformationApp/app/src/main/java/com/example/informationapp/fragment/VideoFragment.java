package com.example.informationapp.fragment;

import com.dueeeke.videoplayer.player.VideoView;
import com.example.informationapp.R;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dueeeke.videocontroller.StandardVideoController;
import com.dueeeke.videocontroller.component.CompleteView;
import com.dueeeke.videocontroller.component.ErrorView;
import com.dueeeke.videocontroller.component.GestureView;
import com.dueeeke.videocontroller.component.TitleView;
import com.dueeeke.videocontroller.component.VodControlView;
import com.dueeeke.videoplayer.player.VideoView;
import com.google.gson.Gson;
import com.example.informationapp.R;
import com.example.informationapp.adapter.VideoAdapter;
import com.example.informationapp.entity.VideoEntity;
import com.example.informationapp.entity.VideoListResponse;
import com.example.informationapp.listener.OnItemChildClickListener;
import com.example.informationapp.util.Tag;
import com.example.informationapp.util.Utils;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class VideoFragment extends BaseFragment implements OnItemChildClickListener {
    private String URL;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private VideoAdapter mVideoAdapter;
    private List<VideoEntity> mDatas = new ArrayList<>();

    protected VideoView mVideoView;
    protected StandardVideoController mController;
    protected ErrorView mErrorView;
    protected CompleteView mCompleteView;
    protected TitleView mTitleView;

    protected int mCurPos = -1;//?????????????????????
    protected int mLastPos = mCurPos;//???????????????????????????????????????????????????????????????

    public VideoFragment() {
    }

    public static VideoFragment newInstance(String URL) {
        VideoFragment fragment = new VideoFragment();
        fragment.URL = URL;
        return fragment;
    }

    @Override
    protected void initView() {
        mRecyclerView = mRootView.findViewById(R.id.video_recycler_View);
        //???????????????????????????
        Bundle bundle = getArguments();
        String videoURL = bundle.getString("url");
        this.URL = videoURL;

        initVideoView();
    }

    @Override
    protected void initData() {
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mVideoAdapter = new VideoAdapter(getActivity());
        mVideoAdapter.setOnItemChildClickListener(this);
        mRecyclerView.setAdapter(mVideoAdapter);
        mRecyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {

            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {
                FrameLayout playerContainer = view.findViewById(R.id.player_container);
                View v = playerContainer.getChildAt(0);
                if (v != null && v == mVideoView && !mVideoView.isFullScreen()) {
                    releaseVideoView();
                }
            }
        });
        try {
            refresh();
        } catch (IOException e) {
        }
    }

    private void refresh() throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
//                .url(URL)
                .url("http://47.112.180.188:8080/renren-fast/app/videolist/listAll")
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("onFailure", e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String resultJson = response.body().string();
                VideoListResponse mVideoListResponse = new Gson().fromJson(resultJson, VideoListResponse.class);

                if (mVideoListResponse.getPage().getList() != null) {
                    if (mVideoListResponse.getCode() == 0) {
                        mDatas.clear();
                        mDatas.addAll(mVideoListResponse.getPage().getList());

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mVideoAdapter.setDatas(mDatas);
                                mVideoAdapter.notifyDataSetChanged();
                            }
                        });
                    } else if (mVideoListResponse.getCode() == 10011 || mVideoListResponse.getCode() == 10012 || mVideoListResponse.getCode() == 10013) {
                        showToastSync("??????????????????????????????");
                    } else {
                        showToastSync("????????????");
                    }
                }
            }
        });
    }

    @Override
    protected int initLayout() {
        return R.layout.fragment_video;
    }

    /**
     * ??????????????????
     */
    protected void initVideoView() {
        mVideoView = new VideoView(getActivity());
        mVideoView.setOnStateChangeListener(new VideoView.SimpleOnStateChangeListener() {
            @Override
            public void onPlayStateChanged(int playState) {
                //??????VideoViewManager?????????????????????
                if (playState == VideoView.STATE_IDLE) {
                    Utils.removeViewFormParent(mVideoView);
                    mLastPos = mCurPos;
                    mCurPos = -1;
                }
            }
        });
        mController = new StandardVideoController(getActivity());
        mErrorView = new ErrorView(getActivity());
        mController.addControlComponent(mErrorView);
        mCompleteView = new CompleteView(getActivity());
        mController.addControlComponent(mCompleteView);
        mTitleView = new TitleView(getActivity());
        mController.addControlComponent(mTitleView);
        mController.addControlComponent(new VodControlView(getActivity()));
        mController.addControlComponent(new GestureView(getActivity()));
        mController.setEnableOrientation(true);
        mVideoView.setVideoController(mController);
    }

    @Override
    public void onPause() {
        super.onPause();
        pause();
    }

    /**
     * ??????onPause????????????super????????????????????????
     * ????????????????????????????????????onPause?????????
     */
    protected void pause() {
        releaseVideoView();
    }

    @Override
    public void onResume() {
        super.onResume();
        resume();
    }

    /**
     * ??????onResume????????????super????????????????????????
     * ????????????????????????????????????onResume?????????
     */
    protected void resume() {
        if (mLastPos == -1)
            return;
        //???????????????????????????
        startPlay(mLastPos);
    }

    /**
     * PrepareView?????????
     */
    @Override
    public void onItemChildClick(int position) {
        startPlay(position);
    }

    /**
     * ????????????
     *
     * @param position ????????????
     */
    protected void startPlay(int position) {
        if (mCurPos == position) return;
        if (mCurPos != -1) {
            releaseVideoView();
        }
        VideoEntity videoEntity = mDatas.get(position);
        mTitleView.setTitle(videoEntity.getVtitle());
        mVideoView.setUrl(videoEntity.getPlayurl());
        View itemView = mLinearLayoutManager.findViewByPosition(position);
        if (itemView == null) return;
        VideoAdapter.ViewHolder viewHolder = (VideoAdapter.ViewHolder) itemView.getTag();
        //?????????????????????PrepareView??????????????????????????????isPrivate???????????????true???
        mController.addControlComponent(viewHolder.mPrepareView, true);
        Utils.removeViewFormParent(mVideoView);
        viewHolder.mPlayerContainer.addView(mVideoView, 0);
        //???????????????VideoView?????????VideoViewManager????????????????????????????????????
        getVideoViewManager().add(mVideoView, Tag.LIST);
        mVideoView.start();
        mCurPos = position;
    }

    /**
     * ????????????
     */
    private void releaseVideoView() {
        mVideoView.release();
        if (mVideoView.isFullScreen()) {
            mVideoView.stopFullScreen();
        }
        if (getActivity().getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        mCurPos = -1;
    }
}
