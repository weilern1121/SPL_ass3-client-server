package bgu.spl181.net.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MovieListJson {


    @SerializedName("movies")
    @Expose
    private List<MovieJson> moviesList;

    public List<MovieJson> getMoviesList() {
        return moviesList;
    }

    public void setMoviesList(List<MovieJson> moviesList) {
        this.moviesList = moviesList;
    }
}
