package user;

public class AccessToken {

    private final String TOKEN_SUFFIX = "87";
    private final int TOKEN_SUFFIX_SIZE = TOKEN_SUFFIX.length();
    private int tokenLength;
    private String username;

    public AccessToken(String username){
        this.username = username;
        this.tokenLength = this.username.length() + TOKEN_SUFFIX_SIZE;
    }

    public AccessToken(Client client){
        this.username = client.getUsername();
        this.tokenLength = this.username.length() + TOKEN_SUFFIX_SIZE;
    }

    public AccessToken(String username, int tokenLength){
        this.username = username;
        if (tokenLength > username.length() + TOKEN_SUFFIX_SIZE)
            this.tokenLength = this.username.length() + TOKEN_SUFFIX_SIZE;
        else
            this.tokenLength = tokenLength;
    }

    public AccessToken(Client client, int tokenLength){
        this.username = client.getUsername();
        if (tokenLength  > username.length() + TOKEN_SUFFIX_SIZE)
            this.tokenLength = this.username.length() + TOKEN_SUFFIX_SIZE;
        else
            this.tokenLength = tokenLength;
    }


    public void setUsername(String username) {
        this.username = username;
    }
    public String getUsername() {
        return username;
    }

    /**
     * Given an endIndex, returns a string that consists of the username concatinated with the a pre-specified suffix
     * This returned token will be substring whose end is specified by the endIndex variable.
     * @param endIndex Specifies the last index of the token.
     * @return returns a string that consists of the username concatinated with the a pre-specified suffix
     *      This returned token will be substring whose end is specified by the endIndex variable.
     */
    public String encodedToken(int endIndex){
        this.tokenLength = endIndex;
        String token = this.username.concat(this.TOKEN_SUFFIX);
        return token.substring(0, endIndex).replaceAll("\\s", "");
    }

    @Override
    public String toString() {
        return "user.AccessToken:\nUsername: " + username + "\nToken value: " + this.encodedToken(this.tokenLength);
    }
}
