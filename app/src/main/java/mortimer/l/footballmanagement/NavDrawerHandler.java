
    // Simple class for setting up and handling the navigation drawer on each screen.

    package mortimer.l.footballmanagement;

    // Android imports
    import android.content.Context;
    import android.content.Intent;
    import android.view.MenuItem;
    import android.widget.Toast;

    // Firebase imports
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;

    class NavDrawerHandler
    {

        public void itemSelectHandler( MenuItem selectedItem, Context context )
        { // Identify which menu item clicked, and perform the relevant action

            // React accordingly based on which item is selected
            switch ( selectedItem.getItemId() ) {

                case R.id.teamCalendar :
                    // Take user to team calendar screen
                    Intent teamCalendarActivity = new Intent( context, TeamCalendar.class );
                    context.startActivity( teamCalendarActivity );
                    break;

                case R.id.discussionBoard :
                    // Take user to discussion board screen
                    Intent discussionBoardActivity = new Intent( context, DiscussionBoard.class );
                    context.startActivity( discussionBoardActivity );
                    break;

                case R.id.teamInfo :
                    // Take user to the team info screen
                    Intent teamInfoActivity = new Intent( context, TeamInfo.class );
                    context.startActivity( teamInfoActivity );
                    break;

                case R.id.profileManagement :
                    // Take user to the profile management screen
                    Intent profileManagementActivity = new Intent( context, ProfileManagement.class );
                    context.startActivity( profileManagementActivity );
                    break;
            }
        }

        public void signOut( Context context )
        { // Logout the user if the logout option is selected
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.signOut();

            FirebaseUser currentUser = auth.getCurrentUser();
            // Current user null, sign out successful return user to login screen
            if( currentUser == null )
            {
                // Notify user of succesful logout
                Toast.makeText( context, "You have been signed out",
                        Toast.LENGTH_SHORT).show();

                // Take user back to login screen
                Intent loginActivity = new Intent( context, Login.class );
                context.startActivity( loginActivity );

            }

        }
    }
