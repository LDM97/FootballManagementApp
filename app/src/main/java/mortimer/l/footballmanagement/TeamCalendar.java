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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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


        // Add calendarItem code
        linearLayout = (ViewGroup) findViewById( R.id.content_frame );

        View calendarItem = LayoutInflater.from( this ).inflate( R.layout.calendar_item_layout, linearLayout, false);

        // TextView textView = (TextView) layout2.findViewById(R.id.button1);
        // textView1.setText(textViewText);

        linearLayout.addView( calendarItem );

        // View horizontalLine = findViewById( R.id.horizontalLine );
        // linearLayout.addView( horizontalLine );

        // Add calendar items
        View calendarItem2 = LayoutInflater.from( this ).inflate( R.layout.calendar_item_layout, linearLayout, false);
        linearLayout.addView( calendarItem2 );

        View calendarItem3 = LayoutInflater.from( this ).inflate( R.layout.calendar_item_layout, linearLayout, false);
        linearLayout.addView( calendarItem3 );

        View calendarItem4 = LayoutInflater.from( this ).inflate( R.layout.calendar_item_layout, linearLayout, false);
        linearLayout.addView( calendarItem4 );


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
