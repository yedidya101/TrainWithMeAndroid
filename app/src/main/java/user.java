public class user {
    protected String Email;
    protected String Name;
    protected String Last_name;
    protected String Birthdate;
    protected String Gender;
    protected String Region;

    public user(String Name, String Last_name, String Birthdate, String Gender, String Region, String Email)
    {
        this.Name = Name;
        this.Last_name = Last_name;
        this.Birthdate = Birthdate;
        this.Gender = Gender;
        this.Region = Region;
        this.Email = Email;
    }
    // Getter and Setter for Name
    public String getName() {
        return this.Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    // Getter and Setter for Last_name
    public String getLast_name() {
        return this.Last_name;
    }

    public void setLast_name(String last_name) {
        this.Last_name = last_name;
    }

    // Getter and Setter for Birthdate
    public String getBirthdate() {
        return this.Birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.Birthdate = birthdate;
    }

    // Getter and Setter for Gender
    public String getGender() {
        return this.Gender;
    }

    public void setGender(String gender) {
        this.Gender = gender;
    }

    // Getter and Setter for Region
    public String getRegion() {
        return this.Region;
    }

    public void setRegion(String region) {
        this.Region = region;
    }

}
