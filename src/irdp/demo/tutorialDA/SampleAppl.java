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

package irdp.demo.tutorialDA;

import irdp.protocols.tutorialDA.delay.DelayLayer;
import irdp.protocols.tutorialDA.eagerReliableBroadcast.EagerReliableBroadcastLayer;
import irdp.protocols.tutorialDA.eagerReliableBroadcast.EagerReliableBroadcastSession;
import irdp.protocols.tutorialDA.GUI.MainApp;
import irdp.protocols.tutorialDA.GUI.QuickTerminal;
import irdp.protocols.tutorialDA.basicBroadcast.*;

import irdp.protocols.tutorialDA.sampleAppl.SampleApplLayer;
import irdp.protocols.tutorialDA.sampleAppl.SampleApplSession;
import irdp.protocols.tutorialDA.tcpBasedPerfectP2P.TcpBasedPerfectP2PLayer;
import irdp.protocols.tutorialDA.tcpBasedPerfectP2P.TcpBasedPerfectP2PSession;
import irdp.protocols.tutorialDA.tcpBasedPFD.TcpBasedPFDLayer;
import irdp.protocols.tutorialDA.tcpBasedPFD.TcpBasedPFDSession;
import irdp.protocols.tutorialDA.fdAppl.FDApplLayer;
import irdp.protocols.tutorialDA.fdAppl.FDApplSession;
import irdp.protocols.tutorialDA.lazyRB.LazyRBLayer;
import irdp.protocols.tutorialDA.utils.ProcessSet;
import irdp.protocols.tutorialDA.utils.SampleProcess;
import net.sf.appia.core.*;
import net.sf.appia.protocols.tcpcomplete.TcpCompleteLayer;
import net.sf.appia.protocols.tcpcomplete.TcpCompleteSession;
import net.sf.appia.protocols.udpsimple.UdpSimpleLayer;
import irdp.protocols.tutorialDA.tcpBasedPFD.TcpBasedPFDLayer;
import irdp.protocols.tutorialDA.tcpBasedPFD.TcpBasedPFDSession;

import java.awt.BorderLayout;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;

/**
 * This class is the MAIN class to run the Reliable Broadcast protocols.
 * 
 * @author nuno
 */
public class SampleAppl {

	/**
	 * Builds the Process set, using the information in the specified file.
	 * 
	 * @param filename the location of the file
	 * @param selfProc the number of the self process
	 * @return a new ProcessSet
	 */
	private static ProcessSet buildProcessSet(String filename, int selfProc) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(0);
		}
		String line;
		StringTokenizer st;
		boolean hasMoreLines = true;
		ProcessSet set = new ProcessSet();
		// reads lines of type: <process number> <IP address> <port>
		while (hasMoreLines) {
			try {
				line = reader.readLine();
				if (line == null)
					break;
				st = new StringTokenizer(line);
				if (st.countTokens() != 3) {
					System.err.println("Wrong line in file: " + st.countTokens());
					continue;
				}
				int procNumber = Integer.parseInt(st.nextToken());
				InetAddress addr = InetAddress.getByName(st.nextToken());
				int portNumber = Integer.parseInt(st.nextToken());
				boolean self = (procNumber == selfProc);
				SampleProcess process = new SampleProcess(new InetSocketAddress(addr, portNumber), procNumber, self);
				set.addProcess(process, procNumber);
			} catch (IOException e) {
				hasMoreLines = false;
			} catch (NumberFormatException e) {
				System.err.println(e.getMessage());
			}
		} // end of while
		return set;
	}

	/**
	 * Builds an Appia channel with the specified QoS
	 * 
	 * @param set the ProcessSet
	 * @param qos the specified QoS
	 * @return a new uninitialized channel
	 */
	private static Channel getChannel(ProcessSet set, String qos) {
		if (qos.equals("beb"))
			return getBebChannel(set);
		else if (qos.equals("rb"))
			return getRbChannel(set);
		else if (qos.equals("urb"))
			return getURbChannel(set);
		else if (qos.equals("iurb"))
			return getIURbChannel(set);
		else if (qos.equals("fc"))
			return getFCChannel(set);
		else if (qos.equals("hc"))
			return getHCChannel(set);
		else if (qos.equals("ufc"))
			return getUFCChannel(set);
		else if (qos.equals("uhc"))
			return getUHCChannel(set);
		else if (qos.equals("conow"))
			return getCOnoWChannel(set);
		else if (qos.equals("conowgc"))
			return getCOnoWGCChannel(set);
		else if (qos.equals("cow"))
			return getCOWChannel(set);
		else if (qos.equals("uto"))
			return getUnTOChannel(set);
		else if (qos.equals("nbac"))
			return getNBACChannel(set);
		else if (qos.equals("cmem"))
			return getCMemChannel(set);
		else if (qos.equals("trbvs"))
			return getTrbVSChannel(set);
		else if (qos.equals("r1nr"))
			return getR1NRChannel(set);
		else if (qos.equals("a1nr"))
			return getA1NRChannel(set);
		else if (qos.equals("annr"))
			return getANNRChannel(set);
		else if (qos.equals("erb"))
			return getErbChannel(set);
		// Added by Aaron 8-2-19
		else if (qos.equals("p2p"))
			return getP2PChannel(set);
		else if (qos.equals("fd"))
			return getFDChannel(set);
		else {
			StringTokenizer st = new StringTokenizer(qos);
			if (st.countTokens() != 3)
				invalidArgs("Unexpected number of tokens when creating the channel...");
			if (!st.nextToken().equals("pb"))
				invalidArgs("");
			int fanout = 0;
			int rounds = 0;
			try {
				fanout = Integer.parseInt(st.nextToken());
				rounds = Integer.parseInt(st.nextToken());
			} catch (NumberFormatException e) {
				invalidArgs(e.getMessage());
			}
			return getPBChannel(set, fanout, rounds);
		}
	}

	/**
	 * Builds a new generic Appia Channel
	 * 
	 * @param processes  set of processes
	 * @param qos        the quality of service layers array
	 * @param qosStr     the string to hand to the QoS initializer
	 * @param channelStr the string to hand the channel initializer
	 * @return a new uninitialized Channel
	 */
	private static Channel getGenericChannel(ProcessSet processes, Layer[] qos, String qosStr, String channelStr) {
		/* Create a QoS */
		QoS myQoS = null;
		try {
			myQoS = new QoS(qosStr, qos);
		} catch (AppiaInvalidQoSException ex) {
			System.err.println("Invalid QoS");
			System.err.println(ex.getMessage());
			System.exit(1);
		}
		/* Create a channel. Uses default event scheduler. */
		Channel channel = myQoS.createUnboundChannel(channelStr);
		/*
		 * Application Session requires special arguments: filename and . A session is
		 * created and binded to the stack. Remaining ones are created by default
		 */
		SampleApplSession sas = (SampleApplSession) qos[qos.length - 1].createSession();
		sas.init(processes);
		ChannelCursor cc = channel.getCursor();
		/*
		 * Application is the last session of the array. Positioning in it is simple
		 */
		try {
			cc.top();
			cc.setSession(sas);
		} catch (AppiaCursorException ex) {
			System.err.println("Unexpected exception in main. Type code:" + ex.type);
			System.exit(1);
		}
		return channel;
	}

	/**
	 * Builds a new Channel with Probabilistic Broadcast.
	 * 
	 * @param processes set of processes
	 * @param fanout    fanout to use in the protocol
	 * @param rounds    number of rounds to use in the protocol
	 * @return a new uninitialized Channel
	 */
	private static Channel getPBChannel(ProcessSet processes, int fanout, int rounds) {
		/* Creates a new PBLayer and initializes it */
		/*
		 * EagerPBLayer pbLayer = new EagerPBLayer(); pbLayer.initValues(fanout,
		 * rounds); // Create layers and put them on a array Layer[] qos = {new
		 * UdpSimpleLayer(), pbLayer, new SampleApplLayer()};
		 * 
		 * return getGenericChannel(processes, qos, "Probabilistic Broadcast QoS",
		 * "Probabilistic Broadcast Channel");
		 */
		return null;
	}

	/**
	 * Builds a new Appia Channel with Best Effort Broadcast
	 * 
	 * @param processes set of processes
	 * @return a new uninitialized Channel
	 */
	private static Channel getBebChannel(ProcessSet processes) {
		/* Create layers and put them on a array */
		/*
		 * Layer[] qos = {new TcpCompleteLayer(), new BasicBroadcastLayer(), new
		 * DelayLayer(), new SampleApplLayer()};
		 * 
		 * return getGenericChannel(processes, qos, "Best Effort Broadcast QoS",
		 * "Best effort Broadcast Channel");
		 */
		return null;
	}

	private static Channel getErbChannel(ProcessSet processes) {
		Layer[] qos = { new TcpCompleteLayer(), new BasicBroadcastLayer(), new DelayLayer(),
				new EagerReliableBroadcastLayer(), new SampleApplLayer() };
		return getGenericChannel(processes, qos, " Eager Reliable Broadcast QoS", " Eager Reliable Broadcast Channel");

	}

	// Added Aaron 8-2-19
	private static Channel getP2PChannel(ProcessSet processes) {
		/* Create layers and put them on a array */
		Layer[] qos = { new TcpBasedPerfectP2PLayer(),
				/*
				 * new BasicBroadcastLayer(), new TcpBasedPFDLayer(), new FDApplLayer(),
				 */ new SampleApplLayer() };

		return getGenericChannel(processes, qos, "Point to point", "Point to Point Channel");
	}

	private static Channel getFDChannel(ProcessSet processes) {
		/* Create layers and put them on a array */
		/*
		 * Layer[] qos = {new TcpCompleteLayer(), new BasicBroadcastLayer(), new
		 * TcpBasedPFDLayer(), new FDApplLayer(), new SampleApplLayer()};
		 * 
		 * return getGenericChannel(processes, qos, "Failure Detector QoS",
		 * "Failure Detector Channel");
		 */
		return null;
	}

	/**
	 * Builds a new Appia Channel with Reliable Broadcast
	 * 
	 * @param processes set of processes
	 * @return a new uninitialized Channel
	 */
	private static Channel getRbChannel(ProcessSet processes) {
//		 Create layers and put them on a array */

		Layer[] qos = { new TcpCompleteLayer(), new BasicBroadcastLayer(), new TcpBasedPFDLayer(), new DelayLayer(),
				new LazyRBLayer(), new SampleApplLayer() };

		return getGenericChannel(processes, qos, "Reliable Broadcast QoS", "Reliable Broadcast Channel");

	}

	/**
	 * Builds a new Appia Channel with Uniform Reliable Broadcast
	 * 
	 * @param processes set of processes
	 * @return a new uninitialized Channel
	 */
	private static Channel getURbChannel(ProcessSet processes) {
		/* Create layers and put them on a array */
		/*
		 * Layer[] qos = {new TcpCompleteLayer(), new BasicBroadcastLayer(), new
		 * TcpBasedPFDLayer(), new DelayLayer(), new AllAckURBLayer(), new
		 * SampleApplLayer()};
		 * 
		 * return getGenericChannel(processes, qos, "Uniform Reliable Broadcast QoS",
		 * "Uniform Reliable Broadcast Channel");
		 */
		return null;
	}

	/**
	 * Builds a new Appia Channel with Indulgent Uniform Reliable Broadcast
	 * 
	 * @param processes set of processes
	 * @return a new uninitialized Channel
	 */
	private static Channel getIURbChannel(ProcessSet processes) {
		/* Create layers and put them on a array */
		/*
		 * Layer[] qos = {new TcpCompleteLayer(), new BasicBroadcastLayer(), new
		 * DelayLayer(), new MajorityAckURBLayer(), new SampleApplLayer()};
		 * 
		 * return getGenericChannel(processes, qos,
		 * "Indulgent Uniform Reliable Broadcast QoS",
		 * "Indulgent Uniform Reliable Broadcast Channel");
		 */
		return null;
	}

	/**
	 * Builds a new Appia Channel with Flooding Consensus
	 * 
	 * @param processes set of processes
	 * @return a new uninitialized Channel
	 */
	private static Channel getFCChannel(ProcessSet processes) {
		/* Create layers and put them on a array */
		/*
		 * Layer[] qos = {new TcpCompleteLayer(), new BasicBroadcastLayer(), new
		 * TcpBasedPFDLayer(), new DelayLayer(), new FloodingConsensusLayer(), new
		 * SampleApplLayer()};
		 * 
		 * return getGenericChannel(processes, qos, "Flooding Consensus QoS",
		 * "Flooding Consensus Channel");
		 */
		return null;
	}

	/**
	 * Builds a new Appia Channel with Hierarchical Consensus
	 * 
	 * @param processes set of processes
	 * @return a new uninitialized Channel
	 */
	private static Channel getHCChannel(ProcessSet processes) {
		/* Create layers and put them on a array */
		/*
		 * Layer[] qos = {new TcpCompleteLayer(), new BasicBroadcastLayer(), new
		 * TcpBasedPFDLayer(), new DelayLayer(), new HierarchicalConsensusLayer(), new
		 * SampleApplLayer()};
		 * 
		 * return getGenericChannel(processes, qos, "Hierarchical Consensus QoS",
		 * "Hierarchical Consensus Channel");
		 */
		return null;
	}

	/**
	 * Builds a new Appia Channel with Uniform Flooding Consensus
	 * 
	 * @param processes set of processes
	 * @return a new uninitialized Channel
	 */
	private static Channel getUFCChannel(ProcessSet processes) {
		/* Create layers and put them on a array */
		/*
		 * Layer[] qos = {new TcpCompleteLayer(), new BasicBroadcastLayer(), new
		 * TcpBasedPFDLayer(), new DelayLayer(), new UniformFloodingConsensusLayer(),
		 * new SampleApplLayer()};
		 * 
		 * return getGenericChannel(processes, qos, "Uniform Flooding Consensus QoS",
		 * "Uniform Flooding Consensus Channel");
		 */
		return null;
	}

	/**
	 * Builds two Appia channels for Uniform Hierarchical Consensus - A BeB channel
	 * - A ReliableBroadcast channel that is started from the
	 * UniformHierarchicalConsensusSession
	 * 
	 * @param processes set of processes
	 * @return a new uninitialized Channel
	 */
	private static Channel getUHCChannel(ProcessSet processes) {
		/*
		 * TcpCompleteLayer tcplayer = new TcpCompleteLayer(); BasicBroadcastLayer
		 * beblayer = new BasicBroadcastLayer(); TcpBasedPFDLayer pfdlayer = new
		 * TcpBasedPFDLayer(); LazyRBLayer rblayer = new LazyRBLayer();
		 * UniformHierarchicalConsensusLayer uhclayer = new
		 * UniformHierarchicalConsensusLayer(); SampleApplLayer salayer = new
		 * SampleApplLayer();
		 * 
		 * // Create layers and put them on a array Layer[] bebqos = {tcplayer,
		 * beblayer, pfdlayer, new DelayLayer(), uhclayer, salayer}; // Create a QoS QoS
		 * bebQoS = null; try { bebQoS = new QoS("UHC-BeB QoS", bebqos); } catch
		 * (AppiaInvalidQoSException ex) { ex.printStackTrace(); System.exit(1); } //
		 * Create a channel. Uses default event scheduler. Channel bebchannel =
		 * bebQoS.createUnboundChannel("UHC-BeB Channel");
		 * 
		 * // Create layers and put them on a array Layer[] rbqos = {tcplayer, beblayer,
		 * pfdlayer, new DelayLayer(), rblayer, uhclayer}; // Create a QoS QoS rbQoS =
		 * null; try { rbQoS = new QoS("UHC-RB QoS", rbqos); } catch
		 * (AppiaInvalidQoSException ex) { ex.printStackTrace(); System.exit(1); } //
		 * Create a channel. Uses default event scheduler Channel rbchannel =
		 * rbQoS.createUnboundChannel("UHC-RB Channel");
		 * 
		 * // All sessions are created explicitly so they can be shared
		 * TcpCompleteSession tcpsession = (TcpCompleteSession) tcplayer
		 * .createSession(); BasicBroadcastSession bebsession = (BasicBroadcastSession)
		 * beblayer .createSession(); TcpBasedPFDSession pfdsession =
		 * (TcpBasedPFDSession) pfdlayer .createSession(); LazyRBSession rbsession =
		 * (LazyRBSession) rblayer.createSession(); UniformHierarchicalConsensusSession
		 * uhcsession = (UniformHierarchicalConsensusSession) uhclayer .createSession();
		 * SampleApplSession sasession = (SampleApplSession) salayer.createSession();
		 * 
		 * // Sessions that require initial configuration sasession.init(processes);
		 * uhcsession.rbchannel(rbchannel);
		 * 
		 * // Setting sessions ChannelCursor bebcc = bebchannel.getCursor();
		 * ChannelCursor rbcc = rbchannel.getCursor(); try { bebcc.bottom();
		 * bebcc.setSession(tcpsession); bebcc.up(); bebcc.setSession(bebsession);
		 * bebcc.up(); bebcc.setSession(pfdsession); bebcc.up();
		 * bebcc.setSession(uhcsession); bebcc.up(); bebcc.setSession(sasession);
		 * 
		 * rbcc.bottom(); rbcc.setSession(tcpsession); rbcc.up();
		 * rbcc.setSession(bebsession); rbcc.up(); rbcc.setSession(pfdsession);
		 * rbcc.up(); rbcc.setSession(rbsession); rbcc.up();
		 * rbcc.setSession(uhcsession); } catch (AppiaCursorException ex) {
		 * ex.printStackTrace(); System.exit(1); }
		 * 
		 * return bebchannel;
		 */
		return null;
	}

	/**
	 * Creates a Causal Order No waiting Reliable Broadcast Channel
	 * 
	 * @param processes, set of processes belonging to the group
	 * @return the created channel
	 */
	private static Channel getCOnoWChannel(ProcessSet processes) {
		/* Create layers and put them on a array */
		/*
		 * Layer[] qos = {new TcpCompleteLayer(), new BasicBroadcastLayer(), new
		 * TcpBasedPFDLayer(), new DelayLayer(), new LazyRBLayer(), new
		 * NoWaitingCOLayer(), new SampleApplLayer()};
		 * 
		 * return getGenericChannel(processes, qos, "Casual Order no Waiting QoS",
		 * "Casual Order no Waiting Channel");
		 */
		return null;
	}

	/**
	 * Creates a Causal Order No waiting with GC Reliable Broadcast Channel
	 * 
	 * @param processes, set of processes belonging to the group
	 * @return the new channel
	 */
	private static Channel getCOnoWGCChannel(ProcessSet processes) {
		/* Create layers and put them on a array */
		/*
		 * Layer[] qos = {new TcpCompleteLayer(), new BasicBroadcastLayer(), new
		 * TcpBasedPFDLayer(), new LazyRBLayer(), new DelayLayer(), new GCPastCOLayer(),
		 * new SampleApplLayer()};
		 * 
		 * return getGenericChannel(processes, qos,
		 * "Casual Order no Waiting with GC QoS",
		 * "Casual Order no Waiting with GC Channel");
		 */
		return null;
	}

	/**
	 * Creates a Causal Order Waiting Reliable Broadcast Channel
	 * 
	 * @param processes, set of processes belonging to the group
	 * @return the new channel
	 */
	private static Channel getCOWChannel(ProcessSet processes) {
		/* Create layers and put them on a array */
		/*
		 * Layer[] qos = {new TcpCompleteLayer(), new BasicBroadcastLayer(), new
		 * TcpBasedPFDLayer(), new LazyRBLayer(), new DelayLayer(), new
		 * WaitingCOLayer(), new SampleApplLayer()};
		 * 
		 * return getGenericChannel(processes, qos, "Casual Order Waiting QoS",
		 * "Casual Order Waiting Channel");
		 */
		return null;
	}

	/**
	 * Creates a Uniform Total Order channel
	 * 
	 * @param processes the process set
	 * @return the new channel
	 */
	private static Channel getUnTOChannel(ProcessSet processes) {
		/* Create layers and put them on a array */
		/*
		 * Layer[] qos = {new TcpCompleteLayer(), new BasicBroadcastLayer(), new
		 * TcpBasedPFDLayer(), new AllAckURBLayer(), new
		 * UniformFloodingConsensusLayer(), new DelayLayer(), new ConsensusUTOLayer(),
		 * new SampleApplLayer()};
		 * 
		 * return getGenericChannel(processes, qos, "Uniform Total Order QoS",
		 * "Uniform Total Order Channel");
		 */
		return null;
	}

	/**
	 * Builds a new Appia Channel with Consensus-based Non-Blocking Atomic Commit
	 * 
	 * @param processes set of processes
	 * @return a new uninitialized Channel
	 */
	private static Channel getNBACChannel(ProcessSet processes) {
		/* Create layers and put them on a array */
		/*
		 * Layer[] qos = {new TcpCompleteLayer(), new BasicBroadcastLayer(), new
		 * TcpBasedPFDLayer(), new DelayLayer(), new UniformFloodingConsensusLayer(),
		 * new ConsensusNBACLayer(), new SampleApplLayer()};
		 * 
		 * return getGenericChannel(processes, qos, "Consensus-based NBAC QoS",
		 * "Consensus-based NBAC Channel");
		 */
		return null;
	}

	/**
	 * Builds a new Appia Channel with Uniform Flooding Consensus
	 * 
	 * @param processes set of processes
	 * @return a new uninitialized Channel
	 */
	private static Channel getCMemChannel(ProcessSet processes) {
		/* Create layers and put them on a array */
		/*
		 * Layer[] qos = {new TcpCompleteLayer(), new BasicBroadcastLayer(), new
		 * TcpBasedPFDLayer(), new DelayLayer(), new UniformFloodingConsensusLayer(),
		 * new ConsensusMembershipLayer(), new SampleApplLayer()};
		 * 
		 * return getGenericChannel(processes, qos, "Consensus-based Membership QoS",
		 * "Consensus-based Membership Channel");
		 */
		return null;
	}

	/**
	 * Builds a new Appia Channel with Uniform Flooding Consensus
	 * 
	 * @param processes set of processes
	 * @return a new uninitialized Channel
	 */
	private static Channel getTrbVSChannel(ProcessSet processes) {
		/* Create layers and put them on a array */
		/*
		 * Layer[] qos = {new TcpCompleteLayer(), new BasicBroadcastLayer(), new
		 * TcpBasedPFDLayer(), new UniformFloodingConsensusLayer(), new
		 * ConsensusTRBLayer(), new LazyRBLayer(), new DelayLayer(), new
		 * GCPastCOLayer(), new ConsensusMembershipLayer(), new TRBViewSyncLayer(), new
		 * SampleApplLayer()};
		 * 
		 * return getGenericChannel(processes, qos, "TRB-based View Synchrony QoS",
		 * "TRB-based View Synchrony Channel");
		 */
		return null;
	}

	/**
	 * Builds two Appia channels for Regular (1,N) Register - A BeB channel - A
	 * PerfectPointoToPointLinks channel that is started from the
	 * AbortableConsensusSession
	 * 
	 * @param processes set of processes
	 * @return a new uninitialized Channel
	 */
	private static Channel getR1NRChannel(ProcessSet processes) {
		/* Create layers and put them on a array */
		/*
		 * Layer[] bebqos = {new TcpCompleteLayer(), new BasicBroadcastLayer(), new
		 * TcpBasedPFDLayer(), new DelayLayer(), new ReadOneWriteAll1NRRLayer(), new
		 * SampleApplLayer()};
		 * 
		 * // Create a QoS QoS bebQoS = null; try { bebQoS = new QoS("R1NR-BeB QoS",
		 * bebqos); } catch (AppiaInvalidQoSException ex) { ex.printStackTrace();
		 * System.exit(1); } // Create a channel. Uses default event scheduler. Channel
		 * bebchannel = bebQoS.createUnboundChannel("R1NR-BeB Channel");
		 * 
		 * // Create layers and put them on a array Layer[] pp2pqos = {bebqos[0], new
		 * DelayLayer(), bebqos[bebqos.length - 2]}; // Create a QoS QoS pp2pQoS = null;
		 * try { pp2pQoS = new QoS("R1NR-PP2P QoS", pp2pqos); } catch
		 * (AppiaInvalidQoSException ex) { ex.printStackTrace(); System.exit(1); } //
		 * Create a channel. Channel pp2pchannel =
		 * pp2pQoS.createUnboundChannel("R1NR-PP2P Channel",
		 * bebchannel.getEventScheduler());
		 * 
		 * // Shared sessions and those that require initial configuration are created
		 * // explicitly. TcpCompleteSession tcpsession = (TcpCompleteSession) bebqos[0]
		 * .createSession(); ReadOneWriteAll1NRRSession r1nrsession =
		 * (ReadOneWriteAll1NRRSession) bebqos[bebqos.length - 2] .createSession();
		 * SampleApplSession sasession = (SampleApplSession) bebqos[bebqos.length - 1]
		 * .createSession();
		 * 
		 * sasession.init(processes); r1nrsession.pp2pchannel(pp2pchannel);
		 * 
		 * // Setting sessions ChannelCursor bebcc = bebchannel.getCursor();
		 * ChannelCursor pp2pcc = pp2pchannel.getCursor(); try { bebcc.top();
		 * bebcc.setSession(sasession); bebcc.down(); bebcc.setSession(r1nrsession);
		 * bebcc.bottom(); bebcc.setSession(tcpsession);
		 * 
		 * pp2pcc.top(); pp2pcc.setSession(r1nrsession); pp2pcc.down();
		 * pp2pcc.setSession(tcpsession); } catch (AppiaCursorException ex) {
		 * ex.printStackTrace(); System.exit(1); }
		 * 
		 * return bebchannel;
		 */
		return null;
	}

	/**
	 * Builds two Appia channels for Atomic (1,N) Register - A BeB channel - A
	 * PerfectPointoToPointLinks channel that is started from the
	 * AbortableConsensusSession
	 * 
	 * @param processes set of processes
	 * @return a new uninitialized Channel
	 */
	private static Channel getA1NRChannel(ProcessSet processes) {
		/* Create layers and put them on a array */
		/*
		 * Layer[] bebqos = {new TcpCompleteLayer(), new BasicBroadcastLayer(), new
		 * TcpBasedPFDLayer(), new DelayLayer(), new ReadImposeWriteAll1NARLayer(), new
		 * SampleApplLayer()};
		 * 
		 * // Create a QoS QoS bebQoS = null; try { bebQoS = new QoS("A1NR-BeB QoS",
		 * bebqos); } catch (AppiaInvalidQoSException ex) { ex.printStackTrace();
		 * System.exit(1); } // Create a channel. Uses default event scheduler. Channel
		 * bebchannel = bebQoS.createUnboundChannel("A1NR-BeB Channel");
		 * 
		 * // Create layers and put them on a array Layer[] pp2pqos = {bebqos[0], new
		 * DelayLayer(), bebqos[bebqos.length - 2]}; // Create a QoS QoS pp2pQoS = null;
		 * try { pp2pQoS = new QoS("A1NR-PP2P QoS", pp2pqos); } catch
		 * (AppiaInvalidQoSException ex) { ex.printStackTrace(); System.exit(1); } //
		 * Create a channel. Uses default event scheduler. Channel pp2pchannel =
		 * pp2pQoS.createUnboundChannel("A1NR-PP2P Channel",
		 * bebchannel.getEventScheduler());
		 * 
		 * // Shared sessions and those that require initial configuration are created
		 * // explicitly. TcpCompleteSession tcpsession = (TcpCompleteSession) bebqos[0]
		 * .createSession(); ReadImposeWriteAll1NARSession a1nrsession =
		 * (ReadImposeWriteAll1NARSession) bebqos[bebqos.length - 2] .createSession();
		 * SampleApplSession sasession = (SampleApplSession) bebqos[bebqos.length - 1]
		 * .createSession();
		 * 
		 * sasession.init(processes); a1nrsession.pp2pchannel(pp2pchannel);
		 * 
		 * // Setting sessions ChannelCursor bebcc = bebchannel.getCursor();
		 * ChannelCursor pp2pcc = pp2pchannel.getCursor(); try { bebcc.top();
		 * bebcc.setSession(sasession); bebcc.down(); bebcc.setSession(a1nrsession);
		 * bebcc.bottom(); bebcc.setSession(tcpsession);
		 * 
		 * pp2pcc.top(); pp2pcc.setSession(a1nrsession); pp2pcc.down();
		 * pp2pcc.setSession(tcpsession); } catch (AppiaCursorException ex) {
		 * ex.printStackTrace(); System.exit(1); }
		 * 
		 * return bebchannel;
		 */
		return null;
	}

	/**
	 * Builds two Appia channels for Atomic (N,N) Register - A BeB channel - A
	 * PerfectPointoToPointLinks channel that is started from the
	 * AbortableConsensusSession
	 * 
	 * @param processes set of processes
	 * @return a new uninitialized Channel
	 */
	private static Channel getANNRChannel(ProcessSet processes) {
		/*
		 * TcpCompleteLayer tcplayer = new TcpCompleteLayer(); BasicBroadcastLayer
		 * beblayer = new BasicBroadcastLayer(); TcpBasedPFDLayer pfdlayer = new
		 * TcpBasedPFDLayer(); ReadImposeWriteConsultNNARLayer annrlayer = new
		 * ReadImposeWriteConsultNNARLayer(); SampleApplLayer salayer = new
		 * SampleApplLayer();
		 * 
		 * // Create layers and put them on a array Layer[] bebqos = {tcplayer,
		 * beblayer, pfdlayer, new DelayLayer(), annrlayer, salayer}; // Create a QoS
		 * QoS bebQoS = null; try { bebQoS = new QoS("ANNR-BeB QoS", bebqos); } catch
		 * (AppiaInvalidQoSException ex) { ex.printStackTrace(); System.exit(1); } //
		 * Create a channel. Uses default event scheduler. Channel bebchannel =
		 * bebQoS.createUnboundChannel("ANNR-BeB Channel");
		 * 
		 * // Create layers and put them on a array Layer[] pp2pqos = {tcplayer, new
		 * DelayLayer(), annrlayer}; // Create a QoS QoS pp2pQoS = null; try { pp2pQoS =
		 * new QoS("ANNR-PP2P QoS", pp2pqos); } catch (AppiaInvalidQoSException ex) {
		 * ex.printStackTrace(); System.exit(1); } // Create a channel. Uses default
		 * event scheduler. Channel pp2pchannel =
		 * pp2pQoS.createUnboundChannel("ANNR-PP2P Channel");
		 * 
		 * // Shared sessions and those that require initial configuration are created
		 * // explicitly. TcpCompleteSession tcpsession = (TcpCompleteSession) tcplayer
		 * .createSession(); ReadImposeWriteConsultNNARSession annrsession =
		 * (ReadImposeWriteConsultNNARSession) annrlayer .createSession();
		 * SampleApplSession sasession = (SampleApplSession) salayer.createSession();
		 * 
		 * sasession.init(processes); annrsession.pp2pchannel(pp2pchannel);
		 * 
		 * // Setting sessions ChannelCursor bebcc = bebchannel.getCursor();
		 * ChannelCursor pp2pcc = pp2pchannel.getCursor(); try { bebcc.bottom();
		 * bebcc.setSession(tcpsession); bebcc.up(); bebcc.up(); bebcc.up();
		 * bebcc.setSession(annrsession); bebcc.up(); bebcc.setSession(sasession);
		 * 
		 * pp2pcc.bottom(); pp2pcc.setSession(tcpsession); pp2pcc.up();
		 * pp2pcc.setSession(annrsession); } catch (AppiaCursorException ex) {
		 * ex.printStackTrace(); System.exit(1); }
		 * 
		 * return bebchannel;
		 */
		return null;
	}

	private static final int NUM_ARGS = 8;

	public static void main(String[] args) {
		

		if (args.length < (NUM_ARGS - 2)) {
			invalidArgs("Wrong number of arguments: " + args.length);
		}

		/* Parse arguments */
		int arg = 0, self = -1;
		String filename = null, qos = null;
		try {
			while (arg < args.length) {
				if (args[arg].equals("-f")) {
					arg++;
					filename = args[arg];
//					System.out.println("Reading from file: " + filename);
				} else if (args[arg].equals("-n")) {
					arg++;
					try {
						self = Integer.parseInt(args[arg]);
//						System.out.println("Process number: " + self);
					} catch (NumberFormatException e) {
						invalidArgs(e.getMessage());
					}
				} else if (args[arg].equals("-qos")) {
					arg++;
					qos = args[arg];
					if (qos.equals("pb")) {
						qos = qos + " " + args[++arg] + " " + args[++arg];
					}
//					System.out.println("Starting with QoS: " + qos);
				} else
					invalidArgs("Unknown argument: " + args[arg]);
				arg++;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			invalidArgs(e.getMessage());
		}

		/*
		 * gets a new uninitialized Channel with the specified QoS and the Appl session
		 * created. Remaining sessions are created by default. Just tell the channel to
		 * start.
		 */
		Channel channel = getChannel(buildProcessSet(filename, self), qos);
		try {
			channel.start();
		} catch (AppiaDuplicatedSessionsException ex) {
			System.err.println("Sessions binding strangely resulted in " + "one single sessions occurring more than "
					+ "once in a channel");
			System.exit(1);
		}

		/* All set. Appia main class will handle the rest */
		System.out.println("Starting Appia...");
		Appia.run();
	}

	/**
	 * Prints a error message and exit.
	 * 
	 * @param reason the reason of the failure
	 */
	private static void invalidArgs(String reason) {
		System.out.println("Invalid args: " + reason + "\nUsage SampleAppl -f filemane -n proc_number -qos QoS_type."
				+ "\n QoS can be one of the following:" + "\n\t beb - Best Effort Broadcast"
				+ "\n\t rb - Lazy Reliable Broadcast" + "\n\t urb - All-Ack Uniform Reliable Broadcast"
				+ "\n\t iurb - Majority-Ack Uniform Reliable Broadcast"
				+ "\n\t pb <f> <r> - Probabilistic Broadcast with a fanout f and a number of rounds r"
				+ "\n\t fc - Flooding Consensus" + "\n\t hc - Hierarchical Consensus"
				+ "\n\t ufc - Uniform Flooding Consensus" + "\n\t uhc - Uniform Hierarchical Consensus" +
				// "\n\t clc - Careful Leader Consensus"+
				"\n\t conow - No-Waiting Casual Order"
				+ "\n\t conowgc - No-Waiting Casual Order with Garbage Collection" + "\n\t cow - Waiting Casual Order"
				+ "\n\t uto - Consensus-Based Uniform Total Order"
				+ "\n\t r1nr - Read-One-Write-All Regular (1,N) Register"
				+ "\n\t a1nr - Read-Impose-Write-All Atomic (1,N) Register"
				+ "\n\t annr - Read-Impose Write-Consult Atomic (N,N) Register"
				+ "\n\t nbac - Consensus-based Non-Blocking Atomic Commit" + "\n\t cmem - Consensus-based Membership"
				+ "\n\t trbvs - TRB-based View Synchrony" + "\n\t fd - simply a failure detector");
		System.exit(1);
	}
}
