package bgu.spl181.net.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UsersListJson {

    @SerializedName("users")
    @Expose
    private List<UserJson> usersList;

    public List<UserJson> getUsersList() {
        return usersList;
    }

    public void setUsersList(List<UserJson> usersList) {
        this.usersList = usersList;
    }
}
