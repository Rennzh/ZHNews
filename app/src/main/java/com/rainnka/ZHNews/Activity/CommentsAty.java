package com.rainnka.ZHNews.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.rainnka.ZHNews.Activity.Base.SwipeBackAty;
import com.rainnka.ZHNews.Adapter.CommentsAtyRecvAdp;
import com.rainnka.ZHNews.Application.BaseApplication;
import com.rainnka.ZHNews.Bean.Comments;
import com.rainnka.ZHNews.Bean.ZhihuNewsItemComments;
import com.rainnka.ZHNews.CustomView.CommentsRecvDividerItemDecoration;
import com.rainnka.ZHNews.R;
import com.rainnka.ZHNews.Utility.ConstantUtility;
import com.rainnka.ZHNews.Utility.LengthConverterUtility;
import com.rainnka.ZHNews.Utility.NetworkConnectivityUtility;
import com.rainnka.ZHNews.Utility.SnackbarUtility;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rainnka on 2016/8/14 11:18
 * Project name is ZHKUNews
 */
public class CommentsAty extends SwipeBackAty {

	/*********************************************************************************************
	 * main self member
	 *******************************************************************************************/

	protected CoordinatorLayout coordinatorLayout;
	protected Toolbar toolbar;
	protected RecyclerView recyclerView;
	protected TextView textView_shadow;

	protected Intent intent;
	protected int targetId;

	public CommentsAtyRecvAdp commentsAtyRecvAdp;
	public LinearLayoutManager linearLayoutManager;
	public CommentsRecvDividerItemDecoration dividerItemDecoration;

	public List<ZhihuNewsItemComments> zhihuNewsItemLongCommentsList;
	public List<ZhihuNewsItemComments> zhihuNewsItemShortCommentsList;

	public boolean longCommentsFlag = false;
	public boolean shortCommentsFlag = false;

	protected CommentsHandler commentsHandler;
	protected OkHttpClient okHttpClient;
	protected Gson gson;

	/**********************************************************************************************
	 * main inherited methods
	 ********************************************************************************************/

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.comments_aty);
		setupWindowAnimations();
		setFullScreenLayout();

		bindIntentInfo();

		initCommentsHandler();
		initGson();

		initComponent();
		initToolbarSetting();

		createRecyclerViewAdapter();
		initRecyclerViewSetting();

		initZhihuNewsComments();

	}


	@Override
	protected void onDestroy() {
		commentsHandler.removeCallbacksAndMessages(null);
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
		}
		return true;
	}

	/***********************************************************************************************
	 * main self method
	 ********************************************************************************************/

	private void initComponent() {
		toolbar = (Toolbar) findViewById(R.id.comments_aty_Toolbar);
		coordinatorLayout = (CoordinatorLayout) findViewById(R.id.comments_aty_CoordinatorLayout);
		recyclerView = (RecyclerView) findViewById(R.id.comments_aty_RecyclerView);
		textView_shadow = (TextView) findViewById(R.id.comments_aty_shadow);
	}

	private void initToolbarSetting() {
		toolbar.setTitle("评论");
		toolbar.setTitleTextColor(Color.WHITE);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) toolbar.getLayoutParams();
		layoutParams.setMargins(0, LengthConverterUtility.dip2px(BaseApplication
				.getBaseApplicationContext(), 24), 0, 0);
		toolbar.setLayoutParams(layoutParams);
	}

	public void bindIntentInfo() {
		intent = getIntent();
		targetId = intent.getIntExtra("id", 0);
	}

	private void initGson() {
		gson = new Gson();
	}

	private void initCommentsHandler() {
		commentsHandler = new CommentsHandler(this);
	}

	private void initZhihuNewsComments() {
		if (NetworkConnectivityUtility.getConnectivityStatus(NetworkConnectivityUtility.getConnectivityManager())) {
			okHttpClient = new OkHttpClient();
			String urlLong = String.format(ConstantUtility.ZHIHUAPI_LONG_COMMENTS, String.valueOf
					(targetId));
			Request request_longcomments = new Request.Builder()
					.url(urlLong)
					.build();
			Call call_long_comments = okHttpClient.newCall(request_longcomments);

			String urlShort = String.format(ConstantUtility.ZHIHUAPI_SHORT_COMMENTS, String.valueOf
					(targetId));
			Request request_shortcomments = new Request.Builder()
					.url(urlShort)
					.build();
			Call call_short_comments = okHttpClient.newCall(request_shortcomments);

			call_long_comments.enqueue(new Callback() {
				@Override
				public void onFailure(Request request, IOException e) {
					commentsHandler.sendEmptyMessage(0x0812354);
				}

				@Override
				public void onResponse(Response response) throws IOException {
					if (response.code() == 200) {
						String commentStr = response.body().string();
						if (commentStr.contains("author")) {
							try {
								Comments commentsLong = gson.fromJson(commentStr, Comments.class);
								zhihuNewsItemLongCommentsList = commentsLong.comments;
								addDataToCommentsList(zhihuNewsItemLongCommentsList);
								commentsHandler.sendEmptyMessage(ConstantUtility.ADD_COMMENTSLIST);
								longCommentsFlag = true;
							} catch (Exception e) {
								Log.i("ZRH", e.toString());
							}
						} else {
							commentsHandler.sendEmptyMessage(0x346754);
						}
					}
				}
			});

			call_short_comments.enqueue(new Callback() {
				@Override
				public void onFailure(Request request, IOException e) {

				}

				@Override
				public void onResponse(Response response) throws IOException {
					if (response.code() == 200) {
						String commentStr = response.body().string();
						if (commentStr.contains("author")) {
							try {
								Comments commentsShort = gson.fromJson(commentStr, Comments.class);
								zhihuNewsItemShortCommentsList = commentsShort.comments;
								addDataToCommentsList(zhihuNewsItemShortCommentsList);
								commentsHandler.sendEmptyMessage(ConstantUtility.ADD_COMMENTSLIST);
								shortCommentsFlag = true;
							} catch (Exception e) {
								Log.i("ZRH", e.toString());
							}
						} else {
							commentsHandler.sendEmptyMessage(ConstantUtility.NO_COMMENTSLIST);
						}
					}
				}
			});
		} else {
			networkAccessOutTime();
		}
	}

	private void initRecyclerViewSetting() {
		linearLayoutManager = new LinearLayoutManager(this);
		linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		recyclerView.setLayoutManager(linearLayoutManager);
		recyclerView.setAdapter(commentsAtyRecvAdp);
		dividerItemDecoration = new CommentsRecvDividerItemDecoration(this, ConstantUtility
				.VERTICAL_LIST);
		recyclerView.addItemDecoration(dividerItemDecoration);
	}

	private void createRecyclerViewAdapter() {
		zhihuNewsItemLongCommentsList = new ArrayList<>();
		zhihuNewsItemShortCommentsList = new ArrayList<>();
		commentsAtyRecvAdp = new CommentsAtyRecvAdp(this);
		commentsAtyRecvAdp.addZhihuNewsItemCommentsList(zhihuNewsItemLongCommentsList);
		commentsAtyRecvAdp.addZhihuNewsItemCommentsList(zhihuNewsItemShortCommentsList);
	}

	private void notifyDataHasChanged() {
		if (longCommentsFlag && shortCommentsFlag) {
			if (textView_shadow.getVisibility() == View.VISIBLE) {
				textView_shadow.setVisibility(View.GONE);
			}
			commentsAtyRecvAdp.notifyDataSetChanged();
		}
	}

	synchronized private void addDataToCommentsList(List<ZhihuNewsItemComments> list) {
		commentsAtyRecvAdp.addZhihuNewsItemCommentsList(list);
	}

	public void noCommentsList() {
		if (textView_shadow.getVisibility() == View.GONE) {
			textView_shadow.setVisibility(View.VISIBLE);
			textView_shadow.setText("无评论哦");
		}
	}

	public void networkAccessOutTime() {
		if (textView_shadow.getVisibility() == View.GONE) {
			textView_shadow.setVisibility(View.VISIBLE);
			textView_shadow.setText("网络不在状态,点击刷新");
			textView_shadow.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					initZhihuNewsComments();
				}
			});
		}
	}

	/*********************************************************************************************
	 * main static inner class
	 ********************************************************************************************/

	public static class CommentsHandler extends Handler {

		CommentsAty commentsAty;
		WeakReference<CommentsAty> commentsAtyWeakReference;

		public CommentsHandler(CommentsAty commentsAty) {
			this.commentsAtyWeakReference = new WeakReference<>(commentsAty);
			this.commentsAty = this.commentsAtyWeakReference.get();
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case ConstantUtility.ADD_COMMENTSLIST:
					commentsAty.notifyDataHasChanged();
					break;
				case ConstantUtility.NO_COMMENTSLIST:
					SnackbarUtility.getSnackbarDefault(commentsAty.coordinatorLayout, "无评论",
							Snackbar.LENGTH_LONG).show();
					commentsAty.noCommentsList();
					break;
				default:
					commentsAty.networkAccessOutTime();
					break;
			}
		}
	}

}