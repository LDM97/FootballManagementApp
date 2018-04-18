package mortimer.l.footballmanagement;

public class Comment
{
    private String comment;
    private String userId;

    public Comment()
    {
        // Empty constructor used by Firebase to return comment objects
    }

    public Comment( String comment, String userId )
    {
        this.comment = comment;
        this.userId = userId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUserId() {

        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
