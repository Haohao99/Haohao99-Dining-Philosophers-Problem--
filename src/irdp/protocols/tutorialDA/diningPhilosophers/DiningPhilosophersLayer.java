/*
 *
 * Written by Aaron Wilkin 8-5-19
 * 
 */

package irdp.protocols.tutorialDA.diningPhilosophers;

import irdp.protocols.tutorialDA.events.ProcessInitEvent;
import net.sf.appia.core.Layer;
import net.sf.appia.core.Session;
//import net.sf.appia.core.events.SendableEvent;
import net.sf.appia.core.events.channel.ChannelClose;
import net.sf.appia.core.events.channel.ChannelInit;

/**
 * Layer of the Dining Philosophers.
 * 
 * @author nuno
 */
public class DiningPhilosophersLayer extends Layer {

  public DiningPhilosophersLayer() {
    /* events that the protocol will create */
    evProvide = new Class[3];
    evProvide[0] = RequestChopstickEvent.class;
    evProvide[1] = ChopstickReplyEvent.class;
    evProvide[2] = NotifyEatEvent.class;

    /*
     * events that the protocol require to work. This is a subset of the
     * accepted events
     */
    evRequire = new Class[7];
    evRequire[0] = UserRequestEvent.class;
    evRequire[1] = ChannelInit.class;
    evRequire[2] = ProcessInitEvent.class;
    evRequire[3] = RequestToEatEvent.class;
    evRequire[4] = FinishedEatingEvent.class;
    evRequire[5] = RequestChopstickEvent.class;
    evRequire[6] = ChopstickReplyEvent.class;

    /* events that the protocol will accept */
    evAccept = new Class[8];
    evAccept[0] = UserRequestEvent.class;
    evAccept[1] = ChannelInit.class;
    evAccept[2] = ChannelClose.class;
    evAccept[3] = ProcessInitEvent.class;
    evAccept[4] = RequestToEatEvent.class;
    evAccept[5] = FinishedEatingEvent.class;
    evAccept[6] = RequestChopstickEvent.class;
    evAccept[7] = ChopstickReplyEvent.class;

  }

  /**
   * Creates a new session to this protocol.
   * 
   * @see appia.Layer#createSession()
   */
  public Session createSession() {
    return new DiningPhilosophersSession(this);
  }

}
