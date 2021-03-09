package user;

public class User {

    private String username, password;

    public User(String username, String password){
    this.username = username;
    this.password = password;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public String getUsername(){
        return this.username;
    }

    public String getPassword(){
        return this.password;
    }

    @Override
    public String toString(){
        return "Username: " + this.username;
    }
}
