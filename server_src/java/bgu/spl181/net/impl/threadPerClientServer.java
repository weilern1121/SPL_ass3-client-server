package bgu.spl181.net.impl;

import bgu.spl181.net.api.BidiMessagingProtocol;
import bgu.spl181.net.api.MessageEncoderDecoder;

import java.util.function.Supplier;

public class threadPerClientServer<T> extends BaseServer<T> {
    public threadPerClientServer(int port, Supplier<BidiMessagingProtocol<T>> protocolFactory, Supplier<MessageEncoderDecoder<T>> encdecFactory) {
        super(port, protocolFactory, encdecFactory);
    }

    @Override
    protected void execute(BlockingConnectionHandler<T> handler) {
        new Thread(handler).start();
    }
}
