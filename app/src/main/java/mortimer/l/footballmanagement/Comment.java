
    // Simple class to store a comment made by a user on an post on the discussion board. This object
    // is passed to Firebase to store the comment data

    package mortimer.l.footballmanagement;

    class Comment
    {
        // The data required to be stored
        private String comment;
        private String userId;

        public Comment()
        {
            // Empty constructor used by Firebase to return comment objects
        }

        public Comment( String comment, String userId )
        { // Store the data
            this.comment = comment;
            this.userId = userId;
        }

        // Get and setters
        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getUserId() { return userId; }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }
