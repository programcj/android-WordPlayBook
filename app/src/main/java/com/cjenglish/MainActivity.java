package com.cjenglish;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.cjenglish.db.WordTitle;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    public static class NotificationsUtils {

        private static final String CHECK_OP_NO_THROW = "checkOpNoThrow";
        private static final String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";

        @SuppressLint("NewApi")
        public static boolean isNotificationEnabled(Context context) {

            AppOpsManager mAppOps =
                    (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);

            ApplicationInfo appInfo = context.getApplicationInfo();
            String pkg = context.getApplicationContext().getPackageName();
            int uid = appInfo.uid;
            Class appOpsClass = null;

        /* Context.APP_OPS_MANAGER */
            try {
                appOpsClass = Class.forName(AppOpsManager.class.getName());

                Method checkOpNoThrowMethod =
                        appOpsClass.getMethod(CHECK_OP_NO_THROW,
                                Integer.TYPE, Integer.TYPE, String.class);

                Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);
                int value = (Integer) opPostNotificationValue.get(Integer.class);

                return ((Integer) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) ==
                        AppOpsManager.MODE_ALLOWED);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public static class WordTitleAdapter extends ArrayAdapter<WordTitle> {
        private int mResourceId;
        private final LayoutInflater mInflater;

        public WordTitleAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<WordTitle> objects) {
            super(context, resource, objects);
            mInflater = LayoutInflater.from(context);
            this.mResourceId = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view = null;
            if (convertView != null) {
                view = (TextView) convertView;
            } else {
                convertView = this.mInflater.inflate(mResourceId, null);
                view = (TextView) convertView;
            }
            WordTitle user = getItem(position);
            view.setText(user.getName());
//            ViewHolder holder;
//            if (convertView != null) {
//                holder = (ViewHolder) convertView.getTag();
//            } else {
//                convertView = this.mInflater.inflate(mResourceId, null);
//                holder = new ViewHolder(convertView);
//                convertView.setTag(holder);
//            }
//
//            WordTitle user = getItem(position);

            //holder.textName.setText(user.getName());
            //holder.textWordContext.setText("...");
            //holder.buttonPlay.setTag(position);
            //holder.buttonPlay.setOnClickListener(playOnClick);

            return convertView;
        }

        static interface CallBack {
            void onPlay(Object item, int position);
        }

        private CallBack mCallBack;

        public CallBack getCallBack() {
            return mCallBack;
        }

        public void setCallBack(CallBack mCallBack) {
            this.mCallBack = mCallBack;
        }

        View.OnClickListener playOnClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();
                if (getCallBack() != null) {
                    getCallBack().onPlay(getItem(position), position);
                }

            }
        };

        class ViewHolder {
            @Bind(R.id.textWord)
            TextView textName;
            @Bind(R.id.textWordContent)
            TextView textWordContext;

            @Bind(R.id.buttonPlay)
            Button buttonPlay;

            public ViewHolder(View view) {
                ButterKnife.bind(this, view);
            }
        }
    }

    Context getThisContext() {
        return MainActivity.this;
    }

    @Bind(R.id.list_view)
    ListView listView;

    WordTitleAdapter listViewAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 通过注解绑定控件
        ButterKnife.bind(this);


        tttttttttt();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Add Your World Title class", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                // 不是用这个方法获取EditText的内容的
                final EditText et = new EditText(getThisContext());

                new AlertDialog.Builder(getThisContext()).setTitle("Please inpt world title")
                        .setIcon(android.R.drawable.sym_def_app_icon)
                        .setView(et)
                        .setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) { //按下确定键后的事件 Toast.makeText(getApplicationContext(), et.getText().toString(),Toast.LENGTH_LONG).show(); } }).setNegativeButton("取消",null).show();
                                        //按下确定键后的事件
                                        Toast.makeText(getApplicationContext(), et.getText().toString(), Toast.LENGTH_LONG).show();
                                        String name = et.getText().toString();
                                        if (name.length() > 0)
                                            addWordTitleClass(name);
                                    }
                                }).setNegativeButton("取消", null).show();
                ;

            }


        });

        List<WordTitle> list = CJApp.getInstance().getWordTitleDao().queryBuilder().list();


        listViewAdapter = new WordTitleAdapter(this,
                android.R.layout.simple_list_item_1,
                //R.layout.view_item_word,
                list);
        listView.setAdapter(listViewAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                WordTitle item = listViewAdapter.getItem(i);
                toWordListUI(item);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                final int positionItem = position;

                new AlertDialog.Builder(getThisContext())
                        .setTitle("Do you delete the title?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                WordTitle item = listViewAdapter.getItem(positionItem);
                                removeWordTitle(item);
                            }
                        }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
                return true;
            }
        });

        listViewAdapter.setCallBack(new WordTitleAdapter.CallBack() {
            @Override
            public void onPlay(Object item, int position) {
                WordTitle wordTitle = listViewAdapter.getItem(position);
                Toast.makeText(getThisContext(), wordTitle.getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void tttttttttt() {
        if (!NotificationsUtils.isNotificationEnabled(this)) {
            final AlertDialog dialog = new AlertDialog.Builder(this)
                    .setMessage("检测到您没有打开通知权限")
                    .setPositiveButton("", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent localIntent = new Intent();
                            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            if (Build.VERSION.SDK_INT >= 9) {
                                localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                                localIntent.setData(Uri.fromParts("package", MainActivity.this.getPackageName(), null));
                            } else if (Build.VERSION.SDK_INT <= 8) {
                                localIntent.setAction(Intent.ACTION_VIEW);

                                localIntent.setClassName("com.android.settings",
                                        "com.android.settings.InstalledAppDetails");

                                localIntent.putExtra("com.android.settings.ApplicationPkgName",
                                        MainActivity.this.getPackageName());
                            }
                            startActivity(localIntent);
                        }
                    })
                    .show();

        }
    }

    void uiUpdateListView() {
        List<WordTitle> list = CJApp.getInstance().getWordTitleDao().queryBuilder().list();

        listViewAdapter.clear();
        listViewAdapter.addAll(list);
        listViewAdapter.notifyDataSetInvalidated();
    }

    private void removeWordTitle(WordTitle item) {
        CJApp.getInstance().getWordTitleDao().delete(item);
        uiUpdateListView();
    }

    private void addWordTitleClass(String name) {
        WordTitle obj = new WordTitle();
        obj.setName(name);
        CJApp.getInstance().getWordTitleDao().insert(obj);

        uiUpdateListView();
    }


    private void toWordListUI(WordTitle item) {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, WordListActivity.class);
        intent.putExtra(WordListActivity.INTENT_ARG1, item);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {

            return true;
        }
        if (id == R.id.action_errlog) {

            startActivity(new Intent(this, ErrlogActivity.class));

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }
}
