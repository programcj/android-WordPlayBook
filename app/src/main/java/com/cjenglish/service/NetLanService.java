package com.cjenglish.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.util.Log;

import com.cj.db.greendao.WordItemDao;
import com.cjenglish.CJApp;
import com.cjenglish.db.WordItem;
import com.cjenglish.db.WordTitle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.greenrobot.greendao.query.QueryBuilder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.List;

public class NetLanService extends Service {
    public NetLanService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String msg = null;
        if (intent != null)
            msg = intent.getStringExtra("data");
//        1):START_STICKY： 如果service进程被kill掉，保留service的状态为开始状态，但不保留递送的intent对象。
//                  随后系统会尝试重新创建service，由 于服务状态为开始状态，
//                  所以创建服务后一定会调用onStartCommand(Intent,int,int)方法。
//                  如果在此期间没有任何启动命令被传 递到service，那么参数Intent将为null。
//        2):START_NOT_STICKY：“非粘性的”。使用这个返回值时，如果在执行完onStartCommand后，服务被异常kill掉，系统不会自动重启该服务
//        3):START_REDELIVER_INTENT：重传Intent。使用这个返回值时，如果在执行完onStartCommand后，服务被异常kill掉，系统会自动重启该服务，并将Intent的值传入。
//
//        4):START_STICKY_COMPATIBILITY：START_STICKY的兼容版本，但不保证服务被kill后一定能重启。

        //return super.onStartCommand(intent, flags, startId);
        if (thread == null) {
            thread = new Thread(runnable);
            thread.start();
            Log.i("NetLanService", "start");
        }
        return START_STICKY;
    }

    static Gson gson = new GsonBuilder().serializeNulls()
            .setPrettyPrinting()
            .create();
    static int port = 6589;

    public static class UDPInfo {
        public String wordClass;
        public String wordName;
        public String wordContext;
    }

    Thread thread = null;

    public static String testGson() {
        UDPInfo info = new UDPInfo();

        info.wordClass = "c";
        info.wordName = "unsigned int";
        info.wordContext = "...";
        return gson.toJson(info) + port;
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {

            //1
            DatagramSocket serverSocket = null;
            try {
                serverSocket = new DatagramSocket(6589);
            } catch (SocketException e) {
                e.printStackTrace();
            }

            if (serverSocket == null)
                return;

            try {
                serverSocket.setBroadcast(true);
            } catch (SocketException e) {
                e.printStackTrace();
            }

            while (!Thread.currentThread().isInterrupted()) {

                //2
                byte[] arr = new byte[1024];
                DatagramPacket packet = new DatagramPacket(arr, arr.length);

                //3 当程序运行起来之后,receive方法会一直处于监听状态
                try {
                    serverSocket.receive(packet);
                    byte[] value = packet.getData(); //从包中将数据取出
                    String json = new String(value);
                    UDPInfo udpInfo = gson.fromJson(json, UDPInfo.class);
                    if (udpInfo != null) {
                        WordTitle wordTitle = CJApp.getInstance().getWordTitleDao().queryBuilder()
                                .where(WordItemDao.Properties.Name.eq(udpInfo.wordClass)).unique();

                        if (wordTitle == null) {
                            wordTitle = new WordTitle();
                            wordTitle.setName(udpInfo.wordClass);
                            wordTitle.setTimeCreate(System.currentTimeMillis());
                            CJApp.getInstance().getWordTitleDao().insert(wordTitle);

                            wordTitle = CJApp.getInstance().getWordTitleDao().queryBuilder()
                                    .where(WordItemDao.Properties.Name.eq(udpInfo.wordClass)).unique();
                        }

                        if (wordTitle != null) {

                            List<WordItem> wordItems = CJApp.getInstance().getWordItemDao().queryBuilder()
                                    .where(WordItemDao.Properties.Pid.eq("" + wordTitle.getId()))
                                    .where(WordItemDao.Properties.Name.eq(udpInfo.wordName))
                                    .list();
                            if (wordItems.size() == 0) {
                                WordItem item = new WordItem();
                                item.setPid(wordTitle.getId());
                                item.setName(udpInfo.wordName);
                                item.setContent(udpInfo.wordContext);
                                item.setCreateTime(System.currentTimeMillis());
                                CJApp.getInstance().getWordItemDao().insert(item);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            serverSocket.close();

            stopSelf();
        }
    };

    @Override
    public void onDestroy() {
        Log.i("Service", "destroy");
        super.onDestroy();
    }
}
