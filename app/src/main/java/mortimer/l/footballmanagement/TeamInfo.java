
    // Class for handling the TeamInfo activity. Dynamically displays the team info based on which team
    // the logged in user is assigned to

    package mortimer.l.footballmanagement;

    // Android imports
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
    import android.view.Gravity;
    import android.view.LayoutInflater;
    import android.view.MenuItem;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.EditText;
    import android.widget.ImageView;
    import android.widget.PopupWindow;
    import android.widget.RelativeLayout;
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

    import java.util.HashMap;
    import java.util.Map;

    public class TeamInfo extends AppCompatActivity implements View.OnClickListener
    {
        // Get string resources for pointers to database directories
        private String userTeamPointer;
        private String teamsPointer;
        private String playersPointer;
        private String usersPointer;

        private FirebaseAuth auth;
        private final NavDrawerHandler navDrawerHandler= new NavDrawerHandler();
        private DrawerLayout navDraw;

        private ViewGroup linearLayout;

        private Map<View,User> viewUserMap = new HashMap<>();

        private boolean currentUserIsOrganiser;
        User selectedPlayer;

        // Popup window for player info
        PopupWindow popupWindow;

        // Set to current team data in onCreate by default
        String currentInput = "";
        String newInput;
        String newBioInput;
        String newFootballTypeInput;
        String newTeamNameInput;

        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_team_info);

            // Get string resources for pointers to database directories
            teamsPointer = getString( R.string.teams_pointer );
            userTeamPointer = getString( R.string.user_pointers );
            playersPointer = getString( R.string.players_pointer );
            usersPointer = getString( R.string.no_team_users_pointer );

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

            // Setup edit buttons
            findViewById( R.id.teamBioEdit ).setOnClickListener( this );
            findViewById( R.id.teamFootballEdit ).setOnClickListener( this );
            findViewById( R.id.teamNameEdit ).setOnClickListener( this );

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

                    // Setup the team data in case of editing
                    newTeamNameInput = team.getTeamName();
                    newFootballTypeInput = team.getTypeFootball();
                    newBioInput = team.getTeamBio();

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
                    for (DataSnapshot playerSnapshot : snapshot.child( teamsPointer ).child( teamId ).child( playersPointer ).getChildren() )
                    {
                        User player = playerSnapshot.getValue(User.class);

                        if( player.getUserID().equals( auth.getUid() ) )
                        { // If player is the current user, set if they are a team organiser
                            // ( should they have organiser options on this screen? )
                            currentUserIsOrganiser = player.getTeamOrganiser();

                            if( !player.getTeamOrganiser() )
                            { // User is not an organiser, hide the edit team info options
                                findViewById( R.id.teamBioEdit ).setVisibility( View.GONE );
                                findViewById( R.id.teamFootballEdit ).setVisibility( View.GONE );
                                findViewById( R.id.teamNameEdit ).setVisibility( View.GONE );
                            }
                        }

                        // Add player item
                        View playerListItem = getPlayerContainer();

                        // Display the player's name
                        TextView playerNameText = playerListItem.findViewById(R.id.playerName);
                        playerNameText.setText(player.getName());

                        // Display the player's positions
                        TextView playerPositionsText = playerListItem.findViewById(R.id.playerPositions);
                        playerPositionsText.setText(player.getPreferredPositions());

                        // Set the image for the user's icon
                        ImageView playerImage = playerListItem.findViewById( R.id.playerProfileImage );
                        playerImage.setBackgroundResource(R.drawable.profile_icon_default);

                        // Map the view to the user object. Used for functionality when selecting the item
                        viewUserMap.put( playerListItem, player );

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

        private void updateTeamInfo()
        { // Organiser has edited some team info, update it accordingly
            // Get DB instance and reference
            FirebaseDatabase database =  FirebaseDatabase.getInstance();
            DatabaseReference databaseRef = database.getReference();

            databaseRef.addListenerForSingleValueEvent( new ValueEventListener()
            {
                @Override
                public void onDataChange( DataSnapshot snapshot )
                {
                    // Get the new input
                    if( currentInput.equals( "bio" ) ){ newBioInput = newInput; }
                    if( currentInput.equals( "football type" ) ){ newFootballTypeInput = newInput; }
                    if( currentInput.equals( "team name" ) ){ newTeamNameInput = newInput; }

                    // Get user id, used to locate the team this user plays for
                    FirebaseUser currentUser = auth.getCurrentUser();
                    String userId = currentUser.getUid();

                    // Get the current team id and team object
                    UserTeamPointer pointer = snapshot.child( userTeamPointer ).child(userId).getValue(UserTeamPointer.class);
                    String teamId = pointer.getTeamId();
                    Team team = snapshot.child( teamsPointer ).child(teamId).getValue(Team.class);

                    // Update the team object
                    team.setTeamName( newTeamNameInput );
                    team.setTypeFootball( newFootballTypeInput );
                    team.setTeamBio( newBioInput );

                    // Write these updates to the database
                    FirebaseDatabase database =  FirebaseDatabase.getInstance();
                    DatabaseReference teamRef = database.getReference().child( teamsPointer ).child( teamId );
                    teamRef.setValue( team );

                    // Notify the user the updates have been successful
                    Toast.makeText( TeamInfo.this, "Your team data has been updated.",
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

        private View getPlayerContainer()
        {
            // Return a clickable player list item, which is written to the player layout container
            linearLayout = (ViewGroup) findViewById( R.id.playersListContainer );
            View playerListItem = LayoutInflater.from(getApplicationContext()).inflate(R.layout.players_list_item_layout_team_info, linearLayout, false);
            playerListItem.setClickable( true );
            playerListItem.setOnClickListener( this );

            return playerListItem;
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

            if( v.getId() == R.id.closePopup )
            { // close the popup
                popupWindow.dismiss();
            }

            if( v.getId() == R.id.teamBioEdit || v.getId() == R.id.teamFootballEdit || v.getId() == R.id.teamNameEdit )
            { // Organiser attempts to update some of the team info

                String title = "";
                if( v.getId() == R.id.teamBioEdit )
                { // Team bio edited
                    title = "Write a new team bio:";
                    currentInput = "bio";
                }
                if( v.getId() == R.id.teamFootballEdit )
                { // Team type football edited
                    title = "What type of football does your team play?";
                    currentInput = "football type";
                }
                if( v.getId() == R.id.teamNameEdit )
                { // Team name edited
                    title = "What is your new team name?";
                    currentInput = "team name";
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
                        updateTeamInfo();
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

            if( v.getId() == R.id.makeOrganiserBtn )
            { // Make the selectedPlayer a team organiser

                // Get DB instance and reference
                final FirebaseDatabase database =  FirebaseDatabase.getInstance();
                DatabaseReference databaseRef = database.getReference();

                databaseRef.addListenerForSingleValueEvent( new ValueEventListener()
                {
                    @Override
                    public void onDataChange( DataSnapshot snapshot )
                    {

                        // Get the current team id
                        UserTeamPointer pointer = snapshot.child( userTeamPointer ).child( selectedPlayer.getUserID() ).getValue(UserTeamPointer.class);
                        String teamId = pointer.getTeamId();

                        for( DataSnapshot playerLoop : snapshot.child( teamsPointer ).child( teamId ).child( playersPointer ).getChildren() )
                        {
                            // Find the selected player in the database
                            User player = playerLoop.getValue( User.class );
                            if( player.getUserID().equals( selectedPlayer.getUserID() ) )
                            {
                                // If this is the selected player, update them as a team organiser
                                DatabaseReference selectedPlayerRef = playerLoop.getRef();
                                selectedPlayer.setTeamOrganiser( true );
                                selectedPlayerRef.setValue( selectedPlayer );
                            }
                        }

                        Toast.makeText( TeamInfo.this, selectedPlayer.getName() + " has been made an organiser.",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled (DatabaseError databaseError)
                    {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }
                });
            }

            if( v.getId() == R.id.kickPlayerBtn )
            { // Kick the selected player from the team

                // Get DB instance and reference
                final FirebaseDatabase database =  FirebaseDatabase.getInstance();
                DatabaseReference databaseRef = database.getReference();

                databaseRef.addListenerForSingleValueEvent( new ValueEventListener()
                {
                    @Override
                    public void onDataChange( DataSnapshot snapshot )
                    {

                        // Get the current team id
                        UserTeamPointer pointer = snapshot.child( userTeamPointer ).child( selectedPlayer.getUserID() ).getValue(UserTeamPointer.class);
                        String teamId = pointer.getTeamId();

                        // Remove the selected player from the team
                        for( DataSnapshot playerLoop : snapshot.child( teamsPointer ).child( teamId ).child( playersPointer ).getChildren() )
                        {
                            // Find the selected player in the database
                            User player = playerLoop.getValue( User.class );
                            if( player.getUserID().equals( selectedPlayer.getUserID() ) )
                            {
                                // If this is the selected player, remove them from the team
                                DatabaseReference selectedPlayerRef = playerLoop.getRef();
                                selectedPlayerRef.removeValue();
                            }
                        }

                        // Add the selected player to the no team users section
                        DatabaseReference noTeamUsersRef = database.getReference().child( usersPointer ).child( selectedPlayer.getUserID() );
                        selectedPlayer.setTeamOrganiser( false );
                        selectedPlayer.setTeam( "" );
                        noTeamUsersRef.setValue( selectedPlayer );

                        Toast.makeText( TeamInfo.this, selectedPlayer.getName() + " has been kicked from the team.",
                                Toast.LENGTH_SHORT).show();

                        // Refresh the screen to display user has been removed
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

            if( viewUserMap.containsKey( v ) )
            { // If the clicked item is one of the player containers and logged in user is allowed to see organiser options

                // Keep track of which player the current popup refers to
                selectedPlayer = viewUserMap.get( v );

                // Get layout container and inflate it
                LayoutInflater popupLayout = (LayoutInflater) getApplicationContext().getSystemService( LAYOUT_INFLATER_SERVICE );
                ViewGroup popupContainer;
                if( currentUserIsOrganiser )
                { // Setup popup with organiser options
                    popupContainer = (ViewGroup) popupLayout.inflate( R.layout.organiser_player_view, null );
                    popupContainer.findViewById( R.id.makeOrganiserBtn ).setOnClickListener( this );
                    popupContainer.findViewById( R.id.kickPlayerBtn ).setOnClickListener( this );
                }
                else{ // Setup popup as normal w out organiser options
                    popupContainer = (ViewGroup) popupLayout.inflate( R.layout.player_view, null );
                }

                // Set listener for close button
                popupContainer.findViewById( R.id.closePopup ).setOnClickListener( this );

                // Populate the selected player item

                // Display the player name
                TextView nameText = popupContainer.findViewById( R.id.playerName );
                nameText.setText( selectedPlayer.getName() );

                // Display the player positions
                TextView positionsText = popupContainer.findViewById( R.id.playerPositions );
                positionsText.setText( selectedPlayer.getPreferredPositions() );

                // Display the player bio
                TextView bioText = popupContainer.findViewById( R.id.playerBio );
                bioText.setText( selectedPlayer.getBio() );

                // Get parent layout
                RelativeLayout parentLayout = findViewById( R.id.teamInfoRelLayout );

                // Display popup window
                popupWindow = new PopupWindow( popupContainer, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true );
                popupWindow.showAtLocation( parentLayout, Gravity.CENTER, 0, 35 );
            }

        }
    }
