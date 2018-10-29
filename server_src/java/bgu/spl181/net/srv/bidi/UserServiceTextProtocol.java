package bgu.spl181.net.srv.bidi;

import bgu.spl181.net.api.BidiMessagingProtocol;
import bgu.spl181.net.api.bidi.Connections;
import bgu.spl181.net.json.DatabaseManager;
import bgu.spl181.net.json.UsersManagement;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class UserServiceTextProtocol implements BidiMessagingProtocol<String> {


    private int connectionId;
    private Connections connections;
    private DatabaseManager dataBaseConnection;
    boolean shouldTerminate;
    String userName;


    public DatabaseManager getDataBaseConnection() {
        return dataBaseConnection;
    }

    public Connections getConnections() {
        return connections;
    }

    public String getUserName() {
        return userName;
    }

    public int getConnectionId() {
        return connectionId;
    }

    @Override
    public void start(int connectionId, Connections connections) throws IOException {
        this.connectionId = connectionId;
        this.connections = connections;
        this.dataBaseConnection = DatabaseManager.getInstance();
        this.shouldTerminate = false;
        this.userName = null;
    }

    @Override
    public void process(String message) {
        //divide the string by spaces or tab
        char[] aDelimiters = {' ', '\t'};
        //if the client sent empty message send unknown
        String checkMsg = message.trim();
        if (message == null || message.equals("") || checkMsg.equals(""))
            connections.send(connectionId, "ERROR the command is not supported");
        else {
            LinkedList<String> msg = Split(message, aDelimiters);
            switch (msg.getFirst()) {
                case ("REGISTER"):
                    if (Register(msg, dataBaseConnection)) {
                        connections.send(connectionId, "ACK registration succeeded");
                    } else
                        connections.send(connectionId, "ERROR registration failed");
                    break;
                case ("LOGIN"):
                    if (LOGIN(msg, dataBaseConnection)) {
                        connections.send(connectionId, "ACK login succeeded");
                    } else
                        connections.send(connectionId, "ERROR login failed");
                    break;
                case ("SIGNOUT"):
                    if (SIGNOUT(msg, dataBaseConnection)) {
                        connections.send(connectionId, "ACK signout succeeded");
                    } else
                        connections.send(connectionId, "ERROR signout failed");
                    break;
                case ("REQUEST"):
                    throw new IllegalArgumentException("UserServiceTextProtocol-process- The protocol is not compitable.");
                default:
                    connections.send(connectionId, "ERROR the command is not supported");
            }
        }
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    //this function cut the msg string to list.
    public LinkedList<String> Split(String s, char[] aDelimiters) {
        LinkedList<String> sDivided = new LinkedList<String>();
        while (s.length() > 0) {
            String sPart = "";
            int i = 0;
            for (i = 0; i < s.length(); i++) {
                if (Contains(aDelimiters, s.charAt(i))) {
                    if (sPart.length() > 0)
                        sDivided.add(sPart);
                    sDivided.add(s.charAt(i) + "");
                    break;
                } else
                    sPart += s.charAt(i);
            }
            if (i == s.length()) {
                sDivided.add(sPart);
                s = "";
            } else
                s = s.substring(i + 1);
        }
        for (int i = sDivided.size() - 1; i >= 0; i--) {
            if (sDivided.get(i).compareTo(" ") == 0 || sDivided.get(i).compareTo("\t") == 0 ||
                    sDivided.get(i).compareTo("") == 0)
                sDivided.remove(i);
        }
        return sDivided;
    }

    private boolean Contains(char[] a, char c) {
        for (char c1 : a)
            if (c1 == c)
                return true;
        return false;
    }

    private boolean Register(LinkedList<String> Details, DatabaseManager dataBase) {
        if (Details.get(0).compareTo("REGISTER") == 0)
            Details.remove(0);
        String name, type, password, country = "";
        //here we check that all the argument supplied by the client
        if (Details.size() < 3)
            return false;
        else {
            name = Details.getFirst();
            Details.remove(0);

            type = "normal";

            password = Details.getFirst();
            Details.remove(0);
            while (Details.size() != 0)
                country = country + Details.pollFirst();
            if (country.indexOf('=') != -1)
                country = country.substring(country.indexOf('=') + 1);
            if (country.charAt(0) == '"')
                country = country.substring(1);
            if (country.charAt(country.length() - 1) == '"')
                country = country.substring(0, country.length() - 1);
            if (Details.size() > 0) {
                return false;
            } else {
                try {
                    return dataBase.register(name, type, password, country);
                } catch (IOException e) {
                    return false;
                }
            }
        }


    }

    private boolean LOGIN(LinkedList<String> Details, DatabaseManager dataBase) {
        if (Details.get(0).compareTo("LOGIN") == 0)
            Details.remove(0);
        String name, password;
        //here we check that all the argument supplied by the client
        if (Details.size() < 2)
            return false;
        else {
            name = Details.getFirst();
            Details.remove(0);
            password = Details.getFirst();
            Details.remove(0);
            if (Details.size() > 0) {
                return false;
            } else {
                try {
                    if (dataBase.login(connectionId, name, password)) {
                        this.userName = name;
                        return true;
                    } else
                        return false;
                } catch (IOException e) {
                    return false;
                }
            }
        }
    }

    private boolean SIGNOUT(LinkedList<String> Details, DatabaseManager dataBase) {
        if (Details.get(0).compareTo("SIGNOUT") == 0)
            Details.remove(0);
        if (dataBase.signOut(userName)) {
            this.shouldTerminate = true;
            this.userName = null;
            return true;
        } else
            return false;

    }
}