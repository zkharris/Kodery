package com.zacharyharris.kodery.UI;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
    RecycleAdapter adapter;


    class RecycleAdapter extends RecyclerView.Adapter {

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

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.messageRecycleView);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);
        adapter = new RecycleAdapter();
        recyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged();

        final EditText editText = (EditText) findViewById(message_edit);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendMessage(editText.getText().toString());
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




    }

    @Override
    protected void onResume() {
        super.onResume();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference(root + "/message/" + board.getBoardKey() + "/messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Message message = dataSnapshot.getValue(Message.class);
                messageList.add(message);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.w(TAG, "messageRef:onCancelled");
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.w(TAG, "messageRef:onCancelled");
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.w(TAG, "messageRef:onCancelled");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "messageRef:onCancelled", databaseError.toException());
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
        childUpdates.put(root + "/message/" + board.getBoardKey() + "/messages/" + key, message.toFirebaseObject());
        mDatabase.updateChildren(childUpdates);
    }
}
