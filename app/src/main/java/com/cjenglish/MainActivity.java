package com.cjenglish;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {


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
            TextView  view= null;
            if (convertView != null) {
                view=(TextView)convertView;
            }else {
                convertView = this.mInflater.inflate(mResourceId, null);
                view=(TextView)convertView;
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
        if (id == R.id.action_test) {
//            CJApp.getInstance().onPlayWord("pthread");
//            CJApp.getInstance().onPlayWord("switch");

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }
}
