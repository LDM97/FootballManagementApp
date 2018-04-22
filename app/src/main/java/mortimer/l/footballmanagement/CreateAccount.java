
    // Class to handle the CreateAccount activity. Handles user inputs for the account data to be
    // created, writes these valid inputs as a new user to the database.

    package mortimer.l.footballmanagement;

    // Andorid imports
    import android.content.Intent;
    import android.os.Bundle;
    import android.support.annotation.NonNull;
    import android.support.v7.app.AppCompatActivity;
    import android.support.v7.widget.Toolbar;
    import android.text.TextUtils;
    import android.util.Log;
    import android.view.View;
    import android.widget.EditText;
    import android.widget.TextView;
    import android.widget.Toast;

    // Firebase imports
    import com.google.android.gms.tasks.OnCompleteListener;
    import com.google.android.gms.tasks.Task;
    import com.google.firebase.auth.AuthResult;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.DatabaseReference;


    public class CreateAccount extends AppCompatActivity implements View.OnClickListener
    {

        // Setup the id for the type of account to be created, used by Firebase
        private static final String TAG = "EmailPassword";

        // Get string resources for pointers to database directories
        String userPointer;

        // Get the inputs for the new account data
        private EditText emailInput;
        private EditText passwordInput;
        private EditText nameInput;
        private EditText prefPositionsInput;
        private EditText bioInput;

        // Get the Firebase authenticator to check logins
        private FirebaseAuth auth;

        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
            super.onCreate( savedInstanceState );
            setContentView( R.layout.activity_create_account );

            userPointer = getString( R.string.user_pointers );

            // Custom toolbar setup
            Toolbar custToolBar = (Toolbar) findViewById( R.id.my_toolbar );
            setSupportActionBar( custToolBar );
            getSupportActionBar().setDisplayShowTitleEnabled( false );
            TextView actionBarTitle = (TextView) findViewById( R.id.toolbarTitle );
            actionBarTitle.setText( getString( R.string.create_account_title ) );

            // Get the user input views
            emailInput = findViewById( R.id.newAccEmail );
            passwordInput = findViewById( R.id.newAccPassword );
            nameInput = findViewById( R.id.newAccName );
            prefPositionsInput = findViewById( R.id.newAccPrefPositions );
            bioInput = findViewById( R.id.newAccBio );

            // Setup the button listeners
            findViewById( R.id.createNewAccBtn ).setOnClickListener( this );

            // Get Firebase authenticator
            auth = FirebaseAuth.getInstance();

            // Hide the home button, user not logged in yet
            findViewById( R.id.homeBtn ).setVisibility( View.GONE );
        }

        private void createAccount( String email, String password )
        { // Attempt to create the new account

            // Check the inputs are there and valid
            Log.d( TAG, "createAccount:" + email );
            if ( !validateForm() )
            {
                return;
            }

            // Valid inputs, create the account on Firebase database
            auth.createUserWithEmailAndPassword( email, password )
                    .addOnCompleteListener( this, new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete( @NonNull Task<AuthResult> task )
                        {
                            if ( task.isSuccessful() )
                            {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d( TAG, "createUserWithEmail:success" );
                                FirebaseUser user = auth.getCurrentUser();

                                // Authentication success, upload the inputted data to the real time
                                // Firebase database.
                                uploadData();

                                // Login successful, clear the create acc details.
                                emailInput.setText( "" );
                                passwordInput.setText( "" );
                                nameInput.setText( "" );
                                prefPositionsInput.setText( "" );
                                bioInput.setText( "" );

                                // Run login to change to logged in activity.
                                checkLogin( user );
                            }
                            else
                            {
                                // If sign in fails, display a message to the user.
                                Log.w( TAG, "createUserWithEmail:failure", task.getException() );
                                Toast.makeText( CreateAccount.this, "Failed to create Account.",
                                        Toast.LENGTH_SHORT).show();
                                checkLogin( null );
                            }
                        }
                    } );
        }

        private void uploadData()
        { // Authentication & form validation success in createAccount func, upload data to the database

            // Get the inputs
            String newEmail = emailInput.getText().toString();
            String newName = nameInput.getText().toString();
            String newPositions = prefPositionsInput.getText().toString();
            String newBio = bioInput.getText().toString();

            // Get the userID
            FirebaseUser user = auth.getCurrentUser();
            String uid = user.getUid();

            // Get DB instance and create a new user object
            FirebaseDatabase database =  FirebaseDatabase.getInstance();
            User newUser = new User( uid, newName, newEmail, newPositions, newBio );

            // Get database reference and write the data of the new user to the database
            DatabaseReference dbRef = database.getReference().child( userPointer ).child( uid );
            dbRef.setValue( newUser );
        }

        private boolean validateForm()
        { // Check all data inputted is valid before trying to create the new account

            // Go through each field and check that all fields have been filled.
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

            // Check there is a name input
            String name = nameInput.getText().toString();
            if( TextUtils.isEmpty( password ) )
            {
                // No input, inform the user and return false
                nameInput.setError( "Required" );
                valid = false;
            }

            // Check there is an preferred positions input
            String prefPositions = prefPositionsInput.getText().toString();
            if ( TextUtils.isEmpty( prefPositions ) )
            {
                // No input, inform user and return false
                prefPositionsInput.setError( "Required" );
                valid = false;
            }
            else
            {
                prefPositionsInput.setError( null );
            }

            // Check there is an bio input
            String bio = bioInput.getText().toString();
            if ( TextUtils.isEmpty( bio ) )
            {
                // No input, inform user and return false
                bioInput.setError( "Required" );
                valid = false;
            }
            else
            {
                bioInput.setError( null );
            }

            // Return valid boolean, true if all fields present, false if at least one field is empty.
            return valid;
        }


        private void checkLogin( FirebaseUser user )
        {
            if ( user != null )
            { // User is already logged in

                // Send the user to the home screen.
                Intent loginActivity = new Intent( getApplicationContext(), Login.class );
                startActivity( loginActivity );

            }
            // else Do nothing, still need to create account
        }

        @Override
        public void onBackPressed()
        { // Take user back to login screen
            Intent intent = new Intent(this, Login.class);
            startActivity( intent );
        }

        @Override
        public void onClick( View v )
        {
            if ( v.getId() == R.id.createNewAccBtn )
            { // User attempts to create new account, run create account code
                    createAccount( emailInput.getText().toString(), passwordInput.getText().toString() );
            }

        }

    }
