package com.zacharyharris.kodery.UI;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.zacharyharris.kodery.Model.Board;
import com.zacharyharris.kodery.Model.Message;
import com.zacharyharris.kodery.R;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        if(getIntent().getExtras() != null) {
            Bundle extras = getIntent().getExtras();
            board = (Board)extras.get("board");
        }

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

    private void sendMessage(String text) {
        Log.w(TAG, "message sent");

        String key = mDatabase.child(root).child("message").child(board.getBoardKey()).child("messages").push().getKey();

        Message message = new Message();
        message.setMessageKey(key);
        message.setAuthor(mFirebaseUser.getDisplayName());
        message.setText(text);

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(root + "/message/" + board.getBoardKey() + "/messages/" + key, message.toFirebaseObject());
        mDatabase.updateChildren(childUpdates);
    }
}
