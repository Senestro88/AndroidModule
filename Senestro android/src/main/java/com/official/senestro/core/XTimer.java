package com.official.senestro.core;

import android.os.Handler;
import android.os.Looper;
import com.official.senestro.core.callbacks.interfaces.XTimerBackgroundCallback;
import com.official.senestro.core.callbacks.interfaces.XTimerCallback;

import java.util.Timer;
import java.util.TimerTask;

public class XTimer {

    private XTimer(){}

    /**
     * Schedule a task to run
     */
    public static void schedule(XTimerCallback callback, long delay) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                postOnMainThread(callback::run);
            }
        }, delay);
    }

    /**
     * Schedule a task to run in background
     */
    public static void scheduleInBackground(XTimerBackgroundCallback callback, long delay) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                XHandlerThread.runInBackground(callback::run);
            }
        }, delay);
    }

    /**
     * Schedule a task to run (It will be repeated according to the repeat value. 0 means infinite)
     */
    public static void scheduleRepeat(XTimerCallback callback, int delay, int repeat) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new RepeatRunnable(handler, callback, delay, repeat), delay);
    }

    /**
     * Schedule a task to run in background(It will be repeated according to the repeat value. 0 means infinite)
     */
    public static void scheduleRepeatInBackground(XTimerBackgroundCallback callback, int delay, int repeat) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new RepeatRunnableInBackground(handler, callback, delay, repeat), delay);
    }

    //PRIVATE
    private static void postOnMainThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    private static class RepeatRunnable implements Runnable {
        private final Handler handler;
        private final XTimerCallback callback;
        private final int delay;
        private final int repeatEndTimes;
        private int endTimes = 0;

        public RepeatRunnable(Handler handler, XTimerCallback callback, int delay, int repeatEndTimes) {
            this.handler = handler;
            this.callback = callback;
            this.delay = delay;
            this.repeatEndTimes = repeatEndTimes;
        }

        @Override
        public void run() {
            postOnMainThread(() -> {
                callback.run();
                endTimes++;
                if (repeatEndTimes > 0 && endTimes >= repeatEndTimes) {
                    handler.removeCallbacksAndMessages(null);
                } else {
                    handler.postDelayed(this, delay);
                }
            });
        }
    }

    private static class RepeatRunnableInBackground implements Runnable {
        private final Handler handler;
        private final XTimerBackgroundCallback callback;
        private final int delay;
        private final int repeatEndTimes;
        private int endTimes = 0;

        public RepeatRunnableInBackground(Handler handler, XTimerBackgroundCallback callback, int delay, int repeatEndTimes) {
            this.handler = handler;
            this.callback = callback;
            this.delay = delay;
            this.repeatEndTimes = repeatEndTimes;
        }

        @Override
        public void run() {
            XHandlerThread.runInBackground(() -> {
                callback.run();
                endTimes++;
                if (repeatEndTimes > 0 && endTimes >= repeatEndTimes) {
                    handler.removeCallbacksAndMessages(null);
                } else {
                    handler.postDelayed(this, delay);
                }
            });
        }
    }
}