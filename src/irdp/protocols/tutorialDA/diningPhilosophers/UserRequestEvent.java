/*
 *
 * Written by Aaron Wilkin 8-5-19
 * 
 */

package irdp.protocols.tutorialDA.diningPhilosophers;

import net.sf.appia.core.AppiaEventException;
import net.sf.appia.core.Channel;
import net.sf.appia.core.Session;
import net.sf.appia.core.Event;

/**
 * Sendable Event used by the application.
 * 
 * @author nuno
 */
public class UserRequestEvent extends Event {

  private String command;

  /**
   * Default constructor.
   */
  public UserRequestEvent() {
    super();
  }
  
  public UserRequestEvent(Channel c, int dir, Session s)
  throws AppiaEventException{
	    super(c, dir, s);
  }
  

  /**
   * @return Returns the command.
   */
  public String getCommand() {
    return command;
  }

  /**
   * @param command
   *          The command to set.
   */
  public void setCommand(String command) {
    this.command = command;
  }

}
