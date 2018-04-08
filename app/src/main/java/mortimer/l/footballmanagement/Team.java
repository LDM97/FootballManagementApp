package mortimer.l.footballmanagement;

import java.util.List;

public class Team
{

    private String teamId;
    private String teamName;
    private String typeFootball;
    private String teamBio;
    private List<User> players;

    public Team ()
    {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    Team( String teamId, String teamName, String typeFootball, String teamBio, List<User> players )
    {
        this.teamId = teamId;
        this.teamName = teamName;
        this.typeFootball = typeFootball;
        this.teamBio = teamBio;
        this.players = players;
    }

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

    public void addPlayer( User player ) { this.players.add( player ); }

}
