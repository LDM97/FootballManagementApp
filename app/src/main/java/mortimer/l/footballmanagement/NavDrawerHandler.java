package mortimer.l.footballmanagement;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class NavDrawerHandler {

    public void itemSelectHandler( MenuItem selectedItem, Context context )
    { // Identify which menu item clicked, and perform the relevant action

        // set item as selected to persist highlight
        // selectedItem.setChecked( true );

        // Add code here to update the UI based on the item selected
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
        }
    }

    public void signOut( Context context )
    {
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
