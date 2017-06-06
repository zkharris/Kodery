package com.zacharyharris.kodery.UI;

import android.content.DialogInterface;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
//import com.zacharyharris.kodery.Adapter.MainAdapter;
import com.zacharyharris.kodery.Model.Board;
import com.zacharyharris.kodery.Model.Channel;
import com.zacharyharris.kodery.R;
import com.zacharyharris.kodery.Model.Update;
import com.zacharyharris.kodery.Model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.zacharyharris.kodery.R.id.board;
import static com.zacharyharris.kodery.R.id.start;
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

        boardsList = new ArrayList<>();

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

        FirebaseDatabase database = FirebaseDatabase.getInstance();
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

        // Create General Chat channel
        String generalKey = mDatabase.child(root).child("channels").child(board.getBoardKey()).push().getKey();
        Channel generalchannel = new Channel();
        generalchannel.setName("general");
        generalchannel.setKey(generalKey);

        mDatabase.child(root).child("channels").child(board.getBoardKey()).child(generalKey).setValue(generalchannel);
    }

    public void goToInvites(View view){
        Intent intent = new Intent(this, InvitesActivity.class);
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
                break;

            case R.id.invites:
                startActivity(new Intent(this, InvitesActivity.class));
                break;

            case R.id.add_board:
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                final View mview = getLayoutInflater().inflate(R.layout.create_board_popup, null);
                final EditText mboardname = (EditText) mview.findViewById(R.id.boardnme_edit);
                Button addleboard = (Button) mview.findViewById(R.id.createB);

                addleboard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!mboardname.getText().toString().isEmpty()){
                            saveBoard(mboardname.getText().toString());
                            Toast.makeText(MainActivity.this,
                                    mboardname.getText()+" created!",
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

        return super.onOptionsItemSelected(item);
    }

    public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {

        public static final String TAG = "MainAdapter";
        private ArrayList<Board> boardsList;

        public MainAdapter(ArrayList<Board> boardsList) {
            this.boardsList = boardsList;
        }

        @Override
        public MainAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_board, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.mBoard.setText(boardsList.get(position).getName());
            holder.position = position;
        }


        @Override
        public int getItemCount() {
            return boardsList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener{

            public TextView mBoard;
            public int position;

            public ViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
                mBoard =(TextView) itemView.findViewById(board);
            }

            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Clicked!");
                //Toast.makeText(v.getContext(), "Clicked",Toast.LENGTH_SHORT).show();

                Intent i = new Intent(v.getContext(), SingleBoardActivity.class);
                i.putExtra("board", boardsList.get(position));
                v.getContext().startActivity(i);

            }

            @Override
            public boolean onLongClick(View v) {
                //Toast.makeText(v.getContext(), "Edit Board",Toast.LENGTH_SHORT).show();

                AlertDialog.Builder mBuilder = new AlertDialog.Builder(v.getContext());
                View mview = LayoutInflater.from(v.getContext()).inflate(R.layout.edit_board, null);
                final EditText mboardname = (EditText) mview.findViewById(R.id.boardnme_edit);
                final Board test = boardsList.get(position);
                final TextView mtextview = (TextView) mview.findViewById(R.id.edit_board_title);
                mtextview.setText("Edit "+test.getName());
                mboardname.setText(test.getName());
                Button saveleboard = (Button) mview.findViewById(R.id.saveBoard);
                Button delboard = (Button) mview.findViewById(R.id.delBoard);

                saveleboard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!mboardname.getText().toString().isEmpty()
                                && !(mboardname.getText().toString().equals(test.getName()))){
                            Toast.makeText(v.getContext(),
                                    test.getName()+" renamed to "+mboardname.getText()+"!",
                                    Toast.LENGTH_SHORT).show();
                            updateBoard(boardsList.get(position), mboardname.getText().toString());
                            // Get rid of the pop up go back to main activity
                        }else{
                            Toast.makeText(v.getContext(),
                                    "Please rename the board.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                delboard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(mFirebaseUser.getUid().equals(boardsList.get(position).getOwnerUid())) {
                            deleteBoard(boardsList.get(position));
                            Toast.makeText(v.getContext(),
                                    test.getName()+" deleted.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(v.getContext(), "Only owner can delete boards",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });

                mBuilder.setView(mview);
                AlertDialog dialog = mBuilder.create();
                dialog.show();

                return true;
            }
        }
    }

    private void updateBoard(Board board, String boardName) {
        mDatabase.child(root).child("boards").child(board.getBoardKey()).child("name").setValue(boardName);
        String updateText = (board.getName() + " renamed " + boardName);
        update(board, updateText);
    }

    private void update(Board board, String updateText) {
        final String key = mDatabase.child("update").child(mFirebaseUser.getUid()).push().getKey();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm aa");
        String dateString = format.format(calendar.getTime());

        final Update update = new Update();
        update.setText(updateText);
        update.setBoard(board.getBoardKey());
        update.setKey(key);
        update.setDate(dateString);

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(root + "/updates/" + board.getBoardKey() + "/" + key, update.toFirebaseObject());
        mDatabase.updateChildren(childUpdates);
    }

    private void deleteBoard(Board board) {
        // Delete Lists
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference(root + "/boards/" + board.getBoardKey() + "/lists").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String listKey = data.getKey();
                    mDatabase.child(root).child("lists").child(listKey).removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "deleteBoard:onCancelled", databaseError.toException());
            }
        });

        // Delete channels
        mDatabase.child(root).child("channels").child(board.getBoardKey()).removeValue();

        mDatabase.child(root).child("boards").child(board.getBoardKey()).removeValue();
        mDatabase.child(root).child("updates").child(board.getBoardKey()).removeValue();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

    }


}
