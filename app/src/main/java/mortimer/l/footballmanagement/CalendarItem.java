package mortimer.l.footballmanagement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CalendarItem
{
    private String eventTitle;
    private String time;
    private String date;
    private String location;
    private String notes;
    private List<String> playersGoing;
    private List<String> playersNotGoing;


    CalendarItem()
    {
        // Default constructor required for calls to DataSnapshot.getValue(CalendarItem.class)
    }

    CalendarItem( String eventTitle, String time, String date, String location, String notes )
    {
        this.eventTitle = eventTitle;
        this.time =  time;
        this.date = date;
        this.location = location;
        this.notes = notes;
    }

    // Can set a player as going and get this list
    public void setPlayerGoing ( String userId )
    {
        List<String> going = ( this.playersGoing == null ) ? new ArrayList<String>() : this.playersGoing;
        going.add( userId );
        this.playersGoing = going;

        List<String> notGoing = ( this.getPlayersNotGoing() == null ) ? new ArrayList<String>() : this.getPlayersNotGoing();
        if( notGoing.contains( userId ) )
        { // Remove user from not going list if they are there
            notGoing.remove( userId );
        }
    }

    public List<String> getPlayersGoing() { return this.playersGoing; }

    // Can set a player as not going and get this list
    public void setPlayerNotGoing( String userId )
    {
        List<String> notGoing = ( this.playersNotGoing == null ) ? new ArrayList<String>() : this.playersNotGoing;
        notGoing.add( userId );
        this.playersNotGoing = notGoing;

        List<String> going = ( this.getPlayersGoing() == null ) ? new ArrayList<String>() : this.getPlayersGoing();
        if( going.contains( userId ) )
        { // Remove user from going if they are there
            going.remove( userId );
        }
    }

    public List<String> getPlayersNotGoing() { return this.playersNotGoing; }

    // Get and setter for notes
    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Get and setter for location
    public String getLocation() {

        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    // Get and set for date
    public String getDate() {

        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    // Get and set for time
    public String getTime() {

        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    // Get and set for event title
    public String getEventTitle() {

        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }
}
