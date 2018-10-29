package bgu.spl181.net.impl;

import bgu.spl181.net.api.BidiMessagingProtocol;
import bgu.spl181.net.api.MessageEncoderDecoder;
import bgu.spl181.net.api.ConnectionHandler;
import bgu.spl181.net.impl.ConnectionsImpl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<T> {

    private final BidiMessagingProtocol<T> protocol;
    private final MessageEncoderDecoder<T> encdec;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private volatile boolean connected = true;
    private final ConnectionsImpl connections;
    private int connectionId;


    public BlockingConnectionHandler(Socket sock, MessageEncoderDecoder<T> reader, BidiMessagingProtocol<T> protocol, int connectionId) throws IOException {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
        this.connections = ConnectionsImpl.getInstance();
        this.connectionId = connectionId;

    }

    @Override
    public void run() {
        try (Socket sock = this.sock) { //just for automatic closing
            int read;

            in = new BufferedInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());
            this.connections.connect(this.connectionId, this);
            protocol.start(connectionId, connections);
            while (!protocol.shouldTerminate() && connected && (read = in.read()) >= 0) {
                T nextMessage = encdec.decodeNextByte((byte) read);
                if (nextMessage != null) {
                    protocol.process(nextMessage);
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void close() throws IOException {
        connected = false;
        sock.close();
    }

    @Override
    public void send(T msg) {
        if (msg != null) {
            try {
                out.write(encdec.encode(msg));
                out.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isConnected() {
        return sock.isConnected();
    }
}
