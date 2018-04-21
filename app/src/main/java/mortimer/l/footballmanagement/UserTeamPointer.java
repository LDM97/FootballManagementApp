
    // Simple pointer object, used to point to the given User object in the database.
    // By default users are stored in the "Users" section of the database but the objects are stored
    // under the given Team object once a user joins that team. To save storing the user object twice,
    // this pointer notifies where the user object of a given userId can be found.

    package mortimer.l.footballmanagement;

    public class UserTeamPointer
    {
        String userId;
        String teamId;

        UserTeamPointer()
        {
            // Empty, no argument constructor for firebase calls
        }

        UserTeamPointer( String userId, String teamId )
        {
            this.userId = userId;
            this.teamId = teamId;
        }

        // Team id get and setters
        public void setTeamId( String teamId ){ this.teamId = teamId; }
        public String getTeamId() { return this.teamId; }

        // User id get and setters
        public void setUserId( String userId ){ this.userId = userId; }
        public String getUserId() { return this.userId; }


    }
