package mortimer.l.footballmanagement;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class TeamCalendar extends AppCompatActivity implements View.OnClickListener
{

    private FirebaseAuth auth;
    private NavDrawerHandler navDrawerHandler= new NavDrawerHandler();
    private DrawerLayout navDraw;

    private ViewGroup linearLayout;
    private PopupWindow popupWindow;

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

                        // Used to sort the dates of the eents
                        class StringDateComparator implements Comparator<String>
                        {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                            public int compare(String lhs, String rhs)
                            {
                                try
                                {
                                    return dateFormat.parse(lhs).compareTo(dateFormat.parse(rhs));
                                } catch (ParseException e)
                                {
                                    e.printStackTrace();
                                }
                                // Default return on failure
                                return 0;
                            }

                        }

                        LinkedList<CalendarItem> events = new LinkedList<>();
                        LinkedList<String> dates = new LinkedList<>();
                        HashMap<String,CalendarItem> dateToObj = new HashMap<>();

                        // Check if date in the past, if it is delete this date
                        String currentDate = new SimpleDateFormat( "dd.MM.yyyy").format( new Date() );
                        SimpleDateFormat dateFormat = new SimpleDateFormat( "dd.MM.yyyy" );

                        for( DataSnapshot eventSnapshot : snapshot.getChildren() )
                        { // Get the calendaar items from the snapshot
                            CalendarItem event = eventSnapshot.getValue(CalendarItem.class);

                            if( currentDate.compareTo( event.getDate() ) > 0 )
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

    private void setListener( Button btn )
    {

        btn.setOnClickListener( this );
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

        if( v.getId() == R.id.closePopup )
        { // close the popup
            popupWindow.dismiss();
            // Dim the background around the popup window
                //RelativeLayout calendarMainLayout = (RelativeLayout) findViewById( R.id.calendarRelLayout );
                //calendarMainLayout.getForeground().setAlpha( 0 );
        }

        if( v.getId() == R.id.viewAttendanceBtn )
        {
            // Get layout container and inflate it
            LayoutInflater popupLayout = (LayoutInflater) getApplicationContext().getSystemService( LAYOUT_INFLATER_SERVICE );
            ViewGroup container = (ViewGroup) popupLayout.inflate( R.layout.attendance_popup, null );

            // Set listener for close button
            container.findViewById( R.id.closePopup ).setOnClickListener( this );

            // Get parent layout
            RelativeLayout parentLayout = findViewById( R.id.calendarRelLayout );

            // Get the height and width of the screen, to display the popup accordingly
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels - 10;
            int width = displayMetrics.widthPixels - 10;

            // Dim the background around the popup window
                //RelativeLayout calendarMainLayout = (RelativeLayout) findViewById( R.id.calendarRelLayout );
                //calendarMainLayout.getForeground().setAlpha( 220 );

            // Display popup window
            popupWindow = new PopupWindow( container, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true );
            popupWindow.showAtLocation( parentLayout, Gravity.CENTER, 0, 0 );

        }
    }
}
