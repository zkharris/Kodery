package com.zacharyharris.kodery.UI;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zacharyharris.kodery.Model.Board;
import com.zacharyharris.kodery.Model.Task;
import com.zacharyharris.kodery.Model.User;
import com.zacharyharris.kodery.R;

import java.util.ArrayList;

public class BoardMembersActivity extends AppCompatActivity {

    public static final String TAG = "BoardMembersActivity";
    public static final String root = "testRoot";

    RecycleAdapter adapter;

    private Board board;
    private ArrayList<User> memberList;
    private boolean addMembers;
    private Task task;
    public User owner;
    private DatabaseReference mDatabase;

    class RecycleAdapter extends RecyclerView.Adapter {

        @Override
        public int getItemCount() {
            return memberList.size();
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
            BoardMembersActivity.RecycleAdapter.SimpleItemViewHolder pvh = new BoardMembersActivity.RecycleAdapter.SimpleItemViewHolder(v);
            return pvh;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            BoardMembersActivity.RecycleAdapter.SimpleItemViewHolder viewHolder = (BoardMembersActivity.RecycleAdapter.SimpleItemViewHolder) holder;
            viewHolder.position = position;
            User user = memberList.get(position);
            ((BoardMembersActivity.RecycleAdapter.SimpleItemViewHolder) holder).title.setText(user.getUsername());
            Glide.with(BoardMembersActivity.this).load(user.getPhotoURL()).into(viewHolder.image);

        }

        public final class SimpleItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView image;
            TextView title;
            public int position;

            public SimpleItemViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                title = (TextView) itemView.findViewById(R.id.usr_name);
                image = (ImageView) itemView.findViewById(R.id.usr_pic);
            }

            @Override
            public void onClick(View view) {
                Log.d(TAG, "Clicked user is " + memberList.get(position));
                if(addMembers) {
                    addTaskMembers(task, memberList.get(position));
                }

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_members);

        if (getIntent().getExtras() != null) {
            Bundle extras = getIntent().getExtras();
            board = (Board)extras.get("board");
            task = (Task)extras.get("task");
            if (task != null) {
                addMembers = true;
            }
        }

        // Initialize Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        owner = findOwner(board.getOwnerUid());
        memberList = new ArrayList<>();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.memberRecycleView);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);
        adapter = new RecycleAdapter();
        recyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged();

    }

    @Override
    protected void onResume() {
        super.onResume();

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Peep reference
        database.getReference(root + "/boards/" + board.getBoardKey() + "/peeps").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    findUser(String.valueOf(data.getKey()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "peepReference:onCancelled", databaseError.toException());
            }
        });
    }

    private void findUser(String peepUid) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference(root + "/users/" + peepUid).addValueEventListener(new ValueEventListener() {
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

    private User findOwner(final String ownerUid) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference(root + "/users/" + ownerUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                owner = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "findOwner:onCancelled", databaseError.toException());
            }
        });
        return owner;
    }

    private void addTaskMembers(Task task, User user) {
        mDatabase.child(root).child("tasks").child(task.getKey()).child("members").child(user.getUid()).setValue(true);
        //Intent to Single Task view
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id){
            case R.id.add_item:
                startActivity(new Intent(this, UsersActivity.class));

        }

        return super.onOptionsItemSelected(item);
    }


}
