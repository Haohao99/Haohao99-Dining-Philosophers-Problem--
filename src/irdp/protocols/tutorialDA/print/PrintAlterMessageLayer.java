package irdp.protocols.tutorialDA.print;

import net.sf.appia.core.Layer;
import net.sf.appia.core.Session;
import net.sf.appia.core.events.channel.ChannelInit;

public class PrintAlterMessageLayer  extends Layer{
	
	public PrintAlterMessageLayer() {
		 /* events that the protocol will create */
		
		evProvide = new Class[1];
		evProvide[0] = IllegalPrintRequestEvent.class;
		
		evRequire = new Class[0];
		
		/* events that the protocol will accept */
		evAccept = new Class[5];
		evAccept[0] = PrintRequestEvent.class;
		evAccept[1] = PrintConfirmEvent.class;
	    evAccept[2] = PrintStatusEvent.class;
	    evAccept[3] = PrintAlarmEvent.class;
	    evAccept[4] = ChannelInit.class;
		
	}

	@Override
	public Session createSession() {
		
		return new PrintAlterMessageSession(this);
	}

}
