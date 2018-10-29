package bgu.spl181.net.json;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public abstract class UserTemplate <T> {

    @SerializedName("username")
    @Expose
    protected String username;
    @SerializedName("type")
    @Expose
    protected String type;
    @SerializedName("password")
    @Expose
    protected String password;

    public  abstract String getUsername();

    public abstract String getType();

    public abstract String getPassword();
}
