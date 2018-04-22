
    // Class to handle the comments section screen of any post. Dynamically displays the post and any
    // comments related to the post. Also handles the user adding a new comment to the post

    package mortimer.l.footballmanagement;

    // Android importss
    import android.content.Intent;
    import android.graphics.Color;
    import android.os.Bundle;
    import android.support.design.widget.NavigationView;
    import android.support.v4.view.GravityCompat;
    import android.support.v4.widget.DrawerLayout;
    import android.support.v7.app.AppCompatActivity;
    import android.support.v7.widget.Toolbar;
    import android.text.TextUtils;
    import android.view.Gravity;
    import android.view.LayoutInflater;
    import android.view.MenuItem;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.EditText;
    import android.widget.ImageView;
    import android.widget.LinearLayout;
    import android.widget.TextView;
    import android.widget.Toast;

    // Firebase imports
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.ValueEventListener;

    // Java imports
    import java.util.LinkedList;
    import java.util.List;

    public class CommentsSection extends AppCompatActivity implements View.OnClickListener
    {

        // Get string resources for pointers to database directories
        private String userTeamPointer;
        private String teamsPointer;
        private String postsPointer;
        private String commentsPointer;
        private String playersPointer;

        // Firebase authenticator and navigation drawer items
        private FirebaseAuth auth;
        private final NavDrawerHandler navDrawerHandler= new NavDrawerHandler();
        private DrawerLayout navDraw;

        // View group of the containers for the post and comments to be dynamically displayed in
        private ViewGroup postContainer;
        private ViewGroup commentsContainer;

        // Get the text input for the comment a user can add and store the current post veinw viewed
        private EditText addCommentInput;
        private DiscussionItem currentPost;

        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_comments_section);

            // Get string resources for pointers to database directories
            teamsPointer = getString( R.string.teams_pointer );
            postsPointer = getString( R.string.posts_pointer );
            userTeamPointer = getString( R.string.user_pointers );
            commentsPointer = getString( R.string.comments_pointer );
            playersPointer = getString( R.string.players_pointer );


            // Custom toolbar setup
            Toolbar custToolBar = (Toolbar) findViewById( R.id.my_toolbar );
            setSupportActionBar( custToolBar );

            android.support.v7.app.ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowTitleEnabled( false );

            TextView actionBarTitle = (TextView) findViewById( R.id.toolbarTitle );
            actionBarTitle.setText( getString( R.string.discussion_board_title ) );

            actionBar.setDisplayHomeAsUpEnabled( true );
            actionBar.setHomeAsUpIndicator( R.drawable.menu_icon );

            // Get Firebase authenticator
            auth = FirebaseAuth.getInstance();

            // Setup listerns for the buttons on the screen
            findViewById( R.id.navLogout ).setOnClickListener( this );
            findViewById( R.id.homeBtn ).setOnClickListener( this );
            findViewById( R.id.addCommentBtn ).setOnClickListener( this );

            // Get the comment input
            addCommentInput = findViewById( R.id.addComment );

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

            // Get extras to identify the post that has been clicked
            final String discussionTitle = getIntent().getExtras().getString( "discussionTitle" );
            final String discussionText = getIntent().getExtras().getString( "discussionText" );

            // Populate the discussion board with any comments

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
                    UserTeamPointer pointer = snapshot.child( userTeamPointer ).child(userId).getValue(UserTeamPointer.class);
                    String teamId = pointer.getTeamId();

                    // Get selected post
                    DiscussionItem selectedPost = null;

                    for ( DataSnapshot postSnapshot : snapshot.child( teamsPointer ).child(teamId).child( postsPointer ).getChildren() )
                    { // Loop through and find the selected post
                        DiscussionItem post = postSnapshot.getValue(DiscussionItem.class);

                        if (post.getDiscussionTitle().equals( discussionTitle ) && post.getDiscussionText().equals( discussionText ))
                        { // Found, set the selected post to equal this and store the current post for future use
                            selectedPost = post;
                            currentPost = post;
                        }
                    }

                    // Inflate post
                    postContainer = (ViewGroup) findViewById( R.id.postContainer );
                    View postItem = LayoutInflater.from(getApplicationContext()).inflate( R.layout.discussion_board_item, postContainer, false );

                    // Display the title for the post
                    TextView title = postItem.findViewById( R.id.discussionTitle );
                    title.setText( selectedPost.getDiscussionTitle() );

                    // Display the text for the post
                    TextView text = postItem.findViewById( R.id.discussionText );
                    text.setText( selectedPost.getDiscussionText() );

                    // Display the user who made the post
                    UserTeamPointer postCreatorPointer = snapshot.child( userTeamPointer ).child( selectedPost.getUserId() ).getValue( UserTeamPointer.class );
                    String postCreatorName = "";
                    for ( DataSnapshot userSnapshot : snapshot.child( teamsPointer ).child(postCreatorPointer.getTeamId()).child( playersPointer ).getChildren() )
                    { // Loop through the players and find the post creator
                        User userSnapObj = userSnapshot.getValue( User.class );
                        if ( userSnapObj.getUserID().equals(postCreatorPointer.getUserId() ) )
                        { // This obj is the post creator
                            postCreatorName = userSnapObj.getName();
                        }
                    }

                    // Set the text to the creators name
                    TextView postedBy = postItem.findViewById(R.id.postedBy);
                    postedBy.setText("Post By: " + postCreatorName);

                    // Hide the view comments as user viewing comments
                    TextView viewComments = postItem.findViewById(R.id.viewComments );
                    viewComments.setVisibility( View.GONE );

                    // Set the image for the user's icon
                    ImageView playerImage = postItem.findViewById(R.id.playerProfileImage);
                    playerImage.setBackgroundResource(R.drawable.profile_icon_default);

                    // Add the view to the screen w all the event data now added
                    postContainer.addView(postItem);


                    // Get the comments container to display the data in
                    commentsContainer = (ViewGroup) findViewById( R.id.commentsContainer );

                    // Loop through and list the comments
                    List<Comment> existingComments = selectedPost.getComments();
                    if ( existingComments.isEmpty() )
                    { // No comments on this post, tell the user that

                        TextView tv = new TextView( getApplicationContext() );

                        // Set layout and formatting of text view
                        tv.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT) );
                        tv.setGravity( Gravity.CENTER );
                        tv.setTextSize( 24 );
                        float scale = getResources().getDisplayMetrics().density;
                        int padding = (int) ( 10*scale + 0.5f );
                        tv.setTextColor( Color.BLACK );
                        tv.setPadding( padding, padding, padding, padding );

                        tv.setText( "There are no comments on this post" );

                        // Add the text view to the container to display the status
                        commentsContainer.addView( tv );

                    } else { // Display the comments
                        for (Comment comment : selectedPost.getComments()) {
                            // Inflate comment
                            View commentItem = LayoutInflater.from(getApplicationContext()).inflate(R.layout.comment_item, commentsContainer, false);

                            // Display the comment text
                            TextView commentText = commentItem.findViewById(R.id.commentText);
                            commentText.setText(comment.getComment());

                            // Get the commenter and set their name
                            String commenterNameString = "";
                            for( DataSnapshot player : snapshot.child( teamsPointer ).child(teamId).child( playersPointer ).getChildren() )
                            { // Loop through users of this team
                                User playerObj = player.getValue( User.class );
                                if( playerObj.getUserID().equals( comment.getUserId() ) )
                                { // If the user is the commenter get their name so it can be displayed
                                    commenterNameString = playerObj.getName();
                                }
                            }

                            // Display the commenter's name
                            TextView commenterName = commentItem.findViewById(R.id.commenterName);
                            commenterName.setText( commenterNameString );

                            // Set the image for the user's icon
                            ImageView commenterImage = commentItem.findViewById(R.id.playerProfileImage);
                            commenterImage.setBackgroundResource(R.drawable.profile_icon_default);

                            // Display this comment on the page
                            commentsContainer.addView(commentItem);
                        }
                    }
                }

                @Override
                public void onCancelled( DatabaseError databaseError)
                {
                    System.out.println( "The read failed: " + databaseError.getCode() );
                }
            } );

        }

        @Override
        public boolean onOptionsItemSelected( MenuItem item )
        { // If navdraw icon selected, open the nav draw
            if ( item.getItemId() == android.R.id.home )
            {
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
        { // Return user to the discussion board
            Intent intent = new Intent(this, DiscussionBoard.class );
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
            { // If home button pushed, take the user to the home screen
                Intent homeScreenActivity = new Intent( getApplicationContext(), DefaultHome.class );
                startActivity( homeScreenActivity );
            }

            if( v.getId() == R.id.addCommentBtn )
            { // Attempt to add the comment if the add comment button is pressed
                final String comment = addCommentInput.getText().toString();
                if( !TextUtils.isEmpty( comment ) )
                { // There is data in the text

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
                            UserTeamPointer pointer = snapshot.child( userTeamPointer ).child(userId).getValue(UserTeamPointer.class);
                            String teamId = pointer.getTeamId();

                            String postKey = "";
                            for ( DataSnapshot postSnapshot : snapshot.child( teamsPointer ).child( teamId ).child( postsPointer ).getChildren() )
                            { // Loop through and find the selected post
                                DiscussionItem post = postSnapshot.getValue( DiscussionItem.class );

                                if ( post.getDiscussionTitle().equals(currentPost.getDiscussionTitle()) && post.getDiscussionText().equals(currentPost.getDiscussionText() ) )
                                { // Found, remove this post so that it can be overwritten
                                    postKey = postSnapshot.getRef().getKey();
                                }
                            }

                            // Add the new comment to the comments
                            Comment commentObj = new Comment( comment, auth.getUid() );

                            // Add the updated new post to the list and write to the database
                            List<Comment> newComments = new LinkedList<>();

                            if( snapshot.child( teamsPointer ).child( teamId ).child( postsPointer ).child( postKey ).child( commentsPointer ).hasChildren() )
                            {
                                // Already existing comments, add new comment to the end of this list
                                for ( DataSnapshot comment : snapshot.child( teamsPointer ).child(teamId).child( postsPointer ).child(postKey).child( commentsPointer ).getChildren() ) {
                                    Comment currentCommentObj = comment.getValue(Comment.class);
                                    newComments.add(currentCommentObj);
                                }
                            }

                            // Add the new comment and write to the database
                            newComments.add( commentObj );
                            snapshot.child( teamsPointer ).child( teamId ).child( postsPointer ).child( postKey ).child( commentsPointer ).getRef().setValue( newComments );

                            // Refresh the screen to display the new comment
                            finish();
                            startActivity( getIntent() );

                            // Notify user the comment has been added
                            Toast.makeText( CommentsSection.this, "Comment Added",
                                    Toast.LENGTH_SHORT).show();

                        }

                        @Override
                        public void onCancelled( DatabaseError databaseError)
                        {
                            System.out.println( "The read failed: " + databaseError.getCode() );
                        }
                    } );
                }
                else
                    { // Notify the user they must write a comment before trying to add one
                    addCommentInput.setError( "Required" );
                }
            }

        }

    }
