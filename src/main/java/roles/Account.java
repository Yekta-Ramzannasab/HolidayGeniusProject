package roles;

public interface Account {
    public void register();
    public void infoWriter(String filename, String username, String password, String name, String email);
    public void usernameWriter(String filename, String username);

}
