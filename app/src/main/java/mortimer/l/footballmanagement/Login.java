    package mortimer.l.footballmanagement;

    import android.support.v7.app.AppCompatActivity;
    import android.os.Bundle;

    import android.support.v7.widget.Toolbar;
    import android.view.View;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.TextView;
    import android.util.Log;
    import android.text.TextUtils;
    import android.widget.Toast;

    import com.google.android.gms.tasks.OnCompleteListener;
    import com.google.android.gms.tasks.Task;
    import android.support.annotation.NonNull;
    import com.google.firebase.auth.AuthResult;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;

    public class Login extends AppCompatActivity implements View.OnClickListener
    {
        private static final String TAG = "EmailPassword";


        private EditText emailInput;
        private EditText passwordInput;
        private Button createAccountBtn;
        private Button signInBtn;

        private TextView userStatus;
        private Button logoutBtn;

        private FirebaseAuth auth;

        @Override
        protected void onCreate( Bundle savedInstanceState )
        {
            super.onCreate( savedInstanceState );
            setContentView( R.layout.activity_create_account );

            // Custom toolbar
            Toolbar custToolBar = (Toolbar) findViewById(R.id.my_toolbar);
            setSupportActionBar( custToolBar );
            getSupportActionBar().setDisplayShowTitleEnabled( false );

            // Views
            emailInput = findViewById( R.id.emailInput );
            passwordInput = findViewById( R.id.passwordInput );
            createAccountBtn = findViewById( R.id.createAccountBtn );
            signInBtn = findViewById( R.id.signInBtn );

            userStatus = findViewById( R.id.userStatus );
            logoutBtn = findViewById( R.id.logoutBtn );

            // Buttons
            findViewById( R.id.createAccountBtn ).setOnClickListener( this );
            findViewById( R.id.logoutBtn ).setOnClickListener( this );
            findViewById( R.id.signInBtn ).setOnClickListener( this );

            // Get Firebase authenticator
            auth = FirebaseAuth.getInstance();
        }

        @Override
        public void onStart()
        {
            super.onStart();
            // Check if user is signed in (non-null) and update UI accordingly.
            FirebaseUser currentUser = auth.getCurrentUser();
            checkLogin( currentUser );
        }

        private void checkLogin( FirebaseUser user )
        {
            if ( user != null )
            { // User is already logged in

                // Notify the user
                userStatus.setText( "You are logged in!" );

                // Hide login options
                findViewById( R.id.emailInput ).setVisibility( View.GONE );
                findViewById( R.id.passwordInput ).setVisibility( View.GONE );
                findViewById( R.id.createAccountBtn ).setVisibility( View.GONE );
                findViewById( R.id.signInBtn ).setVisibility( View.GONE );

                // Show status and logout options
                findViewById( R.id.userStatus ).setVisibility( View.VISIBLE );
                findViewById( R.id.logoutBtn ).setVisibility( View.VISIBLE );

            }
            else
            { // User is not logged in

                // Show the login options
                findViewById( R.id.emailInput ).setVisibility( View.VISIBLE );
                findViewById( R.id.passwordInput ).setVisibility( View.VISIBLE );
                findViewById( R.id.createAccountBtn ).setVisibility( View.VISIBLE );
                findViewById( R.id.signInBtn ).setVisibility( View.VISIBLE );

                // Hide the status and logout options
                findViewById( R.id.userStatus ).setVisibility( View.GONE );
                findViewById( R.id.logoutBtn ).setVisibility( View.GONE );
            }
        }

        private void createAccount(String email, String password)
        {
            // Check the inputs are there and valid
            Log.d( TAG, "createAccount:" + email );
            if ( !validateForm() ) {
                return;
            }

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
                                checkLogin( user );

                                // Login successful, clear the login details.
                                emailInput.setText( "" );
                                passwordInput.setText( "" );
                            }
                            else
                            {
                                // If sign in fails, display a message to the user.
                                Log.w( TAG, "createUserWithEmail:failure", task.getException() );
                                Toast.makeText( Login.this, "Failed to create Account.",
                                        Toast.LENGTH_SHORT).show();
                                checkLogin( null );
                            }
                        }
                    } );
        }

        private void signIn( String email, String password ) {
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

        private void signOut()
        {
            auth.signOut();
            checkLogin( null );
        }

        private boolean validateForm()
        {
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

            return valid;
        }

        @Override
        public void onClick( View v )
        {
            switch( v.getId() )
            {
                case ( R.id.createAccountBtn ):

                    createAccount( emailInput.getText().toString(), passwordInput.getText().toString() );

                case ( R.id.logoutBtn ):

                    signOut();

                case ( R.id.signInBtn ):
                    signIn( emailInput.getText().toString(), passwordInput.getText().toString() );
            }

        }

    }
