/*
 * Helper Class to define a user
 * 
 * 11/2023
 * 
 * */

public class User {
    private ClientThread thread;
    private boolean active;
    private long lastActive;
    private String username;
    private String password;
    private final long timeout = 10000;
    private long timeoutStart;

    /**
     * Initialises a new user
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.thread = null;
    }

    /**
     * Sets a thread for the user object
     * @param thread  thread the user is using
     */
    public void setThread(ClientThread thread) {
        this.thread = thread;
    }

    /**
     * Getter
     * @return  the active status of the user
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Setter for the active status of user
     * @param active
     */
    public synchronized void setActive(boolean active) {
        this.active = active;
        if (active) setLastActive();
    }

    /**
     * Getter for the last active time of user
     * @return
     */
    public long getLastActive() {
        return lastActive;
    }

    /**
     * Setter for last active time of user
     * @param lastActive
     */
    public synchronized void setLastActive() {
        this.lastActive = System.currentTimeMillis() / 1000;
    }

    /**
     * Returns true if the timeout is active
     * And false is the timeout is not active 
     * @return
     */
    public boolean getTimeout() {
        long timeDiff = (System.currentTimeMillis() / 1000) - timeoutStart;
        return timeDiff < timeout;
    }

    /**
     * Trigger a timeout
     * @param timeout
     */
    public synchronized void setTimeout() {
        this.timeoutStart = System.currentTimeMillis() / 1000;
    }

    /**
     * Getter for username 
     * @return 
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Checks if the given password is valid
     * @param password
     */
    public boolean validPassword(String password) {
        if (password == this.password) {
            return true;
        }
        return false;
    }
}
