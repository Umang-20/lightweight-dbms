package userAuthentication.model;

import java.util.Objects;

public class User {

  private long id;
    private String email;
    private String password;
    private String secQuesAns;

    public User(long id, String email, String password, String secQuesAns) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.secQuesAns = secQuesAns;
    }

    public User(String email, String password, String secQuesAns) {
        this(-1, email, password, secQuesAns);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSecurityQuestionAnswer() {
        return secQuesAns;
    }

    public void setSecurityQuestionAnswer(String secQuesAns) {
        this.secQuesAns = secQuesAns;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", secQuesAns='" + secQuesAns + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id &&
                Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }
}