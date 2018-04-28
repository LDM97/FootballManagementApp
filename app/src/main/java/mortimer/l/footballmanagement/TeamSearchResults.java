
    // Class to handle the search results activity. Takes the search input and displays any resulting
    // teams dynamically. These results can then be clicked to join the selected team

    package mortimer.l.footballmanagement;

    // Android imports
    import android.content.Intent;
    import android.graphics.Color;
    import android.support.design.widget.NavigationView;
    import android.support.v4.view.GravityCompat;
    import android.support.v4.widget.DrawerLayout;
    import android.support.v7.app.AppCompatActivity;
    import android.os.Bundle;
    import android.support.v7.widget.Toolbar;
    import android.view.Gravity;
    import android.view.LayoutInflater;
    import android.view.Menu;
    import android.view.MenuItem;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.ImageView;
    import android.widget.LinearLayout;
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
    import java.util.HashMap;
    import java.util.LinkedList;
    import java.util.List;
    import java.util.Map;

    public class TeamSearchResults extends AppCompatActivity implements View.OnClickListener
    {
        // Get string resources for pointers to database directories
        private String userTeamPointer;
        private String teamsPointer;
        private String userPointer;

        // Firebase authenticator and navigation draw handler
        private FirebaseAuth auth;
        private final NavDrawerHandler navDrawerHandler= new NavDrawerHandler();
        private DrawerLayout navDraw;

        // Map the dynamically displayed views to the team, so a selected team can be identified
        private final Map<View, String> viewToTeamId = new HashMap<>();

        // Get the layout to dynamically display the search results in
        private ViewGroup linearLayout;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_team_search_results);

            // Get string resources for pointers to database directories
            teamsPointer = getString( R.string.teams_pointer );
            userTeamPointer = getString( R.string.user_pointers );
            userPointer = getString( R.string.no_team_users_pointer );

            // Custom toolbar setup
            Toolbar custToolBar = (Toolbar) findViewById(R.id.my_toolbar);
            setSupportActionBar(custToolBar);

            android.support.v7.app.ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowTitleEnabled(false);

            TextView actionBarTitle = (TextView) findViewById(R.id.toolbarTitle);
            actionBarTitle.setText( getString( R.string.team_search_results ) );

            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.menu_icon);
            // Setup logout button and home button
            findViewById(R.id.navLogout).setOnClickListener(this);
            findViewById(R.id.homeBtn).setOnClickListener(this);

            // Get Firebase authenticator
            auth = FirebaseAuth.getInstance();

            // Nav drawer code
            navDraw = findViewById( R.id.drawer_layout );
            NavigationView navigationView = findViewById( R.id.nav_view );

            // Hide the menu items. User not part of a team, cannot view these screens. Can only logout
            Menu nav_Menu = navigationView.getMenu();
            nav_Menu.findItem( R.id.teamCalendar ).setVisible( false );
            nav_Menu.findItem( R.id.discussionBoard ).setVisible( false );
            nav_Menu.findItem( R.id.teamInfo ).setVisible( false );
            nav_Menu.findItem( R.id.profileManagement ).setVisible( false );

            // Get the inputted query string
            final String QUERY = getIntent().getExtras().getString("mortimer.l.footballmanagement.query");

            // Get teams that match the query input
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference teamsRef = database.getReference().child( teamsPointer );

            teamsRef.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange( DataSnapshot snapshot )
                {
                    Iterable<DataSnapshot> teams = snapshot.getChildren();

                    // Populate the list of teams that match the query
                    List<Team> matchingTeams = new LinkedList<>();

                    for ( DataSnapshot team : teams ) {
                        Team teamObj = team.getValue( Team.class );

                        // If team name matches the query, add it to the matching teams list
                        if ( teamObj.getTeamName().toLowerCase().contains( QUERY.toLowerCase() ) )
                        {
                            matchingTeams.add(teamObj);
                        }
                    }

                    if ( matchingTeams.isEmpty() )
                    { // No teams match the query, display this to the user
                        TextView noResults = getTextView();
                        // noResults.setLayoutParams(params);

                        noResults.setGravity( Gravity.CENTER );
                        noResults.setText("No matches found for: " + QUERY);

                    } else { // Print out the results

                        // Loop through teams and output their data to the screen
                        for ( int i = 0; i < matchingTeams.size(); i++ ) {

                            Team team = matchingTeams.get( i );

                            View teamSearchResult = getResultContainer();

                            // Display the team name
                            TextView teamName = teamSearchResult.findViewById( R.id.teamName );
                            teamName.setText( team.getTeamName() );

                            // Display the team football type
                            TextView typeFootball = teamSearchResult.findViewById( R.id.typeFootball );
                            typeFootball.setText( team.getTypeFootball() );

                            // Display the team bio
                            TextView teamBio = teamSearchResult.findViewById( R.id.teamBio );
                            teamBio.setText( team.getTeamBio() );

                            // Set the image for the user's icon
                            ImageView teamLogo = teamSearchResult.findViewById( R.id.teamLogo );
                            teamLogo.setBackgroundResource(R.drawable.black_and_white_ball);

                            // Map the view to the team
                            viewToTeamId.put( teamSearchResult, team.getTeamId() );

                            // Display the team
                            linearLayout.addView( teamSearchResult );
                        }
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {
                    System.out.println("The read failed: " + databaseError.getCode());
                }
            });

        }

        private View getResultContainer()
        { // Dynamically generate a container for the team info to be displayed in

            // Add result item
            linearLayout = (ViewGroup) findViewById( R.id.content_frame );
            View teamSearchResult = LayoutInflater.from( getApplicationContext() ).inflate( R.layout.team_search_result_item, linearLayout, false);

            // Make clickable for user to select this thing
            teamSearchResult.setClickable( true );
            teamSearchResult.setOnClickListener( this );

            return teamSearchResult;
        }


        private TextView getTextView()
        { // Get a text view to dynamically display the results in
            TextView tv = new TextView( this );

            // Set layout and formatting of text view
            tv.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT) );
            tv.setGravity( Gravity.CENTER );
            tv.setTextSize( 24 );
            float scale = getResources().getDisplayMetrics().density;
            int padding = (int) ( 10*scale + 0.5f );
            tv.setTextColor( Color.BLACK );
            tv.setPadding( padding, padding, padding, padding );

            // Make clickable for user to select this thing
            tv.setClickable( true );
            tv.setOnClickListener( this );

            // Add the text view to the linear layout container on the page
            LinearLayout linearLayout = (LinearLayout)findViewById( R.id.content_frame );
            linearLayout.addView( tv );

            return tv;
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
        public boolean onOptionsItemSelected( MenuItem item )
        { // Open the navigation draw if the menu item is selected
            if ( item.getItemId() == android.R.id.home )
            {
                navDraw.openDrawer( GravityCompat.START );
                return true;
            }
            return super.onOptionsItemSelected( item );
        }

        @Override
        public void onBackPressed()
        { // Return user to the home screen
            Intent intent = new Intent(this, NoTeamHome.class);
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
                Intent homeScreenActivity = new Intent( getApplicationContext(), NoTeamHome.class );
                startActivity( homeScreenActivity );
            }

            if( viewToTeamId.containsKey( v ) )
            { // If clicked view is one of the team results

                final String teamId = viewToTeamId.get( v );
                final String userId = auth.getCurrentUser().getUid();

                // Get teams
                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference teamsRef = database.getReference();

                teamsRef.addListenerForSingleValueEvent( new ValueEventListener() {
                    @Override
                    public void onDataChange( DataSnapshot snapshot )
                    {
                        Team selectedTeam = snapshot.child( teamsPointer ).child( teamId ).getValue( Team.class );
                        User currentUser = snapshot.child( userPointer ).child( userId ).getValue( User.class );

                        // Add user to the team, set this team obj to be the new value for the team
                        selectedTeam.addPlayer( currentUser );
                        DatabaseReference teamRef = database.getReference().child( teamsPointer ).child( teamId );
                        teamRef.setValue( selectedTeam );

                        // Remove the user from the default users database as added to players list of a team
                        DatabaseReference userRef = database.getReference().child( userPointer ).child( userId );
                        userRef.removeValue();

                        // Put pointer to team in User directory
                        UserTeamPointer pointer = new UserTeamPointer( currentUser.getUserID(), selectedTeam.getTeamId() );
                        DatabaseReference pointerRef = database.getReference().child( userTeamPointer ).child( userId );
                        pointerRef.setValue( pointer );

                        // Notify user they have been added to this team
                        Toast.makeText( getApplicationContext(), "You have been added to team: " + selectedTeam.getTeamName(),
                                Toast.LENGTH_SHORT).show();

                        // Return user to home screen
                        Intent homeActivity = new Intent( getApplicationContext(), DefaultHome.class );
                        startActivity( homeActivity );
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }
                });

            }

        }
    }
