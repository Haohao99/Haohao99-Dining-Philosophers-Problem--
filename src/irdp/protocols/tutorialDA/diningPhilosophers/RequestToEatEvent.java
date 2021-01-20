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
public class RequestToEatEvent extends Event {
  /**
   * Default constructor.
   */
  public RequestToEatEvent() {
    super();
  }
  
  public RequestToEatEvent(Channel c, int dir, Session s)
  throws AppiaEventException{
	    super(c, dir, s);
  }
}
