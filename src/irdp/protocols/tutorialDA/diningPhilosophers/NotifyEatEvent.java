/*
 *
 * Written by Aaron Wilkin 8-5-19
 * 
 */

package irdp.protocols.tutorialDA.diningPhilosophers;

import net.sf.appia.core.AppiaEventException;
import net.sf.appia.core.Channel;
import net.sf.appia.core.Session;
import net.sf.appia.core.events.SendableEvent;

/**
 * Sendable Event used by the application.
 * 
 * @author nuno
 */
public class NotifyEatEvent extends SendableEvent {
  /**
   * Default constructor.
   */
  public NotifyEatEvent() {
    super();
  }
  
  public NotifyEatEvent(Channel c, int dir, Session s)
  throws AppiaEventException{
	    super(c, dir, s);
  }
}
