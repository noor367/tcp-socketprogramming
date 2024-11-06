/*
 * Helper Class to define a group as an object
 * 
 * 11/2023
 * 
 * */

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Group {
    private List<String> invited;
    private List<String> members;
    private String name;
    private int numMsgs = 1;

    public Group(String name, String owner, List<String> invited) {
        this.name = name;
        this.invited = new ArrayList<>();
        this.invited.addAll(invited);

        this.members = new ArrayList<>();
        this.members.add(owner);
    }

    /**
     * Function for /groupjoin command
     * true if user is invited and not already a member, false if otherwise
     * @param username  user requesting to join
     * @return          Boolean return is true if user has joined and false if not
     */
    public boolean join(String username) {
        if (invited.contains(username) && !members.contains(username)) {
            invited.remove(username);
            members.add(username);
            return true;
        }
        return false;
    }
    
    /**
     * Checks if a user is a member
     * @param username
     * @return  true if the user has already joined, false if not 
     */
    public boolean alreadyJoined(String username) {
        if (members.contains(username)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if a user was invited to the group during its creation
     * @param username
     * @return  true if the user was invited, false if not
     */
    public boolean invited(String username) {
        return invited.contains(username);
    }

    /**
     * Getter
     * @return
     */
    public List<String> getMembers() {
        return members;
    }

    /**
     * Helper Function for messagelog.txt
     * Creates and then subsequently writes to the GROUPNAME_messageLog.txt file every time a message is sent
     * Recorded in the format of: messageNumber; timestamp; username; message
     * On each new line in the file
     * @param timestamp    time the message was sent
     * @param user         user who sent the message
     * @param message      the message contents
     */
    public void logMsg(String timestamp, String user, String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(name + "_messageLog.txt", true))) {
            writer.write(this.numMsgs + "; " + timestamp + "; " + user + "; " + message);
            writer.newLine();
            writer.flush();
            this.numMsgs++;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}