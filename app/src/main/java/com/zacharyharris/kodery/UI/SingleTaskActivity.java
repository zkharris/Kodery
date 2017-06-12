package com.zacharyharris.kodery.UI;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zacharyharris.kodery.Model.Board;
import com.zacharyharris.kodery.Model.ListofTasks;
import com.zacharyharris.kodery.Model.Task;
import com.zacharyharris.kodery.Model.Update;
import com.zacharyharris.kodery.Model.User;
import com.zacharyharris.kodery.R;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zacharyharris.kodery.UI.BoardMembersActivity.root;

public class SingleTaskActivity extends AppCompatActivity {
    public static Activity fatask;

    private static final String TAG = "test";

    public Task task;
    public ListofTasks list;

    public Board board;
    ArrayList<User> memberList;

    private DatabaseReference mDatabase;
    RecycleAdapter adapter;

    class RecycleAdapter extends RecyclerView.Adapter {


        @Override
        public int getItemCount() {
            return memberList.size();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
            SimpleItemViewHolder pvh = new SimpleItemViewHolder(v);
            return pvh;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            SimpleItemViewHolder viewHolder = (SimpleItemViewHolder) holder;
            viewHolder.position = position;
            User user = memberList.get(position);
            ((SimpleItemViewHolder) holder).title.setText(user.getUsername());
            Glide.with(SingleTaskActivity.this).load(user.getPhotoURL()).into(viewHolder.image);
        }

        public final class SimpleItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView title;
            ImageView image;
            public int position;

            public SimpleItemViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                title = (TextView) itemView.findViewById(R.id.usr_name);
                image = (ImageView) itemView.findViewById(R.id.usr_pic);
            }

            @Override
            public void onClick(View v) {
                Log.w(TAG, memberList.get(position).getEmail());
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_task);

        final TextView nameText = (TextView)findViewById(R.id.task_name);
        final TextView descText = (TextView)findViewById(R.id.task_desc);

        if (getIntent().getExtras() != null) {
            Bundle extras = getIntent().getExtras();
            task = (Task)extras.get("task");
            list = (ListofTasks) extras.get("list");
            board = (Board)extras.get("board");
            Log.w(TAG, board.getName());
            if (task != null) {
                nameText.setText(task.getName());
                descText.setText(task.getDescription());
            }
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();

        fatask = this;
        android.support.v7.app.ActionBar mActionBar = getSupportActionBar();
        ColorDrawable mColor = new ColorDrawable(Color.parseColor((board.getColor())));
        mActionBar.setBackgroundDrawable(mColor);
        mActionBar.setTitle(board.getName()+" > "+list.getName()+" > "+task.getName());

        Log.d(TAG, task.getName());
        Log.d(TAG, list.getName());
        Log.d(TAG, board.getName());

        memberList = new ArrayList<>();

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.member_list_task);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);
        adapter = new RecycleAdapter();
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();


        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                String updateText = (memberList.get(viewHolder.getAdapterPosition()).getUsername() +
                        " removed from " + task.getName() + " in List: " + list.getName());
                update(updateText);

                mDatabase.child(root).child("tasks").child(task.getKey()).child("members").
                        child(memberList.get(viewHolder.getAdapterPosition()).
                                getUid()).removeValue();
                memberList.remove(viewHolder.getAdapterPosition());


            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);

        itemTouchHelper.attachToRecyclerView(recyclerView);

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        if(task != null){
            database.getReference(root + "/tasks/" + task.getKey() + "/members").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    memberList.clear();
                    for(DataSnapshot data : dataSnapshot.getChildren()) {
                        findUser(String.valueOf(data.getKey()));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "findUser:onCancelled", databaseError.toException());

                }
            });
        }

        // Admins Feed
        database.getReference(root + "/boards/" + board.getBoardKey()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                board.getAdmins().clear();
                if(dataSnapshot.hasChild("admins")){
                    for(DataSnapshot data : dataSnapshot.child("admins").getChildren()) {
                        board.addAdmin(data.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getAdmins:onCancelled", databaseError.toException());
            }
        });

        Toast swpToast = Toast.makeText(this, "Swipe to remove members from task", Toast.LENGTH_LONG);
        swpToast.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.task_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id){
            case R.id.move_item:

                /*//build alert
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(SingleTaskActivity.this);
                View mview = getLayoutInflater().inflate(R.layout.move_list_popup, null);

                mDatabase = FirebaseDatabase.getInstance().getReference();
                LinearLayoutManager listsllm = new LinearLayoutManager(this);
                RecyclerView mRV = (RecyclerView) mview.findViewById(R.id.list_cycler_pop);
                mRV.setLayoutManager(listsllm);
                listAdapter = new ListsRecycleAdapter();
                mRV.setAdapter(listAdapter);
                listAdapter.notifyDataSetChanged();

                mBuilder.setView(mview);
                AlertDialog dialog = mBuilder.create();
                dialog.show();*/



                Intent i = new Intent(this, MoveTaskActivity.class);
                i.putExtra("board", board);
                i.putExtra("task", task);
                i.putExtra("list", list);
                SingleTaskActivity.this.startActivity(i);

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void findUser(String userUid) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference(root + "/users/" + userUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                memberList.add(user);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "findUser:onCancelled", databaseError.toException());
            }
        });
    }
/*
    private void moveToList() {
        Log.d(TAG, "moveToList: 123test");
        if (task != null) {
            Intent intent = new Intent(this, SingleBoardActivity.class);
            //intent.putExtra("task", task);
            intent.putExtra("board", board);
            Log.d(TAG, "moveToList: safkajdfskl");
            startActivity(intent);
            mDatabase.child(root).child("lists").child(list.getKey()).child(task.getKey()).removeValue();
        }
    }
 */

    private void update(String updateText) {
        String key = mDatabase.child(root).child("updates").child(board.getBoardKey()).push().getKey();

        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        Log.w(TAG, currentDateTimeString);

        Update update = new Update();
        update.setText(updateText);
        update.setBoard(board.getBoardKey());
        update.setKey(key);
        update.setDate(currentDateTimeString);

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(root + "/updates/" + board.getBoardKey() + "/" + key, update.toFirebaseObject());
        mDatabase.updateChildren(childUpdates);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }

}
