package com.zacharyharris.kodery.UI;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zacharyharris.kodery.Model.Board;
import com.zacharyharris.kodery.Model.ListofTasks;
import com.zacharyharris.kodery.Model.Task;
import com.zacharyharris.kodery.Model.User;
import com.zacharyharris.kodery.Model.boardList;
import com.zacharyharris.kodery.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import static com.zacharyharris.kodery.UI.BoardMembersActivity.root;

public class SingleTaskActivity extends AppCompatActivity {

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
            ((SimpleItemViewHolder) holder).title.setText(user.getEmail());
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

        if(task != null){
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            database.getReference("tasks/" + task.getKey() + "/members").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
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
    }

    private void findUser(String userUid) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("users/" + userUid).addValueEventListener(new ValueEventListener() {
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

    private void moveToList() {
        if (task != null) {
            Intent intent = new Intent(this, SingleBoardActivity.class);
            intent.putExtra("task", task);
            intent.putExtra("board", board);
            startActivity(intent);
            mDatabase.child(root).child("lists").child(list.getKey()).child(task.getKey()).removeValue();
        }
    }
}
