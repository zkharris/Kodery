package com.zacharyharris.kodery.UI;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
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
import com.google.firebase.appindexing.Action;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.appindexing.builders.Actions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zacharyharris.kodery.Model.Board;
import com.zacharyharris.kodery.Model.Channel;
import com.zacharyharris.kodery.Model.Task;
import com.zacharyharris.kodery.Model.Update;
import com.zacharyharris.kodery.Model.User;
import com.zacharyharris.kodery.R;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BoardMembersActivity extends AppCompatActivity {

    public static final String TAG = "BoardMembersActivity";
    public static final String root = "testRoot";

    RecycleAdapter adapter;

    private Board board;
    private ArrayList<User> memberList;
    private boolean addMembersToTask;
    private boolean addMembersToChannel;
    private Task task;
    public User owner;
    private DatabaseReference mDatabase;
    private Channel channel;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        return Actions.newView("BoardMembers", "http://[ENTER-YOUR-URL-HERE]");
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
            Glide.with(BoardMembersActivity.this).load(user.getPhotoURL()).into(viewHolder.image);
            if(user.getUid().equals(board.getOwnerUid())) {
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
            public void onClick(View view) {
                Log.d(TAG, "Clicked user is " + memberList.get(position));

                AlertDialog.Builder myBuilder = new AlertDialog.Builder(BoardMembersActivity.this);
                final View myview = getLayoutInflater().inflate(R.layout.memberopt_popup, null);
                TextView mTV = (TextView) myview.findViewById(R.id.question_title);
                TextView mTVs = (TextView) myview.findViewById(R.id.question_text);
                Button makeAd = (Button) myview.findViewById(R.id.make_ans);
                Button demoteAd = (Button) myview.findViewById(R.id.demote_ans);
                Button removeM = (Button) myview.findViewById(R.id.remove_ans);
                mTV.setText("Actions for "+memberList.get(position).getUsername());
                mTVs.setText("Are you sure?");

                myBuilder.setView(myview);
                final AlertDialog mydialog = myBuilder.create();

                makeAd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(mFirebaseUser.getUid().equals(board.getOwnerUid()) ||
                                board.getAdmins().containsKey(mFirebaseUser.getUid())) {

                            if(!board.getAdmins().containsKey(memberList.get(position).getUid())){
                            makeAdmin(memberList.get(position));
                            Toast t = Toast.makeText(v.getContext(),
                                    memberList.get(position).getUsername() + " is now an Admin.",
                                    Toast.LENGTH_LONG);
                            LinearLayout layout = (LinearLayout) t.getView();
                            if (layout.getChildCount() > 0) {
                                TextView tv = (TextView) layout.getChildAt(0);
                                tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                            }
                            t.show();
                            mydialog.dismiss();

                            } else {
                                Toast t = Toast.makeText(v.getContext(),
                                        memberList.get(position).getUsername() + " is already an admin",
                                        Toast.LENGTH_LONG);
                                LinearLayout layout = (LinearLayout) t.getView();
                                if (layout.getChildCount() > 0) {
                                    TextView tv = (TextView) layout.getChildAt(0);
                                    tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                                }
                                t.show();
                                mydialog.dismiss();
                            }

                        } else {
                            Toast t = Toast.makeText(v.getContext(),
                                    "Only board owners and admins can make admins",Toast.LENGTH_LONG);
                            LinearLayout layout = (LinearLayout) t.getView();
                            if (layout.getChildCount() > 0) {
                                TextView tv = (TextView) layout.getChildAt(0);
                                tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                            }
                            t.show();
                            mydialog.dismiss();

                        }

                    }
                });

                demoteAd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(mFirebaseUser.getUid().equals(board.getOwnerUid())) {
                            if(board.getAdmins().containsKey(memberList.get(position).getUid())) {
                                removeAdmin(memberList.get(position));
                                Toast t = Toast.makeText(v.getContext(),
                                        memberList.get(position).getUsername() + " is no longer an Admin.",
                                        Toast.LENGTH_LONG);
                                LinearLayout layout = (LinearLayout) t.getView();
                                if (layout.getChildCount() > 0) {
                                    TextView tv = (TextView) layout.getChildAt(0);
                                    tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                                }
                                t.show();
                                mydialog.dismiss();
                            } else {
                                Toast t = Toast.makeText(v.getContext(),
                                        memberList.get(position).getUsername() + " is not an Admin.",
                                        Toast.LENGTH_LONG);
                                LinearLayout layout = (LinearLayout) t.getView();
                                if (layout.getChildCount() > 0) {
                                    TextView tv = (TextView) layout.getChildAt(0);
                                    tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                                }
                                t.show();
                                mydialog.dismiss();
                            }
                        }
                    }
                });

                removeM.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        AlertDialog.Builder my2Builder = new AlertDialog.Builder(BoardMembersActivity.this);
                        final View my2view = getLayoutInflater().inflate(R.layout.yes_no_popup, null);
                        TextView mTV = (TextView) my2view.findViewById(R.id.question_title);
                        TextView mTVs = (TextView) my2view.findViewById(R.id.question_text);
                        Button no = (Button) my2view.findViewById(R.id.no_ans);
                        Button yes = (Button) my2view.findViewById(R.id.yes_ans);
                        mTV.setText("Removing "+memberList.get(position).getUsername()+"...");
                        mTVs.setText("Are you sure?");

                        my2Builder.setView(my2view);
                        final AlertDialog my2dialog = my2Builder.create();

                        yes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //memberList.get(position).getUsername()+" removed from "+board.getName()+"."
                                if(!memberList.get(position).getUid().equals(board.getOwnerUid())) {
                                    Toast t = Toast.makeText(v.getContext(),
                                            memberList.get(position).getUsername()+" removed from "+board.getName()+".",
                                            Toast.LENGTH_LONG);
                                    kickUser(memberList.get(position));
                                    t.show();
                                } else {
                                    Toast t = Toast.makeText(v.getContext(),
                                            "Can't kick the owner",
                                            Toast.LENGTH_LONG);
                                }
                                my2dialog.dismiss();
                            }
                        });

                        no.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                my2dialog.dismiss();
                            }
                        });



                        my2dialog.show();
                        mydialog.dismiss();


                    }
                });

                mydialog.show();




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
            channel = (Channel)extras.get("channel");
            if (task != null) {
                addMembersToTask = true;
            }
            if (channel != null) {
                addMembersToChannel = true;
            }
        }

        // Initialize Database
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        memberList = new ArrayList<>();

        android.support.v7.app.ActionBar mActionBar = getSupportActionBar();
        ColorDrawable mColor = new ColorDrawable(Color.parseColor((board.getColor())));
        mActionBar.setBackgroundDrawable(mColor);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.memberRecycleView);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);
        adapter = new RecycleAdapter();
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        //loadFeed();
        memberList.clear();
        findOwner(board.getOwnerUid());
        // Peep reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
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

    private void loadFeed() {

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

    private void findOwner(final String ownerUid) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference(root + "/users/" + ownerUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User owner = dataSnapshot.getValue(User.class);
                Log.w(TAG, owner.getEmail());
                memberList.add(owner);
                adapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "findOwner:onCancelled", databaseError.toException());
            }
        });

    }

    private void makeAdmin(User user){
        mDatabase.child(root).child("boards").child(board.getBoardKey()).child("admins").
                child(user.getUid()).setValue(true);

        Log.w(TAG, "Admins are: " + String.valueOf(board.getAdmins()));

        String updateText = (user.getUsername() + " is now an Admin");
        update(updateText);
    }

    private void removeAdmin(User user) {
        mDatabase.child(root).child("boards").child(board.getBoardKey()).child("admins").
                child(user.getUid()).removeValue();

        Log.w(TAG, "Admins are: " + String.valueOf(board.getAdmins()));

        // no update text for this action
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


    private void addTaskMembers(Task task, User user) {
        mDatabase.child(root).child("tasks").child(task.getKey()).child("members").
                child(user.getUid()).setValue(true);
        //Intent to Single Task view
    }

    private void addChannelMembers(Channel channel, User user) {
        mDatabase.child(root).child("channels").child(board.getBoardKey()).child(channel.getKey())
                .child("peeps").child(user.getUid()).setValue(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.members_add_item, menu);
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
                Intent intent = new Intent(this, UsersActivity.class);
                intent.putExtra("board", board);
                startActivity(intent);
                break;

            case R.id.leave_item:
                AlertDialog.Builder myBuilder = new AlertDialog.Builder(BoardMembersActivity.this);
                final View myview = getLayoutInflater().inflate(R.layout.yes_no_popup, null);
                TextView mTV = (TextView) myview.findViewById(R.id.question_title);
                TextView mTVs = (TextView) myview.findViewById(R.id.question_text);
                Button no = (Button) myview.findViewById(R.id.no_ans);
                Button yes = (Button) myview.findViewById(R.id.yes_ans);
                mTV.setText("Leaving "+ board.getName()+"...");
                mTVs.setText("Are you sure?");

                myBuilder.setView(myview);
                final AlertDialog mydialog = myBuilder.create();

                yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!mFirebaseUser.getUid().equals(board.getOwnerUid())) {
                            leaveBoard(mFirebaseUser);
                            Toast.makeText(BoardMembersActivity.this,
                                    "You left " + board.getName() + ".",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(BoardMembersActivity.this,
                                    "Board owners cannot leave their own board",
                                    Toast.LENGTH_SHORT).show();
                        }
                        mydialog.dismiss();
                    }
                });

                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mydialog.dismiss();
                    }
                });

                mydialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void leaveBoard(FirebaseUser mFirebaseUser) {
        mDatabase.child(root).child("boards").child(board.getBoardKey()).
                child("peeps").child(mFirebaseUser.getUid()).removeValue();

        String updateText = (mFirebaseUser.getDisplayName() + " has left the board");
        update(updateText);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void kickUser(User user) {
        mDatabase.child(root).child("boards").child(board.getBoardKey()).
                child("peeps").child(user.getUid()).removeValue();

        memberList.remove(user);
        adapter.notifyDataSetChanged();
        // no update Text for this action
    }
}
