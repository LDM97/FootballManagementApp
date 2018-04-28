    package mortimer.l.footballmanagement;

    import android.content.DialogInterface;
    import android.content.Intent;
    import android.support.design.widget.NavigationView;
    import android.support.v4.view.GravityCompat;
    import android.support.v4.widget.DrawerLayout;
    import android.support.v7.app.AlertDialog;
    import android.support.v7.app.AppCompatActivity;
    import android.os.Bundle;
    import android.support.v7.widget.Toolbar;
    import android.text.InputType;
    import android.view.MenuItem;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.EditText;
    import android.widget.ImageView;
    import android.widget.TextView;
    import android.widget.Toast;

    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.ValueEventListener;

    public class ProfileManagement extends AppCompatActivity implements View.OnClickListener
    {

        // Get string resources for pointers to database directories
        private String userTeamPointer;
        private String teamsPointer;
        private String postsPointer;
        private String commentsPointer;
        private String playersPointer;

        // Set to current team data in onCreate by default
        String currentInput = "";
        String newInput;
        String newNameInput;
        String newPositionsInput;
        String newBioInput;

        // Store the current logged in user for future reference
        User loggedInUser;

        // Firebase authenticator and navigation drawer items
        private FirebaseAuth auth;
        private final NavDrawerHandler navDrawerHandler= new NavDrawerHandler();
        private DrawerLayout navDraw;

        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_profile_management);

            // Get string resources for pointers to database directories
            teamsPointer = getString( R.string.teams_pointer );
            postsPointer = getString( R.string.posts_pointer );
            userTeamPointer = getString( R.string.user_pointers );
            commentsPointer = getString( R.string.comments_pointer );
            playersPointer = getString( R.string.players_pointer );


            // Custom toolbar setup
            Toolbar custToolBar = (Toolbar) findViewById( R.id.my_toolbar );
            setSupportActionBar( custToolBar );

            android.support.v7.app.ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowTitleEnabled( false );

            TextView actionBarTitle = (TextView) findViewById( R.id.toolbarTitle );
            actionBarTitle.setText( getString( R.string.profile_management ) );

            actionBar.setDisplayHomeAsUpEnabled( true );
            actionBar.setHomeAsUpIndicator( R.drawable.menu_icon );

            // Get Firebase authenticator
            auth = FirebaseAuth.getInstance();

            // Setup listeners for the buttons on the screen
            findViewById( R.id.navLogout ).setOnClickListener( this );
            findViewById( R.id.homeBtn ).setOnClickListener( this );

            findViewById( R.id.profileNameEdit ).setOnClickListener( this );
            findViewById( R.id.prefPositionsEdit ).setOnClickListener( this );
            findViewById( R.id.playerBioEdit ).setOnClickListener( this );

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

            // Get DB instance and reference
            FirebaseDatabase database =  FirebaseDatabase.getInstance();
            DatabaseReference databaseRef = database.getReference();

            databaseRef.addListenerForSingleValueEvent( new ValueEventListener()
            {
                @Override
                public void onDataChange( DataSnapshot snapshot ) {

                    // Get user id, used to locate the team this user plays for
                    FirebaseUser currentUser = auth.getCurrentUser();
                    String userId = currentUser.getUid();

                    // Get the current team id and current user obj
                    UserTeamPointer pointer = snapshot.child( userTeamPointer ).child(userId).getValue(UserTeamPointer.class);
                    String teamId = pointer.getTeamId();

                    User user = new User();

                    for( DataSnapshot userLoop : snapshot.child( teamsPointer ).child( teamId ).child( playersPointer ).getChildren() )
                    { // Find the current user
                        User userLoopObj = userLoop.getValue( User.class );

                        if( userLoopObj.getUserID().equals( userId ) )
                        { // If this is the current logged in user
                            user = userLoopObj;
                            break;
                        }
                    }

                    // Setup the team data in case of editing
                    newNameInput = user.getName();
                    newPositionsInput = user.getPreferredPositions();
                    newBioInput = user.getBio();

                    // Store the current user
                    loggedInUser = user;

                    // Set the player name
                    TextView playerNameText = findViewById( R.id.profileName );
                    playerNameText.setText( user.getName() );

                    // Set the player positions
                    TextView playerPositionsText = findViewById( R.id.prefPositions );
                    playerPositionsText.setText( user.getPreferredPositions() );

                    // Set the player bio
                    TextView playerBio = findViewById( R.id.playerBio );
                    playerBio.setText( user.getBio() );

                }

                @Override
                public void onCancelled (DatabaseError databaseError)
                {
                    System.out.println("The read failed: " + databaseError.getCode());
                }
            });

        }

        private void updatePlayerInfo()
        { // Organiser has edited some team info, update it accordingly
            // Get DB instance and reference
            final FirebaseDatabase database =  FirebaseDatabase.getInstance();
            DatabaseReference databaseRef = database.getReference();

            databaseRef.addListenerForSingleValueEvent( new ValueEventListener()
            {
                @Override
                public void onDataChange( DataSnapshot snapshot )
                {
                    // Get the new input
                    if( currentInput.equals( "name" ) ){ newNameInput = newInput; }
                    if( currentInput.equals( "pref positions" ) ){ newPositionsInput = newInput; }
                    if( currentInput.equals( "bio" ) ){ newBioInput = newInput; }

                    // Get user id, used to locate the team this user plays for
                    FirebaseUser currentUser = auth.getCurrentUser();
                    String userId = currentUser.getUid();

                    // Get the current team id and team object
                    UserTeamPointer pointer = snapshot.child( userTeamPointer ).child(userId).getValue(UserTeamPointer.class);
                    String teamId = pointer.getTeamId();

                    // Get the databas reference to the player
                    DatabaseReference playerRef = database.getReference();

                    for( DataSnapshot userLoop : snapshot.child( teamsPointer ).child( teamId ).child( playersPointer ).getChildren() )
                    { // Find the current user
                        User userLoopObj = userLoop.getValue( User.class );

                        if( userLoopObj.getUserID().equals( userId ) )
                        { // If this is the current logged in user
                            playerRef = userLoop.getRef();
                        }
                    }


                    // Update the player obj
                    loggedInUser.setName( newNameInput );
                    loggedInUser.setPreferredPositions( newPositionsInput );
                    loggedInUser.setBio( newBioInput );

                    // Write these updates to the database
                    playerRef.setValue( loggedInUser );

                    // Notify the user the updates have been successful
                    Toast.makeText( ProfileManagement.this, "Your profile data has been updated.",
                            Toast.LENGTH_SHORT).show();

                    // Refresh the screen to display the edit updates
                    finish();
                    startActivity( getIntent() );
                }

                @Override
                public void onCancelled (DatabaseError databaseError)
                {
                    System.out.println("The read failed: " + databaseError.getCode());
                }
            });
        }

        @Override
        public boolean onOptionsItemSelected( MenuItem item )
        { // If navdraw icon selected, open the nav draw
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
        { // Return user to the discussion board
            Intent intent = new Intent(this, DefaultHome.class );
            startActivity( intent );
        }

        @Override
        public void onClick( View v )
        {
            if (v.getId() == R.id.navLogout)
            { // If logout clicked on nav drawer, run the signout function
                View thisView = findViewById(android.R.id.content);
                navDrawerHandler.signOut(thisView.getContext());
            }

            if (v.getId() == R.id.homeBtn)
            { // If home button pushed, take the user to the home screen
                Intent homeScreenActivity = new Intent(getApplicationContext(), DefaultHome.class);
                startActivity(homeScreenActivity);
            }

            if( v.getId() == R.id.profileNameEdit || v.getId() == R.id.prefPositionsEdit || v.getId() == R.id.playerBioEdit )
            { // User attempts to update their info

                String title = "";
                if( v.getId() == R.id.profileNameEdit )
                { // profile name edited
                    title = "Change your name:";
                    currentInput = "name";
                }
                if( v.getId() == R.id.prefPositionsEdit )
                { // pref positions edited
                    title = "What are your preferred positions?";
                    currentInput = "pref positions";
                }
                if( v.getId() == R.id.playerBioEdit )
                { // Player bio edited
                    title = "Enter your new bio: ";
                    currentInput = "bio";
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle( title );

                // Set up the input
                final EditText input = new EditText( this );

                // Specify the type of input expected
                input.setInputType( InputType.TYPE_TEXT_FLAG_MULTI_LINE );
                builder.setView( input );

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        newInput = input.getText().toString();

                        if( newInput.isEmpty() )
                        { // Check the user has entered some new data
                            Toast.makeText( ProfileManagement.this, "You must enter some new data!",
                                    Toast.LENGTH_SHORT).show();
                        } else
                            { // Update the new data
                            updatePlayerInfo();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                    }
                });

                builder.show();
            }

        }

    }
