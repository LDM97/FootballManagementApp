
    // Simple class to store data on a Team. Used to store and retrieve team data from the Firebase
    // database

    package mortimer.l.footballmanagement;

    // Java imports
    import java.util.LinkedList;
    import java.util.List;

    class Team
    {
        // Team data to be stored
        private String teamId;
        private String teamName;
        private String typeFootball;
        private String teamBio;
        private List<User> players;
        private List<CalendarItem> events;
        private List<DiscussionItem> posts;

        public Team ()
        {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        Team( String teamId, String teamName, String typeFootball, String teamBio, List<User> players )
        {
            // Store the team data
            this.teamId = teamId;
            this.teamName = teamName;
            this.typeFootball = typeFootball;
            this.teamBio = teamBio;
            this.players = players;
            this.events = new LinkedList<>();
        }

        public List<DiscussionItem> getPosts()
        { // Get the teams posts / discussion items. Return empty list if no posts currently exist
            List<DiscussionItem> posts = ( this.posts == null ) ? new LinkedList<DiscussionItem>() : this.posts;
            return this.posts;
        }

        public void addPost( DiscussionItem newPost )
        { // Add a given post to the posts for this team
            List<DiscussionItem> posts = ( this.posts == null ) ? new LinkedList<DiscussionItem>() : this.posts;
            posts.add( newPost );
            this.posts = posts;
        }

        // Get events and add event to the events list
        public List<CalendarItem> getEvents() { return this.events; }

        public void addEvent( CalendarItem event ) { this.events.add( event ); }

        // teamId getter and setters
        public String getTeamId() { return this.teamId; }

        public void setTeamId( String teamId ) { this.teamId = teamId; }

        // team name getter and setters
        public String getTeamName() { return this.teamName; }

        public void setTeamName( String teamName ) { this.teamName = teamName; }

        // type of football getter and setters
        public String getTypeFootball() { return this.typeFootball; }

        public void setTypeFootball( String typeFootball ) { this.typeFootball = typeFootball; }

        // team bio getter and setters
        public String getTeamBio() { return this.teamBio; }

        public void setTeamBio( String teamBio ) { this.teamBio = teamBio; }

        // players getter and setters
        public List getPlayers() { return this.players; }

        public void setPlayers( List<User> players ) { this.players = players; }

        // allow a team to add a new player
        public void addPlayer( User player ) { this.players.add( player ); }

    }
