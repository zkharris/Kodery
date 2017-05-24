package com.zacharyharris.kodery;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

import java.util.ArrayList;

public class UsersActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = "UsersActivity";
    public static final String ANONYMOUS = "anonymous";

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

    class RecycleAdapter extends RecyclerView.Adapter {

        @Override
        public int getItemCount() {
            return userList.size();
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
            SimpleItemViewHolder pvh = new SimpleItemViewHolder(v);
            return pvh;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            SimpleItemViewHolder viewHolder = (SimpleItemViewHolder) holder;
            viewHolder.position = position;
            User user = userList.get(position);
            ((SimpleItemViewHolder) holder).title.setText(user.getUsername());
            Glide.with(UsersActivity.this).load(user.getPhotoURL()).into(viewHolder.image);

        }

        public final class SimpleItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView image;
            TextView title;
            public int position;

            public SimpleItemViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                title = (TextView) itemView.findViewById(R.id.mTextView);
                image = (ImageView) itemView.findViewById(R.id.imageView);
            }

            @Override
            public void onClick(View view) {
                Log.d(TAG, "Clicked user is "+ userList.get(position));
                addUser(userList.get(position));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

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
        database.getReference("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseUser fuser = mFirebaseAuth.getCurrentUser();
                for(DataSnapshot data: dataSnapshot.getChildren()) {
                    User user = data.getValue(User.class);
                    if(!fUser.getUid().equals(user.getUid())) {
                        userList.add(user);
                    }
                }

                adapter.notifyDataSetChanged();
                Log.d(TAG, "User List: " + userList);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("TodoApp", "getUser:onCancelled", databaseError.toException());
            }
        });

        userList = new ArrayList<>();
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.mRecycleView);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);
        adapter = new RecycleAdapter();
        recyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged();

    }

    public void addUser(User addedUser) {
        String id = addedUser.getUid();
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        mDatabase.child("friendships").child(user.getUid()).child(id).setValue(true);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }
}
