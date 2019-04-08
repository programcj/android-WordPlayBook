package com.cjenglish.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.cj.db.greendao.WordTitleDao;
import com.cjenglish.CJApp;
import com.cjenglish.MainActivity;
import com.cjenglish.R;
import com.cjenglish.WordListActivity;
import com.cjenglish.db.WordItem;
import com.cjenglish.db.WordTitle;

import java.util.List;

public class WordPlayService extends Service {
    public WordPlayService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static void buildThisStopService(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, WordPlayService.class);
        intent.putExtra("option", "stop");

        context.stopService(intent);
    }

    public static void buildThisStopPlay(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, WordPlayService.class);
        intent.putExtra("option", "stop");
        context.startService(intent);

        //context.stopService(intent);
    }

    public static void buildThisPlayTitle(Context context, String title, int playIndex) {
        Intent intent = new Intent();
        intent.setClass(context, WordPlayService.class);
        intent.putExtra("option", "play");
        intent.putExtra("playTitle", title);
        intent.putExtra("playIndex", playIndex);
        context.startService(intent);
    }


    /**
     * 8.0以上需要增加channel
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannelIfNeeded() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel("1", "小米视频", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("word");
            channel.enableLights(false);
            channel.enableVibration(false);
            //channel.importance = NotificationManager.IMPORTANCE_LOW //设置为low, 通知栏不会有声音
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        //定义一个notification

        Intent mainIntent = new Intent(this, MainActivity.class);

        PendingIntent mainPendingIntent = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        final Integer notificationID = 100;
        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), WordPlayService.class.getName());
        notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("play")
                .setContentText("start play...")
                .setDefaults(NotificationCompat.FLAG_ONLY_ALERT_ONCE)
                .setVibrate(new long[]{0})
                .setSound(null)
                .setContentIntent(mainPendingIntent);


        //Send the notification:
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = null;

        //Android8.0要求设置通知渠道
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel(WordPlayService.class.getName(), "cj play", NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setVibrationPattern(new long[]{0});
            channel.setSound(null, null);
            notificationManager.createNotificationChannel(channel);
            notificationBuilder.setChannelId(WordPlayService.class.getName());
        }

        final Notification notification = notificationBuilder.build();
        startForeground(11, notification);
    }

    //@IntDef(value = {Service.START_FLAG_REDELIVERY, Service.START_FLAG_RETRY, flag = true)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        if (intent != null) {
            String option = intent.getStringExtra("option");
            String playTitle = intent.getStringExtra("playTitle");
            int playIndex = intent.getIntExtra("playIndex", 0);
            callOption(option, playTitle, playIndex);
        }
        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);// 停止前台服务--参数：表示是否移除之前的通知
        stopPlay();
        Log.d(WordPlayService.class.getName(), "stop");
        super.onDestroy();
    }

    private void callOption(String option, String playTitle, int playIndex) {
        Log.d(WordPlayService.class.getName(), "" + option + "," + playTitle + "," + playIndex);

        if (option.compareToIgnoreCase("play") == 0) {
            if (playTitle == null) {
                stopPlay();
                return;
            }

            WordTitle wordTitle = CJApp.getInstance().getWordTitleDao().queryBuilder()
                    .where(WordTitleDao.Properties.Name.eq(playTitle)).unique();
            if (wordTitle == null) {
                stopPlay();
                return;
            }

            List<WordItem> listWords = WordListActivity.WordItemGetAll(wordTitle);
            if (listWords.size() == 0) {
                stopPlay();
                return;
            }

            playList(listWords, playIndex);
        }

        if (option.compareToIgnoreCase("stop") == 0) {
            stopPlay();
            stopSelf();
        }
    }

    private void playList(List<WordItem> listWords, int playIndex) {
        Log.d(WordPlayService.class.getName(), "playList " + listWords.size());
        _isRunning = true;

        if (playThread == null) {
            playThread = new PlayThread(listWords, playIndex);
            playThread.start();
        } else {

        }
    }

    private void stopPlay() {
        if (playThread != null)
            playThread.interrupt();
        playThread = null;
    }

    static volatile boolean _isRunning = false;

    public static boolean isRunning() {
        return _isRunning;
    }

    PlayThread playThread = null;

    public static class PlayThread extends Thread {
        List<WordItem> listWords;
        int playIndex;


        public PlayThread(List<WordItem> listWords, int playIndex) {
            this.listWords = listWords;
            this.playIndex = playIndex;
        }


        @Override
        public void run() {

            while (!this.isInterrupted()) {
                try {

                    if (playIndex >= listWords.size())
                        playIndex = 0;

                    WordItem wordItem = listWords.get(playIndex);
                    CJApp.getInstance().onPlayWord(wordItem.getName());
                    playIndex++;
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
            Log.d(WordPlayService.class.getName(), "PlayThread exit run");

            _isRunning = false;
        }
    }
}
