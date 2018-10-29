package bgu.spl181.net.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class UsersManagement {
    private static String sourcePath = "Database/Users.json";
    private ConcurrentHashMap<String, Boolean> connectedUsersMap;
    private ConcurrentHashMap<Integer, String> userPerClientMap;

    UsersManagement() {
        this.connectedUsersMap = new ConcurrentHashMap<>();
        this.userPerClientMap = new ConcurrentHashMap<>();
        try {
            UsersListJson inputJson = getUsersList();
            for (UserJson user : inputJson.getUsersList()) {
                connectedUsersMap.put(user.getUsername(), false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ConcurrentHashMap<Integer, String> getUserPerClientMap() {
        return userPerClientMap;
    }

    public ConcurrentHashMap<String, Boolean> getConnectedUsersMap() {
        return connectedUsersMap;
    }

    //There is a need to synch this method because the readLock allows multi reading from the Json
    //and there are buffering issues while running multi-thread reading on the same Json
    //the sync is local and limit only the Json access, after getting the Json class the threads will continue run parallelly
    private synchronized static UsersListJson getUsersList() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Reader reader = new FileReader(sourcePath);
        UsersListJson inputJson = gson.fromJson(reader, UsersListJson.class);
        reader.close();
        return inputJson;
    }

    private static void setUserList(UsersListJson inputJson) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Writer writer = new FileWriter(sourcePath);
        gson.toJson(inputJson, writer);
        writer.flush();
        writer.close();
    }

    public boolean register(String username, String type, String password, String country) throws IOException {
        UsersListJson inputJson = getUsersList();
        //first- check if the name of the user is used - if true->false;
        if (ifContainsUserName(username))
            return false;
        //second- create a new user and add the new user to the users list
        UserJson newUser = new UserJson();
        newUser.setUsername(username);
        newUser.setType(type);
        newUser.setPassword(password);
        newUser.setCountry(country);
        //NOTE - the balance and the movie list are initial while registering
        newUser.setBalance(0);
        newUser.setMovies(new LinkedList<>());
        inputJson.getUsersList().add(newUser);
        setUserList(inputJson);
        //add the user to the connectedUsersMap
        connectedUsersMap.put(username, false);
        return true;
    }

    //true - if the username is exists in the list, false otherwise
    public boolean ifContainsUserName(String name) throws IOException {
        UsersListJson inputJson = getUsersList();
        //first time need to enter as null
        if (inputJson == null)
            return false;
        for (UserJson user : inputJson.getUsersList()) {
            if (name.compareTo(user.getUsername()) == 0)
                return true;
        }
        return false;
    }

    //NOTE - assume that userName is unique. therefore the output will be changed once.
    //NOTE - the func won't check if the userName is exists, will be checked in the protocol
    public Integer balanceInfo(String userName) throws IOException {
        UserJson userBal = getUser(userName);
        if (userBal == null)
            return -1;
        return userBal.getBalance();
    }

    //NOTE - the func won't check if the userName is exists, will be checked in the protocol
    public Integer balanceAdd(String userName, Integer num) throws IOException {
        UsersListJson inputJson = getUsersList();
        Integer output = -1;
        UserJson userBalAdd = getUser(userName);
        if (userBalAdd == null)
            return output;
        userBalAdd.setBalance(userBalAdd.getBalance() + num);
        output = userBalAdd.getBalance();
        //update the users' json
        makeChangesInUser(inputJson, userBalAdd);
        setUserList(inputJson);
        return output;
    }

    private void makeChangesInUser(UsersListJson inputJson, UserJson user) {
        for (UserJson oldUser : inputJson.getUsersList()) {
            if (user.getUsername().compareTo(oldUser.getUsername()) == 0) {
                //update all of the oldUser fields
                oldUser.setBalance(user.getBalance());
                oldUser.setMovies(user.getMovies());
            }
        }
    }

    //return false=error , true= succeed to login
    public boolean login(Integer connectID, String name, String password) throws IOException {
        UsersListJson inputJson = getUsersList();
        //if client already logged in with other user-return false
        if (userPerClientMap.containsKey(connectID) &&
                userPerClientMap.get(connectID).compareTo(name) != 0)
            return false;
        if (inputJson != null) {
            for (UserJson user : inputJson.getUsersList()) {
                if (user.getUsername().compareTo(name) == 0) {
                    if (user.getPassword().compareTo(password) == 0) {
                        //if got here - the name and password equal- then check the connectedUsersMap
                        //if already login (true) - return false. else- update and return true
                        if (connectedUsersMap.get(name))
                            return false;
                        else {
                            connectedUsersMap.replace(name, true);
                            userPerClientMap.put(connectID, name);
                            return true;
                        }
                    }
                }
            }
        }
        //if got here- there is no user with this name and password-return false=error
        return false;
    }

    public boolean signout(String name) {
        if (connectedUsersMap.containsKey(name)) {
            if (connectedUsersMap.get(name)) {
                connectedUsersMap.replace(name, false);
                //remove the connectID from userPerClientMap
                //have to foreach because don't have the connectID
                Integer toRemove = null;
                for (Integer client : userPerClientMap.keySet()) {
                    if (userPerClientMap.get(client).compareTo(name) == 0)
                        toRemove = client;
                }
                if (toRemove != null) {
                    userPerClientMap.remove(toRemove);
                    return true;
                } else
                    return false;
            }
        }
        return false;
    }

    public UserJson getUser(String name) throws IOException {
        UserJson output = null;
        UsersListJson inputJson = getUsersList();
        for (UserJson user : inputJson.getUsersList()) {
            if (user.getUsername().compareTo(name) == 0)
                output = user;
        }
        return output;
    }

    public void addMovie(String userName, String movieName, Integer movieId, Integer movPrice) throws IOException {
        UsersListJson inputJson = getUsersList();
        for (UserJson user : inputJson.getUsersList()) {
            if (user.getUsername().compareTo(userName) == 0) {
                user.getMovies().add(new UserMovieRef(movieName, movieId));
                user.setBalance(user.getBalance() - movPrice);
            }
        }
        setUserList(inputJson);
    }

    public boolean isLogin(String userName) {
        return connectedUsersMap.get(userName);
    }

    public void returnMovie(String userName, String movieName) throws IOException {
        UsersListJson inputJson = getUsersList();
        //find the user
        for (UserJson us : inputJson.getUsersList()) {
            if (us.getUsername().compareTo(userName) == 0) {
                //delete the movie from user's movie list
                for (UserMovieRef movieRef : us.getMovies()) {
                    if (movieRef.getMovieName().compareTo(movieName) == 0) {
                        us.getMovies().remove(movieRef);
                        setUserList(inputJson);
                        return;
                    }
                }
            }
        }
    }
}
