package bgu.spl181.net.srv.bidi;

import bgu.spl181.net.api.bidi.Connections;
import bgu.spl181.net.json.DatabaseManager;
import bgu.spl181.net.json.UsersManagement;

import java.io.IOException;
import java.util.LinkedList;

import bgu.spl181.net.srv.bidi.UserServiceTextProtocol;

public class MovieRentalServiceProtocol extends UserServiceTextProtocol {

    @Override
    public void process(String message) {
        try {
            super.process(message);
            //should catch if is not part of the generic protocol command.
        } catch (IllegalArgumentException e) {
            //cut the string between the aDelimiters chars
            char[] aDelimiters = {' ', '\t'};
            LinkedList<String> msg = super.Split(message, aDelimiters);
            if (msg.getFirst().compareTo("REQUEST") == 0)
                msg.remove(0);
            //if the command is two word the function will connect them together.
            if (msg.getFirst().compareTo("balance") == 0 && (msg.get(1).compareTo("info") == 0 || msg.get(1).compareTo("add") == 0)) {
                msg.add(0, msg.getFirst() + msg.get(1));
                msg.remove(1);
                msg.remove(1);
            }
            switch (msg.getFirst()) {
                case ("balanceinfo"):
                    int balanceinfo = balanceInfo(msg, getDataBaseConnection());
                    if (balanceinfo != -1 && balanceinfo >= 0)
                        getConnections().send(getConnectionId(), "ACK balance " + balanceinfo);
                    else {
                        getConnections().send(getConnectionId(), "ERROR request balance failed");
                    }
                    break;
                case ("balanceadd"):
                    balanceinfo = balanceInfo(msg, getDataBaseConnection());
                    int balanceadd = balanceAdd(msg, getDataBaseConnection());
                    if (balanceadd != -1 && balanceadd >= 0)
                        getConnections().send(getConnectionId(), "ACK balance " + balanceadd + " added " + (balanceadd - balanceinfo));
                    else {
                        getConnections().send(getConnectionId(), "ERROR request balance failed");
                    }
                    break;
                case ("info"):
                    String info = info(msg, getDataBaseConnection());
                    if (info != null)
                        getConnections().send(getConnectionId(), "ACK info " + info);
                    else {
                        getConnections().send(getConnectionId(), "ERROR request info failed");
                    }
                    break;
                case ("rent"):
                    String[] rent = rentAndReturnandRemmovie(msg, getDataBaseConnection(), 1);
                    if (rent != null && rent[0] != null && rent[1] != null && rent[2] != null) {
                        getConnections().send(getConnectionId(), "ACK rent " + '"' + rent[0] + '"' + " success");
                        getConnections().broadcast("BROADCAST " + '"' + rent[0] + '"' + " " + rent[1] + " " + rent[2]);
                    } else {
                        getConnections().send(getConnectionId(), "ERROR request rent failed");
                    }
                    break;
                case ("return"):
                    String[] returnMovie = rentAndReturnandRemmovie(msg, getDataBaseConnection(), 2);
                    if (returnMovie != null && returnMovie[0] != null && returnMovie[1] != null && returnMovie[2] != null) {
                        getConnections().send(getConnectionId(), "ACK return " + '"' + returnMovie[0] + '"' + " success");
                        getConnections().broadcast("BROADCAST " + '"' + returnMovie[0] + '"' + " " + returnMovie[1] + " " + returnMovie[2]);
                    } else {
                        getConnections().send(getConnectionId(), "ERROR request return failed");
                    }
                    break;
                case ("addmovie"):
                    String[] addmovie = addmovie(msg, getDataBaseConnection());
                    if (addmovie != null) {
                        getConnections().send(getConnectionId(), "ACK addmovie " + '"' + addmovie[0] + '"' + " success");
                        getConnections().broadcast("BROADCAST movie " + addmovie[0] + " " + addmovie[1] + " " + addmovie[2]);
                    } else {
                        getConnections().send(getConnectionId(), "ERROR request addmovie failed");
                    }
                    break;
                case ("remmovie"):
                    String[] remmovie = rentAndReturnandRemmovie(msg, getDataBaseConnection(), 3);
                    if (remmovie != null) {
                        getConnections().send(getConnectionId(), "ACK remmovie " + remmovie[0] + " success");
                        getConnections().broadcast("BROADCAST movie " + remmovie[0] + " removed");
                    } else {
                        getConnections().send(getConnectionId(), "ERROR request remmovie failed");
                    }
                    break;
                case ("changeprice"):
                    String[] changeprice = changeprice(msg, getDataBaseConnection());
                    if (changeprice != null) {
                        getConnections().send(getConnectionId(), "ACK changeprice " + changeprice[0] + " success");
                        getConnections().broadcast("BROADCAST movie " + changeprice[0] + " " + changeprice[1] + " " + changeprice[2]);
                    } else {
                        getConnections().send(getConnectionId(), "ERROR request changeprice failed");
                    }
                    break;
                //if the command is unknown
                default:
                    getConnections().send(getConnectionId(), "ERROR the command is not supported");
                    break;
            }
        }
    }

    private int balanceInfo(LinkedList<String> Details, DatabaseManager dataBase) {
        //if the user is not login
        if (getUserName() == null)
            return -1;
        try {
            return getDataBaseConnection().balanceInfo(getUserName());
        } catch (IOException e) {
            //if the request fail return -1
            return -1;
        }
    }

    private int balanceAdd(LinkedList<String> Details, DatabaseManager dataBase) {
        //if the user is not login
        if (getUserName() == null)
            return -1;
        if (Details.getFirst().compareTo("balanceadd") == 0)
            Details.remove(0);
        String numToAddString = Details.getFirst();
        int numToAdd;
        try {
            numToAdd = Integer.parseInt(numToAddString);
            //if the number to add is not int or integer
        } catch (NumberFormatException e) {
            return -1;
        }
        try {
            return getDataBaseConnection().balanceAdd(getUserName(), numToAdd);
        } catch (IOException e) {
            return -1;
        }
    }

    private String info(LinkedList<String> Details, DatabaseManager dataBase) {
        //if the user is not login
        if (getUserName() == null)
            return null;
        if (Details.getFirst().compareTo("info") == 0)
            Details.remove(0);
        //the user want info for all movies
        if (Details.size() == 0) {
            try {
                return dataBase.info(null);
            } catch (IOException e) {
                return null;
            }
        } else {
            String movie = getTheMovieName(Details);
            if (movie == null) {
                return null;
            } else {
                try {
                    return dataBase.info(movie.substring(1, movie.length() - 1));
                } catch (IOException e) {
                    return null;
                }
            }
        }
    }

    //1=return,2=return,3=remmovie
    private String[] rentAndReturnandRemmovie(LinkedList<String> Details, DatabaseManager dataBase, int numOfActive) {
        if (getUserName() == null || numOfActive < 1 || numOfActive > 3)
            return null;
        if (Details.getFirst().compareTo("rent") == 0 || Details.getFirst().compareTo("return") == 0
                || Details.getFirst().compareTo("remmovie") == 0)
            Details.remove(0);
        String movie = getTheMovieName(Details);
        if (movie == null) {
            return null;
        } else {
            String[] output = null;
            try {
                if (numOfActive == 1) {
                    output = (dataBase.rent(getUserName(), movie.substring(1, movie.length() - 1)));
                    if (output == null)
                        return null;
                    else
                        return output;

                }
                if (numOfActive == 2) {
                    output = (dataBase.returnMovie(getUserName(), movie.substring(1, movie.length() - 1)));
                    if (output == null)
                        return null;
                    else
                        return output;
                }
                if (numOfActive == 3) {
                    if (dataBase.remMovie(getUserName(), movie.substring(1, movie.length() - 1))) {
                        String[] remMovie = {movie};
                        return remMovie;
                    } else
                        return null;
                }
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    //return array with strings: first place=movie name, second=amount and third=price
    private String[] addmovie(LinkedList<String> Details, DatabaseManager dataBase) {
        if (getUserName() == null)
            return null;
        if (Details.getFirst().compareTo("addmovie") == 0)
            Details.remove(0);
        String[] output = new String[3];
        String name = Details.pollFirst(), amountString = "", priceString = "", boundedCountry = "";
        //cut the name of movie
        if (name.charAt(name.length() - 1) != '"') {
            boolean findthename = false;
            String check = '"' + "";
            while (!findthename && Details.size() > 0) {
                if (Details.getFirst().contains(check))//||Details.getFirst().indexOf('"') >= 0)
                    findthename = true;
                name += " ";
                name += Details.pollFirst();
            }
        }
        name = name.substring(1, name.length() - 1);
        amountString = Details.pollFirst();
        priceString = Details.pollFirst();
        int amount, price;
        try {
            amount = Integer.parseInt(amountString);
            price = Integer.parseInt(priceString);

        } catch (NumberFormatException e) {
            return null;
        }
        //bound country
        LinkedList<String> country = new LinkedList<>();
        //cut the '"' from the begin and the end
        for (int i = 0; i < Details.size(); i++) {
            country.add(Details.get(i).substring(1, Details.get(i).length() - 1));
        }
        try {
            if (dataBase.addMovie(getUserName(), name, amount, price, country)) {
                output[0] = name;
                output[1] = Integer.toString(amount);
                output[2] = Integer.toString(price);
                return output;
            } else
                return null;
        } catch (IOException e) {
            return null;
        }
    }

    private String[] changeprice(LinkedList<String> Details, DatabaseManager dataBase) {
        if (getUserName() == null)
            return null;
        if (Details.getFirst().compareTo("changeprice") == 0)
            Details.remove(0);
        String[] output = new String[3];
        boolean findthename = Details.peekFirst().charAt(0) == '"' && Details.peekFirst().charAt(Details.peekFirst().length() - 1) == '"';
        String name = Details.pollFirst(), amountString = "", priceString = "", boundedCountry = "";
        //cut the name of movie
        while (!findthename) {
            if (Details.getFirst().indexOf('"') >= 0 ||
                    Details.getFirst().charAt(Details.getFirst().length() - 1) == '"' ||
                    Details.getFirst().charAt(Details.getFirst().length() - 1) == 34)
                findthename = true;
            name += " " + Details.pollFirst();
        }
        priceString = Details.pollFirst();
        int price;
        try {
            price = Integer.parseInt(priceString);

        } catch (NumberFormatException e) {
            return null;
        }
        //banned country

        try {
            int numCopiesLeft = dataBase.changePrice(getUserName(), name.substring(1, name.length() - 1), price);
            if (numCopiesLeft >= 0) {
                output[0] = name;
                output[1] = Integer.toString(numCopiesLeft);
                output[2] = Integer.toString(price);
                return output;
            } else
                return null;
        } catch (IOException e) {
            return null;
        }
    }

    //this function cut movie name from list of string.
    private String getTheMovieName(LinkedList<String> Details) {
        boolean isName = false;
        String movieName = "";
        for (String str : Details) {
            if (str.indexOf('"') != -1 && !isName) {
                isName = true;
            }
            if (str.indexOf('"') != -1 && !isName) {
                return movieName + str.substring(0, str.indexOf('"') - 1);
            }
            if (isName) {
                movieName += ' ' + str;
            }
        }
        return movieName.substring(1);
    }
}
