#include <stdlib.h>
#include <iostream>
#include <boost/thread.hpp>
#include "../include/connectionHandler.h"
#include <boost/algorithm/string.hpp>
/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/
using namespace boost;
using namespace std;
using boost::asio::ip::tcp;

using std::cin;
using std::cout;
using std::cerr;
using std::endl;
using std::string;

class ConnectionServer{
private:
    ConnectionHandler* ch;
public:
    ConnectionServer(ConnectionHandler* connectionHandler):ch(connectionHandler){}
    void run(){
        while(1){
            std::string answer;
            if (!ch->getLine(answer)) {
                std::cout << "Disconnected. Exiting...\n" << std::endl;
                break;
            }

            int len=answer.length();

            answer.resize(len-1);
            std::cout << answer << std::endl;
            if (answer == "ACK signout succeeded") {
                std::cout << "Ready to exit. Press enter\n" << std::endl;
                break;
            }
        }
    };
};

class IOTask {
private:
    ConnectionHandler *ch;
    bool isSIGNOUT;
public:
    IOTask(ConnectionHandler *connectionHandler) : ch(connectionHandler),isSIGNOUT(false) {}

    void run() {
//	isSIGNOUT=false;
        while (1) {
            const short bufsize = 1024;
            char buf[bufsize];
            std::cin.getline(buf, bufsize);
            std::string line(buf);
            int len = line.length();
            if (!ch->sendLine(line)) {
                std::cout << "Disconnected. Exiting...\n" << std::endl;
                break;
            }
            if (line.compare("SIGNOUT") ){
                isSIGNOUT=true;
		}
            if (isSIGNOUT && len==0 ){
                break;
		}

        }
    };
};

    int main(int argc, char *argv[]) {
        if (argc < 3) {
            std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
            return -1;
        }
        std::string host = argv[1];
        short port = atoi(argv[2]);

        ConnectionHandler connectionHandler(host, port);
        if (!connectionHandler.connect()) {
            std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
            return 1;
        }
        IOTask IO(&connectionHandler);
        boost::thread IOThread(&IOTask::run,&IO);
        ConnectionServer Com(&connectionHandler);
        boost::thread ComThread(&ConnectionServer::run, &Com);

        IOThread.join();
        ComThread.join();
        return 0;
    }

