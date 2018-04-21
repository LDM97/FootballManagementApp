
    // Simple object for storing the data related to a post on the discussion board. Passed and
    // returned from Firebase for storage.

    package mortimer.l.footballmanagement;

    // Java imports
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
            // Empty constructor used by Firebase for returning objects
        }

        public DiscussionItem( String title, String text, String userId )
        { // Set the post data
            this.discussionTitle = title;
            this.discussionText = text;
            this.userId = userId;

        }

        // Get and setter for comments
        public List<Comment> getComments()
        {
            // Return newly created list if is the first comment
            List<Comment> comments = ( this.comments == null ) ?  new LinkedList<Comment>() : this.comments;
            return comments;
        }

        public void setComments( List<Comment> comments )
        {
            this.comments = comments;
        }

        // Get and setter for text
        public String getDiscussionText()
        {
            return discussionText;
        }

        public void setDiscussionText(String discussionText)
        {
            this.discussionText = discussionText;
        }

        // Get and setter for the user ID of the post creator
        public String getUserId()
        {

            return userId;
        }

        public void setUserId(String userId)
        {
            this.userId = userId;
        }

        // Get and setter for the post title
        public String getDiscussionTitle()
        {
            return discussionTitle;
        }

        public void setDiscussionTitle(String discussionTitle)
        {
            this.discussionTitle = discussionTitle;
        }
    }
