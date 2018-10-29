package bgu.spl181.net.impl.BBreactor;

import bgu.spl181.net.impl.Reactor;
import bgu.spl181.net.srv.LineMessageEncoderDecoder;
import bgu.spl181.net.srv.bidi.MovieRentalServiceProtocol;

import java.util.function.Supplier;

public class ReactorMain {
    public static void main(String[] args) {
        Reactor r = new Reactor(8, Integer.parseInt(args[0]), MovieRentalServiceProtocol::new, LineMessageEncoderDecoder::new);
        r.serve();
    }
}
