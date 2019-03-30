package com.cjenglish;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cj.db.greendao.WordItemDao;
import com.cjenglish.db.WordItem;
import com.cjenglish.db.WordTitle;

import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;

public class WordListActivity extends AppCompatActivity {
    public final static String INTENT_ARG1 = "wordTitle";

    public static class WordItemAdapter extends ArrayAdapter<WordItem> {
        private final LayoutInflater mInflater;

        public WordItemAdapter(@NonNull Context context, @NonNull List<WordItem> objects) {
            super(context, 0, objects);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView != null) {
                holder = (ViewHolder) convertView.getTag();
            } else {
                convertView = this.mInflater.inflate(R.layout.view_item_word, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            }

            WordItem user = getItem(position);

            holder.textName.setText(user.getName());
            holder.textWordContext.setText(user.getContent());
            holder.buttonPlay.setTag(position);
            holder.buttonPlay.setOnClickListener(playOnClick);
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

    WordTitle wordTitle;

    @Nullable
    @Bind(R.id.wordlist)
    ListView listViewWord;

    WordItemAdapter adapterListViewWord = null;

    //        android.support.v7.app.ActionBar actionBar =getSupportActionBar();
//        //调用hide方法，隐藏actionbar
//        actionBar.hide();
//        //调用show方法，展示actionbar
//        actionBar.show();

    void uiChangeToListView() {
        setContentView(R.layout.activity_word_list);
        ButterKnife.bind(this); // 通过注解绑定控件

        List<WordItem> listWords = WordListActivity.WordItemGetAll(wordTitle);

        adapterListViewWord = new WordItemAdapter(this, listWords);
        listViewWord.setAdapter(adapterListViewWord);

        adapterListViewWord.setCallBack(new WordItemAdapter.CallBack() {
            @Override
            public void onPlay(Object item, int position) {
                WordItem wordItem = adapterListViewWord.getItem(position);
                CJApp.getInstance().onPlayWord(wordItem.getName());
            }
        });
        listViewWord.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final int positionItem = position;

                new AlertDialog.Builder(getThisContext())
                        .setTitle("Do you delete the word?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                WordItem item = adapterListViewWord.getItem(positionItem);
                                removeWordItem(item);
                            }
                        }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
                return true;
            }
        });
        listViewWord.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }


    public static class GridViewAdapter extends RecyclerView.Adapter<GridViewAdapter.MyViewHolder> {
        Context context;
        List<WordItem> listWords;

        public GridViewAdapter(Context context, List<WordItem> list) {
            this.context = context;
            this.listWords = list;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.view_item_word, parent,
                    false);
            MyViewHolder holder = new MyViewHolder(view);

            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, typedValue, true);
            view.setBackgroundResource(typedValue.resourceId);

            return holder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            WordItem wordItem = listWords.get(position);
            holder.textName.setText(wordItem.getName());
            holder.textWordContext.setText("" + wordItem.getSortline());

            holder.buttonPlay.setTag(holder);
            holder.buttonPlay.setOnClickListener(playOnclick);

            holder.itemView.setTag(holder);
            holder.itemView.setOnLongClickListener(itemLongListener);

            holder.itemView.setOnClickListener(itemClickListener);
        }

        View.OnClickListener itemClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyViewHolder holder = (MyViewHolder) v.getTag();
                int position = holder.getAdapterPosition();

                if (onPlayCallback != null)
                    onPlayCallback.onPlayOnClick(position);
            }
        };

        View.OnLongClickListener itemLongListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                MyViewHolder holder = (MyViewHolder) v.getTag();
                int position = holder.getAdapterPosition();

                if (onItemLongClick != null) {
                    onItemLongClick.onLongClick(position);
                }
                return true;
            }
        };


        @Override
        public int getItemCount() {
            return listWords.size();
        }

        View.OnClickListener playOnclick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();
                if (onPlayCallback != null)
                    onPlayCallback.onPlayOnClick(position);
            }
        };

        public WordItem getItem(int position) {
            return listWords.get(position);
        }

        class MyViewHolder extends RecyclerView.ViewHolder {

            @Nullable
            @Bind(R.id.textWord)
            TextView textName;
            @Nullable
            @Bind(R.id.textWordContent)
            TextView textWordContext;
            @Nullable
            @Bind(R.id.buttonPlay)
            Button buttonPlay;

            public MyViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }

        OnPlayCallback onPlayCallback;
        OnItemLongClick onItemLongClick;

        interface OnPlayCallback {
            void onPlayOnClick(int position);
        }

        interface OnItemLongClick {
            void onLongClick(int position);
        }

    }

    @Nullable
    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;

    private GridViewAdapter adapterViewGrid;

    void uiChangeToGridView() {
        setContentView(R.layout.activity_word_grid);
        ButterKnife.bind(this); // 通过注解绑定控件

        List<WordItem> listWords = WordListActivity.WordItemGetAll(wordTitle);

        GridLayoutManager layoutManage = new GridLayoutManager(getThisContext(), 3);

        recyclerView.setLayoutManager(layoutManage);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapterViewGrid = new GridViewAdapter(this, listWords));
        //添加分割线
//        recyclerView.addItemDecoration(new DividerItemDecoration(
//                getThisContext(), DividerItemDecoration.HORIZONTAL));
//
//        recyclerView.addItemDecoration(new DividerItemDecoration(
//                getThisContext(), DividerItemDecoration.VERTICAL));

        adapterViewGrid.onPlayCallback = new GridViewAdapter.OnPlayCallback() {
            @Override
            public void onPlayOnClick(int position) {
                WordItem wordItem = adapterViewGrid.getItem(position);
                CJApp.getInstance().onPlayWord(wordItem.getName());
                playIndex = position;
            }
        };
        /*
        adapterViewGrid.onItemLongClick = new GridViewAdapter.OnItemLongClick() {
            @Override
            public void onLongClick(int position) {
                final int positionItem = position;

                new AlertDialog.Builder(getThisContext())
                        .setTitle("Do you delete the title?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                WordItem item = adapterViewGrid.getItem(positionItem);
                                removeWordItem(item);
                            }
                        }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
            }
        };*/

        //创建一个ItemTouchHelper，需要传入一个继承自ItemTouchHelper.Callback
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            //这个方法用于让RecyclerView拦截向上滑动，向下滑动，想左滑动
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                // 上下拖动
                int dragFlags = 0;
                if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
                    // GridView 样式四个方向都可以
                    dragFlags = ItemTouchHelper.UP | ItemTouchHelper.LEFT |
                            ItemTouchHelper.DOWN | ItemTouchHelper.RIGHT;
                } else {
                    // ListView 样式不支持左右，只支持上下
                    dragFlags = ItemTouchHelper.UP |
                            ItemTouchHelper.DOWN;
                }
                // 向左滑动
                int swipeFlags = ItemTouchHelper.LEFT;
                //swipeFlags=0;
                return makeMovementFlags(dragFlags, swipeFlags);
            }

            /**
             * drag状态下，在canDropOver()返回true时，会调用该方法让我们拖动换位置的逻辑(需要自己处理变换位置的逻辑)
             */
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition(); // 获取原来的位置
                int targetPosition = target.getAdapterPosition();  // 得到目标的位置

                Log.i(WordListActivity.class.getName(), "from:" + fromPosition + ",target:" + targetPosition);

                //得到当拖拽的viewHolder的Position
                fromPosition = viewHolder.getAdapterPosition();
                //拿到当前拖拽到的item的viewHolder
                int toPosition = target.getAdapterPosition();
                if (fromPosition < toPosition) {
                    for (int i = fromPosition; i < toPosition; i++) {
                        WordItem item = adapterViewGrid.getItem(i);
                        WordItem item1 = adapterViewGrid.getItem(i + 1);
                        WordItemSwap(item, item1);  // 改变实际的数据集

                        Collections.swap(adapterViewGrid.listWords, i, i + 1);
                    }
                } else {
                    for (int i = fromPosition; i > toPosition; i--) {
                        WordItem item = adapterViewGrid.getItem(i);
                        WordItem item1 = adapterViewGrid.getItem(i - 1);
                        WordItemSwap(item, item1);  // 改变实际的数据集

                        Collections.swap(adapterViewGrid.listWords, i, i - 1);
                    }
                }

                adapterViewGrid.notifyItemMoved(fromPosition, targetPosition);

                List<WordItem> listWords1 = adapterViewGrid.listWords;
                for (WordItem item : listWords1
                        ) {
                    Log.d("wordItemData", "" + item.getName() + "," + item.getSortline());
                }

                if (fromPosition >= 0)
                    return true;

                //} else {
                //    Collections.swap(adapterViewGrid.listWords, fromPosition, targetPosition);
                //}
                //adapterViewGrid.notifyDataSetChanged();
                //adapterViewGrid.notifyItemMoved(fromPosition, targetPosition); //不会刷新数据
                return true;
            }


            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // 获取当前删除的位置
                int position = viewHolder.getAdapterPosition();
                final int positionItem = position;

                new AlertDialog.Builder(getThisContext())
                        .setTitle("Do you delete the word?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                WordItem item = adapterViewGrid.getItem(positionItem);
                                WordItemDelete(item);
                                adapterViewGrid.listWords.remove(positionItem);
                                adapterViewGrid.notifyItemRemoved(positionItem);
                            }
                        }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        adapterViewGrid.notifyItemChanged(positionItem);
                    }
                }).show();
            }

            Drawable background = null;

            /**
             * 拖动选择状态改变回调
             */
            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                    // ItemTouchHelper.ACTION_STATE_IDLE 看看源码解释就能理解了
                    // 侧滑或者拖动的时候背景设置为灰色
                    background = viewHolder.itemView.getBackground();

                    viewHolder.itemView.setBackgroundColor(Color.GRAY);
                }
            }

            /**
             * 回到正常状态的时候回调
             */
            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                // 正常默认状态下背景恢复默认
                if (background != null)
                    viewHolder.itemView.setBackground(background);
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);

    }


    int UIChange = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        wordTitle = (WordTitle) intent.getSerializableExtra(INTENT_ARG1);
        setTitle(wordTitle.getName());

        if (UIChange == 1) {
            uiChangeToGridView();
        } else
            uiChangeToListView();

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_wordlist, menu);
        return true;
    }

    Context getThisContext() {
        return WordListActivity.this;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest'ting
        int id = item.getItemId();

        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
            case R.id.action_add: {
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
                                            addWordItem(name);
                                    }
                                }).setNegativeButton("取消", null).show();
            }
            break;
            case R.id.action_auto_play:
                //自动朗读
                if (timerIsRun) {
                    timerIsRun = false;
                    timer.cancel();
                    timer = null;
                    Toast.makeText(getThisContext(), "stop play..", Toast.LENGTH_SHORT).show();
                } else {
                    timerIsRun = true;
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Message message = handler.obtainMessage(200);
                            handler.sendMessageAtTime(message, 0);
                        }
                    }, 0, 1500);
                    Toast.makeText(getThisContext(), "start play..", Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.action_refurbish: {
                List<WordItem> listWords = WordListActivity.WordItemGetAll(wordTitle);
                if (adapterViewGrid != null) {
                    adapterViewGrid.listWords=listWords;
                    adapterViewGrid.notifyDataSetChanged();
                }
            }
            break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (timer != null)
            timer.cancel();
        timer = null;
    }


    Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 200:      //               "执行我要做的事情用来刷新UI";
                    playNext();
                    break;
            }
        }
    };

    int playIndex = 0;

    private void playNext() {
        if (adapterViewGrid != null) {
            if (playIndex >= adapterViewGrid.getItemCount())
                playIndex = 0;

            if (playIndex < adapterViewGrid.getItemCount()) {
                WordItem item = adapterViewGrid.getItem(playIndex);
                CJApp.getInstance().onPlayWord(item.getName());
                playIndex++;
            }
            return;
        }
    }

    volatile boolean timerIsRun = false;

    Timer timer = null;


    private void removeWordItem(WordItem item) {
        WordItemDelete(item);

        if (adapterListViewWord != null) {
            adapterListViewWord.remove(item);
            adapterListViewWord.notifyDataSetChanged();
        }

        if (adapterViewGrid != null) {
            adapterViewGrid.listWords.remove(item);
            adapterViewGrid.notifyDataSetChanged();
        }
    }


    private void addWordItem(String name) {

        WordItem wordItem = WordItemAdd(name, wordTitle.getId());

        if (adapterListViewWord != null) {
            adapterListViewWord.add(wordItem);
            adapterListViewWord.notifyDataSetChanged();
        }

        if (adapterViewGrid != null) {
            adapterViewGrid.listWords.add(wordItem);
            adapterViewGrid.notifyDataSetChanged();
        }
    }

    public static void WordItemDelete(WordItem worldTitle) {
        CJApp.getInstance().getWordItemDao().delete(worldTitle);
    }

    public static WordItem WordItemAdd(String name, Long pid) {
        WordItem obj = new WordItem();
        obj.setName(name);
        obj.setPid(pid);
        obj.setCreateTime(System.currentTimeMillis());
        long count = CJApp.getInstance().getWordItemDao().queryBuilder().where(WordItemDao.Properties.Pid.eq("" + pid)).count();
        obj.setSortline(count + 1);
        CJApp.getInstance().getWordItemDao().insert(obj);
        return obj;
    }

    public static void WordItemAdd(String name, WordTitle worldTitle) {

    }

    public static List<WordItem> WordItemGetAll(WordTitle wordTitle) {
        //orderAsc：升序排序
        //orderDesc: 降序排序
        return CJApp.getInstance().getWordItemDao()
                .queryBuilder().where(WordItemDao.Properties.Pid.eq("" + wordTitle.getId())).orderAsc(WordItemDao.Properties.Sortline).list();
    }

    private static void WordItemSwap(WordItem item, WordItem item1) {
        Long line = item.getSortline();
        item.setSortline(item1.getSortline());
        CJApp.getInstance().getWordItemDao().update(item);
        item1.setSortline(line);
        CJApp.getInstance().getWordItemDao().update(item1);
        //Log.i(WordListActivity.class.getName(),"->" + item.getName() + "<>" + item1.getName());
    }
}
