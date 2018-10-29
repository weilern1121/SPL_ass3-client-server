package bgu.spl181.net.json;

import bgu.spl181.net.impl.ConnectionsImpl;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseManager {

    //singleton
    private static class DatabaseManagerHolder {
        private static DatabaseManager instance;

        static {
            try {
                instance = new DatabaseManager();
            } catch (IOException e) {
            }
        }

    }

    public static DatabaseManager getInstance() {
        return DatabaseManager.DatabaseManagerHolder.instance;
    }


    //the class
    private UsersManagement usersManagement;
    private MovieManager movieManager;
    //READ\WRITE LOCK
    //true=can write , false= some else catch the writing optoin
    private AtomicInteger numOfUserReaders;
    private AtomicInteger numOfMovieReaders;
    private AtomicBoolean userWriter;
    private AtomicBoolean movieWriter;


    private DatabaseManager() throws IOException {
        this.usersManagement = new UsersManagement();
        this.movieManager = new MovieManager();
        this.numOfMovieReaders = new AtomicInteger(0);
        this.numOfUserReaders = new AtomicInteger(0);
        this.userWriter = new AtomicBoolean(true);
        this.movieWriter = new AtomicBoolean(true);
    }

    //Atomicboolen=true ->can write, false->can't write and read
    //the ReadWriteLock FUNCS - LOCKING ORDER: FIRST=USER, SECOND=MOVIE
    //USER ReadWriteLock
    private synchronized void beforeUserRead() {
        while (!userWriter.get()) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        this.numOfUserReaders.incrementAndGet();
    }

    private synchronized void afterUserRead() {
        this.numOfUserReaders.decrementAndGet();
        notifyAll();
    }

    private synchronized void beforeUserWrite() {
        while (this.numOfUserReaders.get() > 0 || !(userWriter.get())) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        this.userWriter.set(false);
    }

    private synchronized void afterUserWrite() {
        this.userWriter.set(true);
        notifyAll();
    }

    //MOVIE ReadWriteLock
    private synchronized void beforeMovieRead() {
        while (!movieWriter.get()) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        this.numOfMovieReaders.incrementAndGet();
    }

    private synchronized void afterMovieRead() {
        this.numOfMovieReaders.decrementAndGet();
        notifyAll();
    }

    private synchronized void beforeMovieWrite() {
        while (this.numOfMovieReaders.get() > 0 || !(movieWriter.get())) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        this.movieWriter.set(false);
    }

    private synchronized void afterMovieWrite() {
        this.movieWriter.set(true);
        notifyAll();
    }


    //THE MANAGMENT FUNCS
    public boolean register(String username, String type, String password, String country) throws IOException {
        if (username == null || type == null || password == null || country == null)
            return false;
        beforeUserWrite();
        boolean output = usersManagement.register(username, type, password, country);
        afterUserWrite();
        return output;
    }

    //NOTE - in this func return -1 symbolize error
    public Integer balanceInfo(String userName) throws IOException {
        if (userName == null)
            return -1;
        //another cond- check that user is login
        if (!(usersManagement.isLogin(userName)))
            return -1;
        beforeUserRead();
        Integer output = usersManagement.balanceInfo(userName);
        afterUserRead();
        return output;
    }

    public Integer balanceAdd(String userName, Integer num) throws IOException {
        if (userName == null || num == null)
            return -1;
        //another cond- check that user is login
        if (!(usersManagement.isLogin(userName)))
            return -1;
        beforeUserWrite();
        Integer output = usersManagement.balanceAdd(userName, num);
        afterUserWrite();
        return output;
    }

    //NOTE - output should be string[3] because the broadcast
    //       return null*3 if fail (instead of return false.
    //       if succeed- return { movieName , numOfCopies , price}
    public String[] rent(String userName, String movieName) throws IOException {
        String[] output = {null, null, null};
        if (userName == null || movieName == null)
            return output;
        //another cond- check that user is login
        if (!(usersManagement.isLogin(userName)))
            return output;
        //from this part there is a need to lock both user and movie jsons
        beforeUserWrite();
        beforeMovieWrite();

        //if the username/movie doesn't exists-return false;
        if (!(usersManagement.ifContainsUserName(userName))) {
            afterUserWrite();
            afterMovieWrite();
            return output;
        }
        if (!(movieManager.isContains(movieName))) {
            afterUserWrite();
            afterMovieWrite();
            return output;
        }

        //getters for the elements
        UserJson user = usersManagement.getUser(userName);
        MovieJson movie = movieManager.getMovie(movieName);
        boolean flag = true;
        //conditions
        if (user.getBalance() - movie.getPrice() < 0)
            flag = false;
        if (movie.getAvailableAmount() == 0)
            flag = false;
        if (movie.getBannedCountries().contains(user.getCountry()))
            flag = false;
        //check that the user isn't already rent this movie
        for (UserMovieRef movieRef : user.getMovies()) {
            if (movieRef.getMovieName().compareTo(movieName) == 0)
                flag = false;
        }
        if (!flag) {
            afterUserWrite();
            afterMovieWrite();
            return output;
        } else {
            //got here - adders
            usersManagement.addMovie(userName, movieName, movie.getMovieId(), movie.getPrice());
            movieManager.updateMovieAvailableAmount(movieName, '-');
            //release the ReadWriteLock
            afterUserWrite();
            afterMovieWrite();
            //update the output after succeed
            output[0] = movie.getName();
            output[1] = String.valueOf(movie.getAvailableAmount() - 1);
            output[2] = String.valueOf(movie.getPrice());
            return output;
        }
    }

    //NOTE - there is  a need to broadcast after return,hence we return StringArray
    //NOTE - if output= null*3 = return false.
    //NOTE - else - output= {movieName , numOfCopies , price}
    public String[] returnMovie(String userName, String movieName) throws IOException {
        String[] output = {null, null, null};
        if (userName == null || movieName == null)
            return output;
        //another cond- check that user is login
        if (!(usersManagement.isLogin(userName)))
            return output;

        //from this part there is a need to lock both user and movie jsons
        beforeUserWrite();
        beforeMovieWrite();
        boolean flag = true;
        //if the username doesn't exists-return false;
        if (!(usersManagement.ifContainsUserName(userName)))
            flag = false;
        //cond- user doesn't have the movie
        List<UserMovieRef> userMovies = usersManagement.getUser(userName).getMovies();
        boolean check = false;
        for (UserMovieRef mv : userMovies) {
            if (mv.getMovieName().compareTo(movieName) == 0)
                check = true;
        }
        if (!check)
            flag = false;
        //if movie doesn't exists
        if (flag && !(movieManager.isContains(movieName)))
            flag = false;
        if (!flag) {
            afterUserWrite();
            afterMovieWrite();
            return output;
        }
        //if got here- make the changes and return true
        usersManagement.returnMovie(userName, movieName);
        movieManager.updateMovieAvailableAmount(movieName, '+');
        MovieJson movie = movieManager.getMovie(movieName);
        afterUserWrite();
        afterMovieWrite();
        //update the output after succeed
        output[0] = movie.getName();
        output[1] = String.valueOf(movie.getAvailableAmount());
        output[2] = String.valueOf(movie.getPrice());
        return output;
    }

    public boolean addMovie(String userName, String movieName, Integer amount
            , Integer price, List<String> countries) throws IOException {
        if (userName == null || movieName == null || price == null || amount == null)
            return false;
        //another cond- check that user is login
        if (!(usersManagement.isLogin(userName)))
            return false;
        //in this fun there is only admin check hance there is a readUser synch and not write
        beforeUserRead();
        beforeMovieWrite();
        boolean output = true;
        //if user ins't admin
        if (usersManagement.getUser(userName).getType().compareTo("admin") != 0)
            output = false;

        //if movie exists
        if (output && movieManager.isContains(movieName))
            output = false;

        //after the admin check there is no need to hold the user lock
        afterUserRead();
        if (amount <= 0 || price <= 0)
            output = false;
        if (!output) {
            afterMovieWrite();
            return false;
        }
        movieManager.addMovie(movieName, amount, price, countries);
        afterMovieWrite();
        return true;
    }

    public boolean remMovie(String userName, String movieName) throws IOException {
        if (userName == null || movieName == null)
            return false;
        //another cond- check that user is login
        if (!(usersManagement.isLogin(userName)))
            return false;
        //in this func there is only admin check, hance there is a readUser synch and not write
        beforeUserRead();
        beforeMovieWrite();
        boolean output = true;
        //if user ins't admin
        if (usersManagement.getUser(userName).getType().compareTo("admin") != 0)
            output = false;
        //after the admin check there is no need to hold the user lock
        afterUserRead();
        //if movie exists
        if (output && !(movieManager.isContains(movieName)))
            output = false;
        if (!output) {
            afterMovieWrite();
            return false;
        }
        output = movieManager.remMovie(movieName);
        afterMovieWrite();
        return output;
    }

    public Integer changePrice(String userName, String movieName, Integer newPrice) throws IOException {
        if (userName == null || movieName == null)
            return -1;
        //another cond- check that user is login
        if (!(usersManagement.isLogin(userName)))
            return -1;
        //in this func there is only admin check, hance there is a readUser synch and not write
        beforeUserRead();
        beforeMovieWrite();
        Integer output = 0;
        //if user ins't admin
        if (usersManagement.getUser(userName).getType().compareTo("admin") != 0)
            output = -1;
        //after the admin check there is no need to hold the user lock
        afterUserRead();
        //if movie exists
        if (output != -1 && !(movieManager.isContains(movieName)))
            output = -1;
        //if new price isn't legal
        if (newPrice <= 0)
            output = -1;
        if (output == -1) {
            afterMovieWrite();
            return output;
        }
        output = movieManager.changePrice(movieName, newPrice);
        afterMovieWrite();
        return output;
    }

    //NOTE- there is no need to lock this func because we use ConcurrentHashMap dataBase in userManager
    public boolean login(Integer connectID, String name, String password) throws IOException {
        //if illegal input- return false= error login
        return name != null && password != null && connectID != null && usersManagement.login(connectID, name, password);
    }

    //NOTE- there is no need to lock this func because we use ConcurrentHashMap dataBase in userManager
    public boolean signOut(String userName) {
        return userName != null && usersManagement.signout(userName);
    }

    public String info(String movieName) throws IOException {
        beforeMovieRead();
        String output = movieManager.info(movieName);
        afterMovieRead();
        return output;
    }

    public ConcurrentHashMap<String, Boolean> getConnectedUsersMap() {
        return usersManagement.getConnectedUsersMap();
    }

    public ConcurrentHashMap<Integer, String> getUserPerClientMap() {
        return usersManagement.getUserPerClientMap();
    }
}
