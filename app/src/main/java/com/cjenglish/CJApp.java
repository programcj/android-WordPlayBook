package com.cjenglish;

import android.app.Application;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Process;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import com.cj.db.greendao.DaoMaster;
import com.cj.db.greendao.DaoSession;
import com.cj.db.greendao.WordItemDao;
import com.cj.db.greendao.WordTitleDao;
import com.cjenglish.service.NetLanService;

import java.util.Locale;

/**
 * Created by cc on 2019/3/5.
 */

public class CJApp extends Application {
    private DaoMaster.DevOpenHelper mDevOpenHelper;
    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;

    public WordTitleDao getWordTitleDao() {
        return wordTitleDao;
    }

    public WordItemDao getWordItemDao() {
        return mDaoSession.getWordItemDao();
    }

    private WordTitleDao wordTitleDao;


    private SQLiteDatabase db;

    TextToSpeech textToSpeech = null;
    private static CJApp instance = null;

    public static CJApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        mDevOpenHelper = new DaoMaster.DevOpenHelper(this, "cj_world.db", null);
        mDaoMaster = new DaoMaster(mDevOpenHelper.getWritableDb());
        mDaoSession = mDaoMaster.newSession();
        wordTitleDao = mDaoSession.getWordTitleDao();

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == textToSpeech.SUCCESS) {
//                    int result = textToSpeech.setLanguage(Locale.ENGLISH);
//                    if (result != TextToSpeech.LANG_COUNTRY_AVAILABLE && result != TextToSpeech.LANG_AVAILABLE) {
//                        Toast.makeText(CJApp.this, "TTS暂时不支持这种语音的朗读！", Toast.LENGTH_SHORT).show();
//                    } else {

                    //textToSpeech.speak("Hello,World!",
                    //        TextToSpeech.QUEUE_ADD, null);
//                    }
                }
            }
        });

        Intent intent = new Intent(this, NetLanService.class);
        intent.putExtra("data", "start");
        startService(intent);

        String s = NetLanService.testGson();
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();

        defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                //打印堆栈       
                throwable.printStackTrace();
                //   //如果系统提供了默认的处理器,交给系统处理,否则kill掉自己     
                //
                if (defaultUncaughtExceptionHandler != null) {
                   // defaultUncaughtExceptionHandler.uncaughtException(thread, throwable);
                } else {
                    Process.killProcess(Process.myPid());
                }

            }
        });
    }

    Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = null;

    /**
     * play world: onPlayWorld("hello");
     *
     * @param string
     */
    public void onPlayWord(String string) {
        if (textToSpeech == null || string == null)
            return;
        //1、TextToSpeech.QUEUE_FLUSH:如果指定该模式，当TTS调用speak方法时，它会中断当前实例正在运行的任务(也可以理解为清除当前语音任务，转而执行新的语音任务)
        //2、TextToSpeech.QUEUE_ADD:如果指定为该模式，当TTS调用speak方法时，会把新的发音任务添加到当前发音任务列队之后——也就是等任务队列中的发音任务执行完成后在来执行speak()方法指定的发音任务。
        ///textToSpeech.stop();
        string=string.replaceAll("_"," ");
        string=string.replaceAll("#","");

        textToSpeech.speak(string,
                TextToSpeech.QUEUE_ADD, null);

    }

    @Override
    public void onTerminate() {
        if (textToSpeech != null) {
            textToSpeech.shutdown();
        }
        Toast.makeText(this, "exit", Toast.LENGTH_SHORT).show();
        super.onTerminate();
    }
}
