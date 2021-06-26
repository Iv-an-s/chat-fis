package server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InMemoryAuthenticationProvider implements AuthenticationProvider {
    private class User {
        private String login;
        private String password;
        private String nickname;

        public User(String login, String password, String nickname) {
            this.login = login;
            this.password = password;
            this.nickname = nickname;
        }
    }

    private List<User> users;

    public InMemoryAuthenticationProvider() {
        this.users = new ArrayList<>(Arrays.asList(
                new User("Bob", "100", "MegaBob"),
                new User("Jack", "100", "Mystic"),
                new User("John", "100", "Wizard")
        ));
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        for (User user : users){
            if(user.login.equals(login) && user.password.equals(password)){
            return user.nickname;
            }
        }
        return null;
    }

    @Override
    public void changeNickname(String oldNickname, String newNickname) {
        for (User user : users){
            if (user.nickname.equals(oldNickname)){
                user.nickname = newNickname;
                return;
            }
        }
    }
}
