
    // Class to handle the CreateDiscussion event. Allows a user to create a new post on the discussion
    // board. Handles user inputs and writes valid inputs as a new post to the database

    package mortimer.l.footballmanagement;

    // Android imports
    import android.content.Intent;
    import android.os.Bundle;
    import android.support.design.widget.NavigationView;
    import android.support.v4.view.GravityCompat;
    import android.support.v4.widget.DrawerLayout;
    import android.support.v7.app.AppCompatActivity;
    import android.support.v7.widget.Toolbar;
    import android.text.TextUtils;
    import android.view.MenuItem;
    import android.view.View;
    import android.widget.EditText;
    import android.widget.TextView;
    import android.widget.Toast;

    // Firebase imports
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.ValueEventListener;

    // Java imports
    import java.util.List;

    public class CreateDiscussion extends AppCompatActivity implements View.OnClickListener
    {

        // Post data inputs
        private EditText postTitleInput;
        private EditText postTextInput;

        // Firebase authenticator and nav draw handlers
        private FirebaseAuth auth;
        private NavDrawerHandler navDrawerHandler= new NavDrawerHandler();
        private DrawerLayout navDraw;

        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_create_discussion);

            // Custom toolbar setup
            Toolbar custToolBar = (Toolbar) findViewById( R.id.my_toolbar );
            setSupportActionBar( custToolBar );

            android.support.v7.app.ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowTitleEnabled( false );

            TextView actionBarTitle = (TextView) findViewById( R.id.toolbarTitle );
            actionBarTitle.setText( "Create Post" );

            actionBar.setDisplayHomeAsUpEnabled( true );
            actionBar.setHomeAsUpIndicator( R.drawable.menu_icon );

            // Get Firebase authenticator
            auth = FirebaseAuth.getInstance();

            // Setup logout button and home button
            findViewById( R.id.navLogout ).setOnClickListener( this );
            findViewById( R.id.homeBtn ).setOnClickListener( this );
            findViewById( R.id.createPostBtn ).setOnClickListener( this );

            // Get new discussion inputs
            postTitleInput = findViewById( R.id.discussionTitleInput );
            postTextInput = findViewById( R.id.discussionTextInput );

            // Nav drawer code
            navDraw = findViewById( R.id.drawer_layout );
            NavigationView navigationView = findViewById( R.id.nav_view );

            navigationView.setNavigationItemSelectedListener
                    (
                            new NavigationView.OnNavigationItemSelectedListener()
                            {
                                @Override
                                public boolean onNavigationItemSelected( MenuItem menuItem ) {

                                    // close drawer when item is tapped
                                    navDraw.closeDrawers();

                                    // Pass selected item and context to handle view
                                    View thisView = findViewById(android.R.id.content);
                                    navDrawerHandler.itemSelectHandler( menuItem, thisView.getContext() );

                                    return true;
                                }
                            });
        }

        private void createPost()
        { // Write the input data to the database

            // Get a reference to the database
            FirebaseDatabase database =  FirebaseDatabase.getInstance();
            DatabaseReference databaseRef = database.getReference();

            databaseRef.addListenerForSingleValueEvent( new ValueEventListener()
            {
                @Override
                public void onDataChange( DataSnapshot snapshot ) {

                    // Get the inputs
                    String postTitle = postTitleInput.getText().toString();
                    String postText = postTextInput.getText().toString();

                    // Get user id, used to locate the team this user plays for
                    FirebaseUser currentUser = auth.getCurrentUser();
                    String userId = currentUser.getUid();

                    // Get the current team id
                    UserTeamPointer pointer = snapshot.child("UserTeamPointers").child(userId).getValue(UserTeamPointer.class);
                    String teamId = pointer.getTeamId();

                    // Get the current team obj and create a new post obj
                    Team team = snapshot.child( "Teams" ).child( teamId ).getValue( Team.class );
                    DiscussionItem post = new DiscussionItem( postTitle, postText, userId );

                    // Add the new post to the posts for this team, then get that post for writing to the database
                    team.addPost( post );
                    List<DiscussionItem> newPosts = team.getPosts();

                    // Update the posts in the database
                    DatabaseReference postsRef = snapshot.child( "Teams" ).child( teamId ).child( "posts" ).getRef();
                    postsRef.setValue( newPosts );

                    // Notify user the post has been created
                    Toast.makeText( CreateDiscussion.this, "Post Created",
                            Toast.LENGTH_SHORT).show();

                    // Return user to the discussion board screen
                    Intent discussionBoard = new Intent( getApplicationContext(), DiscussionBoard.class );
                    startActivity( discussionBoard );

                }

                @Override
                public void onCancelled( DatabaseError databaseError)
                {
                    System.out.println( "The read failed: " + databaseError.getCode() );
                }
            } );
        }

        private boolean validateForm()
        { // Go through each field and check that all fields have been filled.
            boolean valid = true;

            // Get the inputs
            String postTitle = postTitleInput.getText().toString();
            String postText = postTextInput.getText().toString();


            // Check team title input
            if (TextUtils.isEmpty( postTitle ))
            { // No input, inform user and return false
                postTitleInput.setError("Required");
                valid = false;
            } else {
                postTitleInput.setError(null);
            }

            // Check team post text input
            if (TextUtils.isEmpty( postText ))
            { // No input, inform user and return false
                postTextInput.setError("Required");
                valid = false;
            } else {
                postTextInput.setError(null);
            }

            return valid;
        }

        @Override
        public boolean onOptionsItemSelected( MenuItem item )
        { // Open the nav draw if menu icon selected
            if ( item.getItemId() == android.R.id.home )
            {
                navDraw.openDrawer( GravityCompat.START );
                return true;
            }
            return super.onOptionsItemSelected( item );
        }

        @Override
        public void onStart()
        {
            super.onStart();
            // Check if user is signed in (non-null) and update UI accordingly
            FirebaseUser currentUser = auth.getCurrentUser();

            if( currentUser == null )
            {
                // User not logged in, bad navigation attempt, return user to login screen
                Intent loginActivity = new Intent( getApplicationContext(), Login.class );
                startActivity( loginActivity );
            }
        }

        @Override
        public void onBackPressed()
        { // Return user to the home screen
            Intent intent = new Intent(this, DiscussionBoard.class );
            startActivity( intent );
        }

        @Override
        public void onClick( View v )
        {
            if( v.getId() == R.id.navLogout )
            { // If logout clicked on nav drawer, run the signout function
                View thisView = findViewById(android.R.id.content);
                navDrawerHandler.signOut( thisView.getContext() );
            }

            if( v.getId() == R.id.homeBtn )
            { // Return the user to the home screen if the home icon is pressed
                Intent homeScreenActivity = new Intent( getApplicationContext(), DefaultHome.class );
                startActivity( homeScreenActivity );
            }

            if( v.getId() == R.id.createPostBtn )
            { // Create the post

                if( validateForm() )
                { // Only attempt to create the post if all form data is valid
                    createPost();
                }

            }

        }

    }
