package com.zacharyharris.kodery.UI;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zacharyharris.kodery.Model.Board;
import com.zacharyharris.kodery.Model.ListofTasks;
import com.zacharyharris.kodery.Model.Task;
import com.zacharyharris.kodery.Model.Update;
import com.zacharyharris.kodery.Model.boardList;
import com.zacharyharris.kodery.R;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.R.id.message;
import static com.zacharyharris.kodery.UI.BoardMembersActivity.root;

public class SingleListActivity extends AppCompatActivity {
    public static Activity falist;

    public static final String TAG = "SingleListActivity";

    RecycleAdapter adapter;
    ArrayList<Task> taskList;

    private ListofTasks list;
    private Board board;
    private DatabaseReference mDatabase;

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
            if(task.getDescription() != null) {
                ((SimpleItemViewHolder) holder).subtitle.setText(task.getDescription());
            } else {
                ((SimpleItemViewHolder) holder).subtitle.setText(null);
            }
            // set number of members in each task
            if(task.getNumMembers() != null) {
                ((SimpleItemViewHolder) holder).memSubtitle.setText(task.getNumMembers() + " members");
            } else {
                ((SimpleItemViewHolder) holder).memSubtitle.setText("No Members");
            }
        }

        public final class SimpleItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
            TextView title;
            TextView subtitle;
            TextView memSubtitle;
            public int position;

            public SimpleItemViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
                title = (TextView) itemView.findViewById(R.id.task_name);
                subtitle = (TextView) itemView.findViewById(R.id.task_desc_subt);
                memSubtitle = (TextView) itemView.findViewById(R.id.task_mem_subt);
            }

            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: sdfkjgshljgh");
                Intent newIntent = new Intent(SingleListActivity.this, SingleTaskActivity.class);
                newIntent.putExtra("task", taskList.get(position));
                newIntent.putExtra("list", list);
                newIntent.putExtra("board", board);
                SingleListActivity.this.startActivity(newIntent);

            }

            @Override
            public boolean onLongClick(View v) {
                //////////////////////////////
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(v.getContext());
                View mview = LayoutInflater.from(v.getContext()).inflate(R.layout.edit_task_item, null);
                final EditText mboardname = (EditText) mview.findViewById(R.id.taskname_edit);
                final EditText mlistdesc = (EditText) mview.findViewById(R.id.taskdesc_edit);
                final Task test = taskList.get(position);
                final TextView mtextview = (TextView) mview.findViewById(R.id.edit_task_item_title);
                mtextview.setText("Edit "+test.getName());
                mboardname.setText(test.getName());
                mlistdesc.setText(test.getDescription());
                Button saveleboard = (Button) mview.findViewById(R.id.saveBoard);
                Button delboard = (Button) mview.findViewById(R.id.delBoard);

                saveleboard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!mboardname.getText().toString().isEmpty()
                                && !(mboardname.getText().toString().equals(test.getName())
                                && mlistdesc.getText().toString().equals(test.getDescription()))){
                            Toast.makeText(v.getContext(),
                                    mboardname.getText()+" saved!",
                                    Toast.LENGTH_SHORT).show();
                            //updateBoard(boardsList.get(position), mboardname.getText().toString());
                            // Get rid of the pop up go back to main activity
                        }else{
                            Toast t = Toast.makeText(v.getContext(),
                                    "Please change the name or description of "+test.getName(),
                                    Toast.LENGTH_LONG);
                            LinearLayout layout = (LinearLayout) t.getView();
                            if (layout.getChildCount() > 0) {
                                TextView tv = (TextView) layout.getChildAt(0);
                                tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                            }
                            t.show();
                        }
                    }
                });

                delboard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast t = Toast.makeText(v.getContext(),
                                mboardname.getText()+" deleted.",
                                Toast.LENGTH_LONG);
                        LinearLayout layout = (LinearLayout) t.getView();
                        if (layout.getChildCount() > 0) {
                            TextView tv = (TextView) layout.getChildAt(0);
                            tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                        }
                        t.show();
                        deleteTask(taskList.get(position));

                    }
                });

                mBuilder.setView(mview);
                AlertDialog dialog = mBuilder.create();
                dialog.show();

                return true;
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

        falist = this;

        android.support.v7.app.ActionBar mActionBar = getSupportActionBar();
        mActionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#abe9a1")));
        mActionBar.setTitle(board.getName()+" > "+list.getName());

        loadTaskFeed();

    }

    private void loadTaskFeed() {
        mDatabase = FirebaseDatabase.getInstance().getReference();

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

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference(root + "/lists/" + board.getBoardKey() + "/" + list.getKey() + "/tasks").addValueEventListener(new ValueEventListener() {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list_menu_item, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id){
            case R.id.add_item:
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(SingleListActivity.this);
                View mview = getLayoutInflater().inflate(R.layout.create_task_popup, null);
                final EditText mtaskname = (EditText) mview.findViewById(R.id.taskname_edit);
                final EditText mtaskdesc = (EditText) mview.findViewById(R.id.taskdesc_edit);
                Button addleboard = (Button) mview.findViewById(R.id.createTsk);

                addleboard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!mtaskname.getText().toString().isEmpty()){
                            Toast.makeText(SingleListActivity.this,
                                    mtaskname.getText()+" created!",
                                    Toast.LENGTH_SHORT).show();
                            /* CODE TO ADD NAME AND DESC OF LIST */
                            if(mtaskdesc.getText().toString().isEmpty()){
                                saveTask(mtaskname.getText().toString(), "No Description");
                            } else {
                                saveTask(mtaskname.getText().toString(), mtaskdesc.getText().toString());
                            }
                        }else{
                            Toast.makeText(SingleListActivity.this,
                                    "Please name the list.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });

                mBuilder.setView(mview);
                AlertDialog dialog = mBuilder.create();
                dialog.show();
                break;
/*
            case R.id.members_item:
                Intent i = new Intent(this, BoardMembersActivity.class);
                i.putExtra("board", board);
                startActivity(i);
                break;*/

        }

        return super.onOptionsItemSelected(item);
    }

    private void saveTask(String name, String desc) {
        //second section
        //save it to the firebase db
        String key = mDatabase.child(root).child("tasks").push().getKey();
        String updateKey = mDatabase.child(root).child(board.getBoardKey()).child("updates").push().getKey();

        Task task = new Task();
        task.setKey(key);
        task.setName(name);
        task.setBoard(board.getBoardKey());
        task.setDescription(desc);
        task.setList(list.getKey());

        String updateText = ("Task: " + name + " added to List: " + list.getName());
        update(updateText);

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(root + "/tasks/" + key, task.toFirebaseObject());
        mDatabase.updateChildren(childUpdates);

        mDatabase.child(root).child("lists").child(board.getBoardKey()).child(list.getKey()).child("tasks").child(key).setValue(true);

    }

    private void update(String updateText) {
        String key = mDatabase.child(root).child("updates").child(board.getBoardKey()).push().getKey();
/*
        Calendar calendar = Calendar.getInstance();
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        //String dateString = new SimpleDateFormat("dd/MM/yyy hh:mm aa").format(Calendar.getInstance().getTime());
        Log.w(TAG, currentDateTimeString);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyy hh:mm aa");
        String dateString = format.format(calendar.getTime());
*/
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

    private void findTask(final String taskKey) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference(root + "/tasks/" + taskKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Task task = dataSnapshot.getValue(Task.class);
                if(dataSnapshot.hasChild("members")) {
                    String numMembers = String.valueOf(dataSnapshot.child("members").getChildrenCount());
                    task.setNumMembers(numMembers);
                }
                taskList.add(task);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "findTask:onCancelled", databaseError.toException());
            }
        });
    }

    private void deleteTask(Task task) {
        // surround this with checks if user is admin or owner
        mDatabase.child(root).child("lists").child(board.getBoardKey()).child(list.getKey()).
                child("tasks").child(task.getKey()).removeValue();
        mDatabase.child(root).child("tasks").child(task.getKey()).removeValue();

        // Updates
        String updateText = ("Task:" + task.getName() + " deleted from List:" + list.getName());
        update(updateText);

        // reload the recycler view
        loadTaskFeed();
    }
    
    @Override
    public void onBackPressed(){
        //Go back to single board
        //moveTaskToBack(true);
        //Intent i = new Intent(this, SingleBoardActivity.class);
        //i.putExtra("board", board);
        //startActivity(i);
        super.onBackPressed();
    }
}
