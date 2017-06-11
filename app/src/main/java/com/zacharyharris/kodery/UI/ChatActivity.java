package com.zacharyharris.kodery.UI;
//c6ffed

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.zacharyharris.kodery.Model.Board;
import com.zacharyharris.kodery.Model.Channel;
import com.zacharyharris.kodery.Model.Message;
import com.zacharyharris.kodery.Model.Update;
import com.zacharyharris.kodery.Model.User;
import com.zacharyharris.kodery.R;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.LogRecord;

import static com.zacharyharris.kodery.R.id.item_touch_helper_previous_elevation;
import static com.zacharyharris.kodery.R.id.message_edit;
import static com.zacharyharris.kodery.R.id.saveChannel;
import static com.zacharyharris.kodery.UI.BoardMembersActivity.root;

public class ChatActivity extends AppCompatActivity {

    private String TAG = "ChatActivity";
    private DatabaseReference mDatabase;
    public Board board;
    int tap_num = 0;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    ArrayList<Message> messageList;
    MessageRecycleAdapter messageAdapter;
    public Channel currChannel;
    ChannelRecycleAdapter channelAdapter;
    ArrayList<Channel> channelList;
    private Channel generalChannel;

    memberRecyclerAdapter memberAdapter;
    private ArrayList<User> memberList;

    class ChannelRecycleAdapter extends RecyclerView.Adapter {

        @Override
        public int getItemCount() {
            return channelList.size();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_channel, parent, false);
            SimpleItemViewHolder pvh = new SimpleItemViewHolder(v);
            return pvh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            SimpleItemViewHolder viewHolder = (SimpleItemViewHolder) holder;
            viewHolder.position = position;
            Channel channel = channelList.get(position);
            ((SimpleItemViewHolder) holder).title.setText("#" + channel.getName());
            if(channel.getType().equals("private")) {
                ((SimpleItemViewHolder) holder).privC.setVisibility(View.VISIBLE);
            }

        }

        public final class SimpleItemViewHolder extends ViewHolder implements View.OnClickListener,View.OnLongClickListener {
            TextView title;
            int position;
            //CardView mCV;
            ImageView privC;

            public SimpleItemViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
                title = (TextView) itemView.findViewById(R.id.channel_name);
                //mCV = (CardView) itemView.findViewById(R.id.channel_cards);
                privC = (ImageView) itemView.findViewById(R.id.priv_channel);
            }

            @Override
            public void onClick(View v) {
                // Insert loading animations here
                tap_num++;
                android.os.Handler mHandler = new android.os.Handler();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(tap_num==1){
                            Log.w(TAG, "clicked channel:" + channelList.get(position).getName());
                            currChannel = channelList.get(position);
                            Log.w(TAG,  "current channel:" + currChannel.getName());
                            channelFeed();

                            android.support.v7.app.ActionBar mActionBar = getSupportActionBar();
                            if(channelList.get(position).getType().equals("public")) {
                                mActionBar.setTitle(board.getName() + " #" + channelList.get(position).getName() + " Public Chat");
                            } else{
                                mActionBar.setTitle(board.getName() + " #" + channelList.get(position).getName() + " Private Chat");
                            }

                        } else if (tap_num==2){
                            if(channelList.get(position).getType().equals("private")) {
                                //Toast.makeText(ChatActivity.this, "double clicked", Toast.LENGTH_SHORT).show();

                                AlertDialog.Builder mBuilder = new AlertDialog.Builder(ChatActivity.this);
                                View mview = getLayoutInflater().inflate(R.layout.addto_channel_popup, null);
                                Button doneb = (Button) mview.findViewById(R.id.finish_adding_btn);
                                RecyclerView memberrecyclerView = (RecyclerView) mview.findViewById(R.id.add_channel_RV);
                                LinearLayoutManager llm = new LinearLayoutManager(ChatActivity.this);
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
                                        dialog.dismiss();
                                    }
                                });

                                dialog.show();
                            }

                        }
                        tap_num=0;
                    }
                }, 500);
            }

            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(ChatActivity.this, "long clicked", Toast.LENGTH_SHORT).show();

                /* THIS TAKES YOU TO EDIT POPUP FOR CHANNEL */
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(ChatActivity.this);
                View mview = getLayoutInflater().inflate(R.layout.edit_channel, null);
                final EditText mboardname = (EditText) mview.findViewById(R.id.Channelname);
                final TextView mtitle = (TextView) mview.findViewById(R.id.edit_channel_tag);
                Button saveleboard = (Button) mview.findViewById(R.id.saveChannel);
                Button delboard = (Button) mview.findViewById(R.id.delChannel);
                final Channel mchannel= channelList.get(position);
                mtitle.setText("Rename "+mchannel.getName());
                mboardname.setText(mchannel.getName());
                mBuilder.setView(mview);
                final AlertDialog dialog = mBuilder.create();

                saveleboard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                            if(mFirebaseUser.getUid().equals(board.getOwnerUid()) ||
                                    board.getAdmins().containsKey(mFirebaseUser.getUid())) {
                                if(!mboardname.getText().toString().isEmpty()
                                        || !(mboardname.getText().toString().equals(mchannel.getName()))) {
                                    Toast.makeText(ChatActivity.this,
                                            mchannel.getName() + " renamed to " + mboardname.getText() + "!",
                                            Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    editChannel(channelList.get(position), mboardname.getText().toString());
                                    // Get rid of the pop up go back to main activity
                                } else{
                                    Toast.makeText(ChatActivity.this,
                                            "Please rename the channel.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(ChatActivity.this,
                                        "Only owners and admins can edit channels",
                                        Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                            }
                        }
                });

                delboard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(v.getContext(),
                                "#"+mchannel.getName()+" deleted.",
                                Toast.LENGTH_SHORT).show();
                        dialog.dismiss();

                    }
                });

                dialog.show();

                return true;
            }
        }

    }

    class MessageRecycleAdapter extends RecyclerView.Adapter {

        @Override
        public int getItemCount() {
            return messageList.size();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
            SimpleItemViewHolder pvh = new SimpleItemViewHolder(v);
            return pvh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            SimpleItemViewHolder viewHolder = (SimpleItemViewHolder) holder;
            viewHolder.position = position;
            Message message = messageList.get(position);
            ((SimpleItemViewHolder) holder).author.setText(message.getAuthor());
            ((SimpleItemViewHolder) holder).text.setText(message.getText());
            Glide.with(ChatActivity.this).load(message.getMessagePhotoURL()).into(viewHolder.image);
        }

        public final class SimpleItemViewHolder extends ViewHolder implements View.OnClickListener {
            TextView text;
            TextView author;
            ImageView image;
            public int position;

            public SimpleItemViewHolder(View itemView) {
                super(itemView);
                text = (TextView) itemView.findViewById(R.id.message_text);
                author = (TextView) itemView.findViewById(R.id.prof_name);
                image = (ImageView) itemView.findViewById(R.id.prof_pic);
            }

            @Override
            public void onClick(View v) {
                Log.d(TAG, "text: " + messageList.get(position).getText());
                Log.d(TAG, "key: " + messageList.get(position).getMessageKey());
            }
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        if(getIntent().getExtras() != null) {
            Bundle extras = getIntent().getExtras();
            board = (Board)extras.get("board");
        }

        messageList = new ArrayList<>();
        channelList = new ArrayList<>();
        memberList = new ArrayList<>();

        android.support.v7.app.ActionBar mActionBar = getSupportActionBar();
        ColorDrawable mColor = new ColorDrawable(Color.parseColor((board.getColor())));
        mActionBar.setBackgroundDrawable(mColor);
        mActionBar.setTitle(board.getName()+" Chats");
/*

*/
        //Message recycler View
        RecyclerView messagerecyclerView = (RecyclerView)findViewById(R.id.messageRecycleView);
        LinearLayoutManager mllm = new LinearLayoutManager(this);
        mllm.setStackFromEnd(true);
        messagerecyclerView.setLayoutManager(mllm);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(messagerecyclerView.getContext(),
                mllm.getOrientation());
        messagerecyclerView.addItemDecoration(dividerItemDecoration);
        messageAdapter = new MessageRecycleAdapter();
        messagerecyclerView.setAdapter(messageAdapter);
        messageAdapter.notifyDataSetChanged();

        //Channel recycler View
        RecyclerView channelrecyclerView = (RecyclerView)findViewById(R.id.channelRecycleView);
        LinearLayoutManager cllm = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        channelrecyclerView.setLayoutManager(cllm);
        channelAdapter = new ChannelRecycleAdapter();
        channelrecyclerView.setAdapter(channelAdapter);

        final EditText editText = (EditText) findViewById(message_edit);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    if(currChannel != null) {
                        sendMessage(editText.getText().toString());
                        handled = true;
                        //Clears the keyboard and hides it when enter is pressed
                        editText.setText("");
                        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        mgr.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                    } else {
                        Toast.makeText(v.getContext(), "Please select a channel",
                                Toast.LENGTH_LONG).show();
                    }
                }
                return handled;
            }
        });

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        FirebaseDatabase database = FirebaseDatabase.getInstance();

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

        // Channel List
        database.getReference(root + "/channels/" + board.getBoardKey()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                channelList.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Channel channel = data.getValue(Channel.class);
                    if (channel.getType().equals("private") && data.child("peeps").
                            child(mFirebaseUser.getUid()).getValue() != null) {
                        channelList.add(channel);
                    }
                    if(channel.getType().equals("public")) {
                        channelList.add(channel);
                    }
                }
                channelAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getChannels:onCancelled", databaseError.toException());
            }
        });

    }

    private void loadMembers() {
        // Member feed
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference(root + "/boards/" + board.getBoardKey()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                memberList.clear();
                findOwner(String.valueOf(dataSnapshot.child("ownerUid").getValue()));
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

    private void findOwner(String ownerUid) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference(root + "/users/" + ownerUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User owner = dataSnapshot.getValue(User.class);
                Log.w(TAG, owner.getEmail());
                memberList.add(owner);
                memberAdapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "findOwner:onCancelled", databaseError.toException());
            }
        });
    }

    private void sendMessage(String text) {
        Log.w(TAG, "message sent");

        String key = mDatabase.child(root).child("message").child(board.getBoardKey()).child("messages").push().getKey();

        Message message = new Message();
        message.setMessageKey(key);
        message.setAuthor(mFirebaseUser.getDisplayName());
        message.setAuthorUid(mFirebaseUser.getUid());
        message.setMessagePhotoURL(mFirebaseUser.getPhotoUrl().toString());
        message.setText(text);

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(root + "/channels/" + board.getBoardKey() + "/" + currChannel.getKey() + "/messages/" + key, message.toFirebaseObject());
        mDatabase.updateChildren(childUpdates);
    }

    private void channelFeed() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference(root + "/channels/" + board.getBoardKey() + "/" + currChannel.getKey() + "/messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                messageList.clear();
                for(DataSnapshot data : dataSnapshot.getChildren()) {
                    Message message = data.getValue(Message.class);
                    messageList.add(message);
                    Log.d(TAG, message.getText() + " added");
                }
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "messageRef:onCancelled", databaseError.toException());
            }
        });

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
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(ChatActivity.this);
                View mview = getLayoutInflater().inflate(R.layout.create_channel_popup, null);
                final EditText mboardname = (EditText) mview.findViewById(R.id.Channelname);
                Button addleboard = (Button) mview.findViewById(R.id.createChannel);
                final ToggleButton mswitch = (ToggleButton) mview.findViewById(R.id.switchtog);
               // mswitch.set
                mBuilder.setView(mview);
                final AlertDialog dialog = mBuilder.create();

                addleboard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!mboardname.getText().toString().isEmpty()){
                            if(!mswitch.isChecked()) {
                                savePublicChannel(mboardname.getText().toString());
                                Toast.makeText(ChatActivity.this,
                                        mboardname.getText()+" created!",
                                        Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } else {
                                if(mFirebaseUser.getUid().equals(board.getOwnerUid()) ||
                                        board.getAdmins().containsKey(mFirebaseUser.getUid())) {
                                    savePrivateChannel(mboardname.getText().toString());
                                    Toast.makeText(ChatActivity.this,
                                            mboardname.getText()+" created!",
                                            Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();

                                } else {
                                    Toast.makeText(ChatActivity.this,
                                            "Only board owners and admins can make a private channel",
                                            Toast.LENGTH_LONG).show();
                                    dialog.dismiss();
                                }
                            }

                            //mswitch.isChecked();
                            // check if admin if switch is set to private
                            // Get rid of the pop up go back to main activity

                        }else{
                            Toast.makeText(ChatActivity.this,
                                    "Please add a channel.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                dialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void savePublicChannel(String name) {
        String key = mDatabase.child(root).child("channels").child(board.getBoardKey()).push().getKey();

        Channel channel = new Channel();
        channel.setName(name);
        channel.setKey(key);
        channel.setType("public");

        String updateText = ("Channel:" + name + " created");
        update(updateText);

        Map<String, Object> channelUpdates = new HashMap<>();
        channelUpdates.put(root + "/channels/" + "/" + board.getBoardKey() + "/" +key,
                channel.toFirebaseObject());
        mDatabase.updateChildren(channelUpdates);

    }



    private void savePrivateChannel(String name) {
        String key = mDatabase.child(root).child("channels").child(board.getBoardKey()).
                push().getKey();

        Channel channel = new Channel();
        channel.setName(name);
        channel.setKey(key);
        channel.setType("private");

        Map<String, Object> channelUpdates = new HashMap<>();
        channelUpdates.put(root + "/channels/" + board.getBoardKey() + "/" +key,
                channel.toFirebaseObject());
        mDatabase.updateChildren(channelUpdates);

        mDatabase.child(root).child("channels").child(board.getBoardKey()).child(key).
                child("peeps").child(mFirebaseUser.getUid()).setValue(true);
    }

    private void editChannel(Channel channel, String name) {
        Channel newChannel = new Channel();
        channel.setName(name);
        channel.setKey(channel.getKey());
        channel.setType(channel.getType());

        Map<String, Object> channelUpdates = new HashMap<>();
        channelUpdates.put(root + "/channels/" + board.getBoardKey() + "/" + channel.getKey(),
                channel.toFirebaseObject());
        mDatabase.updateChildren(channelUpdates);

        if(channel.getType().equals("public")){
            String updateText = ("Channel:" + channel.getName() + " renamed to " + name);
            update(updateText);
        }
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



    class memberRecyclerAdapter extends RecyclerView.Adapter {

        @Override
        public int getItemCount() {
            return memberList.size();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
            SimpleItemViewHolder pvh = new SimpleItemViewHolder(v);
            return pvh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            SimpleItemViewHolder viewHolder = (SimpleItemViewHolder) holder;
            viewHolder.position = position;
            User user = memberList.get(position);
            ((SimpleItemViewHolder) holder).title.setText(user.getUsername());
            Glide.with(ChatActivity.this).load(user.getPhotoURL()).into(viewHolder.image);
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
                mDatabase.child(root).child("channels").child(board.getBoardKey()).
                        child(channelList.get(position).getKey()).child("peeps").
                        child(memberList.get(position).getUid()).setValue(true);

                Toast.makeText(ChatActivity.this,
                        memberList.get(position).getUsername()+" invited to channel "
                                + channelList.get(position).getName(),
                        Toast.LENGTH_SHORT).show();

            }
        }

    }

}
