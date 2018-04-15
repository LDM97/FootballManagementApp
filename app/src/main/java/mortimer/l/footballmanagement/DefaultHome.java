package mortimer.l.footballmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import android.support.v4.widget.DrawerLayout;
import android.support.design.widget.NavigationView;
import android.view.MenuItem;
import android.support.v4.view.GravityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DefaultHome extends AppCompatActivity implements View.OnClickListener
{

    private FirebaseAuth auth;
    private NavDrawerHandler navDrawerHandler= new NavDrawerHandler();
    private DrawerLayout navDraw;

    private ViewGroup linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_default_home );

        // Custom toolbar setup
        Toolbar custToolBar = (Toolbar) findViewById( R.id.my_toolbar );
        setSupportActionBar( custToolBar );

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled( false );

        TextView actionBarTitle = (TextView) findViewById( R.id.toolbarTitle );
        actionBarTitle.setText( "Home" );

        actionBar.setDisplayHomeAsUpEnabled( true );
        actionBar.setHomeAsUpIndicator( R.drawable.menu_icon );

        // Get Firebase authenticator
        auth = FirebaseAuth.getInstance();

        // Setup logout button
        findViewById( R.id.navLogout ).setOnClickListener( this );

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

        // Get the team name and set the text view to equal the team name
        // Get a reference to the database
        FirebaseDatabase database =  FirebaseDatabase.getInstance();
        DatabaseReference databaseRef = database.getReference();

        databaseRef.addListenerForSingleValueEvent( new ValueEventListener()
        {
            @Override
            public void onDataChange( DataSnapshot snapshot ) {

                // Get user id, used to locate the team this user plays for
                FirebaseUser currentUser = auth.getCurrentUser();
                String userId = currentUser.getUid();

                // Get the current team id
                UserTeamPointer pointer = snapshot.child( "UserTeamPointers" ).child( userId ).getValue( UserTeamPointer.class );
                String teamId = pointer.getTeamId();

                // Get the team, the text view, and set the name text to equal the team name
                Team team = snapshot.child( "Teams" ).child( teamId ).getValue( Team.class );
                TextView nameOutput = findViewById( R.id.teamName );
                nameOutput.setText( team.getTeamName() );



                // ==== Dynamically add data for the next (upto 3) upcoming events ====

                // Get DB instance and reference to the team's events list in the database
                FirebaseDatabase database =  FirebaseDatabase.getInstance();
                DatabaseReference eventsRef = database.getReference().child( "Teams" ).child( teamId ).child( "events" );

                eventsRef.addListenerForSingleValueEvent( new ValueEventListener()
                {
                    @Override
                    public void onDataChange( DataSnapshot snapshot )
                    {
                        int i = 0;

                        for( DataSnapshot eventSnapshot : snapshot.getChildren() )
                        {
                            if( i == 3 )
                            { // Dynamically display a max of 3 events
                                break;
                            }

                            CalendarItem event = eventSnapshot.getValue( CalendarItem.class );

                            // Add calendarItem
                            linearLayout = (ViewGroup) findViewById( R.id.upcomingEventsContainer );
                            View upcomingEventItem = LayoutInflater.from( getApplicationContext() ).inflate( R.layout.upcoming_events_layout, linearLayout, false);

                            // Display the title for the event
                            TextView title = upcomingEventItem.findViewById( R.id.eventName );
                            title.setText( event.getEventTitle() );

                            // Display the date for the event
                            TextView date = upcomingEventItem.findViewById( R.id.eventDate );
                            date.setText( event.getDate() );

                            // Display the location for the event
                            TextView location = upcomingEventItem.findViewById( R.id.eventLocation );
                            location.setText( event.getLocation() );

                            // Add the view to the screen w all the event data
                            linearLayout.addView( upcomingEventItem );

                            // Set increment
                            i++;
                        }

                    }

                    @Override
                    public void onCancelled( DatabaseError databaseError)
                    {
                        System.out.println( "The read failed: " + databaseError.getCode() );
                    }
                } );

            }

            @Override
            public void onCancelled( DatabaseError databaseError)
            {
                System.out.println( "The read failed: " + databaseError.getCode() );
            }
        } );
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
    public void onClick( View v )
    {
        if( v.getId() == R.id.navLogout )
        { // If logout clicked on nav drawer, run the signout function
            View thisView = findViewById(android.R.id.content);
            navDrawerHandler.signOut( thisView.getContext() );
        }

    }

}