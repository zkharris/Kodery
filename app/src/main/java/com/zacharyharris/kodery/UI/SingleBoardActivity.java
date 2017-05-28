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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zacharyharris.kodery.Model.Board;
import com.zacharyharris.kodery.Model.Task;
import com.zacharyharris.kodery.Model.boardList;
import com.zacharyharris.kodery.R;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class SingleBoardActivity extends AppCompatActivity {

    public static final String TAG = "SingleBoardActivity";

    RecycleAdapter adapter;
    ArrayList<boardList> boardsList;

    private Board board;
    private DatabaseReference mDatabase;
    private Task task;


    class RecycleAdapter extends RecyclerView.Adapter {


        @Override
        public int getItemCount() {
            return boardsList.size();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_board_list, parent, false);
            SimpleItemViewHolder pvh = new SimpleItemViewHolder(v);
            return pvh;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            SimpleItemViewHolder viewHolder = (SimpleItemViewHolder) holder;
            viewHolder.position = position;
            boardList list = boardsList.get(position);
            ((SimpleItemViewHolder) holder).title.setText(list.getName());
            // set number of todos in each list
        }

        public final class SimpleItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView title;
            public int position;

            public SimpleItemViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                //title = (TextView) itemView.findViewById(R.id.TextView);
            }

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SingleBoardActivity.this, SingleBoardActivity.class);
                intent.putExtra("list", boardsList.get(position));
                intent.putExtra("board", board);
                SingleBoardActivity.this.startActivity(intent);
                if(task != null){
                    mDatabase.child("list").child(boardsList.get(position).getKey()).child("tasks")
                            .child(task.getKey()).setValue(true);
                    mDatabase.child("task").child(task.getKey()).child("list")
                            .setValue(boardsList.get(position).getKey());
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_board);

        if(getIntent().getExtras() != null) {
            Bundle extras = getIntent().getExtras();
            board = (Board)extras.get("board");
        }

        //final TextView boardTitle = (TextView) findViewById(R.id.boardTitle);
        //boardTitle.setText(board.getName());

        boardsList = new ArrayList<>();

        //RecyclerView recyclerView = (RecyclerView)findViewById(R.id.SingleBoardRecycleView);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        //recyclerView.setLayoutManager(llm);
        adapter = new RecycleAdapter();
        //recyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("boards/" + board.getBoardKey() + "/lists").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boardsList.clear();
                        Log.w("TodoApp", "getUser:onCancelled " + dataSnapshot.toString());
                        Log.w("TodoApp", "count = " + String.valueOf(dataSnapshot.getChildrenCount()) + " values " + dataSnapshot.getKey());
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            Log.w(TAG, String.valueOf(data.getKey()));
                            findList(String.valueOf(data.getKey()));
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("TodoApp", "getUser:onCancelled", databaseError.toException());
                    }
                });


    }

    private void findList(final String listKey) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("lists").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    boardList boardList = data.getValue(boardList.class);
                    if(boardList.getKey().equals(listKey)){
                        boardsList.add(boardList);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "findList:onCancelled", databaseError.toException());
            }
        });

    }
}
