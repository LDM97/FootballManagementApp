package mortimer.l.footballmanagement;

public class CalendarItem
{
    private String eventTitle;
    private String time;
    private String date;
    private String location;
    private String notes;


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
