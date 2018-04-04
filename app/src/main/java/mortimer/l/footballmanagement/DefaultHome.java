package mortimer.l.footballmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DefaultHome extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_default_home );

        // Custom toolbar
        Toolbar custToolBar = (Toolbar) findViewById( R.id.my_toolbar );
        setSupportActionBar( custToolBar );
        getSupportActionBar().setDisplayShowTitleEnabled( false );

        TextView actionBarTitle = (TextView) findViewById( R.id.toolbarTitle );
        actionBarTitle.setText( "Home" );

        // Button listener
        findViewById( R.id.logoutBtn ).setOnClickListener( this );
        findViewById( R.id.navBarBtn ).setOnClickListener( this );

        // Get Firebase authenticator
        auth = FirebaseAuth.getInstance();

        findViewById( R.id.navigationView ).setVisibility( View.GONE );
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
        if( v.getId() == R.id.logoutBtn )
        {
            signOut();
        }

        if( v.getId() == R.id.navBarBtn )
        {
            findViewById( R.id.navigationView ).setVisibility( View.VISIBLE );
        }
    }

}