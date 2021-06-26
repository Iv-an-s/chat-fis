package server;

public interface AuthenticationProvider {
    String getNicknameByLoginAndPassword(String login, String password);
    void changeNickname(String oldNickname, String newNickname);
}
