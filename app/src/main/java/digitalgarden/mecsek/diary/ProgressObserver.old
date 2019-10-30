package digitalgarden.mecsek.diary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

public class ProgressObserver extends BroadcastReceiver
    {
    public static final String ACTION_STRING = "Progress";
    public static final String DATA_WHO = "Who";
    public static final String DATA_CYCLE = "Cycle";
    public static final String DATA_MAX_CYCLES = "Max";

    // Progress senders
    public static final int LOADER = 1;
    public static final int FILTER = 2;

    public interface OnProgressListener
        {
        public void onProgress(int who, int maxCycle, int cycle);
        }

    private OnProgressListener onProgressListener;

    public ProgressObserver(Context context, OnProgressListener onProgressListener )
        {
        this.onProgressListener = onProgressListener;

        // Register this Receiver to receive messages.
        IntentFilter filter = new IntentFilter( ACTION_STRING );
        LocalBroadcastManager.getInstance( context ).registerReceiver( this, filter);
        }

    @Override
    public void onReceive(Context context, Intent intent)
        {
        int who = intent.getIntExtra( DATA_WHO, 0 );
        int cycle = intent.getIntExtra( DATA_CYCLE, 0 );
        int maxCycle = intent.getIntExtra( DATA_MAX_CYCLES, -1 );

        onProgressListener.onProgress( who, maxCycle, cycle );
        }
    }