package bgu.spl181.net.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class MovieManager {
    private static String sourcePath = "Database/Movies.json";
    private int counter = 1;

    //There is a need to synch this method because the readLock allows multi reading from the Json
    //and there are buffering issues while running multi-thread reading on the same Json
    //the sync is local and limit only the Json access, after getting the Json class the threads will continue run parallelly
    private synchronized static MovieListJson getMoviesList() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Reader reader = new FileReader(sourcePath);
        MovieListJson inputJson = gson.fromJson(reader, MovieListJson.class);
        reader.close();
        return inputJson;
    }

    private synchronized static void setMovieList(MovieListJson inputJson) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Writer writer = new FileWriter(sourcePath);
        gson.toJson(inputJson, writer);
        writer.flush();
        writer.close();
    }

    public String info(String movieName) throws IOException {
        MovieListJson inputJson = getMoviesList();
        //case that name is null- return all the movies
        if (movieName == null) {
            String output = new String();
            for (MovieJson mov : inputJson.getMoviesList()) {
                output += '"' + mov.getName() + '"' + " ";
            }
            return output.substring(0, output.length() - 1);
        }
        //case that name is specific
        //NOTE - output won't return null because isContains func checked it via the protocol
        String output = null;
        for (MovieJson mov : inputJson.getMoviesList()) {
            if (mov.getName().compareTo(movieName) == 0)
                output = '"' + mov.getName() + '"' + " " + mov.getAvailableAmount() + " " + mov.getPrice();
        }
        return output;
    }

    //true - movie name is exists , else- false
    public boolean isContains(String mvName) throws IOException {
        MovieListJson inputJson = getMoviesList();
        if (mvName == null)
            throw new NullPointerException("MovieManager - isContains - illegal input");
        for (MovieJson mv : inputJson.getMoviesList()) {
            if (mv.getName().compareTo(mvName) == 0)
                return true;
        }
        return false;
    }

    public MovieJson getMovie(String movName) throws IOException {
        MovieListJson inputJson = getMoviesList();
        MovieJson output = null;
        for (MovieJson mv : inputJson.getMoviesList()) {
            if (mv.getName().compareTo(movName) == 0)
                output = mv;
        }
        return output;
    }

    public void updateMovieAvailableAmount(String movName, char c) throws IOException {
        MovieListJson inputJson = getMoviesList();
        for (MovieJson mv : inputJson.getMoviesList()) {
            if (mv.getName().compareTo(movName) == 0) {
                if (c == '-')
                    mv.setAvailableAmount(mv.getAvailableAmount() - 1);
                if (c == '+')
                    mv.setAvailableAmount(mv.getAvailableAmount() + 1);
            }
        }
        setMovieList(inputJson);
    }

    public void addMovie(String movieName, Integer amount,
                         Integer price, List<String> countries) throws IOException {

        MovieListJson inputJson = getMoviesList();
        //create the new movie and set him the fields by input
        MovieJson newMovie = new MovieJson();
        newMovie.setMovieId(counter);
        newMovie.setName(movieName);
        newMovie.setPrice(price);
        newMovie.setBannedCountries(countries);
        newMovie.setAvailableAmount(amount);
        newMovie.setTotalAmount(amount);
        //add the new movie into the list and update the list
        inputJson.getMoviesList().add(newMovie);
        setMovieList(inputJson);
        //update the counter - that the next movie to be inserted will be greater in 1
        counter++;
    }

    public boolean remMovie(String movieName) throws IOException {
        MovieListJson inputJson = getMoviesList();
        for (MovieJson rm : inputJson.getMoviesList()) {
            if (rm.getName().compareTo(movieName) == 0) {
                //if the amount is smaller then total - there is a rented copy of the movie
                if (rm.getAvailableAmount() < rm.getTotalAmount())
                    return false;
                //else- you found the remove movie and not copies to users-remove
                inputJson.getMoviesList().remove(rm);
                //update the input json
                setMovieList(inputJson);
                return true;
            }
        }
        return false;
    }

    public Integer changePrice(String movieName, Integer newPrice) throws IOException {
        MovieListJson inputJson = getMoviesList();
        Integer output = null;
        for (MovieJson mvSet : inputJson.getMoviesList()) {
            if (mvSet.getName().compareTo(movieName) == 0) {
                mvSet.setPrice(newPrice);
                output = mvSet.getAvailableAmount();
            }
        }
        //update the input json
        setMovieList(inputJson);
        return output;
    }


}
