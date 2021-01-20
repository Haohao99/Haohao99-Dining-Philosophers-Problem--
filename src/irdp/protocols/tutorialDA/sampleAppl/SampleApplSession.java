/*
 *
 * Hands-On code of the book Introduction to Reliable Distributed Programming
 * by Christian Cachin, Rachid Guerraoui and Luis Rodrigues
 * Copyright (C) 2005-2011 Luis Rodrigues
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 * Contact
 * 	Address:
 *		Rua Alves Redol 9, Office 605
 *		1000-029 Lisboa
 *		PORTUGAL
 * 	Email:
 * 		ler@ist.utl.pt
 * 	Web:
 *		http://homepages.gsd.inesc-id.pt/~ler/
 * 
 */

package irdp.protocols.tutorialDA.sampleAppl;

import irdp.protocols.tutorialDA.consensusUtils.StringProposal;
import irdp.protocols.tutorialDA.delay.DelayEvent;
import irdp.protocols.tutorialDA.events.*;
import irdp.protocols.tutorialDA.utils.ProcessSet;
import irdp.protocols.tutorialDA.tcpBasedPFD.PFDStartEvent;

import net.sf.appia.core.*;
import net.sf.appia.core.events.channel.ChannelClose;
import net.sf.appia.core.events.channel.ChannelInit;
import net.sf.appia.core.events.SendableEvent;
import net.sf.appia.core.message.Message;
import net.sf.appia.protocols.common.RegisterSocketEvent;

import java.awt.BorderLayout;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import java.util.Scanner;

/**
 * Session implementing the sample application.
 * 
 * @author nuno
 */
public class SampleApplSession extends Session {

	Channel channel;
	private ProcessSet processes;
	private SampleApplReader reader;
	private boolean blocked = false;
	private String usernameString = "default";
	private ArrayList<String> historyArrayList = new ArrayList<String>();
	private JFrame frame;

	public SampleApplSession(Layer layer) {
		super(layer);
	}

	public void init(ProcessSet processes) {
		this.processes = processes;
	}

	public void handle(Event event) {
		// System.out.println("Received event: " + event.getClass().getName());
		if (event instanceof SampleSendableEvent)
			handleSampleSendableEvent((SampleSendableEvent) event);
		else if (event instanceof ChannelInit) {
//			Scanner input = new Scanner(System.in);
//			System.out.println("What is your username?");
//			String usernameString = input.next();
//			
			handleChannelInit((ChannelInit) event);
		}

		else if (event instanceof ChannelClose)
			handleChannelClose((ChannelClose) event);
		else if (event instanceof RegisterSocketEvent)
			handleRegisterSocket((RegisterSocketEvent) event);
		else if (event instanceof ConsensusDecide)
			handleConsensusDecide((ConsensusDecide) event);
		else if (event instanceof NBACDecide)
			handleNBACDecide((NBACDecide) event);
		else if (event instanceof ViewEvent)
			handleMembView((ViewEvent) event);
		else if (event instanceof BlockEvent)
			handleBlock((BlockEvent) event);
		else if (event instanceof ReleaseEvent)
			handleRelease((ReleaseEvent) event);
		else if (event instanceof SharedReadReturn)
			handleSharedReadReturn((SharedReadReturn) event);
		else if (event instanceof SharedWriteReturn)
			handleSharedWriteReturn((SharedWriteReturn) event);
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
			// sends this event to open a socket in the layer that is used has perfect
			// point to point
			// channels or unreliable point to point channels.
			RegisterSocketEvent rse = new RegisterSocketEvent(channel, Direction.DOWN, this);
			rse.port = ((InetSocketAddress) processes.getSelfProcess().getSocketAddress()).getPort();
			rse.localHost = ((InetSocketAddress) processes.getSelfProcess().getSocketAddress()).getAddress();

			rse.go();
			ProcessInitEvent processInit = new ProcessInitEvent(channel, Direction.DOWN, this);

			processInit.setProcessSet(processes);
			processInit.go();
		} catch (AppiaEventException e1) {
			e1.printStackTrace();
		}
//		System.out.println("Channel is open.");
		// starts the thread that reads from the keyboard.
		System.out.println("Welcome to Knock Knock!");
		reader = new SampleApplReader(this);
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
	 * @param decide
	 */
	private void handleConsensusDecide(ConsensusDecide decide) {
		System.out.println("Receive Consensus decision: " + ((StringProposal) decide.decision).msg);
	}

	/**
	 * @param decide
	 */
	private void handleNBACDecide(NBACDecide decide) {
		System.out.println("Commit " + decide.decision);
	}

	/**
	 * @param event
	 */
	private void handleMembView(ViewEvent event) {
		System.out.println("View: " + event.view);
	}

	/**
	 * @param event
	 */
	private void handleRelease(ReleaseEvent event) {
		System.out.println("The group is no longer blocked, messages can be sent.");
		blocked = false;
	}

	/**
	 * @param event
	 */
	private void handleBlock(BlockEvent event) {
		System.out.println("The group is blocked for view change, therefore messages can not be sent.");
		blocked = true;

		try {
			BlockOkEvent ev = new BlockOkEvent(event.getChannel(), Direction.DOWN, this);
			ev.go();
		} catch (AppiaEventException ex) {
			ex.printStackTrace();
		}
	}

	private void handleSharedReadReturn(SharedReadReturn event) {
		System.out.println("Register " + event.reg + " reads " + event.value);
	}

	private void handleSharedWriteReturn(SharedWriteReturn event) {
		System.out.println("Register " + event.reg + " written");
	}

	/**
	 * @param event
	 */
	private void handleIncomingEvent(SampleSendableEvent event) {

		String message = event.getMessage().popString();

		if (message.startsWith("%")) {
			int Pnum = this.processes.getSelfProcess().getProcessNumber();
			String[] msgList = message.split("%");
//			System.out.println(msgList.length);
//			System.out.println("Mglist"+msgList[1]);
//			System.out.println(Pnum+"");
			if (Integer.parseInt(msgList[1]) == Pnum && msgList[4].equals("N")) {
				System.out.print("Received event with message :" + msgList[5] + "from " + msgList[3] + "\n>");
				this.historyArrayList.add(message);
				return;
			} else if (Integer.parseInt(msgList[1]) == Pnum && msgList[4].equals("A")) {
				System.out.print("Received anonymous event with message :" + msgList[5] + "\n>");
				this.historyArrayList.add(message);
				return;
			}

		} else {
			this.historyArrayList.add(message);
			System.out.print("Received event with message by boardcast: " + message + "\n>");
		}
	}

	/**
	 * @param event
	 */
	private void handleOutgoingEvent(SampleSendableEvent event) {
		String command = event.getCommand();
		if ("bcast".equals(command))
			handleBCast(event);
		else if ("startpfd".equals(command))
			handleStartPFD(event);
		else if ("consensus".equals(command))
			handleConsensus(event);
		else if ("atomic".equals(command))
			handleAtomic(event);
		else if ("read".equals(command))
			handleRead(event);
		else if ("write".equals(command))
			handleWrite(event);
		else if ("delay".equals(command))
			handleDelay(event);
		else if ("help".equals(command))
			printHelp();
		// added Aaron 8-2-19
		else if ("send".equals(command))
			handleSend(event, "N");
		else if ("history".equals(command))
			handleHistory(event);
		else if ("rename".equals(command))
			handleRename(event);
		else if ("anonymous".equals(command))
			handleSend(event, "A");
		else if ("guiopen".equals(command))
			handleGui(event);
		else if ("guiclose".equals(command))
			handleGuiclose(event);
		else {
			System.out.println("Invalid command: " + command);
			printHelp();
		}
	}

	private void handleGuiclose(SampleSendableEvent event) {
		// TODO Auto-generated method stub
		frame.setVisible(false);

	}

	private void handleGui(SampleSendableEvent event) {

		frame = new JFrame("Chat Frame");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 800);

		JMenuBar mb = new JMenuBar();

		JMenu help = new JMenu("Help");
		JMenuItem startpfd = new JMenuItem("startpfd - starts the Perfect Failure detector (when it applies)");
		JMenuItem bcast = new JMenuItem("bcast <msg> - Broadcast the message \"msg\"");
		JMenuItem consensus = new JMenuItem("consensus <string> - Initiates a consensus decision with the given value");
		JMenuItem atomic = new JMenuItem("atomic <value> - Initiates an atomic commit with the given value (0 or 1)");
		JMenuItem read = new JMenuItem(
				"read <register> - Reads the shared memory register with the given id (integer)");
		JMenuItem write = new JMenuItem(
				"write <register> <value> - Writes the value in the shared memory register with the given id (integer)");
		JMenuItem delay = new JMenuItem(
				"delay <process_number> <tics> - Delays all messages received from the given process number by a number of tics, usually seconds");
		JMenuItem send = new JMenuItem("send <process_number> <msg> - Send message to specified process");
		JMenuItem history = new JMenuItem("history - Print History");
		JMenuItem anonymous = new JMenuItem("anonymous - send anonymous message");
		mb.add(help);
		help.add(startpfd);
		help.add(bcast);
		help.add(consensus);
		help.add(atomic);
		help.add(read);
		help.add(write);
		help.add(delay);
		help.add(send);
		help.add(history);
		help.add(anonymous);

		JPanel panel = new JPanel();
		panel.setToolTipText("Welcome to knockknock");

		JLabel label = new JLabel("Enter Text");
		JTextField tf = new JTextField(10);
		JButton sendb = new JButton("Send");
		JButton Anonymous = new JButton("Anonymous");
//				JButton startButton = new JButton("Start");
		panel.add(label);
		panel.add(tf);
		panel.add(sendb);
		panel.add(Anonymous);
//				panel.add(startButton);

		JTextArea ta = new JTextArea("Welcome to Knock Knock");

		frame.getContentPane().add(BorderLayout.SOUTH, panel);
		frame.getContentPane().add(BorderLayout.NORTH, mb);
		frame.getContentPane().add(BorderLayout.CENTER, ta);

		frame.setVisible(true);

	}

	private void handleRename(SampleSendableEvent event) {
		// TODO Auto-generated method stub
		String s = event.getMessage().popString();
		StringTokenizer st = new StringTokenizer(s);
		String usern = st.nextToken();
		this.usernameString = usern;
		System.out.println("Your name is successfully changed to: " + usernameString);
	}

	private void handleHistory(SampleSendableEvent event) {
		// TODO Auto-generated method stub
		for (String str : historyArrayList) {
			System.out.println(str);
		}

	}

	/**
	 * @param event
	 */
	private void handleBCast(SampleSendableEvent event) {
		if (blocked) {
			System.out.println("The group is blocked, therefore message can not be sent.");
			return;
		}

		try {

			event.go();
		} catch (AppiaEventException e) {
			e.printStackTrace();
		}
	}

	private void handleSend(SampleSendableEvent event, String key) {
		if (blocked) {
			System.out.println("The group is blocked, therefore message can not be sent.");
			return;
		}
		int Pnum = this.processes.getSelfProcess().getProcessNumber();

		String s = event.getMessage().popString();
		StringTokenizer st = new StringTokenizer(s);
		String sprocess = st.nextToken();
		String msg = "" + "%" + sprocess + "%" + Pnum + "%" + this.usernameString + "%" + key + "%";
		while (st.hasMoreTokens())
			msg += (st.nextToken() + " ");
		String[] msgList = msg.split("%");
		System.out.println("Handling Send to process " + sprocess + " with message: " + msgList[5]);
		this.historyArrayList.add(msg);
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
	 * @param event
	 */
	private void handleStartPFD(SampleSendableEvent event) {
		try {
			PFDStartEvent pfdStart = new PFDStartEvent(channel, Direction.DOWN, this);
			pfdStart.go();
		} catch (AppiaEventException e) {
			e.printStackTrace();
		}

	}

	private void handleConsensus(SampleSendableEvent event) {
		String s = event.getMessage().popString();
		try {
			ConsensusPropose ev = new ConsensusPropose(event.getChannel(), Direction.DOWN, this);
			ev.value = new StringProposal(s);
			ev.go();
		} catch (AppiaEventException ex) {
			ex.printStackTrace();
		}
	}

	private void handleAtomic(SampleSendableEvent event) {
		String s = event.getMessage().popString();
		try {
			NBACPropose ev = new NBACPropose(event.getChannel(), Direction.DOWN, this);
			ev.value = (Integer.parseInt(s.trim()) == 0 ? 0 : 1);
			ev.go();
		} catch (AppiaEventException ex) {
			ex.printStackTrace();
		}
	}

	private void handleRead(SampleSendableEvent event) {
		String s = event.getMessage().popString();
		try {
			SharedRead ev = new SharedRead(event.getChannel(), Direction.DOWN, this);
			ev.reg = Integer.parseInt(s.trim());
			ev.go();
		} catch (AppiaEventException ex) {
			ex.printStackTrace();
		}
	}

	private void handleWrite(SampleSendableEvent event) {
		String s = event.getMessage().popString();
		StringTokenizer st = new StringTokenizer(s);
		String sreg = st.nextToken();
		s = "";
		while (st.hasMoreTokens())
			s += st.nextToken();

		try {
			SharedWrite ev = new SharedWrite(event.getChannel(), Direction.DOWN, this);
			ev.reg = Integer.parseInt(sreg.trim());
			ev.value = s;
			ev.go();
		} catch (AppiaEventException ex) {
			ex.printStackTrace();
		}
	}

	private void handleDelay(SampleSendableEvent event) {
		String s = event.getMessage().popString();
		StringTokenizer st = new StringTokenizer(s);
		String sprocess = st.nextToken();
		String stics = st.nextToken();

		try {
			DelayEvent ev = new DelayEvent(event.getChannel(), Direction.DOWN, this);
			ev.processDelayed = Integer.parseInt(sprocess);
			ev.ticsDelayed = Integer.parseInt(stics);
			ev.go();
		} catch (AppiaEventException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private void printHelp() {
		System.out.println("Available commands:\n"
				+ "startpfd - starts the Perfect Failure detector (when it applies)\n"
				+ "bcast <msg> - Broadcast the message \"msg\"\n"
				+ "consensus <string> - Initiates a consensus decision with the given value\n"
				+ "atomic <value> - Initiates an atomic commit with the given value (0 or 1)\n"
				+ "read <register> - Reads the shared memory register with the given id (integer)\n"
				+ "write <register> <value> - Writes the value in the shared memory register with the given id (integer)\n"
				+ "delay <process_number> <tics> - Delays all messages received from the given process number by a number of tics, usually seconds\n"
				+ "help - Print this help information.\n"
				+ "send <process_number> <msg> - Send message to specified process\n" + "history - Print History\n"
				+ "anonymous - send anonymous message\n"
				+"guiopen - show gui\n"
				+"guiclose - close gui\n");
	}

}
