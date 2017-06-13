package com.zacharyharris.kodery.UI;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.helper.ItemTouchHelper;

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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
//import com.zacharyharris.kodery.Adapter.MainAdapter;
import com.zacharyharris.kodery.Model.Board;
import com.zacharyharris.kodery.Model.Channel;
import com.zacharyharris.kodery.Model.Task;
import com.zacharyharris.kodery.R;
import com.zacharyharris.kodery.Model.Update;
import com.zacharyharris.kodery.Model.User;
import com.zacharyharris.kodery.UI.SingleBoardActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.zacharyharris.kodery.R.id.board;
import static com.zacharyharris.kodery.R.id.start;
import static com.zacharyharris.kodery.R.id.view;
import static java.lang.String.valueOf;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = "MainActivity";
    private RecyclerView mRecyclerView;
    MainAdapter mAdapter;
    private RecyclerView.LayoutManager mlayoutManager;
    public SwipeRefreshLayout mswipe;

    public int tap_num = 0;


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
    ArrayList<Board> boardsList = new ArrayList<>();

    private static final String root = "testRoot";
    private User currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        android.support.v7.app.ActionBar mActionBar = getSupportActionBar();
        mActionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#245a7a")));

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mlayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mAdapter = new MainAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        /*mswipe = (SwipeRefreshLayout) findViewById(R.id.refresh_pull);
        mswipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                database.getReference(root + "/boards").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.w(TAG, "Boards list: " + String.valueOf(boardsList));
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            Board boards = data.getValue(Board.class);
                            if (!has(boardsList, boards)) {
                                Log.w(TAG, valueOf(data.child("peeps").child(mFirebaseUser.getUid()).getValue()));
                                if (boards.getOwnerUid().equals(mFirebaseUser.getUid())) {
                                    boardsList.add(boards);
                                    mAdapter.notifyDataSetChanged();
                                }
                                if (data.hasChild("peeps") && data.child("peeps").child(mFirebaseUser.getUid())
                                        .getValue() != null) {
                                    boardsList.add(boards);
                                    mAdapter.notifyDataSetChanged();
                                }
                            }
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "refresh:onCancelled", databaseError.toException());
                    }
                });

                mswipe.setRefreshing(false);
            }
        });*/

        final FirebaseDatabase mdatabase = FirebaseDatabase.getInstance();

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, 0) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                /*
                int oldPos = viewHolder.getAdapterPosition();
                int newPos = target.getAdapterPosition();
                Board mboard = (Board) boardsList.get(oldPos);
                boardsList.remove(oldPos);
                boardsList.add(newPos, mboard);
                mAdapter.notifyItemMoved(oldPos, newPos);
                */
                final int firstPosition = viewHolder.getAdapterPosition();
                final int secondPosition = target.getAdapterPosition();
                /*DatabaseReference firstItemRef = mdatabase.getReference(root + "/boards/" + boardsList.get(viewHolder.getAdapterPosition()).getBoardKey());
                DatabaseReference secondItemRef = mdatabase.getReference(root + "/boards/" + boardsList.get(target.getAdapterPosition()).getBoardKey());

                HashMap<String, Object> updateFirstItemOrderNumber = new HashMap<>();
                updateFirstItemOrderNumber.put("orderNumber", secondPosition);
                firstItemRef.updateChildren(updateFirstItemOrderNumber);

                HashMap<String, Object> updateSecondItemOrderNumber = new HashMap<>();
                updateSecondItemOrderNumber.put("orderNumber", firstPosition);
                secondItemRef.updateChildren(updateSecondItemOrderNumber);*/

                mAdapter.notifyItemMoved(firstPosition, secondPosition);

                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

            }
        };

        ItemTouchHelper mITH = new ItemTouchHelper(simpleItemTouchCallback);
        mITH.attachToRecyclerView(mRecyclerView);


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


        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        // Check new user
        database.getReference(root + "/users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(mFirebaseUser.getUid())) {
                    User newUser = new User(mFirebaseUser.getDisplayName(), mFirebaseUser.getEmail(),
                            mFirebaseUser.getUid(), mFirebaseUser.getPhotoUrl().toString(), null);
                    Log.w(TAG, "user is new");
                    mDatabase.child(root).child("users").
                            child(mFirebaseUser.getUid()).setValue(newUser);
                } else {
                    DataSnapshot currUserRef = dataSnapshot.child(mFirebaseUser.getUid());
                    currentUser = currUserRef.getValue(User.class);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "storeUser:onCancelled", databaseError.toException());
            }
        });

        loadBoard();
    }

    private void loadBoard() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        // Board feed
        database.getReference(root + "/boards").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boardsList.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Board boards = data.getValue(Board.class);
                    Log.w(TAG, valueOf(data.child("peeps").child(mFirebaseUser.getUid()).getValue()));
                    if (data.hasChild("peeps") && data.child("peeps").child(mFirebaseUser.getUid())
                            .getValue() != null) {
                        boardsList.add(boards);
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "boardFeed:onCancelled", databaseError.toException());
            }
        });
    }

        /*Query orderedListQuery = database.getReference(root + "/boards").orderByChild("orderNumber");
        orderedListQuery.addValueEventListener(new ValueEventListener() {
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
                    Log.w(TAG, "query:onCancelled", databaseError.toException());
                }
            });*/


/*
        String name = mFirebaseUser.getDisplayName();
        String displayname="";
        int i = 0;
        while(!(String.valueOf(name.charAt(i)).equals(" "))){
            displayname += name.charAt(i);
            i++;
        }

        mActionBar.setTitle(displayname+"'s Kodery");
*/

    private void saveBoard(String name, String hc) {
        String key = mDatabase.child(root).child("boards").push().getKey();
        Board board = new Board();
        board.setName(name);
        board.setOwner(mFirebaseUser.getDisplayName());
        board.setBoardKey(key);
        board.setOwnerUid(mFirebaseUser.getUid());
        board.setColor(hc);

        Map<String, Object> boardUpdates = new HashMap<>();
        boardUpdates.put(root + "/boards/" + key, board.toFirebaseObject());
        mDatabase.updateChildren(boardUpdates);

        Log.d(TAG, "saveBoard: "+board.getColor());

        // Create General Chat channel
        String generalKey = mDatabase.child(root).child("channels").child(board.getBoardKey()).push().getKey();
        Channel generalchannel = new Channel();
        generalchannel.setName("general");
        generalchannel.setKey(generalKey);
        generalchannel.setType("public");

        mDatabase.child(root).child("channels").child(board.getBoardKey()).child(generalKey).setValue(generalchannel);
        mDatabase.child(root).child("boards").child(key).child("admins").
                child(mFirebaseUser.getUid()).setValue(true);
        mDatabase.child(root).child("boards").child(key).child("peeps").child(mFirebaseUser.getUid()).setValue(true);

        loadBoard();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
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

            case R.id.add_tag:
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                final View mview = getLayoutInflater().inflate(R.layout.tag_popup, null);
                final EditText mCompName = (EditText) mview.findViewById(R.id.tag_popup_edit);
                TextView mtitlep = (TextView) mview.findViewById(R.id.tag_popup_title);
                Button saveupdate = (Button) mview.findViewById(R.id.tag_popup_button);
                mCompName.setText(currentUser.getNetwork());
                mBuilder.setView(mview);
                final AlertDialog dialog = mBuilder.create();

                saveupdate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!mCompName.getText().toString().isEmpty()) {
                            Toast.makeText(MainActivity.this,
                                    mCompName.getText() + " added!",
                                    Toast.LENGTH_SHORT).show();
                        }
                        saveNetwork(mCompName.getText().toString());
                        dialog.dismiss();
                    }
                });
                dialog.show();
                break;

            case R.id.add_board:
                AlertDialog.Builder myBuilder = new AlertDialog.Builder(MainActivity.this);
                final View myview = getLayoutInflater().inflate(R.layout.create_board_popup, null);
                final EditText mboardname = (EditText) myview.findViewById(R.id.boardnme_edit);
                Button addleboard = (Button) myview.findViewById(R.id.createB);

                RadioGroup mRG = (RadioGroup) myview.findViewById(R.id.color_radio_group);
                final RadioButton purpRB = (RadioButton) myview.findViewById(R.id.purp_b);
                final RadioButton blueRB = (RadioButton) myview.findViewById(R.id.blue_b);
                final RadioButton greenRB = (RadioButton) myview.findViewById(R.id.green_b);
                final RadioButton yellRB = (RadioButton) myview.findViewById(R.id.yell_b);
                final RadioButton orngRB = (RadioButton) myview.findViewById(R.id.orng_b);
                final RadioButton redRB = (RadioButton) myview.findViewById(R.id.red_b);

                myBuilder.setView(myview);
                final AlertDialog mydialog = myBuilder.create();
                //Log.d(TAG, "onOptionsItemSelected: "+HC);

                addleboard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!mboardname.getText().toString().isEmpty()){
                            if (purpRB.isChecked()) {
                                Log.d(TAG, "onClick: purple");
                                saveBoard(mboardname.getText().toString(), "#d1afff");

                            }else if(blueRB.isChecked()){
                                Log.d(TAG, "onClick: blue");
                                saveBoard(mboardname.getText().toString(), "#b2cefe");

                            }else if(greenRB.isChecked()){
                                Log.d(TAG, "onClick: green");
                                saveBoard(mboardname.getText().toString(), "#abe9a1");

                            }else if(yellRB.isChecked()){
                                Log.d(TAG, "onClick: yellow");
                                saveBoard(mboardname.getText().toString(), "#ffe77f");

                            }else if(orngRB.isChecked()){
                                Log.d(TAG, "onClick: orange");
                                saveBoard(mboardname.getText().toString(), "#ffc2a2");

                            }else /*if(radioButtonid==R.id.red_b)*/{
                                Log.d(TAG, "onClick: red");
                                saveBoard(mboardname.getText().toString(), "#fd9995");
                            }
                            Toast.makeText(MainActivity.this,
                                    mboardname.getText()+" created!",
                                    Toast.LENGTH_SHORT).show();
                            mydialog.dismiss();
                            // Get rid of the pop up go back to main activity
                        }else{
                            Toast.makeText(MainActivity.this,
                                    "Please name the board.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                mydialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveNetwork(String name) {
        mDatabase.child(root).child("users").child(mFirebaseUser.getUid()).child("network")
                .setValue(name);

    }

    public class MainAdapter extends RecyclerView.Adapter {

        public static final String TAG = "MainAdapter";

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_board, parent, false);
            SimpleItemViewHolder vh = new SimpleItemViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            SimpleItemViewHolder viewHolder = (SimpleItemViewHolder) holder;
            viewHolder.position = position;
            Board board = boardsList.get(position);
            ((SimpleItemViewHolder) holder).mBoard.setText(board.getName());
            ((SimpleItemViewHolder) holder).mCV.setCardBackgroundColor(Color.parseColor(board.getColor()));
            if(board.getColor().equals("#ffff99")){
                ((SimpleItemViewHolder) holder).mBoard.setTextColor(Color.GRAY);
            }

        }

        /*@Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.mBoard.setText(boardsList.get(position).getName());
            holder.position = position;
            Log.d(TAG, "onBindViewHolder: color: "+boardsList.get(position).getColor());
            //String str = boardsList.get(position).getColor();
            //Color colorCV = new Color();
            //colorCV.parseColor(str);
            holder.mCV.setCardBackgroundColor(Color.parseColor(boardsList.get(position).getColor()));
        }*/


        @Override
        public int getItemCount() {
            return boardsList.size();
        }

        public class SimpleItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener/*,View.OnLongClickListener*/{

            public TextView mBoard;
            public int position;
            CardView mCV;

            public SimpleItemViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                //itemView.setOnLongClickListener(this);
                mBoard =(TextView) itemView.findViewById(board);
                mCV = (CardView) itemView.findViewById(R.id.board_cv);
            }

            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Clicked!");
                //Toast.makeText(v.getContext(), "Clicked",Toast.LENGTH_SHORT).show();
                final View myView = v;
                tap_num++;
                android.os.Handler mHandler = new android.os.Handler();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(tap_num==1){
                            Intent i = new Intent(myView.getContext(), SingleBoardActivity.class);
                            i.putExtra("board", boardsList.get(position));
                            myView.getContext().startActivity(i);
                            //overridePendingTransition(R.anim.slide_to_left, R.anim.slide_from_right);
                        } else if(tap_num==2){

                            AlertDialog.Builder mBuilder = new AlertDialog.Builder(myView.getContext());
                            View mview = LayoutInflater.from(myView.getContext()).inflate(R.layout.edit_board, null);
                            final EditText mboardname = (EditText) mview.findViewById(R.id.boardnme_edit);
                            final Board test = boardsList.get(position);
                            String boardC = test.getColor();
                            final TextView mtextview = (TextView) mview.findViewById(R.id.edit_board_title);
                            mtextview.setText("Edit "+test.getName());
                            mboardname.setText(test.getName());
                            Button saveleboard = (Button) mview.findViewById(R.id.saveBoard);
                            Button delboard = (Button) mview.findViewById(R.id.delBoard);

                            RadioGroup mRG = (RadioGroup) mview.findViewById(R.id.color_radio_group);
                            final RadioButton purpRB = (RadioButton) mview.findViewById(R.id.purp_b);
                            final RadioButton blueRB = (RadioButton) mview.findViewById(R.id.blue_b);
                            final RadioButton greenRB = (RadioButton) mview.findViewById(R.id.green_b);
                            final RadioButton yellRB = (RadioButton) mview.findViewById(R.id.yell_b);
                            final RadioButton orngRB = (RadioButton) mview.findViewById(R.id.orng_b);
                            final RadioButton redRB = (RadioButton) mview.findViewById(R.id.red_b);
                            int check=0;

                            if(boardC.equals("#d1afff")){purpRB.setChecked(true); check=R.id.purp_b;}
                            else if(boardC.equals("#b2cefe")){blueRB.setChecked(true);check=R.id.blue_b;}
                            else if(boardC.equals("#abe9a1")){greenRB.setChecked(true); check=R.id.green_b;}
                            else if(boardC.equals("#ffe77f")){yellRB.setChecked(true); check=R.id.yell_b;}
                            else if(boardC.equals("#ffc2a2")){orngRB.setChecked(true); check=R.id.orng_b;}
                            else {redRB.setChecked(true); check=R.id.red_b;}
                            final int check2 = check;




                            mBuilder.setView(mview);
                            final AlertDialog dialog = mBuilder.create();

                            saveleboard.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(!mboardname.getText().toString().isEmpty()
                                            && !(mboardname.getText().toString().equals(test.getName()))) {
                                        Toast.makeText(v.getContext(),
                                                test.getName() + " renamed to " + mboardname.getText() + "!",
                                                Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                        if (purpRB.isChecked()) {
                                            Log.d(TAG, "onClick: purple");
                                            updateBoard(boardsList.get(position), mboardname.getText().toString(), "#d1afff");

                                        } else if (blueRB.isChecked()) {
                                            Log.d(TAG, "onClick: blue");
                                            updateBoard(boardsList.get(position), mboardname.getText().toString(), "#b2cefe");

                                        } else if (greenRB.isChecked()) {
                                            Log.d(TAG, "onClick: green");
                                            updateBoard(boardsList.get(position), mboardname.getText().toString(), "#abe9a1");

                                        } else if (yellRB.isChecked()) {
                                            Log.d(TAG, "onClick: yellow");
                                            updateBoard(boardsList.get(position), mboardname.getText().toString(), "#ffe77f");

                                        } else if (orngRB.isChecked()) {
                                            Log.d(TAG, "onClick: orange");
                                            updateBoard(boardsList.get(position), mboardname.getText().toString(), "#ffc2a2");

                                        } else {
                                            Log.d(TAG, "onClick: red");
                                            updateBoard(boardsList.get(position), mboardname.getText().toString(), "#fd9995");
                                        }
                                        updateBoard(boardsList.get(position), mboardname.getText().toString());
                                        // Get rid of the pop up go back to main activity
                                    }else if(!mboardname.getText().toString().isEmpty()){
                                        if (purpRB.isChecked() && check2!=R.id.purp_b) {
                                            Log.d(TAG, "onClick: purple");
                                            updateBoard(boardsList.get(position), mboardname.getText().toString(), "#d1afff");
                                            dialog.dismiss();

                                        } else if (blueRB.isChecked() && check2!=R.id.blue_b) {
                                            Log.d(TAG, "onClick: blue");
                                            updateBoard(boardsList.get(position), mboardname.getText().toString(), "#b2cefe");
                                            dialog.dismiss();

                                        } else if (greenRB.isChecked() && check2!=R.id.green_b) {
                                            Log.d(TAG, "onClick: green");
                                            updateBoard(boardsList.get(position), mboardname.getText().toString(), "#abe9a1");
                                            dialog.dismiss();

                                        } else if (yellRB.isChecked() && check2!=R.id.yell_b) {
                                            Log.d(TAG, "onClick: yellow");
                                            updateBoard(boardsList.get(position), mboardname.getText().toString(), "#ffe77f");
                                            dialog.dismiss();

                                        } else if (orngRB.isChecked() && check2!=R.id.orng_b) {
                                            Log.d(TAG, "onClick: orange");
                                            updateBoard(boardsList.get(position), mboardname.getText().toString(), "#ffc2a2");
                                            dialog.dismiss();

                                        } else if(redRB.isChecked() && check2!=R.id.red_b) {
                                            Log.d(TAG, "onClick: red");
                                            updateBoard(boardsList.get(position), mboardname.getText().toString(), "#fd9995");
                                            dialog.dismiss();
                                        } else{
                                            Toast.makeText(v.getContext(),
                                                    "Please change the Board name or color.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }else{
                                        Toast.makeText(v.getContext(),
                                                "Please change the Board name or color.",
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
                                        dialog.dismiss();
                                    } else {
                                        Toast.makeText(v.getContext(), "Only owner can delete boards",
                                                Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });

                            dialog.show();



                        }
                        tap_num = 0;
                    }
                }, 250);

            }

            /*
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

                mBuilder.setView(mview);
                final AlertDialog dialog = mBuilder.create();

                saveleboard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!mboardname.getText().toString().isEmpty()
                                && !(mboardname.getText().toString().equals(test.getName()))){
                            Toast.makeText(v.getContext(),
                                    test.getName()+" renamed to "+mboardname.getText()+"!",
                                    Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
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
                            dialog.dismiss();
                        } else {
                            Toast.makeText(v.getContext(), "Only owner can delete boards",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });

                dialog.show();

                return true;
            }*/
        }
    }

    private void updateBoard(Board board, String boardName, String boardColor) {
        mDatabase.child(root).child("boards").child(board.getBoardKey()).child("name").setValue(boardName);
        mDatabase.child(root).child("boards").child(board.getBoardKey()).child("color").setValue(boardColor);
        String updateText = (board.getName() + " renamed " + boardName);
        update(board, updateText);
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

    private void deleteBoard(final Board board) {
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

        // Delete channels, updates and board
        mDatabase.child(root).child("channels").child(board.getBoardKey()).removeValue();
        mDatabase.child(root).child("boards").child(board.getBoardKey()).removeValue();
        mDatabase.child(root).child("updates").child(board.getBoardKey()).removeValue();

        // Delete Tasks
        database.getReference(root + "/tasks").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Task task = data.getValue(Task.class);
                    if(task.getBoard().equals(board.getBoardKey())) {
                        mDatabase.child(root).child("tasks").child(task.getKey()).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "deleteTasks:onCancelled", databaseError.toException());
            }
        });

        loadBoard();
    }

    @Override
    public void onBackPressed(){
        //super.onBackPressed();
        moveTaskToBack(true);
    }

}
