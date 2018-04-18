package mortimer.l.footballmanagement;

import java.util.LinkedList;
import java.util.List;

public class DiscussionItem
{
    private String discussionTitle;
    private String discussionText;
    private String userId;
    private List<Comment> comments;

    public DiscussionItem()
    {
        // Empty constructor used by fFirebase for returning objects
    }

    public DiscussionItem( String title, String text, String userId )
    {
        this.discussionTitle = title;
        this.discussionText = text;
        this.userId = userId;

    }

    public List<Comment> getComments()
    { // Return newly created list if is the first comment
        List<Comment> comments = ( this.comments == null ) ?  new LinkedList<Comment>() : this.comments;
        return comments;
    }

    public void setComments( List<Comment> comments ) {
        this.comments = comments;
    }

    public String getDiscussionText() {
        return discussionText;
    }

    public void setDiscussionText(String discussionText) {
        this.discussionText = discussionText;
    }

    public String getUserId() {

        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDiscussionTitle() {

        return discussionTitle;
    }

    public void setDiscussionTitle(String discussionTitle) {
        this.discussionTitle = discussionTitle;
    }
}
