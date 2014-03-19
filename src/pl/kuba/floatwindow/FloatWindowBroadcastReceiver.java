package pl.kuba.floatwindow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class FloatWindowBroadcastReceiver extends BroadcastReceiver {

	public static final String ACTION_SHOW_FLOAT_VIEW = "pl.kuba.show_float_view";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(ACTION_SHOW_FLOAT_VIEW)) {
			context.startService(new Intent(context, FloatWindowService.class));
		}
	}

}
