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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedList;
import java.util.List;

public class TeamSearchResults extends AppCompatActivity implements View.OnClickListener
{

    String query;

    private FirebaseAuth auth;
    private NavDrawerHandler navDrawerHandler= new NavDrawerHandler();
    private DrawerLayout navDraw;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_team_search_results );

        // Custom toolbar setup
        Toolbar custToolBar = (Toolbar) findViewById( R.id.my_toolbar );
        setSupportActionBar( custToolBar );

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled( false );

        TextView actionBarTitle = (TextView) findViewById(R.id.toolbarTitle);
        actionBarTitle.setText( "Search Results" );

        actionBar.setDisplayHomeAsUpEnabled( true );
        actionBar.setHomeAsUpIndicator( R.drawable.menu_icon );
        // Setup logout button and home button
        findViewById( R.id.navLogout ).setOnClickListener( this );
        findViewById( R.id.homeBtn ).setOnClickListener( this );

        // Get Firebase authenticator
        auth = FirebaseAuth.getInstance();

        // Nav drawer code
        navDraw = findViewById( R.id.drawer_layout );
        NavigationView navigationView = findViewById( R.id.nav_view );

        // Hide items, no team not allowed to navigate to team info screens
        Menu nav_Menu = navigationView.getMenu();
        nav_Menu.findItem( R.id.teamCalendar ).setVisible( false );
        nav_Menu.findItem( R.id.discussionBoard ).setVisible( false );
        nav_Menu.findItem( R.id.teamInfo ).setVisible( false );


        final String QUERY;
        // Display thing query search results
        //if( getIntent().hasExtra( "mortimer.l.footballmanagement.query" ) )
        //{
        QUERY = getIntent().getExtras().getString( "mortimer.l.footballmanagement.query" );

        //}

        // Get teams
        FirebaseDatabase database =  FirebaseDatabase.getInstance();
        DatabaseReference teamsRef = database.getReference().child( "Teams" );

        teamsRef.addListenerForSingleValueEvent( new ValueEventListener()
        {
            @Override
            public void onDataChange( DataSnapshot snapshot )
            {
                Iterable <DataSnapshot> teams = snapshot.getChildren();

                // Populate the list of teams
                List<Team> teamsList = new LinkedList<>();

                for( DataSnapshot team : teams )
                {
                    teamsList.add( team.getValue( Team.class ) );
                }

                // Set top padding to 80dp so doesn't clash w action bar
                float scale = getResources().getDisplayMetrics().density;
                int padding = (int) ( 8*scale + 0.5f );
                int topPadding = (int) ( 80*scale + 0.5f );

                // Make list of teams that actually match the query
                List<Team> matchingTeams = new LinkedList<>();
                for( Team team : teamsList )
                {
                    if( team.getTeamName().toLowerCase().contains( QUERY.toLowerCase() ) )
                    {
                        matchingTeams.add( team );
                    }
                }


                if( matchingTeams.isEmpty() )
                { // No teams match the query
                    TextView noResults = getTextView();
                    noResults.setPadding(padding, topPadding, padding, padding);
                    noResults.setText( "No matches found for: " + QUERY );
                }
                else { // Print out the results

                    // Loop through teams and output their name to the screen
                    for (int i = 0; i < matchingTeams.size(); i++) {

                        if (i == 0) {
                            // Set padding to not clash w action bar for first item
                            TextView resultFrame = getTextView();
                            resultFrame.setPadding(padding, topPadding, padding, padding);
                            resultFrame.setText( matchingTeams.get(i).getTeamName());
                        } else {
                            // Set text to team name
                            TextView resultFrame = getTextView();
                            resultFrame.setText( matchingTeams.get(i).getTeamName());
                        }
                    }
                }

            }

            @Override
            public void onCancelled( DatabaseError databaseError) {
                System.out.println( "The read failed: " + databaseError.getCode() );
            }
        } );

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
    public boolean onOptionsItemSelected( MenuItem item ) {
        if ( item.getItemId() == android.R.id.home ) {
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

    }
}
