package com.zacharyharris.kodery.UI;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.appindexing.Action;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.appindexing.builders.Actions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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
import java.util.Locale;
import java.util.Map;

import static com.zacharyharris.kodery.UI.BoardMembersActivity.root;

public class SingleBoardActivity extends AppCompatActivity {

    public static final String TAG = "SingleBoardActivity";

    ListsRecycleAdapter listAdapter;
    UpdatesRecycleAdapter updateAdapter;
    ArrayList<ListofTasks> boardsList;
    ArrayList<Update> updateList;

    private Board board;
    private DatabaseReference mDatabase;
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth mFirebaseAuth;

    private Task task;
    private boolean addTaskToList;

    LinearLayout mll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_board);

        if (getIntent().getExtras() != null) {
            Bundle extras = getIntent().getExtras();
            board = (Board) extras.get("board");
            task = (Task) extras.get("task");
            if (task != null) {
                addTaskToList = true;
            }
        }
        android.support.v7.app.ActionBar mActionBar = getSupportActionBar();
        ColorDrawable mColor = new ColorDrawable(Color.parseColor((board.getColor())));
        mActionBar.setBackgroundDrawable(mColor);
        mActionBar.setTitle(board.getName());


        final TextView boardTitle = (TextView) findViewById(R.id.boardname_view);
        boardTitle.setText(board.getName());

        boardsList = new ArrayList<>();
        updateList = new ArrayList<>();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        LinearLayoutManager listsllm = new LinearLayoutManager(this);
        RecyclerView ListrecyclerView = (RecyclerView) findViewById(R.id.board_lists_recyclerView);
        ListrecyclerView.setLayoutManager(listsllm);
        listAdapter = new ListsRecycleAdapter();
        ListrecyclerView.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();

        LinearLayoutManager updatesllm = new LinearLayoutManager(this);
        updatesllm.setReverseLayout(true);
        updatesllm.setStackFromEnd(true);
        RecyclerView UpdaterecyclerView = (RecyclerView) findViewById(R.id.board_update_recyclerView);
        UpdaterecyclerView.setLayoutManager(updatesllm);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(UpdaterecyclerView.getContext(),
                updatesllm.getOrientation());
        UpdaterecyclerView.addItemDecoration(dividerItemDecoration);
        updateAdapter = new UpdatesRecycleAdapter();
        UpdaterecyclerView.setAdapter(updateAdapter);
        updateAdapter.notifyDataSetChanged();

        Log.w(TAG, "Admins are: " + String.valueOf(board.getAdmins()));

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Lists Feed
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference(root + "/lists/" + board.getBoardKey()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boardsList.clear();
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

        // Updates Feed
        database.getReference(root + "/updates/" + board.getBoardKey()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                updateList.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Update update = data.getValue(Update.class);
                    updateList.add(update);
                }
                updateAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getUpdates:onCancelled", databaseError.toException());
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

        //Log.w(TAG, "Admins are: " + String.valueOf(board.getAdmins()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.board_menu_item, menu);
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
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(SingleBoardActivity.this);
                View mview = getLayoutInflater().inflate(R.layout.create_list_popup, null);
                final EditText mlistname = (EditText) mview.findViewById(R.id.listname_edit);
                final EditText mlistdesc = (EditText) mview.findViewById(R.id.listdesc_edit);
                Button addleboard = (Button) mview.findViewById(R.id.createLst);

                mBuilder.setView(mview);
                final AlertDialog dialog = mBuilder.create();

                addleboard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!mlistname.getText().toString().isEmpty()){
                            Toast.makeText(SingleBoardActivity.this,
                                    mlistname.getText()+" created!",
                                    Toast.LENGTH_SHORT).show();
                            /* CODE TO ADD NAME AND DESC OF LIST */
                            if(mlistdesc.getText().toString().isEmpty()){
                                saveList(mlistname.getText().toString(), "No Description");
                            }else {
                                saveList(mlistname.getText().toString(), mlistdesc.getText().toString());
                            }
                            dialog.dismiss();

                        }else{
                            Toast.makeText(SingleBoardActivity.this,
                                    "Please name the list.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });

                dialog.show();
                break;

            case R.id.chat_item:
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra("board", board);
                startActivity(intent);
                break;

            case R.id.members_item:
                Intent i = new Intent(this, BoardMembersActivity.class);
                i.putExtra("board", board);
                startActivity(i);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveList(String name, String desc) {
        String key = mDatabase.child(root).child("lists").push().getKey();

        ListofTasks listoftasks = new ListofTasks();
        listoftasks.setKey(key);
        listoftasks.setBoard(board.getBoardKey());
        listoftasks.setName(name);
        listoftasks.setDescription(desc);

        String updateText = ("Added List: " + name);
        update(updateText);

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(root + "/lists/" + board.getBoardKey() + "/" + key, listoftasks.toFirebaseObject());
        mDatabase.updateChildren(childUpdates);

        mDatabase.child(root).child("boards").child(board.getBoardKey()).child("lists")
                .child(listoftasks.getKey()).setValue(true);
    }

    private void editList(ListofTasks list, String name, String desc) {
        ListofTasks newList = new ListofTasks();
        newList.setKey(list.getKey());
        newList.setBoard(board.getBoardKey());
        newList.setName(name);
        newList.setDescription(desc);

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(root + "/lists/" + board.getBoardKey() + "/" + list.getKey(),
                newList.toFirebaseObject());
        mDatabase.updateChildren(childUpdates);

        String updateText = null;
        if(!name.equals(list.getName())) {
            updateText = ("List: " + list.getName() + " renamed to " + name);
        }
        if(!desc.equals(list.getDescription())) {
            updateText = ("List: " + list.getName() + " description changed");
        }

        if(!name.equals(list.getName()) && !desc.equals(list.getDescription())) {
            updateText = ("List: " + list.getName() + " renamed to " + name
                    + " and description changed" );
        }
        update(updateText);
    }

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

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        return Actions.newView("SingleBoard", "http://[ENTER-YOUR-URL-HERE]");
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        FirebaseUserActions.getInstance().start(getIndexApiAction());
    }

    @Override
    public void onStop() {

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        FirebaseUserActions.getInstance().end(getIndexApiAction());
        super.onStop();
    }

    class ListsRecycleAdapter extends RecyclerView.Adapter {


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
            ListofTasks list = boardsList.get(position);
            ((SimpleItemViewHolder) holder).title.setText(list.getName());
            // set number of todos in each list
            if(list.getNumTasks() != null) {
                if(list.getNumTasks().toString().equals("1")){
                    ((SingleBoardActivity.ListsRecycleAdapter.SimpleItemViewHolder) holder).subtitle.setText(list.getNumTasks() + " task");
                }else {
                    ((SingleBoardActivity.ListsRecycleAdapter.SimpleItemViewHolder) holder).subtitle.setText(list.getNumTasks() + " tasks");
                }
            } else {
                ((SimpleItemViewHolder) holder).subtitle.setText("No tasks");
            }
        }

        public final class SimpleItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener {
            TextView title;
            TextView subtitle;
            public int position;

            public SimpleItemViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
                title = (TextView) itemView.findViewById(R.id.list_name);
                subtitle = (TextView) itemView.findViewById(R.id.list_subt);
            }

            @Override
            public void onClick(View view) {
                if(addTaskToList){
                    mDatabase.child(root).child("lists").child(boardsList.get(position).getKey()).child("tasks")
                            .child(task.getKey()).setValue(true);
                    mDatabase.child(root).child("tasks").child(task.getKey()).child("list")
                            .setValue(boardsList.get(position).getKey());
                    Toast.makeText(view.getContext(), "Task: " + task.getName() +
                            " moved to List: " + boardsList.get(position).getName(),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(SingleBoardActivity.this, SingleListActivity.class);
                    intent.putExtra("list", boardsList.get(position));
                    intent.putExtra("board", board);
                    SingleBoardActivity.this.startActivity(intent);
                }
            }

            @Override
            public boolean onLongClick(View v) {

                AlertDialog.Builder mBuilder = new AlertDialog.Builder(v.getContext());
                View mview = LayoutInflater.from(v.getContext()).inflate(R.layout.edit_list_item, null);
                final EditText mlistname = (EditText) mview.findViewById(R.id.listname_edit);
                final EditText mlistdesc = (EditText) mview.findViewById(R.id.listdesc_edit);
                final ListofTasks test = boardsList.get(position);
                final TextView mtextview = (TextView) mview.findViewById(R.id.edit_list_item_title);
                mtextview.setText("Edit "+test.getName());
                mlistname.setText(test.getName());
                mlistdesc.setText(test.getDescription());
                Button saveleboard = (Button) mview.findViewById(R.id.saveBoard);
                Button delboard = (Button) mview.findViewById(R.id.delBoard);

                mBuilder.setView(mview);
                final AlertDialog dialog = mBuilder.create();

                saveleboard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!mlistname.getText().toString().isEmpty()
                                && !(mlistname.getText().toString().equals(test.getName())
                                && mlistdesc.getText().toString().equals(test.getDescription()))) {
                            editList(boardsList.get(position), mlistname.getText().toString(),
                                    mlistdesc.getText().toString());
                            Toast t = Toast.makeText(v.getContext(),
                                    mlistname.getText() + " saved!",
                                    Toast.LENGTH_SHORT);
                            LinearLayout layout = (LinearLayout) t.getView();
                            if (layout.getChildCount() > 0) {
                                TextView tv = (TextView) layout.getChildAt(0);
                                tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                            }
                            t.show();
                            dialog.dismiss();
                                //updateBoard(test, mboardname.getText().toString());
                                // Get rid of the pop up go back to main activity
                        } else {
                            Toast t = Toast.makeText(v.getContext(),
                                    "Please change the name or description of " + test.getName(),
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
                                    mlistname.getText() + " deleted.",
                                    Toast.LENGTH_LONG);
                            LinearLayout layout = (LinearLayout) t.getView();
                            if (layout.getChildCount() > 0) {
                                TextView tv = (TextView) layout.getChildAt(0);
                                tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                            }
                            t.show();
                            dialog.dismiss();
                            deleteList(boardsList.get(position));
                        } else {
                            Toast t = Toast.makeText(v.getContext(),
                                    "Only board owners and admins can delete Lists",
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

    public void deleteList(final ListofTasks list) {
        String updateText = ("List: " + list.getName() + " deleted");
        update(updateText);

        // Delete Tasks
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference(root + "/tasks").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Task task = data.getValue(Task.class);
                    if(task.getList().equals(list.getKey())) {
                        mDatabase.child(root).child("tasks").child(task.getKey()).removeValue();
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "deleteList:onCancelled", databaseError.toException());
            }
        });

        // Delete List
        mDatabase.child(root).child("lists").child(board.getBoardKey()).
                child(list.getKey()).removeValue();
        mDatabase.child(root).child("boards").child(board.getBoardKey()).
                child("lists").child(list.getKey()).removeValue();

    }

    class UpdatesRecycleAdapter extends RecyclerView.Adapter {


        @Override
        public int getItemCount() {
            return updateList.size();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_update, parent, false);
            SimpleItemViewHolder pvh = new SimpleItemViewHolder(v);
            return pvh;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            SimpleItemViewHolder viewHolder = (SimpleItemViewHolder) holder;
            viewHolder.position = position;
            Update update = updateList.get(position);
            ((SimpleItemViewHolder) holder).name.setText(update.getText());
            ((SimpleItemViewHolder) holder).date.setText(update.getDate());
        }

        public final class SimpleItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView name;
            TextView date;
            public int position;

            public SimpleItemViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                name = (TextView) itemView.findViewById(R.id.update_name);
                date = (TextView) itemView.findViewById(R.id.update_date);
            }

            @Override
            public void onClick(View v) {
                AlertDialog.Builder mBuilderup = new AlertDialog.Builder(v.getContext());
                View mviewup = LayoutInflater.from(v.getContext()).inflate(R.layout.update_popup, null);
                Update mupdate=updateList.get(position);
                TextView tvn = (TextView) mviewup.findViewById(R.id.update_name_pp);
                TextView tvd = (TextView) mviewup.findViewById(R.id.update_date_pp);
                tvn.setText(mupdate.getText());
                String day="";
                String time="";
                String space = " ";
                int i;
                int j=0;
                for(i=0; i<mupdate.getDate().length(); i++){
                    if(String.valueOf(mupdate.getDate().charAt(i)).equals(space)){
                        j++;
                    }
                    if(j<3) {
                        day += mupdate.getDate().charAt(i);
                    } else{
                        time += mupdate.getDate().charAt(i);
                    }
                }
                tvd.setText(day+" at"+time+":");

                mBuilderup.setView(mviewup);
                AlertDialog dialog = mBuilderup.create();
                dialog.show();
            }
        }
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }

}


