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

        // Display thing query search results
        if( getIntent().hasExtra( "mortimer.l.footballmanagement.query" ) )
        {
            query = getIntent().getExtras().getString( "mortimer.l.footballmanagement.query" );

            LinearLayout linearLayout = (LinearLayout)findViewById( R.id.content_frame );

            TextView result = new TextView( this );
            result.setText( query );
            result.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT) );
            result.setGravity( Gravity.CENTER );
            result.setTextSize( 24 );
            float scale = getResources().getDisplayMetrics().density;
            int paddingTop = (int) ( 80*scale + 0.5f );
            int padding = (int) ( 8*scale + 0.5f );
            result.setTextColor( Color.BLACK );
            result.setPadding( padding, paddingTop, padding, padding );
            linearLayout.addView( result );
        }

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
