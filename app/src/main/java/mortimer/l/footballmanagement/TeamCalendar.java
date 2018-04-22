
    // Class for handling the TeamCalendar activity. Dynamically displays any events a team has
    // Dynamically displays attendance for a given event on click and allows users to set their
    // attendance for any event

    package mortimer.l.footballmanagement;

    // Android imports
    import android.content.Intent;
    import android.support.design.widget.FloatingActionButton;
    import android.support.design.widget.NavigationView;
    import android.support.v4.view.GravityCompat;
    import android.support.v4.widget.DrawerLayout;
    import android.support.v7.app.AppCompatActivity;
    import android.os.Bundle;
    import android.support.v7.widget.Toolbar;
    import android.view.Gravity;
    import android.view.LayoutInflater;
    import android.view.MenuItem;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.Button;
    import android.widget.ImageView;
    import android.widget.LinearLayout;
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

    // Java imports
    import java.text.ParseException;
    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.Collections;
    import java.util.Date;
    import java.util.HashMap;
    import java.util.LinkedList;
    import java.util.List;
    import java.util.Map;

    import static android.view.View.GONE;

    public class TeamCalendar extends AppCompatActivity implements View.OnClickListener
    {
        // Get string resources for pointers to database directories
        String userTeamPointer;
        String teamsPointer;
        String playersPointer;
        String eventsPointer;

        private FirebaseAuth auth;
        private NavDrawerHandler navDrawerHandler= new NavDrawerHandler();
        private DrawerLayout navDraw;

        private ViewGroup linearLayout;
        private PopupWindow popupWindow;
        private Map<View,CalendarItem> attendanceBtnToEvent = new HashMap<>();
        private Map<View,CalendarItem> goingBtnToEvent = new HashMap<>();
        private Map<View,CalendarItem> notGoingBtnToEvent = new HashMap<>();
        private String teamId = "";

        @Override
        protected void onCreate( Bundle savedInstanceState )
        {
            super.onCreate( savedInstanceState );
            setContentView( R.layout.activity_team_calendar );

            // Get string resources for pointers to database directories
            teamsPointer = getString( R.string.teams_pointer );
            eventsPointer = getString( R.string.events_pointer );
            userTeamPointer = getString( R.string.user_pointers );
            playersPointer = getString( R.string.players_pointer );

            // Custom toolbar setup
            Toolbar custToolBar = (Toolbar) findViewById( R.id.my_toolbar );
            setSupportActionBar( custToolBar );

            android.support.v7.app.ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowTitleEnabled( false );

            TextView actionBarTitle = (TextView) findViewById( R.id.toolbarTitle );
            actionBarTitle.setText( getString( R.string.team_calendar_title ) );

            actionBar.setDisplayHomeAsUpEnabled( true );
            actionBar.setHomeAsUpIndicator( R.drawable.menu_icon );

            // Setup logout button and home button
            findViewById( R.id.navLogout ).setOnClickListener( this );
            findViewById( R.id.homeBtn ).setOnClickListener( this );

            // Get Firebase authenticator
            auth = FirebaseAuth.getInstance();

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
                    UserTeamPointer pointer = snapshot.child( userTeamPointer ).child( userId ).getValue( UserTeamPointer.class );
                    String teamId = pointer.getTeamId();

                    for( DataSnapshot userSnapshot : snapshot.child( teamsPointer ).child( teamId ).child( playersPointer ).getChildren() )
                    { // Get the current user and check if they are an organiser
                        User user = userSnapshot.getValue( User.class );

                        if( user.getUserID().equals( userId ) )
                        { // Current user found
                            if( !user.getTeamOrganiser() )
                            { // User is not the team organiser, hide the add event button
                                findViewById( R.id.addEvent ).setVisibility( GONE );
                            }
                                break; // Operation done if required, break
                        }
                    }

                    // Display the calendar items that already exist
                    LinkedList<CalendarItem> events = new LinkedList<>();
                    LinkedList<String> dates = new LinkedList<>();
                    HashMap<String,CalendarItem> dateToObj = new HashMap<>();

                    // Check if date in the past, if it is delete this date
                    Date currentDate = new Date();
                    SimpleDateFormat dateFormat = new SimpleDateFormat( "dd.MM.yyyy" );

                    for( DataSnapshot eventSnapshot : snapshot.child( teamsPointer ).child( teamId ).child( eventsPointer ).getChildren() )
                    {
                        // Get the calendar items from the snapshot
                        CalendarItem event = eventSnapshot.getValue(CalendarItem.class);

                        Date eventDate = null;
                        try { // Set event date to Date obj for comparison
                            eventDate = dateFormat.parse( event.getDate() );
                            } catch (ParseException e) {
                            e.printStackTrace();
                            }

                        if( currentDate.compareTo( eventDate ) > 0 )
                        { // Current date is after the event date, event past delete it
                            DatabaseReference eventRef = eventSnapshot.getRef();
                            eventRef.removeValue();
                        }
                        else { // Setup the event for date ordering
                            events.add(event);
                            dateToObj.put(event.getDate(), event);
                            dates.add(event.getDate());
                        }
                    }

                    // Sort the events based on date
                    Collections.sort( dates, new StringDateComparator() );

                    for( String date : dates )
                    { // Append items based on date order

                        CalendarItem event = dateToObj.get( date );

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
                        TextView eventDate = calendarItem.findViewById( R.id.eventDate );
                        eventDate.setText( event.getDate() );

                        // Display the location for the event
                        TextView location = calendarItem.findViewById( R.id.eventLocation );
                        location.setText( event.getLocation() );


                        // Set listener on the attendance button
                        Button attendanceBtn = calendarItem.findViewById( R.id.viewAttendanceBtn );
                        setListener( attendanceBtn );

                        // Map attendance button to the calendar item
                        attendanceBtnToEvent.put( (View) attendanceBtn, event );

                        // Add the view to the screen w all the event data
                        linearLayout.addView( calendarItem );
                    }

                }

                @Override
                public void onCancelled( DatabaseError databaseError) {
                    System.out.println( "The read failed: " + databaseError.getCode() );
                }
            } );

        }

        private void setListener( Button btn )
        { // Given a button, set the listener.
            // Used to set the listeners for dynamically generated buttons inside a calendar item View
            btn.setOnClickListener( this );
        }

        @Override
        public boolean onOptionsItemSelected( MenuItem item )
        { // Open the navigation drawer if the navigation icon is clicked
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

        private List<CalendarItem> deleteEvent( List<CalendarItem> events, CalendarItem event )
        { // Auxillary function to delete an event from the current events list based on its hash code
            for( CalendarItem item : events )
            {
                if( item.getDate().equals( event.getDate() ) && item.getTime().equals( event.getTime() ) )
                { // Remove the item
                    events.remove( item );
                    break;
                }
            }
            return events;
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
            { // Return the user to the home screen if the home icon is selected
                Intent homeScreenActivity = new Intent( getApplicationContext(), DefaultHome.class );
                startActivity( homeScreenActivity );
            }

            if( v.getId() == R.id.closePopup )
            { // close the popup
                popupWindow.dismiss();
            }

            if( v.getId() == R.id.goingBtn || v.getId() == R.id.notGoingBtn )
            { // Handle attendance selection event

                final View view = v;

                // Get a reference to the database
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference databaseRef = database.getReference();

                databaseRef.addListenerForSingleValueEvent( new ValueEventListener()
                {
                    @Override
                    public void onDataChange( DataSnapshot snapshot )
                    {
                        // Get team ref for overwrite
                        Team team = snapshot.child( teamsPointer ).child( teamId ).getValue( Team.class );

                        List<CalendarItem> events = ( team.getEvents() == null ) ? new ArrayList<CalendarItem>() : team.getEvents();
                        String userId = auth.getUid();


                        // If going button, add to calendar item using method, write to database
                        if( view.getId() == R.id.goingBtn )
                        {
                            CalendarItem event = goingBtnToEvent.get( view );
                            events = deleteEvent( events, event );
                            event.setPlayerGoing( auth.getUid() );
                            events.add( event );
                            // Add the newly made event to the list of events for this team

                            // Notify user they are attending event
                            Toast.makeText( TeamCalendar.this, "You are attending this event",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // If not going button, add to calendar item using method, write to database
                        if( view.getId() == R.id.notGoingBtn )
                        {
                            CalendarItem event = notGoingBtnToEvent.get( view );
                            events = deleteEvent( events, event );
                            event.setPlayerNotGoing( auth.getUid() );
                            events.add( event );
                            // Add the newly made event to the list of events for this team

                            // Notify user they are not attending the event
                            Toast.makeText( TeamCalendar.this, "You are not attending this event",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // Get DB instance and reference to the team's events list in the database
                        FirebaseDatabase database =  FirebaseDatabase.getInstance();
                        DatabaseReference teamRef = database.getReference().child( teamsPointer ).child( teamId ).child( eventsPointer );

                        // Update the events list w the list that has the new event added to it
                        teamRef.setValue( events );
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }
                });

            }

            if( v.getId() == R.id.viewAttendanceBtn )
            { // Create the popup to display attendance

                // Get layout container and inflate it
                LayoutInflater popupLayout = (LayoutInflater) getApplicationContext().getSystemService( LAYOUT_INFLATER_SERVICE );
                final ViewGroup popupContainer = (ViewGroup) popupLayout.inflate( R.layout.attendance_popup, null );

                // Set listener for close button
                popupContainer.findViewById( R.id.closePopup ).setOnClickListener( this );

                // Get parent layout
                RelativeLayout parentLayout = findViewById( R.id.calendarRelLayout );

                // Display popup window
                popupWindow = new PopupWindow( popupContainer, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true );
                popupWindow.showAtLocation( parentLayout, Gravity.CENTER, 0, 35 );


                // Get the corresponding event based on which attendance button is pressed
                CalendarItem event = attendanceBtnToEvent.get( v );

                // Set the event title on the popup
                TextView popupTitle = popupContainer.findViewById( R.id.popupTitle );
                popupTitle.setText( event.getEventTitle() );

                final List<String> playersGoing = ( event.getPlayersGoing() == null ) ? new ArrayList<String>() : event.getPlayersGoing();
                final List<String> playersNotGoing = ( event.getPlayersNotGoing() == null ) ? new ArrayList<String>() : event.getPlayersNotGoing();

                // Assign button mappings to map the button to the event
                final Button goingBtn = popupContainer.findViewById( R.id.goingBtn );
                goingBtnToEvent.put( (View) goingBtn, event );
                goingBtn.setOnClickListener( this );
                if( playersGoing.contains( auth.getUid() ) )
                { // If player is going to event already, cannot see going button
                    goingBtn.setVisibility( View.GONE );
                }

                final Button notGoingBtn = popupContainer.findViewById( R.id.notGoingBtn );
                notGoingBtnToEvent.put( (View) notGoingBtn, event );
                notGoingBtn.setOnClickListener( this );
                if( playersNotGoing.contains( auth.getUid() ) )
                { // Player not going, hide not going button
                    notGoingBtn.setVisibility( View.GONE );
                }

                // Get a reference to the database
                FirebaseDatabase database =  FirebaseDatabase.getInstance();
                DatabaseReference databaseRef = database.getReference();

                databaseRef.addListenerForSingleValueEvent( new ValueEventListener()
                {
                    @Override
                    public void onDataChange( DataSnapshot snapshot ) {

                        // Get user id, used to locate the team this user plays for
                        FirebaseUser currentUser = auth.getCurrentUser();
                        final String userId = currentUser.getUid();

                        // Get the current team id
                        UserTeamPointer pointer = snapshot.child( userTeamPointer ).child(userId).getValue(UserTeamPointer.class);
                        teamId = pointer.getTeamId();


                        for (DataSnapshot userSnapshot : snapshot.child( teamsPointer ).child( teamId ).child( playersPointer ).getChildren() )
                        {

                            User player = userSnapshot.getValue(User.class);
                            LinearLayout attendanceList;

                            if( playersGoing.contains( player.getUserID() ) )
                            { // Set list as the going list
                                attendanceList = popupContainer.findViewById( R.id.playersGoingContainer );
                            }
                            else if( playersNotGoing.contains( player.getUserID() ) )
                            { // Set list as the not going list
                                attendanceList = popupContainer.findViewById( R.id.playersNotGoingContainer );
                            }
                            else { // Set the list as no response
                                attendanceList = popupContainer.findViewById( R.id.playersNotRespondedContainer );
                            }


                            // Draw the player item into the selected list
                            View playerListItem = LayoutInflater.from(getApplicationContext()).inflate(R.layout.players_list_item_layout, attendanceList, false);

                            // Display the player's name
                            TextView playerNameText = playerListItem.findViewById(R.id.playerName);
                            playerNameText.setText(player.getName());

                            // Display the player's positions
                            TextView playerPositionsText = playerListItem.findViewById(R.id.playerPositions);
                            playerPositionsText.setText(player.getPreferredPositions());

                            // Set the image for the user's icon
                            ImageView playerImage = playerListItem.findViewById( R.id.playerProfileImage );
                            playerImage.setBackgroundResource(R.drawable.profile_icon_default);
                            playerImage.setMaxWidth( 10 );
                            playerImage.setMaxHeight( 5 );

                            // Add the view to the screen w all the event data
                            attendanceList.addView(playerListItem);
                        }

                    }

                    @Override
                    public void onCancelled( DatabaseError databaseError )
                    {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }
                });

            }
        }
    }
