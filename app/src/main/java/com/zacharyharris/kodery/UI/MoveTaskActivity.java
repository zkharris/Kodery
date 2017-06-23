package com.zacharyharris.kodery.UI;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zacharyharris.kodery.Model.Board;
import com.zacharyharris.kodery.Model.ListofTasks;
import com.zacharyharris.kodery.Model.Task;
import com.zacharyharris.kodery.Model.Update;
import com.zacharyharris.kodery.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.R.id.list;
import static com.zacharyharris.kodery.UI.BoardMembersActivity.root;


public class MoveTaskActivity extends AppCompatActivity {

    public static final String TAG = "MoveTaskActivity";

    ArrayList<ListofTasks> boardsList;
    ListsRecycleAdapter listAdapter;

    private Board board;
    private DatabaseReference mDatabase;

    public ListofTasks list;

    private FirebaseUser mFirebaseUser;
    private Task task;
    private boolean addTaskToList;
    private Task currTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_move_task);

        if (getIntent().getExtras() != null) {
            Bundle extras = getIntent().getExtras();
            board = (Board) extras.get("board");
            task = (Task) extras.get("task");
            list = (ListofTasks) extras.get("list");
            if (task != null) {
                addTaskToList = true;
            }
        }

        android.support.v7.app.ActionBar mActionBar = getSupportActionBar();
        ColorDrawable mColor = new ColorDrawable(Color.parseColor((board.getColor())));
        mActionBar.setBackgroundDrawable(mColor);
        //mActionBar.setTitle("Move "+task.getName()+" to...");

        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setDisplayShowCustomEnabled(true);
        View customView = getLayoutInflater().inflate(R.layout.actionbar_title, null);
        TextView customTitle = (TextView) customView.findViewById(R.id.actionbarTitle);
        customTitle.setText("Move "+task.getName()+" to...");
        customTitle.setTextSize(20);
        ImageView customImage = (ImageView) customView.findViewById(R.id.actionbarImage);
        customImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                SingleTaskActivity.fatask.finish();
                SingleListActivity.falist.finish();
                SingleBoardActivity.faboard.finish();
            }
        });
        mActionBar.setCustomView(customView);

        boardsList = new ArrayList<>();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        LinearLayoutManager listsllm = new LinearLayoutManager(this);

        RecyclerView ListrecyclerView = (RecyclerView) findViewById(R.id.list_cycler_pop);
        ListrecyclerView.setLayoutManager(listsllm);
        listAdapter = new ListsRecycleAdapter();
        ListrecyclerView.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();

        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);

    }

    @Override
    protected void onResume(){
        super.onResume();

        // Lists Feed
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference(root + "/lists/" + board.getBoardKey()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boardsList.clear();
                Log.w("TodoApp", "getUser:onCancelled " + dataSnapshot.toString());
                Log.w("TodoApp", "count = " + String.valueOf(dataSnapshot.getChildrenCount()) + " values " + dataSnapshot.getKey());
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    ListofTasks list = data.getValue(ListofTasks.class);
                    boardsList.add(list);
                    if(data.hasChild("tasks")) {
                        String numTasks = String.valueOf(data.child("tasks").getChildrenCount());
                        list.setNumTasks(numTasks);
                    }
                }
                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getLists:onCancelled", databaseError.toException());
            }
        });
    }



    class ListsRecycleAdapter extends RecyclerView.Adapter {


        @Override
        public int getItemCount() {
            return boardsList.size();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_move_task, parent, false);
            ListsRecycleAdapter.SimpleItemViewHolder pvh = new ListsRecycleAdapter.SimpleItemViewHolder(v);
            return pvh;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ListsRecycleAdapter.SimpleItemViewHolder viewHolder = (ListsRecycleAdapter.SimpleItemViewHolder) holder;
            viewHolder.position = position;
            ListofTasks list = boardsList.get(position);
            ((ListsRecycleAdapter.SimpleItemViewHolder) holder).title.setText(list.getName());
        }

        public final class SimpleItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView title;
            public int position;

            public SimpleItemViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                title = (TextView) itemView.findViewById(R.id.list_name);
            }

            @Override
            public void onClick(View view) {
                if(list.getName().equals(title.getText().toString())){
                    Toast.makeText(view.getContext(), "Please add "+task.getName()+" to a new List", Toast.LENGTH_LONG).show();
                } else {
                    ListofTasks listc = boardsList.get(position);
                    Toast.makeText(view.getContext(), "Added " + task.getName() + " to " + title.getText() + "!", Toast.LENGTH_SHORT).show();
                    moveTask(listc);
                    // Deletes? vv
                    //mDatabase.child(root).child("lists").child(list.getKey()).child(task.getKey()).removeValue();

                    Intent intent2 = new Intent(view.getContext(), SingleBoardActivity.class);
                    Intent intent = new Intent(view.getContext(), SingleListActivity.class);
                    intent.putExtra("board", board);
                    intent2.putExtra("board", board);
                    intent.putExtra("list", listc);
                    startActivity(intent);

                    SingleListActivity.falist.finish();
                    SingleTaskActivity.fatask.finish();
                    finish();

                }
            }
        }
    }

    private void moveTask(ListofTasks listc) {
        //second section
        //save it to the firebase db
        // remove it from list
        mDatabase.child(root).child("lists").child(board.getBoardKey()).
                child(list.getKey()).child("tasks").child(task.getKey()).removeValue();
        mDatabase.child(root).child("tasks").child(task.getKey()).child("list").setValue(listc.getKey());
        mDatabase.child(root).child("lists").child(board.getBoardKey()).child(listc.getKey()).
                child("tasks").child(task.getKey()).setValue(true);

        String updateText = ("Task:" + task.getName() + " moved to List:" + listc.getName());
        update(updateText);

    }


    private void update(String updateText) {
        String key = mDatabase.child(root).child("updates").child(board.getBoardKey()).push().getKey();
/*
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyy hh:mm aa");
        String dateString = format.format(calendar.getTime());
*/
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm aa");
        String dateString = format.format(calendar.getTime());

        Update update = new Update();
        update.setText(updateText);
        update.setBoard(board.getBoardKey());
        update.setKey(key);
        update.setDate(dateString);

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(root + "/updates/" + board.getBoardKey() + "/" + key, update.toFirebaseObject());
        mDatabase.updateChildren(childUpdates);
    }

}
