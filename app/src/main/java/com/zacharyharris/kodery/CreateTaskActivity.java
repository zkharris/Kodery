package com.zacharyharris.kodery;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CreateTaskActivity extends AppCompatActivity {

    private static final String TAG = "test";
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    public boolean editTask;
    public Task task;
    public String todoID;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        final EditText nameEdtText = (EditText)findViewById(R.id.nameEditText);
        final EditText descEdtText = (EditText)findViewById(R.id.descEditText);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveTodo(nameEdtText.getText().toString(), descEdtText.getText().toString());

            }
        });

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

    }

    private void saveTodo(String name, String desc) {
        String key = mDatabase.child("task").push().getKey();

        Task task = new Task();
        task.setKey(key);
        task.setName(name);
        task.setDescription(desc);
        task.setList("testList");
        task.setBoard("testBoard");

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/tasks/" + key, task.toFirebaseObject());
        mDatabase.updateChildren(childUpdates);

        String updateText = (name + " added to list: " + "<insert list name>");
        update(updateText);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void update(String text) {
        final String key =  mDatabase.child("updates").child(mFirebaseUser.getUid()).push()
                .getKey();

        final Update update = new Update();
        update.setText(text);
        update.setKey(key);
        update.setBoard("testBoard");

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/updates/" + mFirebaseUser.getUid() + "/" + key, update.toFirebaseObject());
        mDatabase.updateChildren(childUpdates);

        // insert update sharing with peeps and owner here
    }


}



