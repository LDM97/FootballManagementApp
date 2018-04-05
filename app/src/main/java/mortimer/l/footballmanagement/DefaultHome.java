package mortimer.l.footballmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import android.support.v4.widget.DrawerLayout;
import android.support.design.widget.NavigationView;
import android.view.MenuItem;
import android.support.v7.app.ActionBar;
import android.support.v4.view.GravityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DefaultHome extends AppCompatActivity implements View.OnClickListener {


    private FirebaseAuth auth;

    private DrawerLayout navDraw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_default_home );

        // Custom toolbar
        Toolbar custToolBar = (Toolbar) findViewById( R.id.my_toolbar );
        setSupportActionBar( custToolBar );

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled( false );

        TextView actionBarTitle = (TextView) findViewById( R.id.toolbarTitle );
        actionBarTitle.setText( "Home" );

        actionBar.setDisplayHomeAsUpEnabled( true );
        actionBar.setHomeAsUpIndicator( R.drawable.menu_icon );


        // Get Firebase authenticator
        auth = FirebaseAuth.getInstance();

        // Nav Draw code
        navDraw = findViewById( R.id.drawer_layout );

        NavigationView navigationView = findViewById( R.id.nav_view );

        /*
        // ==================================================
        // View header = new NavigationView( this );
        NavigationView navView = ( NavigationView ) findViewById( R.id.nav_view );
        View headerView =  navView.getHeaderView( 0 );
        headerView.inflate;
        // TextView nav_user = (TextView) headerView.findViewById( R.id.nav_name );
        // nav_user.setText( "Hello World" );
        */

        navigationView.setNavigationItemSelectedListener
                (
                new NavigationView.OnNavigationItemSelectedListener()
                {
                    @Override
                    public boolean onNavigationItemSelected( MenuItem menuItem ) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        navDraw.closeDrawers();

                        // Add code here to update the UI based on the item selected

                        /*
                        if( menuItem.getItemId() == R.id.logout )
                        {
                            signOut();
                        }
                        */

                        return true;
                    }
                });

        // Set logout navbar listener
        View navBarLogout = findViewById( R.id.navLogout );
        navBarLogout.setOnClickListener( this );
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

    private void signOut()
    {
        auth.signOut();

        FirebaseUser currentUser = auth.getCurrentUser();
        // Current user null, sign out successful return user to login screen
        if( currentUser == null )
        {
            // Notify user of succesful logout
            Toast.makeText( DefaultHome.this, "You have been signed out",
                    Toast.LENGTH_SHORT).show();

            // Take user back to login screen
            Intent loginActivity = new Intent( getApplicationContext(), Login.class );
            startActivity( loginActivity );

        }

    }


    @Override
    public void onClick( View v )
    {
        if ( v.getId() == R.id.navLogout )
        {
            signOut();
        }
    }

}