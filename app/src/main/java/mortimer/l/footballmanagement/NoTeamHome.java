
    // Class to handle the home screen activity for users who are not assigned to a team. Offers
    // the user to search for a team to join, or to create their own team

    package mortimer.l.footballmanagement;

    // Android imports
    import android.content.Context;
    import android.content.Intent;
    import android.support.design.widget.NavigationView;
    import android.support.v4.view.GravityCompat;
    import android.support.v4.widget.DrawerLayout;
    import android.support.v7.app.AppCompatActivity;
    import android.os.Bundle;
    import android.support.v7.widget.Toolbar;
    import android.view.Menu;
    import android.view.MenuItem;
    import android.view.View;
    import android.view.inputmethod.InputMethodManager;
    import android.widget.SearchView;
    import android.widget.TextView;

    // Firebase imports
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;

    public class NoTeamHome extends AppCompatActivity implements View.OnClickListener
    {

        // Firebase authenticator and navigation draw handler setup
        private FirebaseAuth auth;
        private final NavDrawerHandler navDrawerHandler= new NavDrawerHandler();
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
            actionBarTitle.setText( getString( R.string.home_screen_title ) );

            actionBar.setDisplayHomeAsUpEnabled( true );
            actionBar.setHomeAsUpIndicator( R.drawable.menu_icon );

            // Setup button listeners
            findViewById(R.id.navLogout).setOnClickListener( this );
            findViewById(R.id.homeBtn).setOnClickListener( this );
            findViewById( R.id.createTeam ).setOnClickListener( this );

            // Get Firebase authenticator
            auth = FirebaseAuth.getInstance();

            // Nav drawer code
            navDraw = findViewById( R.id.drawer_layout );
            NavigationView navigationView = findViewById( R.id.nav_view );

            // Hide the menu items. User not part of a team, cannot view these screens. Can only logout
            Menu nav_Menu = navigationView.getMenu();
            nav_Menu.findItem( R.id.teamCalendar ).setVisible( false );
            nav_Menu.findItem( R.id.discussionBoard ).setVisible( false );
            nav_Menu.findItem( R.id.teamInfo ).setVisible( false );

            // Get the search view
            SearchView searchView = (SearchView) findViewById( R.id.teamSearch );

            // perform set on query text listener event
            searchView.setOnQueryTextListener( new SearchView.OnQueryTextListener()
            {
                @Override
                public boolean onQueryTextSubmit(String query)
                {

                    SearchView searchView = (SearchView) findViewById( R.id.teamSearch );

                    // Remove virtual keyboard after search input
                    InputMethodManager inputManager = (InputMethodManager)getSystemService( Context.INPUT_METHOD_SERVICE );
                    inputManager.hideSoftInputFromWindow( searchView.getWindowToken(), 0);


                    // Pass the query to the results display
                    Intent searchResults = new Intent( getApplicationContext(), TeamSearchResults.class );
                    searchResults.putExtra( "mortimer.l.footballmanagement.query", query );
                    startActivity( searchResults );

                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText)
                {
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
        public boolean onOptionsItemSelected( MenuItem item )
        { // Open the navigation drawer if the nav draw button is selected
            if ( item.getItemId() == android.R.id.home ) {
                navDraw.openDrawer( GravityCompat.START );
                return true;
            }
            return super.onOptionsItemSelected( item );
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

            if( v.getId() == R.id.createTeam )
            { // Take the user to the create team screen
                Intent createTeamActivity = new Intent( getApplicationContext(), CreateTeam.class );
                startActivity( createTeamActivity );
            }

        }

    }
