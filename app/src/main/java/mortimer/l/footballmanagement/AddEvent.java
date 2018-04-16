package mortimer.l.footballmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedList;
import java.util.List;

public class AddEvent extends AppCompatActivity implements View.OnClickListener
{

    // Inputs
    private EditText eventTitleInput;
    private EditText timeInput;
    private EditText dateInput;
    private EditText locationInput;
    private EditText notesInput;

    private FirebaseAuth auth;
    private NavDrawerHandler navDrawerHandler = new NavDrawerHandler();
    private DrawerLayout navDraw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        // Custom toolbar setup
        Toolbar custToolBar = (Toolbar) findViewById( R.id.my_toolbar );
        setSupportActionBar( custToolBar );

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled( false );

        TextView actionBarTitle = (TextView) findViewById( R.id.toolbarTitle );
        actionBarTitle.setText( "Create Event" );

        actionBar.setDisplayHomeAsUpEnabled( true );
        actionBar.setHomeAsUpIndicator( R.drawable.menu_icon );

        // Get Firebase authenticator
        auth = FirebaseAuth.getInstance();

        // Setup logout button and home button
        findViewById( R.id.navLogout ).setOnClickListener( this );
        findViewById( R.id.homeBtn ).setOnClickListener( this );
        findViewById( R.id.createEventBtn ).setOnClickListener( this );

        // Get views
        eventTitleInput = findViewById( R.id.eventTitleInput );
        timeInput = findViewById( R.id.eventTimeInput );
        dateInput = findViewById( R.id.eventDateInput );
        locationInput = findViewById( R.id.eventLocationInput );
        notesInput = findViewById( R.id.eventNotesInput );

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

    private void createEvent()
    { // User attempts to create a new event

        if( validateForm() )
        { // Only attempt to create the event if the form data has been entered correctly

            // Get a reference to the database
            FirebaseDatabase database =  FirebaseDatabase.getInstance();
            DatabaseReference databaseRef = database.getReference();

            databaseRef.addListenerForSingleValueEvent( new ValueEventListener()
            {
                @Override
                public void onDataChange( DataSnapshot snapshot ) {

                    // Get the inputs
                    String eventTitle = eventTitleInput.getText().toString();
                    String time = timeInput.getText().toString();
                    String date = dateInput.getText().toString();
                    String location = locationInput.getText().toString();
                    String notes = "";

                    if( !notesInput.getText().toString().isEmpty() )
                    { // Notes optional input, fill string if there is data there, else stays as empty string
                        notes = notesInput.getText().toString();
                    }

                    // Get user id, used to locate the team this user plays for
                    FirebaseUser currentUser = auth.getCurrentUser();
                    String userId = currentUser.getUid();

                    // Get the current team id
                    UserTeamPointer pointer = snapshot.child( "UserTeamPointers" ).child( userId ).getValue( UserTeamPointer.class );
                    String teamId = pointer.getTeamId();

                    // Get the team the user is part of and add the event to the team
                    Team team = snapshot.child( "Teams" ).child( teamId ).getValue( Team.class );
                    CalendarItem event = new CalendarItem( eventTitle, time, date, location, notes );

                    List<CalendarItem> events;

                    if( team.getEvents() == null )
                    { // If null / teams first event, make an empty linked list
                        events = new LinkedList<CalendarItem>();
                    } else {
                        events = team.getEvents();
                    }

                    // Add the newly made event to the list of events for this team
                    events.add( event );

                    // Get DB instance and reference to the team's events list in the database
                    FirebaseDatabase database =  FirebaseDatabase.getInstance();
                    DatabaseReference teamRef = database.getReference().child( "Teams" ).child( teamId ).child( "events" );

                    // Update the events list w the list that has the new event added to it
                    teamRef.setValue( events );

                    // Notify user the event has been created
                    Toast.makeText( AddEvent.this, "Event Created",
                            Toast.LENGTH_SHORT).show();

                    // Success return the user to the calendar screen
                    Intent teamCalendarActivity = new Intent( getApplicationContext(), TeamCalendar.class );
                    startActivity( teamCalendarActivity );

                }

                @Override
                public void onCancelled( DatabaseError databaseError) {
                    System.out.println( "The read failed: " + databaseError.getCode() );
                }
            } );
        }

    }

    private boolean validateForm()
    {
        // Go through each field and check that all fields have been filled.
        boolean valid = true;

        // Get the inputs
        String eventTitle = eventTitleInput.getText().toString();
        String time = timeInput.getText().toString();
        String date = dateInput.getText().toString();
        String location = locationInput.getText().toString();

        // Notes optional input
        // String notes = notesInput.getText().toString();

        // Check event title input
        if ( TextUtils.isEmpty( eventTitle ) )
        {
            // No input, inform user and return false
            eventTitleInput.setError( "Required" );
            valid = false;
        } else {
            eventTitleInput.setError(null);
        }

        // Check event time input
        if ( TextUtils.isEmpty( time ) )
        {
            // No input, inform user and return false
            timeInput.setError( "Required" );
            valid = false;
        } else {
            timeInput.setError(null);
        }

        // Check event date input
        if ( TextUtils.isEmpty( date ) )
        {
            // No input, inform user and return false
            dateInput.setError( "Required" );
            valid = false;
        } else {
            dateInput.setError(null);
        }

        // Check event location input
        if ( TextUtils.isEmpty( location ) )
        {
            // No input, inform user and return false
            locationInput.setError( "Required" );
            valid = false;
        } else {
            locationInput.setError(null);
        }

        return valid;
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
    { // Take the user back to the calendar screen
        Intent intent = new Intent(this, TeamCalendar.class );
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
        {
            Intent homeScreenActivity = new Intent( getApplicationContext(), DefaultHome.class );
            startActivity( homeScreenActivity );
        }

        if( v.getId() == R.id.createEventBtn )
        {
            createEvent();
        }

    }

}
