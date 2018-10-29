package bgu.spl181.net.impl;

import bgu.spl181.net.api.bidi.Connections;
import bgu.spl181.net.api.ConnectionHandler;
import bgu.spl181.net.json.DatabaseManager;
import bgu.spl181.net.json.UsersManagement;

import java.util.HashMap;

public class ConnectionsImpl<T> implements Connections {

    //singleton
    private static class ConnectionsImplHolder {
        private static ConnectionsImpl instance = new ConnectionsImpl();
    }

    public static ConnectionsImpl getInstance() {
        return ConnectionsImplHolder.instance;
    }

    //the class

    private HashMap<Integer, ConnectionHandler<T>> chMap;
    private DatabaseManager manager;

    private ConnectionsImpl() {
        this.chMap = new HashMap<Integer, ConnectionHandler<T>>();
        this.manager = DatabaseManager.getInstance();
    }

    @Override
    public boolean send(int connectionId, Object msg) {
        //input check
        if (!(chMap.containsKey(connectionId)))
            throw new IllegalArgumentException("ConnectionsImpl -send - connectionId doesn't exists");
        if (msg == null)
            throw new NullPointerException("ConnectionsImpl -send - msg is null");
        chMap.get(connectionId).send((T) msg);
        return true;
    }

    @Override
    public void broadcast(Object msg) {
        if (msg == null)
            throw new NullPointerException("ConnectionsImpl -broadcast - msg is null");
        for (Integer ch : chMap.keySet()) {
            //if the manger is connected value is true then check if the socket is connected
            if (manager.getUserPerClientMap().containsKey(ch)) {
                if (manager.getConnectedUsersMap().containsKey(manager.getUserPerClientMap().get(ch)) && manager.getConnectedUsersMap().get(manager.getUserPerClientMap().get(ch))) {
                    if (chMap.get(ch).isConnected())
                        send(ch, msg);
                }
            }

        }
    }

    @Override
    public void disconnect(int connectionId) {
        chMap.remove(connectionId);
    }

    public void connect(int connectionId, ConnectionHandler<T> ch) {
        if (ch == null)
            throw new IllegalArgumentException("ConnectionsImpl - connect - illegal input");
        if (!(chMap.containsKey(connectionId)))
            chMap.put(connectionId, ch);
    }

}
