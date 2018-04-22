

    //  Class to handle the Add Event activity. Deals with validating inputs for the new event and
    //  writing the new event to the database if all inputs are valid.

    package mortimer.l.footballmanagement;

    // Android imports
    import android.annotation.SuppressLint;
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

    // Jave imports
    import java.util.LinkedList;
    import java.util.List;

    public class AddEvent extends AppCompatActivity implements View.OnClickListener
    {

        // Get string resources for pointers to database directories
        String userTeamPointer;
        String teamsPointer;
        String eventsPointer;

        // Inputs
        private EditText eventTitleInput;
        private EditText timeInput;
        private EditText dateInput;
        private EditText locationInput;
        private EditText notesInput;

        // Firebase auth and navigation drawer items
        private FirebaseAuth auth;
        private NavDrawerHandler navDrawerHandler = new NavDrawerHandler();
        private DrawerLayout navDraw;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_add_event);

            // Get string resources for pointers to database directories
            teamsPointer = getString( R.string.teams_pointer );
            eventsPointer = getString( R.string.events_pointer );
            userTeamPointer = getString( R.string.user_pointers );

            // Custom toolbar setup
            Toolbar custToolBar = (Toolbar) findViewById( R.id.my_toolbar );
            setSupportActionBar( custToolBar );

            android.support.v7.app.ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowTitleEnabled( false );

            TextView actionBarTitle = (TextView) findViewById( R.id.toolbarTitle );
            actionBarTitle.setText( getString( R.string.create_event_title ) );

            actionBar.setDisplayHomeAsUpEnabled( true );
            actionBar.setHomeAsUpIndicator( R.drawable.menu_icon );

            // Get Firebase authenticator
            auth = FirebaseAuth.getInstance();

            // Setup listeners on buttons where required
            findViewById( R.id.navLogout ).setOnClickListener( this );
            findViewById( R.id.homeBtn ).setOnClickListener( this );
            findViewById( R.id.createEventBtn ).setOnClickListener( this );

            // Get the views of the inputs
            eventTitleInput = findViewById( R.id.eventTitleInput );
            timeInput = findViewById( R.id.eventTimeInput );
            dateInput = findViewById( R.id.eventDateInput );
            locationInput = findViewById( R.id.eventLocationInput );
            notesInput = findViewById( R.id.eventNotesInput );

            // Setup the nav drawer
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
                    public void onDataChange( DataSnapshot snapshot )
                    {

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
                        UserTeamPointer pointer = snapshot.child( userTeamPointer ).child( userId ).getValue( UserTeamPointer.class );
                        String teamId = pointer.getTeamId();

                        // Get the team the user is part of and add the event to the team
                        Team team = snapshot.child( teamsPointer ).child( teamId ).getValue( Team.class );
                        CalendarItem event = new CalendarItem( eventTitle, time, date, location, notes );

                        // If teams first event (no existing events) create a new linked list, else get the existing list and add to it
                        List<CalendarItem> events = ( team.getEvents() == null ) ? new LinkedList<CalendarItem>() : team.getEvents();
                        events.add( event );

                        // Get DB instance and reference to the team's events list in the database
                        FirebaseDatabase database =  FirebaseDatabase.getInstance();
                        DatabaseReference eventsRef = database.getReference().child( teamsPointer ).child( teamId ).child( eventsPointer );

                        // Update the events list w the list that has the new event added to it
                        eventsRef.setValue( events );

                        // Notify user the event has been created
                        Toast.makeText( AddEvent.this, "Event Created",
                                Toast.LENGTH_SHORT).show();

                        // Success, return the user to the calendar screen
                        Intent teamCalendarActivity = new Intent( getApplicationContext(), TeamCalendar.class );
                        startActivity( teamCalendarActivity );

                    }

                    @Override
                    public void onCancelled( DatabaseError databaseError)
                    {
                        System.out.println( "The read failed: " + databaseError.getCode() );
                    }
                } );
            }

        }

        private boolean validateForm()
        { // Go through each field and check that all fields have been filled correctly

            boolean valid = true;

            // Get the inputs
            String eventTitle = eventTitleInput.getText().toString();
            String time = timeInput.getText().toString();
            String date = dateInput.getText().toString();
            String location = locationInput.getText().toString();

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
        public boolean onOptionsItemSelected( MenuItem item )
        { // Open the drawer if menu icon pressed
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
            { // If home btn pushed, return the user to the home screen
                Intent homeScreenActivity = new Intent( getApplicationContext(), DefaultHome.class );
                startActivity( homeScreenActivity );
            }

            if( v.getId() == R.id.createEventBtn )
            { // If user attempts to create an event, run the create event function
                createEvent();
            }

        }

    }
