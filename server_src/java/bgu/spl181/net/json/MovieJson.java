package bgu.spl181.net.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MovieJson {

    @SerializedName("id")
    @Expose
    private String movieId;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("price")
    @Expose
    private String price;
    @SerializedName("bannedCountries")
    @Expose
    private List<String> bannedCountries =null;
    @SerializedName("availableAmount")
    @Expose
    private String availableAmount;
    @SerializedName("totalAmount")
    @Expose
    private String totalAmount;

    //getters

    public Integer getMovieId() {
        return Integer.parseInt(movieId);
    }

    public String getName() {
        return name;
    }

    public Integer getPrice() {
        return Integer.parseInt(price);
    }

    public List<String> getBannedCountries() {
        return bannedCountries;
    }

    public Integer getAvailableAmount() {
        return Integer.parseInt(availableAmount);
    }

    public Integer getTotalAmount() {
        return Integer.parseInt(totalAmount);
    }

    //setters

    public void setMovieId(Integer movieId) {
        this.movieId = String.valueOf(movieId);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(Integer price) {
        this.price = String.valueOf(price);
    }

    public void setBannedCountries(List<String> bannedCountries) {
        this.bannedCountries = bannedCountries;
    }

    public void setAvailableAmount(Integer availableAmount) {
        this.availableAmount = String.valueOf(availableAmount);
    }

    public void setTotalAmount(Integer totalAmount) {
        this.totalAmount = String.valueOf(totalAmount);
    }

    public String toString(){
        String output=this.name+" "+this.availableAmount+" "+price;
        for (String c:this.bannedCountries) {
            output+=" "+'"'+c+'"';
        }
        return output;
    }
}
