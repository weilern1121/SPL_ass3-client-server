package bgu.spl181.net.json;

public class UserMovieRef {
    private int movieId;
    private String movieName;

    public UserMovieRef(String movieName , int movieId ) {
        this.movieId = movieId;
        this.movieName=movieName;
    }

    public int getMovieId() {
        return movieId;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }
}
