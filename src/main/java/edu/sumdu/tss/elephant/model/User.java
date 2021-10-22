package edu.sumdu.tss.elephant.model;

import edu.sumdu.tss.elephant.helper.UserRole;
import lombok.Data;

@Data
public class User {

    private Long id;
    private String login;
    private String password;
    private String username; //database user name
    private String dbPassword; //database password
    private Long role;
    private String privateKey;
    private String publicKey;
    private String token;
    private String language;

    //TODO: crypt it
    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole role() {
        return UserRole.byValue(role);
    }

}
