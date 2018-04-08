package mortimer.l.footballmanagement;

import android.content.Intent;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedList;
import java.util.List;

public class CreateTeam extends AppCompatActivity implements View.OnClickListener
{

    private EditText teamNameInput;
    private EditText typeFootballInput;
    private EditText teamBioInput;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_create_team );

        // Custom toolbar setup
        Toolbar custToolBar = (Toolbar) findViewById( R.id.my_toolbar );
        setSupportActionBar( custToolBar );

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled( false );

        TextView actionBarTitle = (TextView) findViewById( R.id.toolbarTitle );
        actionBarTitle.setText( "Create Team" );

        actionBar.setDisplayHomeAsUpEnabled( true );
        actionBar.setHomeAsUpIndicator( R.drawable.menu_icon );

        // Get Firebase authenticator
        auth = FirebaseAuth.getInstance();

        // Views
        teamNameInput = findViewById( R.id.teamNameInput );
        typeFootballInput = findViewById( R.id.typeFootballInput );
        teamBioInput = findViewById( R.id.teamBioInput );

        // Buttons
        findViewById( R.id.createTeamBtn ).setOnClickListener( this );
        findViewById( R.id.homeBtn ).setOnClickListener( this );
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

    private void createTeam()
    {
        // Upload data to db / create the team.

        FirebaseUser currentUser = auth.getCurrentUser();
        String uid = currentUser.getUid();

        // Get a reference to our current user
        FirebaseDatabase database =  FirebaseDatabase.getInstance();
        DatabaseReference currentUserRef = database.getReference().child( "Users" ).child( uid );

        currentUserRef.addListenerForSingleValueEvent( new ValueEventListener()
        {
            @Override
            public void onDataChange( DataSnapshot snapshot ) {

                // do some stuff once
                User currentUserObj = snapshot.getValue( User.class );

                // Get the inputs
                String teamName = teamNameInput.getText().toString();
                String typeFootball = typeFootballInput.getText().toString();
                String teamBio = teamBioInput.getText().toString();

                List<User> players = new LinkedList<User>();
                currentUserObj.setTeamOrganiser( true ); // Make team creator default organiser
                players.add( currentUserObj );

                // Get DB instance and create a new user object
                FirebaseDatabase database =  FirebaseDatabase.getInstance();

                // get db reference, get "reference location relative to this one".
                // Child( "Users" ) get location of users relative to the database,
                String id = snapshot.getKey();
                Team newTeam = new Team( id, teamName, typeFootball, teamBio, players );
                DatabaseReference dbRef = database.getReference().child( "Teams" ).child( id );
                dbRef.setValue( newTeam );

                // Delete the user from the list of users w no team as user now stored in team
                DatabaseReference userRef = database.getReference().child( "Users" ).child( currentUserObj.getUserID() );
                userRef.removeValue();


                // Notify user the team has been created
                Toast.makeText( CreateTeam.this, "Team Created",
                        Toast.LENGTH_SHORT).show();

                // Return user to home screen
                Intent homeActivity = new Intent( getApplicationContext(), DefaultHome.class );
                startActivity( homeActivity );
            }

            @Override
            public void onCancelled( DatabaseError databaseError) {
                System.out.println( "The read failed: " + databaseError.getCode() );
            }
        } );

    }

    private boolean validateForm()
    {
        // Go through each field and check that all fields have been filled.
        boolean valid = true;

        // Check there is an email input
        String teamName = teamNameInput.getText().toString();
        String typeFootball = typeFootballInput.getText().toString();
        String teamBio = teamBioInput.getText().toString();

        // Check team name input
        if ( TextUtils.isEmpty( teamName ) )
        {
            // No input, inform user and return false
            teamNameInput.setError( "Required" );
            valid = false;
        }
        else
        {
            teamNameInput.setError( null );
        }

        // Check type of football input
        if( TextUtils.isEmpty( typeFootball ) )
        {
            // No input, inform user and return false
            typeFootballInput.setError( "Required" );
            valid = false;
        }
        else
        {
            typeFootballInput.setError( null );
        }

        // Check team bio input
        if( TextUtils.isEmpty( teamBio ) )
        {
            // No input, inform user and return false
            teamBioInput.setError( "Required" );
            valid = false;
        }
        else
        {
            teamBioInput.setError( null );
        }

        return valid;
    }

    @Override
    public void onClick( View v )
    {
        if ( v.getId() == R.id.createTeamBtn )
        { // User attempts to create team w entered info

            if( validateForm() ){
                createTeam();
            }

        }

        if( v.getId() == R.id.homeBtn )
        {
            FirebaseUser currentUser = auth.getCurrentUser();
            String uid = currentUser.getUid();

            // See if current user already assigned to a team
            FirebaseDatabase database =  FirebaseDatabase.getInstance();
            DatabaseReference currentUserDefaultDataRef = database.getReference().child( "Users" ).child( uid );


            currentUserDefaultDataRef.addListenerForSingleValueEvent( new ValueEventListener()
            {
                @Override
                public void onDataChange( DataSnapshot snapshot )
                {

                    if ( snapshot.exists() )
                    { // Exists, user has no team
                        Intent noTeamHomeActivity = new Intent( getApplicationContext(), NoTeamHome.class );
                        startActivity( noTeamHomeActivity );
                    }
                    else
                    { // snapshot doesn't exist, login registered to a team
                        Intent defaultHomeActivity = new Intent( getApplicationContext(), DefaultHome.class );
                        startActivity( defaultHomeActivity );
                    }
                }


                @Override
                public void onCancelled( DatabaseError databaseError ) {
                    System.out.println( "The read failed: " + databaseError.getCode() );
                }

            } );
        }
    }

}
