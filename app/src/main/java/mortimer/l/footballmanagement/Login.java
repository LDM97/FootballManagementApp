
    // Class to handle the Login activity. Takes in a user's login details, validates them and either
    // logs the user in or denies access.

    package mortimer.l.footballmanagement;

    // Android imports
    import android.support.v7.app.AppCompatActivity;
    import android.os.Bundle;
    import android.support.v7.widget.Toolbar;
    import android.view.View;
    import android.widget.EditText;
    import android.widget.TextView;
    import android.util.Log;
    import android.text.TextUtils;
    import android.widget.Toast;
    import android.content.Intent;

    // Firebase imports
    import com.google.android.gms.tasks.OnCompleteListener;
    import com.google.android.gms.tasks.Task;
    import android.support.annotation.NonNull;
    import com.google.firebase.auth.AuthResult;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.ValueEventListener;

    public class Login extends AppCompatActivity implements View.OnClickListener
    {
        private static final String TAG = "EmailPassword";

        // Email and password input
        private EditText emailInput;
        private EditText passwordInput;

        // Firebase authenticator
        private FirebaseAuth auth;

        @Override
        protected void onCreate( Bundle savedInstanceState )
        {
            super.onCreate( savedInstanceState );
            setContentView( R.layout.activity_login);

            // Custom toolbar setup
            Toolbar custToolBar = (Toolbar) findViewById( R.id.my_toolbar );
            setSupportActionBar( custToolBar );
            getSupportActionBar().setDisplayShowTitleEnabled( false );

            TextView actionBarTitle = (TextView) findViewById( R.id.toolbarTitle );
            actionBarTitle.setText( getString( R.string.login_title ) );

            // Get the views of the user inputs
            emailInput = findViewById( R.id.emailInput );
            passwordInput = findViewById( R.id.passwordInput );

            // Setup listeners on the buttons
            findViewById( R.id.createAccountBtn ).setOnClickListener( this );
            findViewById( R.id.signInBtn ).setOnClickListener( this );

            // Get Firebase authenticator
            auth = FirebaseAuth.getInstance();

            // Hide the home button, not logged in yet
            findViewById( R.id.homeBtn ).setVisibility( View.GONE );
        }

        @Override
        public void onStart()
        {
            super.onStart();
            // Check if user is signed in (non-null) and update UI accordingly
            FirebaseUser currentUser = auth.getCurrentUser();
            checkLogin( currentUser );
        }

        private void checkLogin( FirebaseUser user )
        {
            if ( user != null )
            { // User is already logged in

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
                        { // Exists, user has no team, sign in to the no team home screen
                            Intent noTeamHomeActivity = new Intent( getApplicationContext(), NoTeamHome.class );
                            startActivity( noTeamHomeActivity );
                        }
                        else
                        { // snapshot doesn't exist, login registered to a team, sign in to default home screen
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

        private void signIn( String email, String password )
        { // Attempt to login the user

            // Only attempt sign in if login credentials are present
            Log.d( TAG, "signIn:" + email );
            if ( !validateForm() ) {
                return;
            }

            auth.signInWithEmailAndPassword( email, password )
                    .addOnCompleteListener( this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete( @NonNull Task<AuthResult> task )
                        {
                            if ( task.isSuccessful() )
                            {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d( TAG, "signInWithEmail:success" );
                                FirebaseUser user = auth.getCurrentUser();
                                checkLogin( user );

                                // Sign in successful, clear the login details
                                emailInput.setText( "" );
                                passwordInput.setText( "" );
                            }
                            else
                            {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText( Login.this, "Sign in Failed.",
                                        Toast.LENGTH_SHORT ).show();
                                checkLogin( null );
                            }

                        }
                    });
        }

        private boolean validateForm()
        { // Check the login credentials have been entered

            boolean valid = true;

            // Check there is an email input
            String email = emailInput.getText().toString();
            if ( TextUtils.isEmpty( email ) )
            {
                // No input, inform user and return false
                emailInput.setError( "Required" );
                valid = false;
            }
            else
            {
                emailInput.setError( null );
            }

            // Check there is a password input
            String password = passwordInput.getText().toString();
            if ( TextUtils.isEmpty( password ) )
            {
                // No input, inform the user and return false
                passwordInput.setError( "Required" );
                valid = false;
            }
            else
            {
                passwordInput.setError( null );
            }

            // False if at least one input is not present
            return valid;
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
            switch( v.getId() )
            {
                case ( R.id.createAccountBtn ): // Take the user to the create account screen

                    Intent createAccountActivity = new Intent( getApplicationContext(), CreateAccount.class );
                    startActivity( createAccountActivity );

                case ( R.id.signInBtn ): // Attempt to sign in the user with the given credentials
                    signIn( emailInput.getText().toString(), passwordInput.getText().toString() );
            }

        }

    }
