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
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zacharyharris.kodery.Model.Board;
import com.zacharyharris.kodery.Model.ListofTasks;
import com.zacharyharris.kodery.Model.Task;
import com.zacharyharris.kodery.Model.boardList;
import com.zacharyharris.kodery.R;

import java.util.ArrayList;

import static com.zacharyharris.kodery.UI.BoardMembersActivity.root;

public class SingleListActivity extends AppCompatActivity {

    public static final String TAG = "SingleListActivity";

    RecycleAdapter adapter;
    ArrayList<Task> taskList;

    private ListofTasks list;
    private Board board;

    class RecycleAdapter extends RecyclerView.Adapter {


        @Override
        public int getItemCount() {
            return taskList.size();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
            SimpleItemViewHolder pvh = new SimpleItemViewHolder(v);
            return pvh;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            SimpleItemViewHolder viewHolder = (SimpleItemViewHolder) holder;
            viewHolder.position = position;
            Task task = taskList.get(position);
            ((SimpleItemViewHolder) holder).title.setText(task.getName());
            // set number of members in each task
            if(task.getNumMembers() != null) {
                ((SimpleItemViewHolder) holder).subtitle.setText(task.getNumMembers() + " members");
            }
        }

        public final class SimpleItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView title;
            TextView subtitle;
            public int position;

            public SimpleItemViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                title = (TextView) itemView.findViewById(R.id.task_name);
                subtitle = (TextView) itemView.findViewById(R.id.task_desc);
            }

            @Override
            public void onClick(View view) {
                /*Intent newIntent = new Intent(SingleListActivity.this, TodoActivity.class);
                newIntent.putExtra("task", taskList.get(position));
                newIntent.putExtra("list", list);
                newIntent.putExtra("board", board);
                SingleListActivity.this.startActivity(newIntent);
                //if(moveTask){
                //  mDatabase.child("task").child(task.getKey()).child("list").setValue(taskList.get(position).getKey());
                // mDatabase.child("list").child(todoList.get(position).getKey()).child("todos").child(task.getKey()).setValue(true);
                //*/
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_list);

        if(getIntent().getExtras() != null) {
            Bundle extras = getIntent().getExtras();
            list = (ListofTasks)extras.get("list");
            board = (Board)extras.get("board");
        }

        final TextView listTitle = (TextView) findViewById(R.id.list_name);
        listTitle.setText(list.getName());

        final TextView listDesc = (TextView) findViewById(R.id.list_desc);
        listDesc.setText(list.getDescription());

        taskList = new ArrayList<>();

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.tasks_list);
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
        database.getReference(root + "lists/" + list.getKey()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                taskList.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Log.w(TAG, String.valueOf(data.getKey()));
                    findTask(String.valueOf(data.getKey()));
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getList:onCancelled", databaseError.toException());
            }
        });
    }

    private void findTask(final String taskKey) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference(root + "/tasks").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()) {
                    Task task = data.getValue(Task.class);
                    if (task.getKey().equals(taskKey)){
                        taskList.add(task);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "findTask:onCancelled", databaseError.toException());
            }
        });
    }
}
