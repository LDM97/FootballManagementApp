    package mortimer.l.footballmanagement;

    public class User
    {
        private String userID;
        private String name;
        private String email;
        private String preferredPositions;
        private String bio;
        private boolean teamOrganiser;

        public User ()
        {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User( String uid, String nameInput, String emailInput, String prefPositionsInput, String bioInput )
        {
            this.userID = uid;
            this.name = nameInput;
            this.email = emailInput;
            this.preferredPositions = prefPositionsInput;
            this.bio = bioInput;

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


    }
