
    // Class for handling the TeamInfo activity. Dynamically displays the team info based on which team
    // the logged in user is assigned to

    package mortimer.l.footballmanagement;

    // Android imports
    import android.content.Intent;
    import android.support.design.widget.NavigationView;
    import android.support.v4.view.GravityCompat;
    import android.support.v4.widget.DrawerLayout;
    import android.support.v7.app.AppCompatActivity;
    import android.os.Bundle;
    import android.support.v7.widget.Toolbar;
    import android.view.LayoutInflater;
    import android.view.MenuItem;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.ImageView;
    import android.widget.TextView;

    // Firebase imports
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.ValueEventListener;

    public class TeamInfo extends AppCompatActivity implements View.OnClickListener
    {
        // Get string resources for pointers to database directories
        String userTeamPointer;
        String teamsPointer;
        String playersPointer;

        private FirebaseAuth auth;
        private NavDrawerHandler navDrawerHandler= new NavDrawerHandler();
        private DrawerLayout navDraw;

        private ViewGroup linearLayout;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_team_info);

            // Get string resources for pointers to database directories
            teamsPointer = getString( R.string.teams_pointer );
            userTeamPointer = getString( R.string.user_pointers );
            playersPointer = getString( R.string.players_pointer );

            // Custom toolbar setup
            Toolbar custToolBar = (Toolbar) findViewById( R.id.my_toolbar );
            setSupportActionBar( custToolBar );

            android.support.v7.app.ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowTitleEnabled( false );

            TextView actionBarTitle = (TextView) findViewById( R.id.toolbarTitle );
            actionBarTitle.setText( getString( R.string.team_info_title ) );

            actionBar.setDisplayHomeAsUpEnabled( true );
            actionBar.setHomeAsUpIndicator( R.drawable.menu_icon );

            // Get Firebase authenticator
            auth = FirebaseAuth.getInstance();

            // Setup logout button and home button
            findViewById( R.id.navLogout ).setOnClickListener( this );
            findViewById( R.id.homeBtn ).setOnClickListener( this );

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

                    // Get the current team id and team object
                    UserTeamPointer pointer = snapshot.child( userTeamPointer ).child(userId).getValue(UserTeamPointer.class);
                    String teamId = pointer.getTeamId();
                    Team team = snapshot.child( teamsPointer ).child(teamId).getValue(Team.class);

                    // Set the team name
                    TextView teamNameText = findViewById(R.id.teamName);
                    teamNameText.setText(team.getTeamName());

                    // Set the team type of football
                    TextView typeOfFootballText = findViewById(R.id.typeOfFootball);
                    typeOfFootballText.setText(team.getTypeFootball());

                    // Set the team bio
                    TextView teamBioText = findViewById(R.id.teamBio);
                    teamBioText.setText(team.getTeamBio());


                    // Dynamically list the players in the team
                    for (DataSnapshot eventSnapshot : snapshot.child( teamsPointer ).child( teamId ).child( playersPointer ).getChildren() )
                    {
                        User player = eventSnapshot.getValue(User.class);

                        // Add player item
                        linearLayout = (ViewGroup) findViewById(R.id.playersListContainer);
                        View playerListItem = LayoutInflater.from(getApplicationContext()).inflate(R.layout.players_list_item_layout_team_info, linearLayout, false);

                        // Display the player's name
                        TextView playerNameText = playerListItem.findViewById(R.id.playerName);
                        playerNameText.setText(player.getName());

                        // Display the player's positions
                        TextView playerPositionsText = playerListItem.findViewById(R.id.playerPositions);
                        playerPositionsText.setText(player.getPreferredPositions());

                        // Set the image for the user's icon
                        ImageView playerImage = playerListItem.findViewById( R.id.playerProfileImage );
                        playerImage.setBackgroundResource(R.drawable.profile_icon_default);

                        // Add the view to the screen w all the event data
                        linearLayout.addView(playerListItem);
                    }

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
        { // Open the navigation drawer if the menu icon selected
            if ( item.getItemId() == android.R.id.home ) {
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
            Intent intent = new Intent(this, DefaultHome.class);
            startActivity(intent);
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
            { // Return the user to the home screen if the home button is pressed
                Intent homeScreenActivity = new Intent( getApplicationContext(), DefaultHome.class );
                startActivity( homeScreenActivity );
            }

        }
    }
