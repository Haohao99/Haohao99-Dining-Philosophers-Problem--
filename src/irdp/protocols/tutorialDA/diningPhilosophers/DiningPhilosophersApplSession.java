/*
 *
 * Written by Aaron Wilkin 8-5-19
 * 
 */

package irdp.protocols.tutorialDA.diningPhilosophers;

import irdp.protocols.tutorialDA.events.*;
import irdp.protocols.tutorialDA.tcpBasedPFD.PFDStartEvent;
import irdp.protocols.tutorialDA.utils.ProcessSet;
import net.sf.appia.core.*;
import net.sf.appia.core.events.channel.ChannelClose;
import net.sf.appia.core.events.channel.ChannelInit;
import net.sf.appia.core.events.SendableEvent;
import net.sf.appia.core.message.Message;
import net.sf.appia.protocols.common.RegisterSocketEvent;

import java.net.InetSocketAddress;
import java.util.StringTokenizer;

//declare enum for states
enum ApplState {
	EATING, HUNGRY, THINKING;
}

/**
 * Session implementing the sample application.
 * 
 * @author nuno
 */
public class DiningPhilosophersApplSession extends Session {

  Channel channel;
  private ProcessSet processes;
  private DiningPhilosophersApplReader reader;
  private ApplState state;
  

  public DiningPhilosophersApplSession(Layer layer) {
    super(layer);
    state = ApplState.THINKING;
  }

  public void init(ProcessSet processes) {
    this.processes = processes;
  }

  public void handle(Event event) {
    //System.out.println("Received event: "+event.getClass().getName());
    if (event instanceof SampleSendableEvent)
      handleSampleSendableEvent((SampleSendableEvent) event);
    else if (event instanceof ChannelInit)
      handleChannelInit((ChannelInit) event);
    else if (event instanceof ChannelClose)
      handleChannelClose((ChannelClose) event);
    else if (event instanceof RegisterSocketEvent)
      handleRegisterSocket((RegisterSocketEvent) event);
    else if (event instanceof NotifyEatEvent)
      handleNotifyEatEvent((NotifyEatEvent) event);
  }

  /**
   * @param event
   */
  private void handleRegisterSocket(RegisterSocketEvent event) {
    if (event.error) {
      System.out.println("Address already in use!");
      System.exit(2);
    }
  }

  /**
   * @param init
   */
  private void handleChannelInit(ChannelInit init) {
    try {
      init.go();
    } catch (AppiaEventException e) {
      e.printStackTrace();
    }
    channel = init.getChannel();

    try {
      // sends this event to open a socket in the layer that is used 
      RegisterSocketEvent rse = new RegisterSocketEvent(channel,
          Direction.DOWN, this);
      rse.port = ((InetSocketAddress) processes.getSelfProcess().getSocketAddress()).getPort();
      rse.localHost = ((InetSocketAddress)processes.getSelfProcess().getSocketAddress()).getAddress();
      rse.go();
      ProcessInitEvent processInit = new ProcessInitEvent(channel,
          Direction.DOWN, this);
      processInit.setProcessSet(processes);
      processInit.go();
    } catch (AppiaEventException e1) {
      e1.printStackTrace();
    }
    System.out.println("Channel is open.");
    // starts the thread that reads from the keyboard.
    reader = new DiningPhilosophersApplReader(this);
    reader.start();
  }

  /**
   * @param close
   */
  private void handleChannelClose(ChannelClose close) {
    channel = null;
    System.out.println("Channel is closed.");
  }

  /**
   * @param event
   */
  private void handleSampleSendableEvent(SampleSendableEvent event) {
    if (event.getDir() == Direction.DOWN)
       handleOutgoingEvent(event);
    else
      handleIncomingEvent(event);
  }

  /**
   * @param event
   */
  private void handleIncomingEvent(SampleSendableEvent event) {
    String message = event.getMessage().popString();
    System.out.print("Received an event with message: " + message + "\n>");
  }

  /**
   * @param event
   */
  private void handleOutgoingEvent(SampleSendableEvent event) {
    String command = event.getCommand();
    if ("hungry".equals(command))
      handleHungry(event);
    else if ("think".equals(command))
      handleThink(event);
    else if ("status".equals(command))
      System.out.println("Philosopher " + processes.getSelfRank() + " is " + state);
    else if ("help".equals(command))
      printHelp();
    else {
      System.out.println("Invalid command: " + command);
      printHelp();
    }
  }

  /**
   * @param event
   */
  private void handleHungry(SampleSendableEvent event) {
    //TODO: Fill out this method to handle the case when the philosopher is hungry.
    //You will need to create a RequstToEat event and send that to the layer below to
    //attempt to acquire the chopsticks
	  if(state == ApplState.EATING) {
		  System.out.println("You are already eating");
		  return;
	  }
	  System.out.println("I an hungry");
	  state = ApplState.HUNGRY;
	  RequestToEatEvent requsteat = new RequestToEatEvent();
	  requsteat.setChannel(event.getChannel());
	  requsteat.setDir(Direction.DOWN);
	  requsteat.setSourceSession(this);
	  try {
		requsteat.init();
		requsteat.go();
	} catch (AppiaEventException e) {
		e.printStackTrace();
	}
	  
	  
  }
  
  /**
   * @param event
   */
  private void handleNotifyEatEvent(NotifyEatEvent event) {
    //TODO: This is an event that should come from the lower layer indicating
    //whether you can eat or not.  When you complete the lower layer, you
    //will decide what the message should day in either the cases (the
    //philosopher can eat, OR the philosopher cannot eat). This event will be
    //delivered either way and should indicate whether or not the philosopher
    //can eat.  If not, then the philosopher should go back to thinking, otherwise
    //enter the EATING state
	 if(event.getMessage().popString().equals("can")) {
		 state = ApplState.EATING;
		 System.out.print("Eating");
	 }else {
		 state = ApplState.THINKING;
		 System.out.print("Fail to Eat");
	 }
  }
  
  /**
   * @param event
   */
  private void handleThink(SampleSendableEvent event) {
    //TODO: If you are in an EATING state and this method is called, the user
    //typed "think" indicating that the philosopher should change to the
    //THINKING state.  You will need to send a FinishedEatingEvent to the
    //lower layer, in which case, to release the chopsticks.  If the
    //philosopher is already thinking, then print out a message 
    //"You are already thinking!" and do nothing further.
	  if(state == ApplState.THINKING) {
		  System.out.println("You are already thinking!");
		  return;
	  }
	  state = ApplState.THINKING;
	  FinishedEatingEvent requsteat = new FinishedEatingEvent();
	  requsteat.setChannel(event.getChannel());
	  requsteat.setDir(Direction.DOWN);
	  requsteat.setSourceSession(this);
	  try {
		requsteat.init();
		requsteat.go();
	} catch (AppiaEventException e) {
		e.printStackTrace();
	}
  }

  private void handleSend(SampleSendableEvent event) {

    String s = event.getMessage().popString();
    StringTokenizer st = new StringTokenizer(s);
    String sprocess = st.nextToken();
    String msg = "";
    while (st.hasMoreTokens())
        msg += (st.nextToken() + " ");

    System.out.println("Handling Send to process " + sprocess + " with message: " + msg);
    SampleSendableEvent sendingEvent = null;
    try {
      sendingEvent = new SampleSendableEvent(event.getChannel(), Direction.DOWN, this);
      Message message = sendingEvent.getMessage();
      message.pushString(msg);
      // set source and destination of event message
      sendingEvent.source = this.processes.getSelfProcess().getSocketAddress();
      int destProcNum = Integer.parseInt(sprocess);
      sendingEvent.dest = this.processes.getProcess(destProcNum).getSocketAddress();

      sendingEvent.setSourceSession(this);

      sendingEvent.init();
      sendingEvent.go();
    } catch (AppiaEventException ex) {
      ex.printStackTrace();
    }
  }
  
  /**
   * 
   */
  private void printHelp() {
    System.out
        .println("Available commands:\n"
            + "hungry - Changes the state of the philosopher to hungry which starts the request to start eating (will attempt to get right and left chopsticks automatically)\n"
            + "think - Indicate that the philosopher is finished eating (will automatically release both chopsticks)\n"
            + "status - Get philosopher eating/thinking status\n"
            + "help - Print this help information.\n");
  }

}
