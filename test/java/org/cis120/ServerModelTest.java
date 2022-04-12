package org.cis120;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class ServerModelTest {
    private ServerModel model;

    /**
     * Before each test, we initialize model to be
     * a new ServerModel (with all new, empty state)
     */
    @BeforeEach
    public void setUp() {
        // We initialize a fresh ServerModel for each test
        model = new ServerModel();
    }

    /**
     * Here is an example test that checks the functionality of your
     * changeNickname error handling. Each line has commentary directly above
     * it which you can use as a framework for the remainder of your tests.
     */
    @Test
    public void testInvalidNickname() {
        // A user must be registered before their nickname can be changed,
        // so we first register a user with an arbitrarily chosen id of 0.
        model.registerUser(0);

        // We manually create a Command that appropriately tests the case
        // we are checking. In this case, we create a NicknameCommand whose
        // new Nickname is invalid.
        Command command = new NicknameCommand(0, "User0", "!nv@l!d!");

        // We manually create the expected Broadcast using the Broadcast
        // factory methods. In this case, we create an error Broadcast with
        // our command and an INVALID_NAME error.
        Broadcast expected = Broadcast.error(
                command, ServerResponse.INVALID_NAME
        );

        // We then get the actual Broadcast returned by the method we are
        // trying to test. In this case, we use the updateServerModel method
        // of the NicknameCommand.
        Broadcast actual = command.updateServerModel(model);

        // The first assertEquals call tests whether the method returns
        // the appropriate Broadcast.
        assertEquals(expected, actual, "Broadcast");

        // We also want to test whether the state has been correctly
        // changed.In this case, the state that would be affected is
        // the user's Collection.
        Collection<String> users = model.getRegisteredUsers();

        // We now check to see if our command updated the state
        // appropriately. In this case, we first ensure that no
        // additional users have been added.
        assertEquals(1, users.size(), "Number of registered users");

        // We then check if the username was updated to an invalid value
        // (it should not have been).
        assertTrue(users.contains("User0"), "Old nickname still registered");

        // Finally, we check that the id 0 is still associated with the old,
        // unchanged nickname.
        assertEquals(
                "User0", model.getNickname(0),
                "User with id 0 nickname unchanged"
        );
    }

    /*
     * Your TAs will be manually grading the tests that you write below this
     * comment block. Don't forget to test the public methods you have added to
     * your ServerModel class, as well as the behavior of the server in
     * different scenarios.
     * You might find it helpful to take a look at the tests we have already
     * provided you with in Task4Test, Task3Test, and Task5Test.
     */
    @Test
    public void testRegisterOneDeleteOne() {
        Broadcast expected = Broadcast.connected("User0");

        model.registerUser(1);
        model.deregisterUser(1);

        assertTrue(model.getRegisteredUsers().isEmpty(), "No registered users");
    }

    @Test
    public void testDeregisterMultipleUsers() {
        model.registerUser(0);
        model.registerUser(1);
        model.registerUser(2);

        model.deregisterUser(0);
        model.deregisterUser(1);
        model.deregisterUser(2);

        assertFalse(model.getRegisteredUsers().contains("User1"), "User1 doesn't exist");
    }

    @Test
    public void testNormalNickname() {
        model.registerUser(0);
        model.registerUser(1);
        model.registerUser(2);

        NicknameCommand command = new NicknameCommand(0, "User0", "Olga");
        NicknameCommand command2 = new NicknameCommand(1, "User1", "Cathy");
        NicknameCommand command3 = new NicknameCommand(2, "User2", "Mia");

        model.changeNickname(command);
        model.changeNickname(command2);
        model.changeNickname(command3);
        Collection<String> users = model.getRegisteredUsers();

        assertTrue(users.contains("Olga"), "User0 changed their nickname to Olga");
        assertTrue(users.contains("Cathy"), "User1 changed their nickname to Cathy");
        assertTrue(users.contains("Mia"), "User2 changed their nickname to  Mia");
        assertEquals(3, model.getRegisteredUsers().size());
    }

    @Test
    public void testCreateChannelAlreadyExists() {
        model.registerUser(0);
        CreateCommand create0 = new CreateCommand(0, "User0", "java", false);
        model.createChannel(create0);

        model.registerUser(1);
        CreateCommand create1 = new CreateCommand(0, "User1", "java", false);

        assertTrue(
                model.getChannels().contains("java"),
                "channel still exists"
        );
        Broadcast expected = Broadcast.error(create1, ServerResponse.NAME_ALREADY_IN_USE);
        assertEquals(expected, model.createChannel(create1), "broadcast");
    }

    @Test
    public void testJoinNonExistentChannel() {
        model.registerUser(0);
        model.registerUser(1);
        Command create = new CreateCommand(0, "User0", "java", false);
        create.updateServerModel(model);
        JoinCommand join = new JoinCommand(1, "User1", "javaNonExistent");
        join.updateServerModel(model);

        Broadcast expected = Broadcast.error(join, ServerResponse.NO_SUCH_CHANNEL);
        assertEquals(expected, model.joinChannel(join), "broadcast");
        assertEquals(1, model.getChannels().size());
    }

    @Test
    public void testMesgNonExistentChannel() {
        model.registerUser(0);

        Command mesg = new MessageCommand(0, "User0", "java", "hello");
        Set<String> recipients = new TreeSet<>();

        Broadcast expected = Broadcast.error(mesg, ServerResponse.NO_SUCH_CHANNEL);
        assertEquals(expected, mesg.updateServerModel(model), "broadcast");
    }

    @Test
    public void testMesgUserNotInChannel() {
        model.registerUser(0);
        model.registerUser(1);
        Command create = new CreateCommand(0, "User0", "java", false);

        create.updateServerModel(model);
        MessageCommand mesg = new MessageCommand(1, "User1", "java", "hello");

        Broadcast expected = Broadcast.error(mesg, ServerResponse.USER_NOT_IN_CHANNEL);
        assertEquals(expected, mesg.updateServerModel(model), "broadcast");
    }

    @Test
    public void testUserLeaveNonExistentChannel() {
        model.registerUser(0);

        LeaveCommand leave = new LeaveCommand(0, "User0", "java");
        leave.updateServerModel(model);

        Broadcast expected = Broadcast.error(leave, ServerResponse.NO_SUCH_CHANNEL);

        assertEquals(expected, leave.updateServerModel(model), "broadcast");
    }

    @Test
    public void testUserLeaveNotHisChannel() {
        model.registerUser(5);
        String user5 = model.getNickname(5);
        model.registerUser(6);
        String user6 = model.getNickname(6);


        Command create = new CreateCommand(5, user5, "java", false);
        create.updateServerModel(model);

        LeaveCommand leave = new LeaveCommand(6, user6, "java");
        leave.updateServerModel(model);

        Broadcast expected = Broadcast.error(leave, ServerResponse.USER_NOT_IN_CHANNEL);
        assertEquals(expected, model.leaveChannel(leave));
    }

    @Test
    public void testJoinPrivateChannel() {
        model.registerUser(0);
        CreateCommand create = new CreateCommand(0, "User0", "java5", true);

        model.registerUser(1);

        create.updateServerModel(model);

        JoinCommand join = new JoinCommand(1, "User1", "java5");

        Broadcast expected = Broadcast.error(join, ServerResponse.JOIN_PRIVATE_CHANNEL);
        assertEquals(expected, model.joinChannel(join));
    }
    @Test
    public void testNonExistentUserInvited() {
        model.registerUser(0);
        CreateCommand create = new CreateCommand(0, "User0", "java5", true);

        create.updateServerModel(model);

        InviteCommand invite = new InviteCommand(0, "User0", "java5", "User1");

        Broadcast expected = Broadcast.error(invite, ServerResponse.NO_SUCH_USER);
        assertEquals(expected, model.inviteUser(invite));
    }
    @Test
    public void testNonPrivateChannel() {
        model.registerUser(0);
        CreateCommand create = new CreateCommand(0, "User0", "java5", false);
        model.registerUser(1);

        create.updateServerModel(model);
        InviteCommand invite = new InviteCommand(0, "User0", "java5", "User1");

        Broadcast expected = Broadcast.error(invite, ServerResponse.INVITE_TO_PUBLIC_CHANNEL);
        assertEquals(expected, model.inviteUser(invite));
    }
    @Test
    public void testInviteNonExistentChannel() {
        model.registerUser(0);
        model.registerUser(1);

        InviteCommand invite = new InviteCommand(0, "User0", "java5", "User1");

        Broadcast expected = Broadcast.error(invite, ServerResponse.NO_SUCH_CHANNEL);
        assertEquals(expected, model.inviteUser(invite));
    }
    @Test
    public void testKickNonExistentChannel() {
        model.registerUser(0);
        model.registerUser(1);

        KickCommand kick = new KickCommand(0, "User0", "java5", "User1");

        Broadcast expected = Broadcast.error(kick, ServerResponse.NO_SUCH_CHANNEL);
        assertEquals(expected, model.kickUser(kick));
    }
    @Test
    public void testNonExistentUserKicked() {
        model.registerUser(0);
        CreateCommand create = new CreateCommand(0, "User0", "java5", false);

        create.updateServerModel(model);

        KickCommand kick = new KickCommand(0, "User0", "java5", "User1");

        Broadcast expected = Broadcast.error(kick, ServerResponse.NO_SUCH_USER);
        assertEquals(expected, model.kickUser(kick));
    }
    @Test
    public void testNonOwnerKicks() {
        model.registerUser(0);
        CreateCommand create = new CreateCommand(0, "User0", "java5", false);
        model.registerUser(1);

        create.updateServerModel(model);
        JoinCommand join = new JoinCommand(1, "User1", "java5");

        KickCommand kick = new KickCommand(1, "User1", "java5", "User0");

        Broadcast expected = Broadcast.error(kick, ServerResponse.USER_NOT_OWNER);
        assertEquals(expected, model.kickUser(kick));
    }
    @Test
    public void testUserNotInChannel() {
        model.registerUser(0);
        CreateCommand create = new CreateCommand(0, "User0", "java5", false);
        model.registerUser(1);
        create.updateServerModel(model);
        KickCommand kick = new KickCommand(0, "User0", "java5", "User1");

        Broadcast expected = Broadcast.error(kick, ServerResponse.USER_NOT_IN_CHANNEL);
        assertEquals(expected, model.kickUser(kick));
    }
    @Test
    public void testDeregisterOwnerOfMultiple() {
        model.registerUser(0);
        CreateCommand create = new CreateCommand(0, "User0", "java1", false);
        CreateCommand create1 = new CreateCommand(0, "User0", "java2", false);
        create.updateServerModel(model);
        create1.updateServerModel(model);

        model.deregisterUser(0);

        assertEquals(0, model.getChannels().size());
    }
}
