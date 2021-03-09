import user.AccessToken;
import user.User;

public class Main {

    public static void main(String[] args){

        User user1 = new User("Abdul Rahman", "12345bg");
        AccessToken token1 = new AccessToken(user1, 14);

        System.out.println(user1);
        System.out.println(token1);
    }

}
