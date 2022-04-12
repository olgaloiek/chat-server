package org.cis120;

import java.util.TreeMap;
/**
 * The {@code User} is the class responsible for storing the
 * information about the user, including their ID and nickname.
 */
public class User implements Comparable<User> {
    private Integer userID;
    private String userNickname;
    private TreeMap<Integer, String> user;

    /**
     * Constructs a {@code User}.
     */
    public User(Integer id, String nick) {
        user = new TreeMap<Integer, String>();
        userID = id;
        userNickname = nick;
    }
    /**
     * Gets the user ID currently associated with the given
     * user.
     *
     * @return The user ID of the user on which the method was called.
     */
    public Integer getUserId() {
        return userID;
    }

    /**
     * Gets the user nickname currently associated with the given
     * user.
     *
     * @return The user nickname of the user on which the method was called.
     */
    public String getNickname() {
        return userNickname;
    }

    /**
     * Compares the user ID. This method is created to implement the
     * Comparable interface.
     *
     * @param user User object
     * @return integer based on how the object compares
     */
    @Override
    public int compareTo(User user) {
        if (getUserId() < user.getUserId()) {
            return -1;
        } else if (getUserId() > user.getUserId()) {
            return 1;
        } else {
            return 0;
        }
    }
}
