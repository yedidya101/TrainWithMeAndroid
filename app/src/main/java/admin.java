public class admin extends user{
    public admin(String Name, String Last_name, String Birthdate, String Gender, String Region, String Email)
    {
        super(Name, Last_name, Birthdate, Gender, Region, Email);
    }
    public void deleteWorkout(workout w){
        // send to server protocol for example: send(workout w, 1) 2 is code for delete workout
    }
    public void muteUser(user u)
    {
        // send to server protocol for example send(user u, 2) 1 is code for mute user from create workout.
    }
    public void banUser(user u)
    {
        // send to server protocol for example send(user u, 3) 1 is code for ban user from the app.
    }
}
