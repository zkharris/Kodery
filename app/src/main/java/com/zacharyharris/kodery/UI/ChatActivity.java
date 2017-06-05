package com.zacharyharris.kodery.UI;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

import static com.zacharyharris.kodery.R.id.message_edit;
import static com.zacharyharris.kodery.UI.BoardMembersActivity.root;

public class ChatActivity extends AppCompatActivity {

    private String TAG = "ChatActivity";
    private DatabaseReference mDatabase;
    public Board board;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    ArrayList<Message> messageList;
    MessageRecycleAdapter messageAdapter;
    private Channel currChannel;
    ChannelRecycleAdapter channelAdapter;
    ArrayList<Channel> channelList;

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

        public final class SimpleItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView title;
            int position;

            public SimpleItemViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                title = (TextView) itemView.findViewById(R.id.channel_name);

            }

            @Override
            public void onClick(View v) {
                Log.w(TAG, channelList.get(position).getName());
                currChannel = channelList.get(position);
                channelFeed(currChannel);
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

        public final class SimpleItemViewHolder extends RecyclerView.ViewHolder {
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
                    sendMessage(currChannel, editText.getText().toString());
                    handled = true;
                    //Clears the keyboard and hides it when enter is pressed
                    editText.setText("");
                    InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    mgr.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                }
                return handled;
            }
        });

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        // set default channel to general
        setGeneralChannel();



    }

    private void setGeneralChannel() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference(root + "/channels/" + board.getBoardKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Channel channel = data.getValue(Channel.class);
                    if(channel.getName().equals("general")) {
                        currChannel = channel;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Display Channels
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference(root + "/channels/" + board.getBoardKey()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Channel channel = data.getValue(Channel.class);
                    channelList.add(channel);
                    Log.w(TAG, String.valueOf(channel.getName()));
                }
                channelAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(Channel channel, String text) {
        Log.w(TAG, "message sent");

        String key = mDatabase.child(root).child("message").child(board.getBoardKey()).child("messages").push().getKey();

        Message message = new Message();
        message.setMessageKey(key);
        message.setAuthor(mFirebaseUser.getDisplayName());
        message.setAuthorUid(mFirebaseUser.getUid());
        message.setMessagePhotoURL(mFirebaseUser.getPhotoUrl().toString());
        message.setText(text);

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(root + "/channels/" + board.getBoardKey() + "/" + currChannel.getChannelKey() + "/messages/" + key, message.toFirebaseObject());
        mDatabase.updateChildren(childUpdates);
    }

    private void channelFeed(Channel channel) {
        // put this in channelFeed functoin
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference(root + "/channels/" + board.getBoardKey() + "/" + channel.getChannelKey() + "/messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Message message = dataSnapshot.getValue(Message.class);
                messageList.add(message);
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "messageRef:onCancelled", databaseError.toException());
            }
        });
    }
}
