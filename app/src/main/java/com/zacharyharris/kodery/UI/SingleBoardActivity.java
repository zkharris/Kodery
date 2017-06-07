package com.zacharyharris.kodery.UI;

import android.content.Intent;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.appindexing.Action;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.appindexing.builders.Actions;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.zacharyharris.kodery.UI.BoardMembersActivity.root;

public class SingleBoardActivity extends AppCompatActivity {

    public static final String TAG = "SingleBoardActivity";

    ListsRecycleAdapter listAdapter;
    ArrayList<ListofTasks> boardsList;

    private Board board;
    private DatabaseReference mDatabase;

    private FirebaseUser mFirebaseUser;
    private Task task;
    private boolean addTaskToList;


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

        final TextView boardTitle = (TextView) findViewById(R.id.boardname_view);
        boardTitle.setText(board.getName());

        boardsList = new ArrayList<>();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        RecyclerView ListrecyclerView = (RecyclerView) findViewById(R.id.board_lists_recyclerView);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        ListrecyclerView.setLayoutManager(llm);
        listAdapter = new ListsRecycleAdapter();
        ListrecyclerView.setAdapter(listAdapter);

        listAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();

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

                addleboard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!mlistname.getText().toString().isEmpty()){
                            Toast.makeText(SingleBoardActivity.this,
                                    mlistname.getText()+" created!",
                                    Toast.LENGTH_SHORT).show();
                            /* CODE TO ADD NAME AND DESC OF LIST */
                            saveList(mlistname.getText().toString(), mlistdesc.getText().toString());
                        }else{
                            Toast.makeText(SingleBoardActivity.this,
                                    "Please name the list.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });

                mBuilder.setView(mview);
                AlertDialog dialog = mBuilder.create();
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
        String updateKey = mDatabase.child(root).child("updates").child(board.getBoardKey()).push().getKey();

        ListofTasks listoftasks = new ListofTasks();
        listoftasks.setKey(key);
        listoftasks.setBoard(board.getBoardKey());
        listoftasks.setName(name);
        listoftasks.setDescription(desc);

        String updateText = ("list: " + name + " added to board: " + board.getName());
        update(updateText);

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(root + "/lists/" + board.getBoardKey() + "/" + key, listoftasks.toFirebaseObject());
        mDatabase.updateChildren(childUpdates);

        mDatabase.child(root).child("boards").child(board.getBoardKey()).child("lists")
                .child(listoftasks.getKey()).setValue(true);


    }

    private void update(String updateText) {
        String key = mDatabase.child(root).child("updates").child(board.getBoardKey()).push().getKey();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyy hh:mm aa");
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
                ((SimpleItemViewHolder) holder).subtitle.setText(list.getNumTasks() + " tasks");
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
                final EditText mboardname = (EditText) mview.findViewById(R.id.listname_edit);
                final EditText mlistdesc = (EditText) mview.findViewById(R.id.listdesc_edit);
                final ListofTasks test = boardsList.get(position);
                final TextView mtextview = (TextView) mview.findViewById(R.id.edit_list_item_title);
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
                            //updateBoard(test, mboardname.getText().toString());
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
                        Toast.makeText(v.getContext(),
                                mboardname.getText()+" deleted.",
                                Toast.LENGTH_SHORT).show();
                        //deleteBoard(boardsList.get(position));

                    }
                });

                mBuilder.setView(mview);
                AlertDialog dialog = mBuilder.create();
                dialog.show();

                return true;
            }
        }
    }
}


