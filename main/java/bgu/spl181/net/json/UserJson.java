package bgu.spl181.net.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UserJson extends UserTemplate{


    @SerializedName("country")
    @Expose
    private String country;
    @SerializedName("movies")
    @Expose
    private List<UserMovieRef> movies = null;
    @SerializedName("balance")
    @Expose
    private String balance;

    UserJson() {}

    //getters

    public String getUsername() {
        return username;
    }

    public String getType() {
        return type;
    }

    public String getPassword() {
        return password;
    }

    public String getCountry() {
        return country;
    }

    public List<UserMovieRef> getMovies() {
        return movies;
    }

    public Integer getBalance() {
        return Integer.parseInt(balance);
    }

    //setters

    public void setUsername(String username) {
        this.username = username;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setMovies(List<UserMovieRef> movies) {
        this.movies = movies;
    }

    public void setBalance(Integer balance) {
        this.balance = String.valueOf(balance);
    }
}
