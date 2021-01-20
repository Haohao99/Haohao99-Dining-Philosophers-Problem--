
package irdp.protocols.tutorialDA.eagerReliableBroadcast;

import java.util.ArrayList;
import java.util.LinkedList;

import irdp.protocols.tutorialDA.events.ProcessInitEvent;
import irdp.protocols.tutorialDA.utils.Debug;
import irdp.protocols.tutorialDA.utils.ProcessSet;
import irdp.protocols.tutorialDA.utils.SampleProcess;
import net.sf.appia.core.AppiaEventException;
import net.sf.appia.core.Direction;
import net.sf.appia.core.Event;
import net.sf.appia.core.Layer;
import net.sf.appia.core.Session;
import net.sf.appia.core.events.SendableEvent;
import net.sf.appia.core.events.channel.ChannelInit;

import irdp.protocols.tutorialDA.utils.MessageID;

/**
 * Session implementing the Basic Broadcast protocol.
 * 
 * @author Howard Hu, Mengyue Chen
 * 
 */
public class EagerReliableBroadcastSession extends Session {

	/*
	 * State of the protocol: the set of processes in the group
	 */
	private ProcessSet processes;
	private int seqNumber;
	
	private LinkedList<MessageID> delivered;

	/**
	 * Builds a new BEBSession.
	 * 
	 * @param layer
	 */
	public EagerReliableBroadcastSession(Layer layer) {
		super(layer);
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
		else if (event instanceof SendableEvent) {
			if (event.getDir() == Direction.DOWN)
				rbBroadcast((SendableEvent) event);
			else
				bebDeliver((SendableEvent) event);
		}
	}

	/**
	 * Gets the process set and forwards the event to other layers.
	 * 
	 * @param event
	 */
	private void handleProcessInitEvent(ProcessInitEvent event) {
		processes = event.getProcessSet();
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
		
		delivered = new LinkedList<MessageID>();
	}

	/**
	 * Broadcasts a message.
	 * 
	 * @param event
	 */
	private void rbBroadcast(SendableEvent event) {
		Debug.print("ErB: broadcasting message.");
		
		SampleProcess self = processes.getSelfProcess();
	    MessageID msgID = new MessageID(self.getProcessNumber(), seqNumber);
	    seqNumber++;
	    Debug.print("RB: broadcasting message.");
	    event.getMessage().pushObject(msgID);
	    // broadcast the message
	    bebBroadcast(event);
	}
	
	private void bebBroadcast(SendableEvent event) {
	    Debug.print("RB: sending message to beb.");
	    try {
	      event.setDir(Direction.DOWN);
	      event.setSourceSession(this);
	      event.init();
	      event.go();
	    } catch (AppiaEventException e) {
	      e.printStackTrace();
	    }
	  }

	/**
	 * Delivers an incoming message.
	 * 
	 * @param event
	 */
	private void bebDeliver(SendableEvent event) {
		// just sends the message event up
		Debug.print("BEB: Delivering message.");
		MessageID msgID = (MessageID) event.getMessage().peekObject();
		if (!delivered.contains(msgID)) {
			delivered.add(msgID);
			SendableEvent cloned = null;
		      try {
		        cloned = (SendableEvent) event.cloneEvent();
		      } catch (CloneNotSupportedException e) {
		        e.printStackTrace();
		        return;
		      }
		      event.getMessage().popObject();
		      try {
		        event.go();
		      } catch (AppiaEventException e) {
		        e.printStackTrace();
		      }
		      SendableEvent retransmission = null;

		        try {
		          retransmission = (SendableEvent) cloned.cloneEvent();
		        } catch (CloneNotSupportedException e1) {
		          e1.printStackTrace();
		        }
		        bebBroadcast(retransmission);
			
		}
	}

}
