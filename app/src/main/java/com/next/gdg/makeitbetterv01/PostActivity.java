package com.next.gdg.makeitbetterv01;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.view.GestureDetectorCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


@RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
public class PostActivity extends AppCompatActivity implements GestureDetector.OnGestureListener,GestureDetector.OnDoubleTapListener{
    private TextView userName;
    private GestureDetectorCompat gDetector;
    private FirebaseAuth myFireBaseAuth;
    private GoogleSignInClient googleSignInClient;
    private TextView commentTextView;
    private ImageView pictureView;
    private Post p;
    private  TextView userNameComplete;
    private ImageButton likeButton;
    private ArrayList<String> whoLiked;
    private ArrayList<String> whoVolunteered;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private DatabaseReference ref ;
    private ImageButton volunteerButton;
    private RecyclerView volunteeredList;
    private RecyclerView.Adapter volunteerListAdapter;
    private RecyclerView.LayoutManager volunteerListLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
         myFireBaseAuth = FirebaseAuth.getInstance();
        userName = findViewById(R.id.UserName);
        commentTextView = findViewById(R.id.commentTextView);
        pictureView = findViewById(R.id.pictureImageView);
        userNameComplete = findViewById(R.id.UserName2);
        likeButton = findViewById(R.id.likeButton);
        volunteerButton = findViewById(R.id.volunteerButton);
        volunteeredList = findViewById(R.id.recyclerView);
        volunteerListLayoutManager = new LinearLayoutManager(this);
        volunteeredList.setLayoutManager(volunteerListLayoutManager);


          user = myFireBaseAuth.getCurrentUser();
        p = getIntent().getParcelableExtra("parcel_data");
        String[] name = p.username.split("@");
       userName.setText(name[0]);
       commentTextView.setText(p.comment);
       userNameComplete.setText(p.username);
        database = FirebaseDatabase.getInstance();
        ref = database.getReference("Posts");
         whoLiked = p.whoLiked;
        whoVolunteered = p.whoVolunteered;
        for (int i=0;i<whoVolunteered.size();i++)
        whoVolunteered.set(i,((whoVolunteered.get(i).split("="))[1]).substring(0,(whoVolunteered.get(i).split("=")[1]).length()-1));
        ArrayList<String> myDataset = whoVolunteered;

        volunteerListAdapter = new MyAdapter(myDataset);
        volunteeredList.setAdapter(volunteerListAdapter);


       pictureView.setImageBitmap(convertBase64StringToBitmapImage());

        this.gDetector=new GestureDetectorCompat(this,this);
        gDetector.setOnDoubleTapListener(this);

          GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
           googleSignInClient = GoogleSignIn.getClient(this,gso);


    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private ArrayList<String> mDataset;


        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView mTextView;
            public ViewHolder(TextView v) {
                super(v);
                mTextView = v;
            }
        }

        public MyAdapter(ArrayList<String> myDataset) {
            mDataset = myDataset;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            TextView v = (TextView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.my_text_view, parent, false);

            ViewHolder vh = new ViewHolder(v);
            return vh;
        }


        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            holder.mTextView.setText(mDataset.get(position));

        }

        @Override
        public int getItemCount() {
            if(mDataset != null){
                return mDataset.size();
            }else return 0;

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.sign_out_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.SignOutB){
            if(myFireBaseAuth.getCurrentUser()!= null){
                myFireBaseAuth.signOut();
                googleSignOut();
                startActivity(new Intent(PostActivity.this,SignInActivity.class));
            }
        }
        return super.onOptionsItemSelected(item);

    }



    public void googleSignOut(){
           googleSignInClient.signOut()
                   .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                       @Override
                       public void onComplete(@NonNull Task<Void> task) {
                           Toast.makeText(PostActivity.this,"Signed Out", Toast.LENGTH_LONG).show();
                       }
                   });
    }

    @Override
    protected void onStart() {
        super.onStart();
        whoLiked = p.whoLiked;
        whoVolunteered = p.whoVolunteered;
        user = myFireBaseAuth.getCurrentUser();
       final String email = user.getEmail();


        if (whoLiked != null ){
            ref.child(p.postId).child("whoLiked")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Iterable<DataSnapshot> whoLikeds = dataSnapshot.getChildren();
                            for(DataSnapshot whoLiked: whoLikeds){
                                String key = whoLiked.getKey();

                                if(whoLiked.getKey().equals(key)){
                                    String tested = whoLiked.getValue().toString();

                                    if(tested.equals(user.getEmail())){
                                        //DatabaseReference refRemove = ref.child(p.postId).child("whoLiked");
                                        likeButton.setBackgroundResource(R.drawable.likered);
                                    }
                                }
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


        }
            likeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (whoLiked != null && whoLiked.contains(email)){
                        likeButton.setBackgroundResource(R.drawable.like);
                        ref.child(p.postId).child("whoLiked")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Iterable<DataSnapshot> whoLikeds = dataSnapshot.getChildren();
                                        for(DataSnapshot whoLiked: whoLikeds){
                                            String key = whoLiked.getKey();

                                             if(whoLiked.getKey().equals(key)){
                                                String tested = whoLiked.getValue().toString();

                                            if(tested.equals ( user.getEmail())){
                                                    DatabaseReference refRemove = ref.child(p.postId).child("whoLiked");
                                                    refRemove.child(key).removeValue();
                                               }
                                             }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                        whoLiked.remove(user.getEmail());

                    }else{
                        likeButton.setBackgroundResource(R.drawable.likered);
                       DatabaseReference whoLikedRef =  ref.child(p.postId).child("whoLiked");
                       whoLikedRef.push().setValue(user.getEmail());
                        whoLiked.add(user.getEmail());
                    }
                }
        });
        if(whoVolunteered != null){

            ref.child(p.postId).child("whoVolunteered")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Iterable<DataSnapshot> whoVolunteereds = dataSnapshot.getChildren();
                            for(DataSnapshot whoVolunteered: whoVolunteereds){
                                String key = whoVolunteered.getKey();
                                if(whoVolunteered.getKey().equals(key)){
                                    String tested = whoVolunteered.getValue().toString();
                                    if(tested.equals(user.getEmail())){
                                        volunteerButton.setEnabled(false);
                                    }
                                }
                            }

                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        volunteerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if(!volunteerButton.isEnabled()){

                   ref.child(p.postId).child("whoVolunteered")
                           .addListenerForSingleValueEvent(new ValueEventListener() {
                               @Override
                               public void onDataChange(DataSnapshot dataSnapshot) {

                                       }
                               @Override
                               public void onCancelled(DatabaseError databaseError) {

                               }
                           });
                       }
                       else{
                           volunteerButton.setEnabled(false);
                           DatabaseReference whoVolunteeredRef = ref.child(p.postId).child("whoVolunteered");
                           whoVolunteeredRef.push().setValue(user.getEmail());
                           whoVolunteered.add(user.getEmail());
               }
                    }
            });
          }
     }




    @Override
    public void onBackPressed(){

    }

    public Bitmap convertBase64StringToBitmapImage(){

        byte[] decodedString = Base64.decode(p.picture,Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedByte;
    }


// Touching methods

    @Override
    public boolean onTouchEvent(MotionEvent event1){

       this.gDetector.onTouchEvent(event1);
        return super.onTouchEvent(event1);

    }
    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
       return false;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
       return  false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {

       return true;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent mE, MotionEvent mE1, float v, float v1) {
        if((mE.getY()-mE1.getY())>150){
            Intent n=new Intent(PostActivity.this,MapsActivity.class);
            startActivity(n);
        }
        return false;
    }
}
