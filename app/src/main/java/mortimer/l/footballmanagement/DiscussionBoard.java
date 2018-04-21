
    // Class for handling the DiscussionBoard activity. Dynamically displays all existing posts, and
    // allows navigation to view the comments for a selected post

    package mortimer.l.footballmanagement;

    // Android imports
    import android.content.Intent;
    import android.support.design.widget.NavigationView;
    import android.support.v4.view.GravityCompat;
    import android.support.v4.widget.DrawerLayout;
    import android.support.v7.app.AppCompatActivity;
    import android.os.Bundle;
    import android.support.v7.widget.Toolbar;
    import android.view.LayoutInflater;
    import android.view.MenuItem;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.ImageView;
    import android.widget.TextView;

    // Firebase imports
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.ValueEventListener;

    // Java imports
    import java.util.HashMap;
    import java.util.Map;

    public class DiscussionBoard extends AppCompatActivity implements View.OnClickListener
    {

        // Firebase authenticator and nav draw handlers
        private FirebaseAuth auth;
        private NavDrawerHandler navDrawerHandler= new NavDrawerHandler();
        private DrawerLayout navDraw;

        // Layout to dynamically display the posts
        private ViewGroup linearLayout;

        // Map the posts to the DiscussionItem object to identify the post when clicked
        private Map<View,DiscussionItem> postItemToObj = new HashMap<>();

        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_discussion_board);

            // Custom toolbar setup
            Toolbar custToolBar = (Toolbar) findViewById( R.id.my_toolbar );
            setSupportActionBar( custToolBar );

            android.support.v7.app.ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowTitleEnabled( false );

            TextView actionBarTitle = (TextView) findViewById( R.id.toolbarTitle );
            actionBarTitle.setText( "Discussion Board" );

            actionBar.setDisplayHomeAsUpEnabled( true );
            actionBar.setHomeAsUpIndicator( R.drawable.menu_icon );

            // Get Firebase authenticator
            auth = FirebaseAuth.getInstance();

            // Setup logout button and home button
            findViewById( R.id.navLogout ).setOnClickListener( this );
            findViewById( R.id.homeBtn ).setOnClickListener( this );
            findViewById( R.id.addPost ).setOnClickListener( this );

            // Nav drawer code
            navDraw = findViewById( R.id.drawer_layout );
            NavigationView navigationView = findViewById( R.id.nav_view );

            navigationView.setNavigationItemSelectedListener
                    (
                            new NavigationView.OnNavigationItemSelectedListener()
                            {
                                @Override
                                public boolean onNavigationItemSelected( MenuItem menuItem ) {

                                    // close drawer when item is tapped
                                    navDraw.closeDrawers();

                                    // Pass selected item and context to handle view
                                    View thisView = findViewById(android.R.id.content);
                                    navDrawerHandler.itemSelectHandler( menuItem, thisView.getContext() );

                                    return true;
                                }
                            });

            // Get a reference to the database
            FirebaseDatabase database =  FirebaseDatabase.getInstance();
            DatabaseReference databaseRef = database.getReference();

            databaseRef.addListenerForSingleValueEvent( new ValueEventListener()
            {
                @Override
                public void onDataChange( DataSnapshot snapshot )
                {

                    // Get user id, used to locate the team this user plays for
                    FirebaseUser currentUser = auth.getCurrentUser();
                    final String userId = currentUser.getUid();

                    // Get the current team id
                    UserTeamPointer pointer = snapshot.child("UserTeamPointers").child(userId).getValue(UserTeamPointer.class);
                    String teamId = pointer.getTeamId();

                    for ( DataSnapshot postSnapshot : snapshot.child( "Teams" ).child( teamId ).child( "posts" ).getChildren() )
                    { // Loop through and display the posts
                        DiscussionItem post = postSnapshot.getValue( DiscussionItem.class );

                        // Inflate post
                        linearLayout = (ViewGroup) findViewById( R.id.content_frame );
                        View postItem = LayoutInflater.from( getApplicationContext() ).inflate( R.layout.discussion_board_item, linearLayout, false);

                        // Display the title for the post
                        TextView title = postItem.findViewById( R.id.discussionTitle );
                        title.setText( post.getDiscussionTitle() );

                        // Display the text for the post
                        TextView text = postItem.findViewById( R.id.discussionText );
                        text.setText( post.getDiscussionText() );

                        // Display the user who made the post
                        UserTeamPointer postCreatorPointer = snapshot.child("UserTeamPointers").child( post.getUserId() ).getValue(UserTeamPointer.class);
                        String postCreatorName = "";
                        for( DataSnapshot userSnapshot : snapshot.child( "Teams" ).child( postCreatorPointer.getTeamId() ).child( "players" ).getChildren() )
                        {
                            User userSnapObj = userSnapshot.getValue( User.class );
                            if( userSnapObj.getUserID().equals( postCreatorPointer.getUserId() ) )
                            { // This obj is the post creator
                                postCreatorName = userSnapObj.getName();
                            }
                        }

                        // Set the text to the creators name
                        TextView postedBy = postItem.findViewById( R.id.postedBy );
                        postedBy.setText( "Post By: " + postCreatorName );

                        // Set the image for the user's icon
                        ImageView playerImage = postItem.findViewById( R.id.playerProfileImage );
                        playerImage.setBackgroundResource(R.drawable.profile_icon_default);


                        // Set listener on the post for user to view comments
                        postItem.setClickable( true );
                        setListener( postItem );

                        // Map the post itself to the post object. Used to get reference when clicked
                        postItemToObj.put( postItem, post );

                        // Add the view to the screen w all the event data
                        linearLayout.addView( postItem );
                    }
                }

                @Override
                public void onCancelled( DatabaseError databaseError)
                {
                    System.out.println( "The read failed: " + databaseError.getCode() );
                }
            } );
        }

        private void setListener( View v )
        {
            v.setOnClickListener( this );
        }

        @Override
        public boolean onOptionsItemSelected( MenuItem item )
        { // Open the navigation drawer if the menu icon is clicked
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

        @Override
        public void onBackPressed()
        { // Return user to the home screen
            Intent intent = new Intent(this, DefaultHome.class );
            startActivity( intent );
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
            { // Return the user to the home screen if home button is clicked
                Intent homeScreenActivity = new Intent( getApplicationContext(), DefaultHome.class );
                startActivity( homeScreenActivity );
            }

            if( postItemToObj.containsKey( v ) )
            { // Post has been clicked, take the user to that post's comments section
                DiscussionItem post = postItemToObj.get( v );

                // Pass the data required to identify the post and start the comments section activity
                Intent commentsSection = new Intent( getApplicationContext(), CommentsSection.class );
                commentsSection.putExtra( "discussionTitle", post.getDiscussionTitle() );
                commentsSection.putExtra( "discussionText", post.getDiscussionText() );

                startActivity( commentsSection );

            }

            if( v.getId() == R.id.addPost )
            { // Take user to the add post screen to create a new post
                Intent addPost = new Intent( getApplicationContext(), CreateDiscussion.class );
                startActivity( addPost );
            }

        }
    }
