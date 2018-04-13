    package mortimer.l.footballmanagement;

    public class UserTeamPointer
    {
        String userId;
        String teamId;

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
