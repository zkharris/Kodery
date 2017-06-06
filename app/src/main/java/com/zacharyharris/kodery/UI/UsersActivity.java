package com.zacharyharris.kodery.UI;

import android.support.v7.widget.SearchView;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zacharyharris.kodery.Model.Board;
import com.zacharyharris.kodery.R;
import com.zacharyharris.kodery.Model.User;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class UsersActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = "UsersActivity";
    public static final String ANONYMOUS = "anonymous";
    private static final String root = "testRoot";

    public User currentUser;

    private String mUsername;
    private String mPhotoUrl;
    private SharedPreferences mSharedPreferences;
    public int friendCount = 0;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;
    private String userId;

    RecycleAdapter adapter;
    ArrayList<User> userList;
    ArrayList<User> userListCopy;
    public Board board;


    class RecycleAdapter extends RecyclerView.Adapter {

        @Override
        public int getItemCount() {
            return userList.size();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add_user, parent, false);
            SimpleItemViewHolder pvh = new SimpleItemViewHolder(v);
            return pvh;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            SimpleItemViewHolder viewHolder = (SimpleItemViewHolder) holder;
            viewHolder.position = position;
            User user = userList.get(position);
            ((SimpleItemViewHolder) holder).username.setText(user.getUsername());
            if(user.getCompanyName() != null) {
                ((SimpleItemViewHolder) holder).compName.setText(user.getCompanyName());
            } else {
                ((SimpleItemViewHolder) holder).compName.setText(null);
            }
            Glide.with(UsersActivity.this).load(user.getPhotoURL()).into(viewHolder.image);

        }

        public final class SimpleItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView image;
            TextView username;
            TextView compName;
            Button mbutton;
            public int position;

            public SimpleItemViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                username = (TextView) itemView.findViewById(R.id.prof_name);
                compName = (TextView) itemView.findViewById(R.id.comp_name);
                image = (ImageView) itemView.findViewById(R.id.prof_pic);
                mbutton = (Button) itemView.findViewById(R.id.Add_friend);
                mbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(v.getContext(), username.getText()+" added!",Toast.LENGTH_SHORT).show();
                        addUser(userList.get(position));
                    }
                });

            }

            @Override
            public void onClick(View view) {
                Log.d(TAG, "Clicked user is " + userList.get(position).getUsername() + " " + userList
                        .get(position).getEmail());
                addUser(userList.get(position));
                // Toast "invite sent"
            }
        }

        public void filter(String text) {
            userList.clear();
            Log.w(TAG, text);
            if(text.isEmpty()){
                Log.w(TAG, "User List: " + userList);
            } else {
                text = text.toLowerCase();
                for(User user : userListCopy){
                    if(user.getUsername().toLowerCase().equals(text) || user.getEmail().toLowerCase().equals(text)){
                        userList.add(user);
                        Log.w(TAG, "User List: " + userList);
                    }
                }
            }
            notifyDataSetChanged();
        }

        public void clear() {
            int size = userList.size();
            if(size > 0) {
                for(int i = 0; i < size; i++) {
                    userList.remove(0);
                }
                notifyItemRangeRemoved(0, size);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        userList = new ArrayList<>();
        userListCopy = new ArrayList<>();

        if (getIntent().getExtras() != null) {
            Bundle extras = getIntent().getExtras();
            board = (Board)extras.get("board");
        }
    }

    @Override
    protected  void onResume() {
        super.onResume();

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        final FirebaseUser fUser = mFirebaseAuth.getCurrentUser();
        userId = fUser.getUid();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference(root + "/users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseUser fuser = mFirebaseAuth.getCurrentUser();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    User user = data.getValue(User.class);
                    if (!fuser.getUid().equals(user.getUid())) {
                        userList.add(user);
                        userListCopy.add(user);
                    }
                }
                adapter.clear();
                //adapter.notifyDataSetChanged();
                Log.d(TAG, "User List: " + userList);
                Log.d(TAG, "User List copy: " + userListCopy);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("TodoApp", "getUser:onCancelled", databaseError.toException());
            }
        });
    }

    public void addUser(User addedUser) {
        String id = addedUser.getUid();
        mDatabase.child(root).child("invites").child(id).child(board.getBoardKey()).setValue(true);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    protected  void onStart() {
        super.onStart();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.mRecycleView);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);
        adapter = new RecycleAdapter();
        recyclerView.setAdapter(adapter);
        adapter.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        Log.w(TAG, "options selected");
        adapter.clear();

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.clear();
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.clear();
                return true;
            }
        });

        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w(TAG, "Search Clicked");
                adapter.clear();
            }
        });
        return true;
    }
}

