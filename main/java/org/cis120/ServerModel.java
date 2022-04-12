package org.cis120;

import java.util.*;

/*
 * Make sure to write your own tests in ServerModelTest.java.
 * The tests we provide for each task are NOT comprehensive!
 */

/**
 * The {@code ServerModel} is the class responsible for tracking the
 * state of the server, including its current users and the channels
 * they are in.
 * This class is used by subclasses of {@link Command} to:
 * 1. handle commands from clients, and
 * 2. handle commands from {@link ServerBackend} to coordinate
 * client connection/disconnection.
 */
public final class ServerModel {
    private TreeMap<Integer, String> users;
    private TreeMap<String, Channel> channels;

    /**
     * Constructs a {@code ServerModel}. Make sure to initialize any collections
     * used to model the server state here.
     */
    public ServerModel() {
        users = new TreeMap<Integer, String>();
        channels = new TreeMap<String, Channel>();

    }

    // =========================================================================
    // == Task 2: Basic Server model queries
    // == These functions provide helpful ways to test the state of your model.
    // == You may also use them in later tasks.
    // =========================================================================

    /**
     * Gets the user ID currently associated with the given
     * nickname. The returned ID is -1 if the nickname is not
     * currently in use.
     *
     * @param nickname The nickname for which to get the associated user ID
     * @return The user ID of the user with the argued nickname if
     * such a user exists, otherwise -1
     */
    public int getUserId(String nickname) {
        for (Map.Entry<Integer, String> entry : users.entrySet()) {
            Integer currUserID = entry.getKey();
            String currNickname = entry.getValue();
            if (currNickname.equals(nickname)) {
                return currUserID;
            }
        }
        return -1;
    }

    /**
     * Gets the nickname currently associated with the given user
     * ID. The returned nickname is null if the user ID is not
     * currently in use.
     *
     * @param userId The user ID for which to get the associated
     *               nickname
     * @return The nickname of the user with the argued user ID if
     * such a user exists, otherwise null
     */
    public String getNickname(int userId) {
        return users.get(userId);
    }

    /**
     * Gets a collection of the nicknames of all users who are
     * registered with the server. Changes to the returned collection
     * should not affect the server state.
     * <p>
     * This method is provided for testing.
     *
     * @return The collection of registered user nicknames
     */
    public Collection<String> getRegisteredUsers() {
        TreeSet<String> copy = new TreeSet<>();
        for (Map.Entry<Integer, String> someUser : users.entrySet()) {
            copy.add(someUser.getValue());
        }
        return copy;
    }

    /**
     * Gets a collection of the names of all the channels that are
     * present on the server. Changes to the returned collection
     * should not affect the server state.
     * <p>
     * This method is provided for testing.
     *
     * @return The collection of channel names
     */
    public Collection<String> getChannels() {
        TreeSet<String> copy = new TreeSet<>();
        for (Map.Entry<String, Channel> someChannel : channels.entrySet()) {
            copy.add(someChannel.getKey());
        }
        return copy;
    }

    /**
     * Gets a collection of the nicknames of all the users in a given
     * channel. The collection is empty if no channel with the given
     * name exists. Modifications to the returned collection should
     * not affect the server state.
     * <p>
     * This method is provided for testing.
     *
     * @param channelName The channel for which to get member nicknames
     * @return A collection of all user nicknames in the channel
     */
    public Collection<String> getUsersInChannel(String channelName) {
        if (!channels.containsKey(channelName)) {
            TreeSet<String> emptySet = new TreeSet<String>();
            return emptySet;
        } else {
            return channels.get(channelName).getUserNicknames();
        }
    }

    /**
     * Gets the nickname of the owner of the given channel. The result
     * is {@code null} if no channel with the given name exists.
     * <p>
     * This method is provided for testing.
     *
     * @param channelName The channel for which to get the owner nickname
     * @return The nickname of the channel owner if such a channel
     * exists; otherwise, return null
     */
    public String getOwner(String channelName) {
        if (!channels.containsKey(channelName)) {
            return null;
        } else {
            return channels.get(channelName).getChannelOwner();
        }
    }

    // ===============================================
    // == Task 3: Connections and Setting Nicknames ==
    // ===============================================

    /**
     * This method is automatically called by the backend when a new client
     * connects to the server. It should generate a default nickname with
     * {@link #generateUniqueNickname()}, store the new user's ID and username
     * in your data structures for {@link ServerModel} state, and construct
     * and return a {@link Broadcast} object using
     * {@link Broadcast#connected(String)}}.
     *
     * @param userId The new user's unique ID (automatically created by the
     *               backend)
     * @return The {@link Broadcast} object generated by calling
     * {@link Broadcast#connected(String)} with the proper parameter
     */
    public Broadcast registerUser(int userId) {
        String nickname = generateUniqueNickname();
        while (!isValidName(nickname)) {
            nickname = generateUniqueNickname();
        }
        users.put(userId, nickname);
        return Broadcast.connected(nickname);
    }

    /**
     * Helper for {@link #registerUser(int)}. (Nothing to do here.)
     * <p>
     * Generates a unique nickname of the form "UserX", where X is the
     * smallest non-negative integer that yields a unique nickname for a user.
     *
     * @return The generated nickname
     */
    private String generateUniqueNickname() {
        int suffix = 0;
        String nickname;
        Collection<String> existingUsers = getRegisteredUsers();
        do {
            nickname = "User" + suffix++;
        } while (existingUsers.contains(nickname));
        return nickname;
    }

    /**
     * This method is automatically called by the backend when a client
     * disconnects from the server. This method should take the following
     * actions, not necessarily in this order:
     * <p>
     * (1) All users who shared a channel with the disconnected user should be
     * notified that they left
     * (2) All channels owned by the disconnected user should be deleted
     * (3) The disconnected user's information should be removed from
     * {@link ServerModel}'s internal state
     * (4) Construct and return a {@link Broadcast} object using
     * {@link Broadcast#disconnected(String, Collection)}.
     *
     * @param userId The unique ID of the user to deregister
     * @return The {@link Broadcast} object generated by calling
     * {@link Broadcast#disconnected(String, Collection)} with the proper
     * parameters
     */
    public Broadcast deregisterUser(int userId) {
        TreeSet<String> recipients = new TreeSet<String>();
        String nickname = users.get(userId);

        TreeSet<Channel> channelsSet = new TreeSet<>();

        for (Map.Entry<String, Channel> someChannel : channels.entrySet()) {
            Channel currChannel = someChannel.getValue();

            if (currChannel.getChannelOwner().equals(nickname)) {
                recipients.addAll(currChannel.getUserNicknames());
                channelsSet.add(currChannel);
            } else if (someChannel.getValue().getUserNicknames().contains(nickname)) {
                currChannel.removeUser(getNickname(userId));
                recipients.addAll(currChannel.getUserNicknames());
            }
        }

        for (Channel currChannel : channelsSet) {
            channels.remove(currChannel.getChannelName());
        }
        recipients.remove(getNickname(userId));
        users.remove(userId);
        return Broadcast.disconnected(nickname, recipients);
    }

    /**
     * This method is called when a user wants to change their nickname.
     *
     * @param nickCommand The {@link NicknameCommand} object containing
     *                    all information needed to attempt a nickname change
     * @return The {@link Broadcast} object generated by
     * {@link Broadcast#okay(Command, Collection)} if the nickname
     * change is successful. The command should be the original nickCommand
     * and the collection of recipients should be any clients who
     * share at least one channel with the sender, including the sender.
     * <p>
     * If an error occurs, use
     * {@link Broadcast#error(Command, ServerResponse)} with either:
     * (1) {@link ServerResponse#INVALID_NAME} if the proposed nickname
     * is not valid according to
     * {@link ServerModel#isValidName(String)}
     * (2) {@link ServerResponse#NAME_ALREADY_IN_USE} if there is
     * already a user with the proposed nickname
     */
    public Broadcast changeNickname(NicknameCommand nickCommand) {
        Integer id = nickCommand.getSenderId();
        String oldName = nickCommand.getSender();
        String newName = nickCommand.getNewNickname();
        TreeSet<String> recipients = new TreeSet<>();

        if (getRegisteredUsers().contains(newName)) {
            return Broadcast.error(nickCommand, ServerResponse.NAME_ALREADY_IN_USE);
        }
        if (!isValidName(newName)) {
            return Broadcast.error(nickCommand, ServerResponse.INVALID_NAME);
        }
        users.replace(id, newName);

        for (Map.Entry<String, Channel> entry : channels.entrySet()) {
            Channel currChannel = entry.getValue();
            TreeSet<String> userNicknames = (TreeSet<String>)
                    getUsersInChannel(currChannel.getChannelName());

            if (userNicknames.contains(oldName)) {
                currChannel.removeUser(oldName);
                currChannel.addUser(newName, id);

                userNicknames.remove(oldName);
                userNicknames.add(newName);

                recipients.addAll(userNicknames);
            }

        }
        return Broadcast.okay(nickCommand, recipients);
    }

    /**
     * Determines if a given nickname is valid or invalid (contains at least
     * one alphanumeric character, and no non-alphanumeric characters).
     * (Nothing to do here.)
     *
     * @param name The channel or nickname string to validate
     * @return true if the string is a valid name
     */
    public static boolean isValidName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        for (char c : name.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) {
                return false;
            }
        }
        return true;
    }

    // ===================================
    // == Task 4: Channels and Messages ==
    // ===================================

    /**
     * This method is called when a user wants to create a channel.
     * You can ignore the privacy aspect of this method for task 4, but
     * make sure you come back and implement it in task 5.
     *
     * @param createCommand The {@link CreateCommand} object containing all
     *                      information needed to attempt channel creation
     * @return The {@link Broadcast} object generated by
     * {@link Broadcast#okay(Command, Collection)} if the channel
     * creation is successful. The only recipient should be the new
     * channel's owner.
     * <p>
     * If an error occurs, use
     * {@link Broadcast#error(Command, ServerResponse)} with either:
     * (1) {@link ServerResponse#INVALID_NAME} if the proposed
     * channel name is not valid according to
     * {@link ServerModel#isValidName(String)}
     * (2) {@link ServerResponse#CHANNEL_ALREADY_EXISTS} if there is
     * already a channel with the proposed name
     */
    public Broadcast createChannel(CreateCommand createCommand) {
        String channelName = createCommand.getChannel();
        TreeSet<String> ownerCollection = new TreeSet<>();
        String owner = createCommand.getSender();
        boolean isPrivate = createCommand.isInviteOnly();

        if (!isValidName(createCommand.getChannel())) {
            return Broadcast.error(createCommand, ServerResponse.INVALID_NAME);
        }
        if (getChannels().contains(channelName)) {
            return Broadcast.error(createCommand, ServerResponse.NAME_ALREADY_IN_USE);
        }
        Channel newChannel = new Channel(channelName, owner, isPrivate);

        channels.put(channelName, newChannel);
        ownerCollection.add(owner);

        for (Map.Entry<String, Channel> entry : channels.entrySet()) {
            Channel currChannel = entry.getValue();
            currChannel.addUser(owner, getUserId(owner));
        }
        return Broadcast.okay(createCommand, ownerCollection);
    }

    /**
     * This method is called when a user wants to join a channel.
     * You can ignore the privacy aspect of this method for task 4, but
     * make sure you come back and implement it in task 5.
     *
     * @param joinCommand The {@link JoinCommand} object containing all
     *                    information needed for the user's join attempt
     * @return The {@link Broadcast} object generated by
     * {@link Broadcast#names(Command, Collection, String)} if the user
     * joins the channel successfully. The recipients should be all
     * people in the joined channel (including the sender).
     * <p>
     * If an error occurs, use
     * {@link Broadcast#error(Command, ServerResponse)} with either:
     * (1) {@link ServerResponse#NO_SUCH_CHANNEL} if there is no
     * channel with the specified name
     * (2) (after Task 5) {@link ServerResponse#JOIN_PRIVATE_CHANNEL} if
     * the sender is attempting to join a private channel
     */
    public Broadcast joinChannel(JoinCommand joinCommand) {
        String channelName = joinCommand.getChannel();
        String senderName = joinCommand.getSender();
        TreeSet<String> recipients = new TreeSet<>();

        for (Map.Entry<String, Channel> entry : channels.entrySet()) {
            Channel channel = entry.getValue();

            if (channel.getChannelName().equals(channelName)) {

                if (channel.isPrivate()) {
                    return Broadcast.error(joinCommand, ServerResponse.JOIN_PRIVATE_CHANNEL);
                }
                channel.addUser(senderName, getUserId(senderName));
                recipients.addAll(channel.getUserNicknames());
                recipients.remove(channel.getChannelName());
                return Broadcast.names(joinCommand, recipients, channel.getChannelOwner());
            }
        }
        return Broadcast.error(joinCommand, ServerResponse.NO_SUCH_CHANNEL);
    }

    /**
     * This method is called when a user wants to send a message to a channel.
     *
     * @param messageCommand The {@link MessageCommand} object containing all
     *                       information needed for the messaging attempt
     * @return The {@link Broadcast} object generated by
     * {@link Broadcast#okay(Command, Collection)} if the message
     * attempt is successful. The recipients should be all clients
     * in the channel.
     * <p>
     * If an error occurs, use
     * {@link Broadcast#error(Command, ServerResponse)} with either:
     * (1) {@link ServerResponse#NO_SUCH_CHANNEL} if there is no
     * channel with the specified name
     * (2) {@link ServerResponse#USER_NOT_IN_CHANNEL} if the sender is
     * not in the channel they are trying to send the message to
     */
    public Broadcast sendMessage(MessageCommand messageCommand) {

        String channelName = messageCommand.getChannel();
        Integer senderId = messageCommand.getSenderId();
        String senderName = getNickname(senderId);

        for (Map.Entry<String, Channel> entry : channels.entrySet()) {
            Channel channel = entry.getValue();

            if (channel.getChannelName().equals(channelName)) {
                if (channel.getUserNicknames().contains(senderName)) {
                    TreeSet<String> recipients = new TreeSet<>();
                    recipients.addAll(channel.getUserNicknames());
                    return Broadcast.okay(messageCommand, recipients);
                }

                return Broadcast.error(messageCommand, ServerResponse.USER_NOT_IN_CHANNEL);
            }
        }
        return Broadcast.error(messageCommand, ServerResponse.NO_SUCH_CHANNEL);
    }

    /**
     * This method is called when a user wants to leave a channel.
     *
     * @param leaveCommand The {@link LeaveCommand} object containing all
     *                     information about the user's leave attempt
     * @return The {@link Broadcast} object generated by
     * {@link Broadcast#okay(Command, Collection)} if the user leaves
     * the channel successfully. The recipients should be all clients
     * who were in the channel, including the user who left.
     * <p>
     * If an error occurs, use
     * {@link Broadcast#error(Command, ServerResponse)} with either:
     * (1) {@link ServerResponse#NO_SUCH_CHANNEL} if there is no
     * channel with the specified name
     * (2) {@link ServerResponse#USER_NOT_IN_CHANNEL} if the sender is
     * not in the channel they are trying to leave
     */
    public Broadcast leaveChannel(LeaveCommand leaveCommand) {
        String user = leaveCommand.getSender();
        String channelName = leaveCommand.getChannel();
        TreeSet<String> recipients = new TreeSet<>();

        for (Map.Entry<String, Channel> entry : channels.entrySet()) {
            Channel channel = entry.getValue();
            if (channel.getChannelName().equals(channelName)) {

                if (!getUsersInChannel(channelName).contains(user)) {
                    return Broadcast.error(leaveCommand, ServerResponse.USER_NOT_IN_CHANNEL);
                }
                recipients.addAll(channel.getUserNicknames());
                channels.get(channelName).removeUser(user);

                if (user.equals(getOwner(channelName))) {
                    channels.remove(channelName);
                }
                return Broadcast.okay(leaveCommand, recipients);
            }
        }
        return Broadcast.error(leaveCommand, ServerResponse.NO_SUCH_CHANNEL);
    }

    // =============================
    // == Task 5: Channel Privacy ==
    // =============================

    // Go back to createChannel and joinChannel and add
    // all privacy-related functionalities, then delete this when you're done.

    /**
     * This method is called when a channel's owner adds a user to that channel.
     *
     * @param inviteCommand The {@link InviteCommand} object containing all
     *                      information needed for the invite attempt
     * @return The {@link Broadcast} object generated by
     * {@link Broadcast#names(Command, Collection, String)} if the user
     * joins the channel successfully as a result of the invite.
     * The recipients should be all people in the joined channel
     * (including the new user).
     * <p>
     * If an error occurs, use
     * {@link Broadcast#error(Command, ServerResponse)} with either:
     * (1) {@link ServerResponse#NO_SUCH_USER} if the invited user
     * does not exist
     * (2) {@link ServerResponse#NO_SUCH_CHANNEL} if there is no channel
     * with the specified name
     * (3) {@link ServerResponse#INVITE_TO_PUBLIC_CHANNEL} if the
     * invite refers to a public channel
     * (4) {@link ServerResponse#USER_NOT_OWNER} if the sender is not
     * the owner of the channel
     */
    public Broadcast inviteUser(InviteCommand inviteCommand) {
        TreeSet<String> recipients = new TreeSet<>();
        String channelName = inviteCommand.getChannel();
        String senderName = inviteCommand.getSender();
        String invitedUser = inviteCommand.getUserToInvite();

        if (!users.containsKey(getUserId(invitedUser))) {
            return Broadcast.error(inviteCommand, ServerResponse.NO_SUCH_USER);
        }
        for (Map.Entry<String, Channel> entry : channels.entrySet()) {
            Channel channel = entry.getValue();
            if (!channel.getChannelOwner().equals(senderName)) {
                return Broadcast.error(inviteCommand, ServerResponse.USER_NOT_OWNER);
            }
            if (!channel.isPrivate()) {
                return Broadcast.error(inviteCommand, ServerResponse.INVITE_TO_PUBLIC_CHANNEL);
            }
            for (Map.Entry<Integer, String> userMap : users.entrySet()) {
                String currUser = userMap.getValue();
                if (channel.getChannelName().equals(channelName)) {
                    channel.addUser(invitedUser, getUserId(invitedUser));
                    recipients.addAll(channel.getUserNicknames());
                }
            }
            return Broadcast.names(inviteCommand, recipients, channel.getChannelOwner());
        }
        return Broadcast.error(inviteCommand, ServerResponse.NO_SUCH_CHANNEL);
    }

    /**
     * This method is called when a channel's owner removes a user from
     * that channel.
     *
     * @param kickCommand The {@link KickCommand} object containing all
     *                    information needed for the kick attempt
     * @return The {@link Broadcast} object generated by
     * {@link Broadcast#okay(Command, Collection)} if the user is
     * successfully kicked from the channel. The recipients should be
     * all clients who were in the channel, including the user
     * who was kicked.
     * <p>
     * If an error occurs, use
     * {@link Broadcast#error(Command, ServerResponse)} with either:
     * (1) {@link ServerResponse#NO_SUCH_USER} if the user being kicked
     * does not exist
     * (2) {@link ServerResponse#NO_SUCH_CHANNEL} if there is no channel
     * with the specified name
     * (3) {@link ServerResponse#USER_NOT_IN_CHANNEL} if the
     * user being kicked is not a member of the channel
     * (4) {@link ServerResponse#USER_NOT_OWNER} if the sender is not
     * the owner of the channel
     */
    public Broadcast kickUser(KickCommand kickCommand) {
        String senderName = kickCommand.getSender();
        String channelName = kickCommand.getChannel();
        String kickedUser = kickCommand.getUserToKick();
        TreeSet<String> recipients = new TreeSet<>();

        if (!users.containsKey(getUserId(kickedUser))) {
            return Broadcast.error(kickCommand, ServerResponse.NO_SUCH_USER);
        }
        for (Map.Entry<String, Channel> entry : channels.entrySet()) {
            Channel channel = entry.getValue();

            if (channel.getChannelName().equals(channelName)) {

                if (!channel.getChannelOwner().equals(senderName)) {
                    return Broadcast.error(kickCommand, ServerResponse.USER_NOT_OWNER);
                }
                if (!channel.getUserNicknames().contains(kickedUser)) {
                    return Broadcast.error(kickCommand, ServerResponse.USER_NOT_IN_CHANNEL);
                }
                recipients.addAll(channel.getUserNicknames());
                channels.get(channelName).removeUser(kickedUser);
                if (kickedUser.equals(getOwner(channelName))) {

                    for (String userName : getUsersInChannel(channelName)) {
                        channel.removeUser(userName);
                    }
                    channels.remove(channelName);
                }
                return Broadcast.okay(kickCommand, recipients);
            }
        }
        return Broadcast.error(kickCommand, ServerResponse.NO_SUCH_CHANNEL);
    }
}