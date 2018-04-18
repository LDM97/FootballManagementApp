package mortimer.l.footballmanagement;

import java.util.LinkedList;
import java.util.List;

public class Team
{

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
        this.teamId = teamId;
        this.teamName = teamName;
        this.typeFootball = typeFootball;
        this.teamBio = teamBio;
        this.players = players;
        this.events = new LinkedList<CalendarItem>();
    }

    public List<DiscussionItem> getPosts() {
        List<DiscussionItem> posts = ( this.posts == null ) ? new LinkedList<DiscussionItem>() : this.posts;
        return this.posts;
    }

    public void addPost( DiscussionItem newPost ) {
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

    public void addPlayer( User player ) { this.players.add( player ); }

}
