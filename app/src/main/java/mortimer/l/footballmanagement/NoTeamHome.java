package mortimer.l.footballmanagement;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class NoTeamHome extends AppCompatActivity implements View.OnClickListener
{

    private FirebaseAuth auth;
    private NavDrawerHandler navDrawerHandler= new NavDrawerHandler();
    private DrawerLayout navDraw;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_no_team_home );

        // Custom toolbar setup
        Toolbar custToolBar = (Toolbar) findViewById( R.id.my_toolbar );
        setSupportActionBar( custToolBar );

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled( false );

        TextView actionBarTitle = (TextView) findViewById(R.id.toolbarTitle);
        actionBarTitle.setText( "Home" );

        actionBar.setDisplayHomeAsUpEnabled( true );
        actionBar.setHomeAsUpIndicator( R.drawable.menu_icon );

        // Setup logout button and home button
        findViewById(R.id.navLogout).setOnClickListener( this );
        findViewById(R.id.homeBtn).setOnClickListener( this );

        // Get Firebase authenticator
        auth = FirebaseAuth.getInstance();

        // Hide menu items, user not part of team can't access team info screens


        // Buttons
        findViewById( R.id.createTeam ).setOnClickListener( this );

        // Nav drawer code
        navDraw = findViewById( R.id.drawer_layout );
        NavigationView navigationView = findViewById( R.id.nav_view );

        // Hide items, no team not allowed to navigate to team info screens
        Menu nav_Menu = navigationView.getMenu();
        nav_Menu.findItem( R.id.teamCalendar ).setVisible( false );
        nav_Menu.findItem( R.id.discussionBoard ).setVisible( false );
        nav_Menu.findItem( R.id.teamInfo ).setVisible( false );



        // Search view listener
        SearchView searchView = (SearchView) findViewById( R.id.teamSearch ); // inititate a search view

        // perform set on query text listener event
        searchView.setOnQueryTextListener( new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                // do something on text submit
                // Notify user of succesful logout
                Toast.makeText( getApplicationContext(), query,
                        Toast.LENGTH_SHORT ).show();

                SearchView searchView = (SearchView) findViewById( R.id.teamSearch );

                InputMethodManager inputManager = (InputMethodManager)getSystemService( Context.INPUT_METHOD_SERVICE );
                inputManager.hideSoftInputFromWindow( searchView.getWindowToken(), 0);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                // do something when text changes
                return false;
            }
        });

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

        if( v.getId() == R.id.createTeam )
        {
            Intent createTeamActivity = new Intent( getApplicationContext(), CreateTeam.class );
            startActivity( createTeamActivity );
        }

    }

}
