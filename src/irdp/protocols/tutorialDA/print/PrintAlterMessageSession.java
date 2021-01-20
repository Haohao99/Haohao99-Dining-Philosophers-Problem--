package irdp.protocols.tutorialDA.print;

import net.sf.appia.core.AppiaEventException;
import net.sf.appia.core.Direction;
import net.sf.appia.core.Event;
import net.sf.appia.core.Layer;
import net.sf.appia.core.Session;
import net.sf.appia.core.events.channel.ChannelInit;

public class PrintAlterMessageSession extends Session {

	public PrintAlterMessageSession(Layer layer) {
		super(layer);
	}

	public void handle(Event event) {
		System.out.println();

		if (event instanceof ChannelInit)
			handleChannelInit((ChannelInit) event);
		else if (event instanceof PrintConfirmEvent)
			handlePrintConfirm((PrintConfirmEvent) event);
		else if (event instanceof PrintAlarmEvent)
			handlePrintAlarm((PrintAlarmEvent) event);
		else if (event instanceof PrintStatusEvent)
			handlePrintStatus((PrintStatusEvent) event);
		else if (event instanceof PrintRequestEvent)
			handlePrintRequestEvent((PrintRequestEvent) event);

	}

	private void handlePrintRequestEvent(PrintRequestEvent request) {
		String reString = request.getString();
		if (reString.contains("illegal")||reString.contains("ILLEGAL")) {
			System.out.println(
					"\t"+"[PrintAlterMessage: found illegal string, returning an IllegalPrintRequestEvent back up]");
			IllegalPrintRequestEvent ill = new IllegalPrintRequestEvent();
			ill.setChannel(request.getChannel());
			ill.setDir(Direction.UP);
			ill.setSourceSession(this);
			try {
				ill.init();
				ill.go();
			} catch (AppiaEventException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("\t"+"[PrintAlterMessage: received print request, changing to uppercase and forwarding it on...]");
			try {
				request.setString(request.getString().toUpperCase());
				request.go();
				
			} catch (AppiaEventException e) {
				e.printStackTrace();
			}
		}
	}

	private void handlePrintStatus(PrintStatusEvent event) {
		PrintStatusEvent status = new PrintStatusEvent();
		if(event.status.equals(Status.OK)) {
			System.out.println("\t"+"[PrintAlterMessage: received status OK for request "+event.getId()+"] now forwarding up");
			
			status.setId(event.getId());
			status.setStatus(Status.OK);
		}else {
			System.out.println("\t"+"[PrintAlterMessage: received status NOK for request "+event.getId()+"] now forwarding up");
			
			status.setId(event.getId());
			status.setStatus(Status.NOK);
		}
		
		

		try {
			
			status.setChannel(event.getChannel());
			status.setDir(Direction.UP);
			status.setSourceSession(this);
			status.init();
			status.go();
		} catch (AppiaEventException e) {
			e.printStackTrace();
		}

	}

	private void handleChannelInit(ChannelInit init) {
		try {

			init.go();
		} catch (AppiaEventException e) {
			e.printStackTrace();
		}
	}

	private void handlePrintConfirm(PrintConfirmEvent conf) {
		PrintStatusEvent conif = new PrintStatusEvent();
		conif.setId(conf.getId());
		conif.setStatus(Status.OK);

		try {
			conif.setChannel(conf.getChannel());
			conif.setDir(Direction.UP);
			conif.setSourceSession(this);
			conif.init();
			conif.go();
		} catch (AppiaEventException e) {
			e.printStackTrace();
		}

	}

	private void handlePrintAlarm(PrintAlarmEvent alarm) {
		PrintAlarmEvent ala = new PrintAlarmEvent();
		ala.setChannel(alarm.getChannel());
		ala.setDir(Direction.UP);
		ala.setSourceSession(this);
		try {
			ala.init();
			ala.go();
		} catch (AppiaEventException e) {
			e.printStackTrace();
		}
	}

}
