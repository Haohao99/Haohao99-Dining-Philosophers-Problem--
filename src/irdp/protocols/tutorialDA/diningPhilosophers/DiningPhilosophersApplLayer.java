/*
 *
 * Written by Aaron Wilkin 8-5-19
 * 
 */

package irdp.protocols.tutorialDA.diningPhilosophers;

import irdp.protocols.tutorialDA.events.ProcessInitEvent;
import irdp.protocols.tutorialDA.events.SampleSendableEvent;
import irdp.protocols.tutorialDA.tcpBasedPFD.PFDStartEvent;
import net.sf.appia.core.Layer;
import net.sf.appia.core.Session;
import net.sf.appia.core.events.channel.ChannelClose;
import net.sf.appia.core.events.channel.ChannelInit;
import net.sf.appia.protocols.common.RegisterSocketEvent;

/**
 * Layer of the application protocol.
 * 
 * @author nuno
 */
public class DiningPhilosophersApplLayer extends Layer {

  public DiningPhilosophersApplLayer() {
    /* events that the protocol will create */
    evProvide = new Class[6];
    evProvide[0] = ProcessInitEvent.class;
    evProvide[1] = RegisterSocketEvent.class;
    evProvide[2] = PFDStartEvent.class;
    evProvide[3] = UserRequestEvent.class;
    evProvide[4] = RequestToEatEvent.class;
    evProvide[5] = FinishedEatingEvent.class;

    /*
     * events that the protocol require to work. This is a subset of the
     * accepted events
     */
    evRequire = new Class[2];
    evRequire[0] = ChannelInit.class;
    evRequire[1] = NotifyEatEvent.class;

    /* events that the protocol will accept */
    evAccept = new Class[5];
    evAccept[0] = ChannelInit.class;
    evAccept[1] = ChannelClose.class;
    evAccept[2] = RegisterSocketEvent.class;
    evAccept[3] = SampleSendableEvent.class;
    evAccept[4] = NotifyEatEvent.class;
  }

  /**
   * Creates a new session to this protocol.
   * 
   * @see appia.Layer#createSession()
   */
  public Session createSession() {
    return new DiningPhilosophersApplSession(this);
  }

}
