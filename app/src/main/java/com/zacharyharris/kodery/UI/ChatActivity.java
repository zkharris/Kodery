package com.zacharyharris.kodery.UI;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

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
import com.zacharyharris.kodery.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.LogRecord;

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

    class ChannelRecycleAdapter extends RecyclerView.Adapter {

        @Override
        public int getItemCount() {
            return channelList.size();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_channel, parent, false);
            SimpleItemViewHolder pvh = new SimpleItemViewHolder(v);
            return pvh;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            SimpleItemViewHolder viewHolder = (SimpleItemViewHolder) holder;
            viewHolder.position = position;
            Channel channel = channelList.get(position);
            ((SimpleItemViewHolder) holder).title.setText("#" + channel.getName());
        }

        public final class SimpleItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener {
            TextView title;
            int position;

            public SimpleItemViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
                title = (TextView) itemView.findViewById(R.id.channel_name);

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

                        } else if (tap_num==2){
                            Toast.makeText(ChatActivity.this, "double clicked", Toast.LENGTH_SHORT).show();

                            /* THIS ACTION WILL TAKE YOU TO EDITCHANNEL ACTIVITY. WHERE CHAT SETTINGS AND MEMBERS OF THE CHANNEL WILL BE */

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

                saveleboard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!mboardname.getText().toString().isEmpty()
                                || !(mboardname.getText().toString().equals(mchannel.getName()))){
                            Toast.makeText(ChatActivity.this,
                                    mchannel.getName()+" renamed to "+mboardname.getText()+"!",
                                    Toast.LENGTH_SHORT).show();
                            // Get rid of the pop up go back to main activity
                        }else{
                            Toast.makeText(ChatActivity.this,
                                    "Please rename the channel.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                delboard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(v.getContext(),
                                "#"+mchannel.getName()+" deleted.",
                                Toast.LENGTH_SHORT).show();

                    }
                });

                mBuilder.setView(mview);
                AlertDialog dialog = mBuilder.create();
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
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
            SimpleItemViewHolder pvh = new SimpleItemViewHolder(v);
            return pvh;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            SimpleItemViewHolder viewHolder = (SimpleItemViewHolder) holder;
            viewHolder.position = position;
            Message message = messageList.get(position);
            ((SimpleItemViewHolder) holder).author.setText(message.getAuthor());
            ((SimpleItemViewHolder) holder).text.setText(message.getText());
            Glide.with(ChatActivity.this).load(message.getMessagePhotoURL()).into(viewHolder.image);
        }

        public final class SimpleItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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

        //Message recycler View
        RecyclerView messagerecyclerView = (RecyclerView)findViewById(R.id.messageRecycleView);
        LinearLayoutManager mllm = new LinearLayoutManager(this);
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
                                Toast.LENGTH_SHORT).show();
                    }
                }
                return handled;
            }
        });

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        // set default channel to general
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference(root + "/channels/" + board.getBoardKey()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                channelList.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Channel channel = data.getValue(Channel.class);
                    channelList.add(channel);
                }
                channelAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getChannels:onCancelled", databaseError.toException());
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

                addleboard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!mboardname.getText().toString().isEmpty()){
                            saveChannel(mboardname.getText().toString());
                            Toast.makeText(ChatActivity.this,
                                    mboardname.getText()+" created!",
                                    Toast.LENGTH_SHORT).show();
                            // Get rid of the pop up go back to main activity

                        }else{
                            Toast.makeText(ChatActivity.this,
                                    "Please add a channel.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                mBuilder.setView(mview);
                AlertDialog dialog = mBuilder.create();
                dialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveChannel(String name) {
        String key = mDatabase.child(root).child("channels").child(board.getBoardKey()).push().getKey();

        Channel channel = new Channel();
        channel.setName(name);
        channel.setKey(key);

        Map<String, Object> channelUpdates = new HashMap<>();
        channelUpdates.put(root + "/channels/" + "/" + board.getBoardKey() + "/" +key,
                channel.toFirebaseObject());
        mDatabase.updateChildren(channelUpdates);

    }
}
