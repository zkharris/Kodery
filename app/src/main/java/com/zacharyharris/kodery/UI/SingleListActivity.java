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
import com.google.firebase.auth.FirebaseAuth;
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
import com.zacharyharris.kodery.Model.User;
import com.zacharyharris.kodery.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.zacharyharris.kodery.UI.BoardMembersActivity.root;

public class SingleListActivity extends AppCompatActivity {
    public static Activity falist;

    public static final String TAG = "SingleListActivity";
    public static final String root = "testRoot";

    RecycleAdapter adapter;
    ArrayList<Task> taskList;

    private ListofTasks list;
    private Board board;
    private DatabaseReference mDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    public int tap_num = 0;
    private ArrayList<User> memberList;
    private memberRecyclerAdapter memberAdapter;
    private Task currTask;

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
                if(task.getNumMembers().equals("1")){
                    ((SimpleItemViewHolder) holder).memSubtitle.setText(task.getNumMembers() + " member");
                }else {
                    ((SimpleItemViewHolder) holder).memSubtitle.setText(task.getNumMembers() + " members");
                }
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
                tap_num++;
                android.os.Handler mHandler = new android.os.Handler();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (tap_num == 1) {
                            Log.d(TAG, "onClick: sdfkjgshljgh");
                            Intent newIntent = new Intent(SingleListActivity.this, SingleTaskActivity.class);
                            newIntent.putExtra("task", taskList.get(position));
                            newIntent.putExtra("list", list);
                            newIntent.putExtra("board", board);
                            SingleListActivity.this.startActivity(newIntent);
                        } else if (tap_num == 2) {
                            currTask = taskList.get(position);
                            Log.d(TAG, "clickedTwice:" + taskList.get(position).getName());
                            AlertDialog.Builder mBuilder = new AlertDialog.Builder(SingleListActivity.this);
                            View mview = getLayoutInflater().inflate(R.layout.addto_task_popup, null);
                            TextView popupTitle = (TextView) mview.findViewById(R.id.invite_title);
                            popupTitle.setText("Add People to " + currTask.getName()+":");
                            Button doneb = (Button) mview.findViewById(R.id.finish_adding_btn);
                            RecyclerView memberrecyclerView = (RecyclerView) mview.findViewById(R.id.add_channel_RV);
                            LinearLayoutManager llm = new LinearLayoutManager(SingleListActivity.this);
                            memberrecyclerView.setLayoutManager(llm);
                            memberList = new ArrayList();
                            memberAdapter = new memberRecyclerAdapter();
                            memberrecyclerView.setAdapter(memberAdapter);
                            memberAdapter.notifyDataSetChanged();

                            loadMembers();

                            mBuilder.setView(mview);
                            final AlertDialog dialog = mBuilder.create();

                            doneb.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    loadTaskFeed();
                                    dialog.dismiss();
                                }
                            });
                            dialog.show();
                        }
                        tap_num = 0;
                    }
                }, 250);
            }



            @Override
            public boolean onLongClick(View v) {
                //////////////////////////////
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(v.getContext());
                View mview = LayoutInflater.from(v.getContext()).inflate(R.layout.edit_task_item, null);
                final EditText mtaskname = (EditText) mview.findViewById(R.id.taskname_edit);
                final EditText mtaskdesc = (EditText) mview.findViewById(R.id.taskdesc_edit);
                final Task test = taskList.get(position);
                final TextView mtextview = (TextView) mview.findViewById(R.id.edit_task_item_title);
                mtextview.setText("Edit "+test.getName());
                mtaskname.setText(test.getName());
                mtaskdesc.setText(test.getDescription());
                Button saveleboard = (Button) mview.findViewById(R.id.saveBoard);
                Button delboard = (Button) mview.findViewById(R.id.delBoard);
                mBuilder.setView(mview);
                final AlertDialog dialog = mBuilder.create();

                saveleboard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!mtaskname.getText().toString().isEmpty()
                                && !(mtaskname.getText().toString().equals(test.getName())
                                && mtaskdesc.getText().toString().equals(test.getDescription()))){
                            Toast.makeText(v.getContext(),
                                    mtaskname.getText()+" saved!",
                                    Toast.LENGTH_SHORT).show();
                            editTask(taskList.get(position), mtaskname.getText().toString(),
                                    mtaskdesc.getText().toString());
                            dialog.dismiss();
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
                        if(mFirebaseUser.getUid().equals(board.getOwnerUid()) ||
                                board.getAdmins().containsKey(mFirebaseUser.getUid())) {
                            Toast t = Toast.makeText(v.getContext(),
                                    mtaskname.getText() + " deleted.",
                                    Toast.LENGTH_SHORT);
                            LinearLayout layout = (LinearLayout) t.getView();
                            if (layout.getChildCount() > 0) {
                                TextView tv = (TextView) layout.getChildAt(0);
                                tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                            }
                            t.show();
                            deleteTask(taskList.get(position));
                            dialog.dismiss();
                            //deleteBoard(boardsList.get(position));
                        } else {
                            Toast t = Toast.makeText(v.getContext(),
                                    "Only board owners and admins can delete tasks",
                                    Toast.LENGTH_LONG);
                            LinearLayout layout = (LinearLayout) t.getView();
                            if (layout.getChildCount() > 0) {
                                TextView tv = (TextView) layout.getChildAt(0);
                                tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                            }
                            t.show();
                            dialog.dismiss();
                        }

                    }
                });

                dialog.show();

                return true;
            }
        }
    }

    class memberRecyclerAdapter extends RecyclerView.Adapter {

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
            Glide.with(SingleListActivity.this).load(user.getPhotoURL()).into(viewHolder.image);
            if (user.getUid().equals(board.getOwnerUid())) {
                // set an icon on the item user
            }
        }

        public final class SimpleItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView image;
            TextView title;
            ImageView ownerBadge;
            public int position;

            public SimpleItemViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                title = (TextView) itemView.findViewById(R.id.usr_name);
                image = (ImageView) itemView.findViewById(R.id.usr_pic);
                //ownerBadge = (ImageView) itemView.findViewById(R.id.owner_badge);
            }

            @Override
            public void onClick(View v) {
                mDatabase.child(root).child("tasks").child(currTask.getKey()).
                        child("members").child(memberList.get(position).getUid()).
                        setValue(true);

                Toast.makeText(SingleListActivity.this,
                        memberList.get(position).getUsername()+" added to Task: "
                                + currTask.getName(),
                        Toast.LENGTH_SHORT).show();

                String updateText = (memberList.get(position).getUsername() + " added to Task: " +
                    currTask.getName());
                update(updateText);

            }
        }

    }

    private void loadMembers() {
        // Member feed
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference(root + "/boards/" + board.getBoardKey()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                memberList.clear();
                //findOwner(String.valueOf(dataSnapshot.child("ownerUid").getValue()));
                DataSnapshot peepsRef = dataSnapshot.child("peeps");
                for(DataSnapshot data : peepsRef.getChildren()) {
                    findUser(String.valueOf(data.getKey()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "memberFeed:onCancelled", databaseError.toException());
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
                memberAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "findUser:onCancelled", databaseError.toException());

            }
        });
    }

    /*private void findOwner(String ownerUid) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference(root + "/users/" + ownerUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User owner = dataSnapshot.getValue(User.class);
                memberList.add(owner);
                memberAdapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "findOwner:onCancelled", databaseError.toException());
            }
        });
    }*/

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

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        android.support.v7.app.ActionBar mActionBar = getSupportActionBar();
        ColorDrawable mColor = new ColorDrawable(Color.parseColor((board.getColor())));
        mActionBar.setBackgroundDrawable(mColor);
        //mActionBar.setTitle(board.getName()+" > "+list.getName());

        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setDisplayShowCustomEnabled(true);
        View customView = getLayoutInflater().inflate(R.layout.actionbar_title, null);
        TextView customTitle = (TextView) customView.findViewById(R.id.actionbarTitle);
        customTitle.setText(board.getName()+" > "+list.getName());
        customTitle.setTextSize(20);
        ImageView customImage = (ImageView) customView.findViewById(R.id.actionbarImage);
        customImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                SingleBoardActivity.faboard.finish();
            }
        });
        mActionBar.setCustomView(customView);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        final TextView listTitle = (TextView) findViewById(R.id.list_name);
        listTitle.setText(list.getName());

        final TextView listDesc = (TextView) findViewById(R.id.list_desc);
        listDesc.setText(list.getDescription());

        taskList = new ArrayList<>();
        memberList = new ArrayList<>();

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.tasks_list);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);
        adapter = new RecycleAdapter();
        recyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged();

        loadTaskFeed();

        //Check if User is kicked
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference(root + "/boards/" + board.getBoardKey()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!board.getOwnerUid().equals(mFirebaseUser.getUid())) {
                    if (!dataSnapshot.child("peeps").hasChild(mFirebaseUser.getUid())) {
                        Intent intent = new Intent(SingleListActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "kickUser:onCancelled", databaseError.toException());
            }
        });

        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    private void loadTaskFeed() {
        final RecyclerView recyclerView = (RecyclerView)findViewById(R.id.tasks_list);
        final TextView taskIndicator = (TextView)findViewById(R.id.task_indicator);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference(root + "/tasks").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                taskList.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Log.w(TAG, String.valueOf(data.getKey()));
                    Task task = data.getValue(Task.class);
                    if(task.getList().equals(list.getKey())) {
                        if(data.hasChild("members")){
                            String numMembers = String.valueOf(data.child("members").getChildrenCount());
                            Log.w(TAG, numMembers);
                            task.setNumMembers(numMembers);
                        }
                        taskList.add(task);
                    }
                }
                adapter.notifyDataSetChanged();

                if(taskList.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    taskIndicator.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    taskIndicator.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getList:onCancelled", databaseError.toException());
            }
        });

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

        Log.w(TAG, "Admins are: " + String.valueOf(board.getAdmins()));
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
                addleboard.setBackgroundColor(Color.parseColor(board.getColor()));
                mBuilder.setView(mview);
                final AlertDialog dialog = mBuilder.create();

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
                            dialog.dismiss();
                        }else{
                            Toast.makeText(SingleListActivity.this,
                                    "Please name the task.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });

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
        String key = mDatabase.child(root).child("tasks").child(list.getKey()).push().getKey();
        String updateKey = mDatabase.child(root).child(board.getBoardKey()).child("updates").push().getKey();

        Task task = new Task();
        task.setKey(key);
        task.setName(name);
        task.setBoard(board.getBoardKey());
        task.setDescription(desc);
        task.setList(list.getKey());

        String updateText = ("Added Task: " + name + " to List: " + list.getName());
        update(updateText);

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(root + "/tasks/" + key, task.toFirebaseObject());
        mDatabase.updateChildren(childUpdates);

        mDatabase.child(root).child("lists").child(board.getBoardKey()).child(list.getKey()).child("tasks").child(key).setValue(true);

        loadTaskFeed();
    }

    private void editTask(Task task, String name, String desc) {
        task.setName(name);
        task.setDescription(desc);

        String updateText = null;
        if (!task.getName().equals(name)) {
            updateText = ("Task: " + task.getName() + " renamed " + name + " in List:" +
                    list.getName());
        }

        if (!task.getDescription().equals(desc)) {
            updateText = ("Task: " + task.getName() + " description " +
                    "changed in List:" + list.getName());
        }

        if(!task.getDescription().equals(desc) && !task.getName().equals(name)){
            updateText = ("Task: " + task.getName() + " renamed to " + name +
                    " and description changed in List: " + list.getName());
        }
        update(updateText);

        mDatabase.child(root).child("tasks").child(task.getKey()).child("name").setValue(name);
        mDatabase.child(root).child("tasks").child(task.getKey()).child("description").setValue(desc);

        loadTaskFeed();
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

    private void findTask(final String taskKey) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference(root + "/tasks/" + taskKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Task task = dataSnapshot.getValue(Task.class);
                if(dataSnapshot.hasChild("members")) {
                    Log.w(TAG, "has members");
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
