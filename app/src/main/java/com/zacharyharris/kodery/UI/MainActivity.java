package com.zacharyharris.kodery.UI;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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
import com.zacharyharris.kodery.Adapter.MainAdapter;
import com.zacharyharris.kodery.Model.Board;
import com.zacharyharris.kodery.R;
import com.zacharyharris.kodery.Model.Update;
import com.zacharyharris.kodery.Model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.valueOf;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = "MainActivity";
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mlayoutManager;


    private SharedPreferences mSharedPreferences;

    // Firebase Auth
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;
    private GoogleApiClient mGoogleApiClient;

    //User data
    private String mUsername;
    private String mPhotoUrl;
    ArrayList<Update> updateList;
    ArrayList<Board> boardsList;

    private static final String root = "testRoot";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        android.support.v7.app.ActionBar menu = getSupportActionBar();
        menu.setDisplayShowHomeEnabled(true);
        menu.setLogo(R.mipmap.ic_launcher_round);
        menu.setDisplayUseLogoEnabled(true);

        /* Implementation of Horizontal Card & Recycler view */

        /*mDataSet = new ArrayList<>();
        for(int i=1; i<10; i++){
            mDataSet.add("Project: "+i);
        }*/


        boardsList = new ArrayList<Board>();

        mRecyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mlayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mAdapter = new MainAdapter(boardsList);
        mRecyclerView.setAdapter(mAdapter);
       // mtoolbar =(Toolbar) findViewById(R.id.nav_drwr);
       // setSupportActionBar(mtoolbar);


        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mUsername = "anonymous";

        //Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            //Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // store user into database
        User currentUser = new User(mFirebaseUser.getDisplayName(), mFirebaseUser.getEmail(),
                mFirebaseUser.getUid(), mFirebaseUser.getPhotoUrl().toString());

        mDatabase.child(root).child("users").child(mFirebaseUser.getUid()).setValue(currentUser);

        /* Create Add Board Button */

        Button addBoard = (Button) findViewById(R.id.adButeon);
        addBoard.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                View mview = getLayoutInflater().inflate(R.layout.activity_create_board, null);
                final EditText mboardname = (EditText) mview.findViewById(R.id.boardnme_edit);
                Button addleboard = (Button) mview.findViewById(R.id.createB);

                addleboard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!mboardname.getText().toString().isEmpty()){
                            saveBoard(mboardname.getText().toString());
                            Toast.makeText(MainActivity.this,
                                    "Board Created!",
                                    Toast.LENGTH_SHORT).show();
                            // Get rid of the pop up go back to main activity
                        }else{
                            Toast.makeText(MainActivity.this,
                                    "Please name the board.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                mBuilder.setView(mview);
                AlertDialog dialog = mBuilder.create();
                dialog.show();

            }
        });


        /* TEST TEST TEST TEST TEST TEST TEST FOR TASK */
/*
        Button addTask = (Button) findViewById(R.id.test_button);
        addBoard.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                View mview = getLayoutInflater().inflate(R.layout.activity_create_task, null);
                final EditText mtaskname = (EditText) mview.findViewById(R.id.Taskname_edit);
                final EditText mtaskdesc = (EditText) mview.findViewById(R.id.Taskndesc_edit);
                Button addletask = (Button) mview.findViewById(R.id.createTsk);

                addletask.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!mtaskname.getText().toString().isEmpty() ||
                                !mtaskdesc.getText().toString().isEmpty()){
                            Toast.makeText(MainActivity.this,
                                    "Board Created!",
                                    Toast.LENGTH_SHORT).show();
                            // Get rid of the pop up go back to main activity
                        }else{
                            if(!mtaskname.getText().toString().isEmpty()) {
                                Toast.makeText(MainActivity.this,
                                        "Please name the task.",
                                        Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(MainActivity.this,
                                        "Please add a desc.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

                mBuilder.setView(mview);
                AlertDialog dialog = mBuilder.create();
                dialog.show();

            }
        });
        */
    }

    private void saveBoard(String name) {
        String key = mDatabase.child("board").push().getKey();

        Board board = new Board();
        board.setName(name);
        board.setOwner(mFirebaseUser.getDisplayName());
        board.setBoardKey(key);
        board.setOwnerUid(mFirebaseUser.getUid());

        Map<String, Object> boardUpdates = new HashMap<>();
        boardUpdates.put(root + "/boards/" + key, board.toFirebaseObject());
        mDatabase.updateChildren(boardUpdates);
    }

    @Override
    protected  void onResume() {
        super.onResume();

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // User updates
        /*database.getReference("updates/" + mFirebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Update update = data.getValue(Update.class);
                    updateList.add(update);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "userUpdate:onCancelled", databaseError.toException());
            }
        });*/

        // Board feed
        database.getReference(root + "/boards").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boardsList.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Board boards = data.getValue(Board.class);
                    Log.w(TAG, valueOf(data.child("peeps").child(mFirebaseUser.getUid()).getValue()));
                    if (boards.getOwnerUid().equals(mFirebaseUser.getUid())) {
                        boardsList.add(boards);
                    }
                    if (data.hasChild("peeps") && data.child("peeps").child(mFirebaseUser.getUid())
                            .getValue() != null) {
                        boardsList.add(boards);
                    }
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "boardFeed:onCancelled", databaseError.toException());
            }
        });
    }

    public void goToUsers(View view){
        Intent intent = new Intent(this, UsersActivity.class);
        startActivity(intent);
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    //bind the sign out button onClick here
    public void signOut(View view){
        mFirebaseAuth.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        mFirebaseUser = null;
        mUsername = "anonymous";
        mPhotoUrl = null;
        startActivity(new Intent(this, SignInActivity.class));
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        return Actions.newView("Main", "http://[ENTER-YOUR-URL-HERE]");
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id){
            case R.id.signouts:
                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mFirebaseUser = null;
                mUsername = "anonymous";
                mPhotoUrl = null;
                startActivity(new Intent(this, SignInActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    public void onTestClicked(View view){
        Intent i = new Intent(this, InvitesActivity.class);
        startActivity(i);
    }
}
