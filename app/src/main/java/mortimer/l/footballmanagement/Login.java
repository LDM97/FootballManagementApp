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
    import android.content.Intent;

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

    import java.util.List;

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
            setContentView( R.layout.activity_login);

            // Custom toolbar
            Toolbar custToolBar = (Toolbar) findViewById( R.id.my_toolbar );
            setSupportActionBar( custToolBar );
            getSupportActionBar().setDisplayShowTitleEnabled( false );

            TextView actionBarTitle = (TextView) findViewById( R.id.toolbarTitle );
            actionBarTitle.setText( "Login" );

            // Views
            emailInput = findViewById( R.id.emailInput );
            passwordInput = findViewById( R.id.passwordInput );
            createAccountBtn = findViewById( R.id.createAccountBtn );
            signInBtn = findViewById( R.id.signInBtn );

            // Buttons
            findViewById( R.id.createAccountBtn ).setOnClickListener( this );
            findViewById( R.id.signInBtn ).setOnClickListener( this );

            // Get Firebase authenticator
            auth = FirebaseAuth.getInstance();

            // Hide the home button
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
            else
            { // User is not logged in

                // Show the login options
                findViewById( R.id.emailInput ).setVisibility( View.VISIBLE );
                findViewById( R.id.passwordInput ).setVisibility( View.VISIBLE );
                findViewById( R.id.createAccountBtn ).setVisibility( View.VISIBLE );
                findViewById( R.id.signInBtn ).setVisibility( View.VISIBLE );

            }
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
        public void onBackPressed()
        { // Not logged in, do nothing
            return;
        }

        @Override
        public void onClick( View v )
        {
            switch( v.getId() )
            {
                case ( R.id.createAccountBtn ):

                    Intent createAccountActivity = new Intent( getApplicationContext(), CreateAccount.class );
                    startActivity( createAccountActivity );

                case ( R.id.signInBtn ):
                    signIn( emailInput.getText().toString(), passwordInput.getText().toString() );
            }

        }

    }
