package pl.kuba.floatwindow;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

public class FloatWindowService extends Service {

	private View mFloatView;
	private WindowManager windowManager;
	private LayoutParams mFloatContainerParams;
	private boolean windowExpanded = false;
	private LinearLayout mFloatViewContainer;
	private int beforeExpandX;
	private int beforeExpandY;
	private ScaleAnimation mScaleDownAnim;
	private ScaleAnimation mScaleUpAnim;

	private class CustomAnimationListener implements AnimationListener {

		@Override
		public void onAnimationStart(Animation animation) {
		}

		@Override
		public void onAnimationEnd(Animation animation) {
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}
	}

	private OnTouchListener mFloatViewTouchListener = new OnTouchListener() {

		private int initialX;
		private int initialY;
		private float initialTouchX;
		private float initialTouchY;
		protected static final int MOVEMENT_THRESHOLD_PX = 10;
		private long pointerDownTimestamp = 0;
		private boolean shouldScaleUp = false;
		protected static final long SCALE_DOWN_ANIM_THRESHOLD = 150;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				pointerDownTimestamp = System.currentTimeMillis();
				initialX = mFloatContainerParams.x;
				initialY = mFloatContainerParams.y;
				initialTouchX = event.getRawX();
				initialTouchY = event.getRawY();

				return true;
			case MotionEvent.ACTION_UP:
				if (shouldScaleUp) {
					shouldScaleUp = !shouldScaleUp;
					mFloatView.startAnimation(mScaleUpAnim);
				}
				windowManager.updateViewLayout(mFloatViewContainer,
						mFloatContainerParams);
				if (Math.abs(event.getRawX() - initialTouchX) < MOVEMENT_THRESHOLD_PX
						|| Math.abs(event.getRawY() - initialTouchY) < MOVEMENT_THRESHOLD_PX) {
					if (windowExpanded) {
						animateFloatView(mFloatContainerParams, true);
					} else {
						animateFloatView(mFloatContainerParams, false);
					}
					windowExpanded = !windowExpanded;
				}
				return true;
			case MotionEvent.ACTION_MOVE:
				int diffX = (int) (event.getRawX() - initialTouchX);
				int diffY = (int) (event.getRawY() - initialTouchY);
				if (!shouldScaleUp
						&& System.currentTimeMillis() - pointerDownTimestamp > SCALE_DOWN_ANIM_THRESHOLD) {
					mFloatView.startAnimation(mScaleDownAnim);
					shouldScaleUp = true;
				}
				if (Math.abs(diffX) > MOVEMENT_THRESHOLD_PX
						|| Math.abs(diffY) > MOVEMENT_THRESHOLD_PX) {
					windowExpanded = false;
					PopupHelper.close();
				}
				if (shouldScaleUp) {
					mFloatContainerParams.x = initialX + diffX;
					mFloatContainerParams.y = initialY + diffY;
					windowManager.updateViewLayout(mFloatViewContainer,
							mFloatContainerParams);
				}
				return true;
			}
			return false;
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		initAnimations();
		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		if (mFloatView != null) {
			windowManager.removeView(mFloatView);
		}
		mFloatView = LayoutInflater.from(this).inflate(R.layout.float_view,
				null);

		mFloatContainerParams = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);
		mFloatContainerParams.y = 0;
		mFloatContainerParams.x = 0;
		mFloatView.setOnTouchListener(mFloatViewTouchListener);
		mFloatViewContainer = new LinearLayout(this);
		mFloatViewContainer.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		windowManager.addView(mFloatViewContainer, mFloatContainerParams);
		mFloatViewContainer.addView(mFloatView);
		AnimationSet as = new AnimationSet(true);
		as.addAnimation(new AlphaAnimation(0, 1));
		as.addAnimation(new ScaleAnimation(0.001f, 1, 0.001f, 1));
		as.setDuration(250);
		mFloatView.startAnimation(as);
	}

	private void initAnimations() {
		mScaleDownAnim = new ScaleAnimation(1f, 0.85f, 1f, 0.85f);
		mScaleDownAnim.setDuration(50);
		mScaleDownAnim.setFillEnabled(true);
		mScaleDownAnim.setFillAfter(true);
		mScaleUpAnim = new ScaleAnimation(0.85f, 1f, 0.85f, 1f);
		mScaleUpAnim.setDuration(50);
		mScaleUpAnim.setFillEnabled(true);
		mScaleUpAnim.setFillAfter(true);
	}

	protected void animateFloatView(final LayoutParams params,
			final boolean toPreviousPosition) {
		float maxDuration = 400f;
		int upperY = -getResources().getDisplayMetrics().heightPixels / 2;
		int upperX = getResources().getDisplayMetrics().widthPixels / 2;
		final int x = params.x;
		final int y = params.y;
		if (!toPreviousPosition) {
			beforeExpandX = x;
			beforeExpandY = y;
		}
		final int distanceX = params.x
				- (toPreviousPosition ? beforeExpandX : upperX);
		final int distanceY = params.y
				- (toPreviousPosition ? beforeExpandY : upperY);
		int duration = (int) (((float) Math.max(Math.abs(distanceX),
				Math.abs(distanceY)) / getResources().getDisplayMetrics().heightPixels) * maxDuration);
		Animation animation = new Animation() {

			@Override
			protected void applyTransformation(float interpolatedTime,
					Transformation t) {
				params.x = (int) (x - interpolatedTime * distanceX);
				params.y = (int) (y - interpolatedTime * distanceY);
				windowManager.updateViewLayout(mFloatViewContainer, params);
			}

		};
		animation.setDuration(duration);
		animation.setInterpolator(new AccelerateInterpolator());
		animation.setAnimationListener(new CustomAnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				super.onAnimationEnd(animation);
				if (toPreviousPosition) {
					PopupHelper.close();
				} else {
					PopupHelper.show(FloatWindowService.this,
							mFloatViewContainer);
				}
			}

		});
		mFloatView.startAnimation(animation);
	}

	@Override
	public void onDestroy() {
		if (mFloatView != null) {
			windowManager.removeView(mFloatView);
		}
		super.onDestroy();
	}
}
