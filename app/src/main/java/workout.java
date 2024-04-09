public class workout {
    private String time;
    private String location;
    private user [] participants;
    private String sportType;
    private String creator;
    private boolean PrivateWorkout;
    public workout(String time, String location, user [] participants, String sportType, String creator)
    {
        this.time = time;
        this.location = location;
        this.participants = participants;
        this.sportType = sportType;
        this.creator = creator;
        this.PrivateWorkout = false;
    }
    // Getter and Setter for time
    public String getTime() {
        return this.time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    // Getter and Setter for location
    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    // Getter and Setter for participants
    public user [] getParticipants() {
        return this.participants;
    }

    public void setParticipants(user [] participants) {
        this.participants = participants;
    }

    // Getter and Setter for sportType
    public String getSportType() {
        return this.sportType;
    }

    public void setSportType(String sportType) {
        this.sportType = sportType;
    }

    // Getter and Setter for creator
    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    // Getter and Setter for PrivateWorkout
    public boolean isPrivateWorkout() {
        return this.PrivateWorkout;
    }

    public void setPrivateWorkout(boolean privateWorkout) {
        this.PrivateWorkout = privateWorkout;
    }
}
