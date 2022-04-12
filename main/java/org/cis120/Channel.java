package org.cis120;

import java.util.TreeSet;

/**
 * The {@code Channel} is the class responsible for storing the
 * information about the channel, including its users, owner,
 * name and privacy status.
 */
public class Channel implements Comparable<Channel> {

    private TreeSet<User> usersInChannel;
    private String owner;
    private String channelName;
    private boolean isPrivate;

    /**
     * Constructs a {@code Channel}.
     */
    public Channel(String channelName, String owner, boolean isPrivate) {
        usersInChannel = new TreeSet<User>();
        this.channelName = channelName;
        this.owner = owner;
        this.isPrivate = isPrivate;
    }

    /**
     * Gets the nickname of the current owner of the channel.
     *
     * @return owner, the current owner of the channel.
     */
    public String getChannelOwner() {
        return owner;
    }

    /**
     * Gets the name of the channel.
     *
     * @return channelName, the current name of the channel.
     */
    public String getChannelName() {
        return channelName;
    }

    /**
     * Gets the privacy status of the channel.
     *
     * @return isPrivate, true if private, false if public.
     */
    public boolean isPrivate() {
        return isPrivate;
    }

    /**
     * Gets a collection of strings that are the nicknames
     * of the users in the channel.
     *
     * @return TreeSet<String>, the nicknames of the users in
     * the channel.
     */
    public TreeSet<String> getUserNicknames() {
        TreeSet<String> names = new TreeSet<>();
        for (User currUser : usersInChannel) {
            names.add(currUser.getNickname());
        }
        return names;
    }
    /**
     * Removes an argued user from the channel.
     *
     * @param name is the name of the user that has to be removed
     */
    public void removeUser(String name) {
        usersInChannel.removeIf(currUser -> currUser.getNickname().equals(name));
    }

    /**
     * Adds an argued user t0 the channel.
     *
     * @param name is the name of the user that has to be added.
     */
    public void addUser(String name, Integer theirID) {
        usersInChannel.add(new User(theirID, name));
    }
    /**
     * Compares the channel name. This method is created to implement the
     * Comparable interface.
     *
     * @param channel Channel object
     * @return integer based on how the object compares
     */
    @Override
    public int compareTo(Channel channel) {
        if (getChannelName().equals(channel.getChannelName())) {
            return 0;
        } else {
            return -1;
        }
    }
}
