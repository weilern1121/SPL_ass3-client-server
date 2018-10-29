package bgu.spl181.net.impl.BBtpc;

import bgu.spl181.net.impl.threadPerClientServer;
import bgu.spl181.net.srv.LineMessageEncoderDecoder;
import bgu.spl181.net.srv.bidi.MovieRentalServiceProtocol;

import java.util.function.Supplier;

public class TPCMain {
    public static void main(String[] args) {
        threadPerClientServer threadPerClient = new threadPerClientServer(Integer.parseInt(args[0]), MovieRentalServiceProtocol::new, LineMessageEncoderDecoder::new);
        threadPerClient.serve();
    }
}
