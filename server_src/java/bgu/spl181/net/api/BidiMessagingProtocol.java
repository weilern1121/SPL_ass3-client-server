/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgu.spl181.net.api;

import bgu.spl181.net.api.bidi.Connections;

import java.io.IOException;

/**
 *
 * @author bennyl
 */
public interface BidiMessagingProtocol<T>  {


    void start(int connectionId, Connections<T> connections) throws IOException;
    
    void process(T message);
	
	/**
     * @return true if the connection should be terminated
     */
    boolean shouldTerminate();


}
