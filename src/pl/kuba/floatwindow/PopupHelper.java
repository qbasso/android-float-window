package pl.kuba.floatwindow;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;

public class PopupHelper {

	static final int DEFAULT_DELAY = 200;

	protected static PopupWindow mWindow;

	protected static Runnable mCloseRunnable = new Runnable() {
		@Override
		public void run() {
			mWindow.dismiss();
		}
	};

	protected static Handler mHandler = new Handler();

	/**
	 * Inits the popup.
	 * 
	 * @param c
	 *            the c
	 */
	protected static void initPopup(Context c,
			boolean shouldDismissOnOutsideTouch) {
		mWindow = new PopupWindow(c);
		mWindow.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
		mWindow.getBackground().setAlpha(200);
		mWindow.setTouchable(true);
		if (shouldDismissOnOutsideTouch) {
			mWindow.setOutsideTouchable(true);
			mWindow.setFocusable(false);
			mWindow.setTouchInterceptor(new OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
						mHandler.postDelayed(new Runnable() {
							@Override
							public void run() {
								mWindow.dismiss();

							}
						}, DEFAULT_DELAY);
						return true;
					}
					return false;
				}
			});
		} else {
			mWindow.setOutsideTouchable(false);
			mWindow.setFocusable(false);
		}
	}

	public static void show(Context c, View anchor) {
		initPopup(c, true);
		View v = initView(c);
		DisplayMetrics dm = c.getResources().getDisplayMetrics();
		mWindow.setContentView(v);
		mWindow.setWidth(dm.widthPixels);
		mWindow.setHeight(dm.heightPixels-anchor.getBottom()*2	);
		// mWindow.setAnimationStyle(R.style.UndoAnimationStyle);
		mWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, 0,
				anchor.getHeight());
	}

	private static View initView(Context c) {
		View v = LayoutInflater.from(c).inflate(R.layout.popup_content, null);
		v.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode==KeyEvent.KEYCODE_BACK) {
					close();
					return true;
				}
				return false;
			}
		});
		v.setLayoutParams(new LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT));
		return v;
	}
	
	public static void close() {
		if (mWindow != null) {
			mWindow.dismiss();
		}
	}
}
