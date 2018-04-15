package mortimer.l.footballmanagement;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TeamSearchResults extends AppCompatActivity implements View.OnClickListener
{

    private FirebaseAuth auth;
    private NavDrawerHandler navDrawerHandler= new NavDrawerHandler();
    private DrawerLayout navDraw;
    private final Map<View, String> viewToTeamId = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_search_results);

        // Custom toolbar setup
        Toolbar custToolBar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(custToolBar);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);

        TextView actionBarTitle = (TextView) findViewById(R.id.toolbarTitle);
        actionBarTitle.setText("Search Results");

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.menu_icon);
        // Setup logout button and home button
        findViewById(R.id.navLogout).setOnClickListener(this);
        findViewById(R.id.homeBtn).setOnClickListener(this);

        // Get Firebase authenticator
        auth = FirebaseAuth.getInstance();

        // Nav drawer code
        navDraw = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Hide items, no team not allowed to navigate to team info screens
        Menu nav_Menu = navigationView.getMenu();
        nav_Menu.findItem(R.id.teamCalendar).setVisible(false);
        nav_Menu.findItem(R.id.discussionBoard).setVisible(false);
        nav_Menu.findItem(R.id.teamInfo).setVisible(false);


        final String QUERY = getIntent().getExtras().getString("mortimer.l.footballmanagement.query");

        // Get teams
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference teamsRef = database.getReference().child("Teams");

        teamsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Iterable<DataSnapshot> teams = snapshot.getChildren();

                // Populate the list of teams
                List<Team> matchingTeams = new LinkedList<>();

                for (DataSnapshot team : teams) {
                    Team teamObj = team.getValue(Team.class);

                    // If team name matches the query, add it to the mathinc teams list
                    if (teamObj.getTeamName().toLowerCase().contains(QUERY.toLowerCase())) {
                        matchingTeams.add(teamObj);
                    }
                }

                // Margin params for first item so it doesn't clash w the action bar
                final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
                int paddingTop = (int) ( 60 * scale + 0.5f);
                int padding = (int) ( 8 * scale + 0.5f );
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins( padding, paddingTop, padding, padding );


                if (matchingTeams.isEmpty()) { // No teams match the query, display this to the user
                    TextView noResults = getTextView();
                    noResults.setLayoutParams(params);

                    noResults.setGravity( Gravity.CENTER );
                    noResults.setText("No matches found for: " + QUERY);
                } else { // Print out the results

                    // Loop through teams and output their name to the screen
                    for ( int i = 0; i < matchingTeams.size(); i++ ) {

                        Team team = matchingTeams.get( i );

                        // Get text view to display the output
                        TextView resultFrame = getTextView();

                        if( i == 0 )
                        { // Set top margin on first result so doesn't clash w action bar
                            resultFrame.setLayoutParams(params);
                        }

                        resultFrame.setGravity( Gravity.CENTER );

                        // Set text to team name
                        resultFrame.setText( team.getTeamName());

                        // Map the view to the team
                        viewToTeamId.put( resultFrame, team.getTeamId() );
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

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
    {
        if ( item.getItemId() == android.R.id.home )
        {
            navDraw.openDrawer( GravityCompat.START );
            return true;
        }
        return super.onOptionsItemSelected( item );
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
        {
            Intent homeScreenActivity = new Intent( getApplicationContext(), NoTeamHome.class );
            startActivity( homeScreenActivity );
        }

        if( viewToTeamId.containsKey( v ) )
        { // If clicked view is one of the team results

            final String teamId = viewToTeamId.get( v );
            final String userId = auth.getCurrentUser().getUid();

            // Get teams
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference teamsRef = database.getReference(); //.child( "Teams" ).child( teamId );

            teamsRef.addListenerForSingleValueEvent( new ValueEventListener() {
                @Override
                public void onDataChange( DataSnapshot snapshot )
                {
                    Team selectedTeam = snapshot.child( "Teams" ).child( teamId ).getValue( Team.class );
                    User currentUser = snapshot.child( "Users" ).child( userId ).getValue( User.class );

                    // Add user to the team, set this team obj to be the new value for the team
                    selectedTeam.addPlayer( currentUser );
                    DatabaseReference teamRef = database.getReference().child( "Teams" ).child( teamId );
                    teamRef.setValue( selectedTeam );

                    // Remove the user from the default users database as added to players list of a team
                    DatabaseReference userRef = database.getReference().child( "Users" ).child( userId );
                    userRef.removeValue();

                    // Put pointer to team in User directory
                    UserTeamPointer pointer = new UserTeamPointer( currentUser.getUserID(), selectedTeam.getTeamId() );
                    DatabaseReference pointerRef = database.getReference().child( "UserTeamPointers" ).child( userId );
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
