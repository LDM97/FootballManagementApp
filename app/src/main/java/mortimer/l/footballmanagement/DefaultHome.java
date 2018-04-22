
    // Class to handle the DefaultHome activity. Displays front page data of the team name and
    // next 3 upcoming events. Offers navigation to other screens.

    package mortimer.l.footballmanagement;

    // Android imports
    import android.content.Intent;
    import android.graphics.Color;
    import android.os.Bundle;
    import android.support.v7.app.AppCompatActivity;
    import android.support.v7.widget.Toolbar;
    import android.view.Gravity;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.LinearLayout;
    import android.widget.TextView;
    import android.support.v4.widget.DrawerLayout;
    import android.support.design.widget.NavigationView;
    import android.view.MenuItem;
    import android.support.v4.view.GravityCompat;

    // Firebase imports
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.ValueEventListener;

    // Java imports
    import java.text.ParseException;
    import java.text.SimpleDateFormat;
    import java.util.Collections;
    import java.util.Date;
    import java.util.HashMap;
    import java.util.LinkedList;

    public class DefaultHome extends AppCompatActivity implements View.OnClickListener
    {
        // Get string resources for pointers to database directories
        private String userTeamPointer;
        private String teamsPointer;
        private String eventsPointer;

        // Firebase authenticator and navigation draw handlers
        private FirebaseAuth auth;
        private final NavDrawerHandler navDrawerHandler= new NavDrawerHandler();
        private DrawerLayout navDraw;

        // Layout for dynamically displaying the upcoming events
        private ViewGroup linearLayout;

        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
            super.onCreate( savedInstanceState );
            setContentView( R.layout.activity_default_home );

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
            actionBarTitle.setText( getString( R.string.home_screen_title ) );

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
                    UserTeamPointer pointer = snapshot.child( userTeamPointer ).child( userId ).getValue( UserTeamPointer.class );
                    String teamId = pointer.getTeamId();

                    // Get the team, the text view, and set the name text to equal the team name
                    Team team = snapshot.child( teamsPointer ).child( teamId ).getValue( Team.class );
                    TextView nameOutput = findViewById( R.id.teamName );
                    nameOutput.setText( team.getTeamName() );


                    // Dynamically add data for the next (upto 3) upcoming events

                    // Get DB instance and reference to the team's events list in the database
                    FirebaseDatabase database =  FirebaseDatabase.getInstance();
                    DatabaseReference eventsRef = database.getReference().child( teamsPointer ).child( teamId ).child( eventsPointer );

                    eventsRef.addListenerForSingleValueEvent( new ValueEventListener()
                    {
                        @Override
                        public void onDataChange( DataSnapshot snapshot )
                        {
                            int i = 0;

                            if( !snapshot.hasChildren() )
                            { // Display that there are no upcoming events for this team
                                TextView noEventsOutput = getTextView();
                                noEventsOutput.setText( "There are no upcoming events" );
                            }
                            else { // Loop through and display the upcoming events

                                LinkedList<CalendarItem> events = new LinkedList<>();
                                LinkedList<String> dates = new LinkedList<>();
                                HashMap<String,CalendarItem> dateToObj = new HashMap<>();

                                // Check if date in the past, if it is delete this date
                                Date currentDate = new Date();
                                SimpleDateFormat dateFormat = new SimpleDateFormat( "dd.MM.yyyy" );

                                for ( DataSnapshot eventSnapshot : snapshot.getChildren() ) {
                                    if (i == 3)
                                    { // Dynamically display a max of 3 events
                                        break;
                                    }

                                    // Get the calendar items from the snapshot
                                    CalendarItem event = eventSnapshot.getValue( CalendarItem.class );

                                    Date eventDate = null;
                                    try { // Set event date to Date obj for comparison
                                        eventDate = dateFormat.parse( event.getDate() );
                                    } catch (ParseException e)
                                    {
                                            e.printStackTrace();
                                    }

                                    if( currentDate.compareTo( eventDate ) > 0 )
                                    { // Current date is after the event date, event past delete it
                                        DatabaseReference eventRef = eventSnapshot.getRef();
                                        eventRef.removeValue();
                                    } else { // Setup the event for date ordering
                                        events.add(event);
                                        dateToObj.put(event.getDate(), event);
                                        dates.add(event.getDate());
                                    }

                                }

                                // Sort the events based on date
                                Collections.sort( dates, new StringDateComparator() );

                                for( String date : dates )
                                { // Loop through and display the upcoming events in date order

                                    CalendarItem event = dateToObj.get( date );

                                    // Add calendarItem
                                    linearLayout = (ViewGroup) findViewById(R.id.upcomingEventsContainer);
                                    View upcomingEventItem = LayoutInflater.from(getApplicationContext()).inflate(R.layout.upcoming_events_layout, linearLayout, false);

                                    // Display the title for the event
                                    TextView title = upcomingEventItem.findViewById(R.id.eventName);
                                    title.setText(event.getEventTitle());

                                    // Display the date for the event
                                    TextView eventDate = upcomingEventItem.findViewById(R.id.eventDate);
                                    eventDate.setText(event.getDate());

                                    // Display the location for the event
                                    TextView location = upcomingEventItem.findViewById(R.id.eventLocation);
                                    location.setText(event.getLocation());

                                    // Add the view to the screen w all the event data
                                    linearLayout.addView(upcomingEventItem);

                                    // Set increment
                                    i++;
                                }

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

        private TextView getTextView()
        { // Get a text view to display if there are no upcoming events
            TextView tv = new TextView( this );

            // Set layout and formatting of text view
            tv.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT) );
            tv.setGravity( Gravity.CENTER_HORIZONTAL );
            tv.setTextSize( 18 );
            float scale = getResources().getDisplayMetrics().density;
            int padding = (int) ( 8*scale + 0.5f );
            tv.setTextColor( Color.BLACK );
            tv.setPadding( padding, padding, padding, padding );

            // Add the text view to the linear layout container on the page
            LinearLayout linearLayout = (LinearLayout)findViewById( R.id.upcomingEventsContainer );
            linearLayout.addView( tv );

            return tv;
        }

        @Override
        public boolean onOptionsItemSelected( MenuItem item )
        { // Open the navigation drawer if the menu icon is pressed
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
        { // Exit the app
            finish();
            System.exit(0);
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