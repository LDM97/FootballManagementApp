
    // Simple class to store the data about a user. Stored in an object and passed too and from the
    // Firebase database for storage

    package mortimer.l.footballmanagement;

    class User
    {
        // User details to be stored
        private String userID;
        private String name = "";
        private String email;
        private String preferredPositions;
        private String bio;
        private boolean teamOrganiser;
        private String team;

        public User ()
        {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User( String uid, String nameInput, String emailInput, String prefPositionsInput, String bioInput )
        {
            // Store the data about the user
            this.userID = uid;
            this.name = nameInput;
            this.email = emailInput;
            this.preferredPositions = prefPositionsInput;
            this.bio = bioInput;
            this.team = "";

        }

        // User ID get and setters
        public String getUserID()
        {
            return this.userID;
        }

        public void setUserID( String UID )
        {
            this.userID = UID;
        }

        // Name get and setters
        public String getName()
        {
            return this.name;
        }

        public void setName( String name )
        {
            this.name = name;
        }

        // Email get and setters
        public String getEmail()
        {
            return this.email;
        }

        public void setEmail( String email )
        {
            this.email = email;
        }

        // Preferred position get and setters
        public String getPreferredPositions()
        {
            return this.preferredPositions;
        }

        public void setPreferredPositions( String positions )
        {
            this.preferredPositions = positions;
        }

        // Bio get and setters
        public String getBio()
        {
            return this.bio;
        }

        public void setBio( String bio )
        {
            this.bio = bio;
        }

        // Team organiser get and setters
        public boolean getTeamOrganiser() { return this.teamOrganiser; }

        public void setTeamOrganiser( boolean teamOrganiser ) { this.teamOrganiser = teamOrganiser; }

        // team id get and setters
        public String getTeam() { return this.team; }

        public void setTeam( String teamId ){ this.team = teamId; }


    }
