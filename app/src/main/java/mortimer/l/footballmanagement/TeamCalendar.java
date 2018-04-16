package mortimer.l.footballmanagement;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedList;
import java.util.List;

public class TeamCalendar extends AppCompatActivity implements View.OnClickListener
{

    private FirebaseAuth auth;
    private NavDrawerHandler navDrawerHandler= new NavDrawerHandler();
    private DrawerLayout navDraw;

    private ViewGroup linearLayout;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_team_calendar );

        // Custom toolbar setup
        Toolbar custToolBar = (Toolbar) findViewById( R.id.my_toolbar );
        setSupportActionBar( custToolBar );

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled( false );

        TextView actionBarTitle = (TextView) findViewById( R.id.toolbarTitle );
        actionBarTitle.setText( "Team Calendar" );

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

        // Get a reference to the database
        FirebaseDatabase database =  FirebaseDatabase.getInstance();
        DatabaseReference databaseRef = database.getReference();

        databaseRef.addListenerForSingleValueEvent( new ValueEventListener()
        {
            @Override
            public void onDataChange( DataSnapshot snapshot )
            {

                // Get user id, used to locate the team this user plays for
                FirebaseUser currentUser = auth.getCurrentUser();
                final String userId = currentUser.getUid();

                // Get the current team id
                UserTeamPointer pointer = snapshot.child( "UserTeamPointers" ).child( userId ).getValue( UserTeamPointer.class );
                String teamId = pointer.getTeamId();

                // Get user reference
                FirebaseDatabase database =  FirebaseDatabase.getInstance();
                DatabaseReference playersRef = database.getReference().child( "Teams" ).child( teamId ).child( "players" );

                playersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        for( DataSnapshot userSnapshot : dataSnapshot.getChildren() )
                        {
                            User user = userSnapshot.getValue( User.class );

                            if( user.getUserID().equals( userId ) )
                            { // Current user found
                                if( !user.getTeamOrganiser() )
                                { // User is not the team organiser, hide the add event button
                                    findViewById( R.id.addEvent ).setVisibility( View.GONE );
                                }
                                break; // Operation done if required, break
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });

                // Get DB instance and reference to the team's events list in the database
                DatabaseReference eventsRef = database.getReference().child( "Teams" ).child( teamId ).child( "events" );

                eventsRef.addListenerForSingleValueEvent( new ValueEventListener()
                {
                    @Override
                    public void onDataChange( DataSnapshot snapshot )
                    {
                        for( DataSnapshot eventSnapshot : snapshot.getChildren() )
                        {
                            CalendarItem event = eventSnapshot.getValue( CalendarItem.class );

                            // Add calendarItem
                            linearLayout = (ViewGroup) findViewById( R.id.content_frame );
                            View calendarItem = LayoutInflater.from( getApplicationContext() ).inflate( R.layout.calendar_item_layout, linearLayout, false);

                            // Display the title for the event
                            TextView title = calendarItem.findViewById( R.id.eventTitle );
                            title.setText( event.getEventTitle() );

                            // Display the notes for the event
                            TextView notes = calendarItem.findViewById( R.id.eventNotes );
                            notes.setText( event.getNotes() );

                            // Display the time for the event
                            TextView time = calendarItem.findViewById( R.id.eventTime );
                            time.setText( event.getTime() );

                            // Display the date for the event
                            TextView date = calendarItem.findViewById( R.id.eventDate );
                            date.setText( event.getDate() );

                            // Display the location for the event
                            TextView location = calendarItem.findViewById( R.id.eventLocation );
                            location.setText( event.getLocation() );

                            // Add the view to the screen w all the event data
                            linearLayout.addView( calendarItem );
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
            public void onCancelled( DatabaseError databaseError) {
                System.out.println( "The read failed: " + databaseError.getCode() );
            }
        } );


        // Set listener for FAB add event click
        FloatingActionButton myFab = (FloatingActionButton) findViewById( R.id.addEvent );
        myFab.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            { // Send user to the add event screen to create a new event
                Intent addEventActivity = new Intent( getApplicationContext(), AddEvent.class );
                startActivity( addEventActivity );
            }
        });
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
    public void onBackPressed()
    { // Take user back to the home screen
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
        {
            Intent homeScreenActivity = new Intent( getApplicationContext(), DefaultHome.class );
            startActivity( homeScreenActivity );
        }
    }
}
