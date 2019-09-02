package sliit.ranjitha.fyp.service;

import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;



public class AppService extends Service {

    public static final String SERVICE_ACTION = "service_action";
    public static final String SERVICE_ACTION_CHECK = "service_action_check";

    static final long CHECK_INTERVAL = 400;


    private Context mContext;
    private Handler mHandler = new Handler();
    private Runnable mRepeatCheckTask = new Runnable() {
        @Override
        public void run() {
            System.out.println("sdeekrhkrhjrjr 55555555 ");
                mHandler.postDelayed(mRepeatCheckTask, CHECK_INTERVAL);
            }

    };

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("sdeekrhkrhjrjr 11111111 ");
        mContext = getApplicationContext();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("sdeekrhkrhjrjr 222222222 ");
        if (intent != null) {
            String action = intent.getStringExtra(SERVICE_ACTION);
            if (!TextUtils.isEmpty(action)) {
                switch (action) {
                    case SERVICE_ACTION_CHECK:
                        System.out.println("sdeekrhkrhjrjr 33333333 ");
                        startIntervalCheck();
                        break;
                }
            }
        }
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        System.out.println("sdeekrhkrhjrjr 66666666 ");
//        stopSelf();
//        mHandler.removeCallbacks(mRepeatCheckTask);
//        super.onDestroy();
        try {
            startService(new Intent(mContext, AppService.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startIntervalCheck() {
        System.out.println("sdeekrhkrhjrjr 44444444444 ");
            mHandler.post(mRepeatCheckTask);

    }

}

