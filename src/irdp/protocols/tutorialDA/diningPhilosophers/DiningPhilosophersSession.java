/*
 *
 * Written by Aaron Wilkin 8-5-19
 * 
 */

package irdp.protocols.tutorialDA.diningPhilosophers;

import irdp.protocols.tutorialDA.events.ProcessInitEvent;
import irdp.protocols.tutorialDA.events.SampleSendableEvent;
import irdp.protocols.tutorialDA.utils.Debug;
import irdp.protocols.tutorialDA.utils.ProcessSet;
import irdp.protocols.tutorialDA.utils.SampleProcess;
import net.sf.appia.core.AppiaEventException;
import net.sf.appia.core.Direction;
import net.sf.appia.core.Event;
import net.sf.appia.core.Layer;
import net.sf.appia.core.Session;
import net.sf.appia.core.message.Message;
import net.sf.appia.core.events.SendableEvent;
import net.sf.appia.core.events.channel.ChannelInit;

import java.net.SocketAddress;
import java.util.StringTokenizer;

enum DPState {
	IDLE, WAITINGFORLEFT, WAITINGFORRIGHT, EATING;
} // "IDLE" means "thinking" at this layer, but it is the application layer
	// that does the actual thinking, so "IDLE" is a good name for this
	// layer to indicate it is not eating, nor in the process of requesting
	// any chopsticks...

enum Chopstick {
	AVAILABLE, HOLDING, OTHERPROCESSHOLDING;
}

/**
 * Session implementing the Basic Broadcast protocol.
 * 
 * @author nuno
 * 
 */
public class DiningPhilosophersSession extends Session {

	/*
	 * State of the protocol: the set of processes in the group
	 */
	private ProcessSet processes;

	private int selfRank;
	private int leftRank;
	private int rightRank;
	private DPState state;
	private Chopstick leftChopstick;
	private Chopstick rightChopstick;

	/**
	 * Builds a new BEBSession.
	 * 
	 * @param layer
	 */
	public DiningPhilosophersSession(Layer layer) {
		super(layer);
		state = DPState.IDLE;
		leftChopstick = Chopstick.AVAILABLE;
		rightChopstick = Chopstick.AVAILABLE;
	}

	/**
	 * Handles incoming events.
	 * 
	 * @see appia.Session#handle(appia.Event)
	 */
	public void handle(Event event) {
		// Init events. Channel Init is from Appia and ProcessInitEvent is to know
		// the elements of the group
		if (event instanceof ChannelInit)
			handleChannelInit((ChannelInit) event);
		else if (event instanceof ProcessInitEvent)
			handleProcessInitEvent((ProcessInitEvent) event);
		else if (event instanceof RequestToEatEvent)
			handleRequestToEatEvent((RequestToEatEvent) event);
		else if (event instanceof FinishedEatingEvent)
			handleFinishedEatingEvent((FinishedEatingEvent) event);
		else if (event instanceof RequestChopstickEvent)
			handleRequestChopstickEvent((RequestChopstickEvent) event);
		else if (event instanceof ChopstickReplyEvent)
			handleChopstickReplyEvent((ChopstickReplyEvent) event);
		else if (event instanceof SendableEvent) {
			if (event.getDir() == Direction.DOWN)
				// UPON event from the above protocol (or application)
				handleEventFromAppl((SendableEvent) event);
			else
				// UPON event from the bottom protocol (or perfect point2point links)
				handleEventFromLowerLayer((SendableEvent) event);
		}
	}

	/**
	 * Gets the process set and forwards the event to other layers.
	 * 
	 * @param event
	 */
	private void handleProcessInitEvent(ProcessInitEvent event) {
		processes = event.getProcessSet();
		selfRank = processes.getSelfRank();
		leftRank = (selfRank + (processes.getSize() - 1)) % processes.getSize();
		rightRank = (selfRank + 1) % processes.getSize();
		System.out.println("I am " + selfRank + " Left: " + leftRank + " Right: " + rightRank);
		try {
			event.go();
		} catch (AppiaEventException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handles the first event that arrives to the protocol session. In this case,
	 * just forwards it.
	 * 
	 * @param init
	 */
	private void handleChannelInit(ChannelInit init) {
		try {
			init.go();
		} catch (AppiaEventException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handles the request to eat event sent from the DiningPhilosophersApplLayer
	 * 
	 * @param init
	 */
	private void handleRequestToEatEvent(RequestToEatEvent event) {
		System.out.println("Received request to eat from application layer");
		// TODO: Complete this method. This event comes from the application layer
		// and indicates that the philosopher is hungry and wishes to start eating.
		// You should create a RequestChopstickEvent and send it to the philosopher
		// on your left (the one with left rank; you can set the destination of
		// the event using this.processes.getProcess(leftRank).getSocketAddress();)
		// You will want to change state to indicate you are waiting for a response
//		// from your left neighbor.
//		String s = event.getMessage().popString();
//	    StringTokenizer st = new StringTokenizer(s);
//	    String sprocess = st.nextToken();
//	    String msg = "";
//	    while (st.hasMoreTokens())
//	        msg += (st.nextToken() + " ");
//
//	    System.out.println("Handling Send to process " + sprocess + " with message: " + msg);
		RequestChopstickEvent sendingEvent = null;
		try {
			sendingEvent = new RequestChopstickEvent(event.getChannel(), Direction.DOWN, this);
			Message message = sendingEvent.getMessage();
//			message.pushString(msg);
			// set source and destination of event message
			sendingEvent.source = this.processes.getSelfProcess().getSocketAddress();
//			int destProcNum = Integer.parseInt(sprocess);
			sendingEvent.dest = this.processes.getProcess(leftRank).getSocketAddress();

			sendingEvent.setSourceSession(this);

			sendingEvent.init();
			sendingEvent.go();
			state = DPState.WAITINGFORLEFT;

		} catch (AppiaEventException e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	/**
	 * Handles the request for chopstick event sent from other process
	 * 
	 * @param init
	 */
	private void handleRequestChopstickEvent(RequestChopstickEvent event) {
		int fromRank = processes.getRank((SocketAddress) event.source);
		System.out.println("Received chopstick request from process " + fromRank);
		// TODO: Finish this method. Just above, the fromRank is the process
		// number that is requesting a chopstick. Depending on which process
		// it is, it could be asking for your left or your right chopstick.
		// You should determine if the chopstick is available (is the upper
		// application in the EATING state, i.e., you are holding both already,
		// or do you already have one chopstick and you're waiting for the other,
		// etc.?). If the chopstick is available, you should send a ChopstickReplyEvent
		// to the requesting process that contains a message. If the chopstick is
		// available, the message should be something like "Accept" and if the
		// chopstick is NOT available, the message should be something like "Deny"
		String s = null;
		if (fromRank == leftRank) {
			if (leftChopstick == Chopstick.AVAILABLE) {
				s = "Accept";
				leftChopstick = Chopstick.OTHERPROCESSHOLDING;

			} else {
				s = "Deny";
			}

//			StringTokenizer st = new StringTokenizer(s);
//			String sprocess = st.nextToken();
//			String msg = s;
//			while (st.hasMoreTokens())
//				msg += (st.nextToken() + " ");
			ChopstickReplyEvent sendingEvent = null;
			try {
				sendingEvent = new ChopstickReplyEvent(event.getChannel(), Direction.DOWN, this);
				Message message = sendingEvent.getMessage();
				message.pushString(s);
				// set source and destination of event message
				sendingEvent.source = this.processes.getSelfProcess().getSocketAddress();
//		      int destProcNum = Integer.parseInt(sprocess);
				sendingEvent.dest = this.processes.getProcess(fromRank).getSocketAddress();

				sendingEvent.setSourceSession(this);

				sendingEvent.init();
				sendingEvent.go();
			} catch (AppiaEventException ex) {
				ex.printStackTrace();
			}

		}

		if (fromRank == rightRank) {
			if (rightChopstick == Chopstick.AVAILABLE) {
				s = "Accept";
				rightChopstick = Chopstick.OTHERPROCESSHOLDING;

			} else {
				s = "Deny";
			}

//			StringTokenizer st = new StringTokenizer(s);
//			String sprocess = st.nextToken();
//			String msg = s;
//			while (st.hasMoreTokens())
//				msg += (st.nextToken() + " ");

//		    System.out.println("Handling Send to process " + sprocess + " with message: " + msg);
			ChopstickReplyEvent sendingEvent = null;
			try {
				sendingEvent = new ChopstickReplyEvent(event.getChannel(), Direction.DOWN, this);
				Message message = sendingEvent.getMessage();
				message.pushString(s);
				// set source and destination of event message
				sendingEvent.source = this.processes.getSelfProcess().getSocketAddress();
//		      int destProcNum = Integer.parseInt(sprocess);
				sendingEvent.dest = this.processes.getProcess(fromRank).getSocketAddress();

				sendingEvent.setSourceSession(this);

				sendingEvent.init();
				sendingEvent.go();
			} catch (AppiaEventException ex) {
				ex.printStackTrace();
			}

		}

	}

	/**
	 * Handles the reply from a previous request for chopstick event sent from other
	 * process
	 * 
	 * @param init
	 */
	private void handleChopstickReplyEvent(ChopstickReplyEvent event) {
		String message = event.getMessage().popString();
		int fromRank = processes.getRank((SocketAddress) event.source);
		
		// TODO: This method is quite involved. When this method is called, it indicates
		// that a process has replied to your previous RequestChopstickEvent and has
		// sent a ChopstickReplyEvent. At this point, you should know which chopstick
		// you are waiting for (right or left) and which process the reply came from.
		// Using that information, the message will either contain "Accept" or "Deny"
		// (see method handleRequestChopstickEvent above).
		//
		// If you receive an accept event from the left process, mark that chopstick as
		// HOLDING and then request the right chopstick, by sending a
		// RequestChopstickEvent
		// to the right process (and set your state accordingly).
		//
		// If the reply is from the right process, and it is an accept message, then
		// you are already holding the left one, now mark the right chopstick as
		// HOLDING and inform the application layer that it may eat by sending a
		// NotifyEatEvent UP to the application layer. The message should indicate
		// a positive result (like "can" or "can eat") to indicate that the application
		// may now enter the EATING state.
		//
		// If the ChopstickReplyEvent you receive above is negative (the message is
		// "Deny"), then you should release ANY and all chopsticks that you may already
		// be holding and then send a negative NotifyEatEvent up to the application
		// layer.
		// To indicate that the application cannot eat, the message within the
		// NotifyEatEvent
		// should be something like "cannot" or "cannot eat").
		if (message.equals("Deny") && fromRank == leftRank) {
			if(rightChopstick==Chopstick.HOLDING)
				rightChopstick = Chopstick.AVAILABLE;
			System.out.println("Cannot have left");
			String s = "cannot";
//			String msg = s;
			NotifyEatEvent sendingEvent = null;
			try {
				sendingEvent = new NotifyEatEvent(event.getChannel(), Direction.UP, this);
				Message message1 = new Message();
				message1.pushString(s);
//				message.pushString(msg);
				// set source and destination of event message
				sendingEvent.setMessage(message1);
				sendingEvent.source = this.processes.getSelfProcess().getSocketAddress();
//				int destProcNum = Integer.parseInt(sprocess);
				sendingEvent.dest = this.processes.getProcess(rightRank).getSocketAddress();

				sendingEvent.setSourceSession(this);

				sendingEvent.init();
				sendingEvent.go();
				state = DPState.WAITINGFORRIGHT;

			} catch (AppiaEventException e) {
				// TODO: handle exception
				e.printStackTrace();
			}

		}
		if (message.equals("Deny") && fromRank == rightRank) {
			if(leftChopstick==Chopstick.HOLDING)
				leftChopstick=Chopstick.AVAILABLE;
			System.out.println("Cannot have right");
			String s = "cannot";
//			String msg = s;
			NotifyEatEvent sendingEvent = null;
			try {
				sendingEvent = new NotifyEatEvent(event.getChannel(), Direction.UP, this);
				Message message1 = new Message();
				message1.pushString(s);
//				message.pushString(msg);
				// set source and destination of event message
				sendingEvent.setMessage(message1);
				sendingEvent.source = this.processes.getSelfProcess().getSocketAddress();
//				int destProcNum = Integer.parseInt(sprocess);
				sendingEvent.dest = this.processes.getProcess(rightRank).getSocketAddress();

				sendingEvent.setSourceSession(this);

				sendingEvent.init();
				sendingEvent.go();
				state = DPState.WAITINGFORRIGHT;

			} catch (AppiaEventException e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			

		}
		if (message.equals("Accept") && fromRank == leftRank) {
			leftChopstick = Chopstick.HOLDING;
			RequestChopstickEvent sendingEvent = null;
			try {
				sendingEvent = new RequestChopstickEvent(event.getChannel(), Direction.DOWN, this);
//				Message newmessage = sendingEvent.getMessage();
//				message.pushString(msg);
				// set source and destination of event message
				sendingEvent.source = this.processes.getSelfProcess().getSocketAddress();
//				int destProcNum = Integer.parseInt(sprocess);
				sendingEvent.dest = this.processes.getProcess(rightRank).getSocketAddress();

				sendingEvent.setSourceSession(this);

				sendingEvent.init();
				sendingEvent.go();
				state = DPState.WAITINGFORRIGHT;

			} catch (AppiaEventException e) {
				// TODO: handle exception
				e.printStackTrace();
			}

		}
		if (message.equals("Accept") && fromRank == rightRank) {
			rightChopstick = Chopstick.HOLDING;
			state = DPState.EATING;
			String s = "can";
//			String msg = s;
			NotifyEatEvent sendingEvent = null;
			try {
				sendingEvent = new NotifyEatEvent(event.getChannel(), Direction.UP, this);
				Message message1 = new Message();
				message1.pushString(s);
//				message.pushString(msg);
				// set source and destination of event message
				sendingEvent.setMessage(message1);
				sendingEvent.source = this.processes.getSelfProcess().getSocketAddress();
//				int destProcNum = Integer.parseInt(sprocess);
				sendingEvent.dest = this.processes.getProcess(rightRank).getSocketAddress();

				sendingEvent.setSourceSession(this);

				sendingEvent.init();
				sendingEvent.go();
				state = DPState.WAITINGFORRIGHT;

			} catch (AppiaEventException e) {
				// TODO: handle exception
				e.printStackTrace();
			}

		}
	} // end method

	/**
	 * Handles the finished eating event sent from the DiningPhilosophersApplLayer
	 * 
	 * @param init
	 */
	private void handleFinishedEatingEvent(FinishedEatingEvent event) {
		System.out.println("Received notification of finished eating from application layer");
		// TODO: YOu actually just need a few lines for this method. This event
		// is sent from the application layer indicating that the philosopher
		// is done eating. In which case, you should set the state of the
		// chopsticks as AVAILABLE and any other state that applies.
		leftChopstick = Chopstick.AVAILABLE;
		rightChopstick = Chopstick.AVAILABLE;
		state = DPState.IDLE;
	}

	/**
	 * Handles the event sent from the DiningPhilosophersApplLayer
	 * 
	 * @param init
	 */
	private void handleEventFromAppl(SendableEvent event) {
		// This is a catch all in case an event is sent and not caught as something else
		// You shouldn't need to change this method, and it should frankly never
		// be called.
		System.out.println("Received SendableEvent from application layer");
	}

	/**
	 * Handles the event sent from the lower layers
	 * 
	 * @param init
	 */
	private void handleEventFromLowerLayer(SendableEvent event) {
		// This is a catch all in case an event is sent and not caught as something else
		// You shouldn't need to change this method, and it should frankly never
		// be called.
		System.out.println("Received SendableEvent from lower layer");
	}

}
