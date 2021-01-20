/*
 *
 * Dining Philosophers implementation written by Aaron Wilkin 8-5-19
 * 
 */

package irdp.demo.tutorialDA;


import irdp.protocols.tutorialDA.diningPhilosophers.DiningPhilosophersApplLayer;
import irdp.protocols.tutorialDA.diningPhilosophers.DiningPhilosophersApplSession;
import irdp.protocols.tutorialDA.diningPhilosophers.DiningPhilosophersLayer;
import irdp.protocols.tutorialDA.diningPhilosophers.DiningPhilosophersSession;
import irdp.protocols.tutorialDA.tcpBasedPFD.TcpBasedPFDLayer;
import irdp.protocols.tutorialDA.tcpBasedPFD.TcpBasedPFDSession;
import irdp.protocols.tutorialDA.tcpBasedPerfectP2P.TcpBasedPerfectP2PLayer;
import irdp.protocols.tutorialDA.tcpBasedPerfectP2P.TcpBasedPerfectP2PSession;
import irdp.protocols.tutorialDA.utils.ProcessSet;
import irdp.protocols.tutorialDA.utils.SampleProcess;

import net.sf.appia.core.*;
import net.sf.appia.protocols.tcpcomplete.TcpCompleteLayer;
import net.sf.appia.protocols.tcpcomplete.TcpCompleteSession;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.StringTokenizer;

/**
 * This class is the MAIN class to run the Dining Philosophers problem code.
 * 
 * @author nuno
 */
public class DiningPhilosophers {

  /**
   * Builds the Process set, using the information in the specified file.
   * 
   * @param filename
   *          the location of the file
   * @param selfProc
   *          the number of the self process
   * @return a new ProcessSet
   */
  private static ProcessSet buildProcessSet(String filename, int selfProc) {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new InputStreamReader(new FileInputStream(
          filename)));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      System.exit(0);
    }
    String line;
    StringTokenizer st;
    boolean hasMoreLines = true;
    ProcessSet set = new ProcessSet();
    // reads lines of type: <process number> <IP address> <port>
    while(hasMoreLines) {
      try {
        line = reader.readLine();
        if (line == null)
          break;
        st = new StringTokenizer(line);
        if (st.countTokens() != 3) {
          System.err.println("Wrong line in file: "+st.countTokens());
          continue;
        }
        int procNumber = Integer.parseInt(st.nextToken());
        InetAddress addr = InetAddress.getByName(st.nextToken());
        int portNumber = Integer.parseInt(st.nextToken());
        boolean self = (procNumber == selfProc);
        SampleProcess process = new SampleProcess(new InetSocketAddress(addr,
            portNumber), procNumber, self);
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
   * @param set
   *          the ProcessSet
   * @param qos
   *          the specified QoS
   * @return a new uninitialized channel
   */
  private static Channel getChannel(ProcessSet processes) {
    /* Create layers and put them on a array */
    Layer[] qos = {new TcpCompleteLayer(), new DiningPhilosophersLayer(),  
    				new DiningPhilosophersApplLayer()};
	
	/* Create a QoS */
    QoS myQoS = null;
    try {
      myQoS = new QoS("Dining Philosophers QoS", qos);
    } catch (AppiaInvalidQoSException ex) {
      System.err.println("Invalid QoS");
      System.err.println(ex.getMessage());
      System.exit(1);
    }
    /* Create a channel. Uses default event scheduler. */
    Channel channel = myQoS
        .createUnboundChannel("Dining Philosophers channel");
    /*
     * Application Session requires special arguments: filename and . A session
     * is created and binded to the stack. Remaining ones are created by default
     */
    DiningPhilosophersApplSession dps = (DiningPhilosophersApplSession) qos[qos.length - 1]
        .createSession();
    dps.init(processes);
    ChannelCursor cc = channel.getCursor();
    /*
     * Application is the last session of the array. Positioning in it is simple
     */
    try {
      cc.top();
      cc.setSession(dps);
    } catch (AppiaCursorException ex) {
      System.err.println("Unexpected exception in main. Type code:" + ex.type);
      System.exit(1);
    }
    return channel;
  }

  private static final int NUM_ARGS = 6;

  public static void main(String[] args) {
    if (args.length < (NUM_ARGS - 2)) {
      invalidArgs("Wrong number of arguments: "+args.length);
    }

    /* Parse arguments */
    int arg = 0, self = -1;
    String filename = null;
    try {
      while (arg < args.length) {
        if (args[arg].equals("-f")) {
          arg++;
          filename = args[arg];
          System.out.println("Reading from file: " + filename);
        } else if (args[arg].equals("-n")) {
          arg++;
          try {
            self = Integer.parseInt(args[arg]);
            System.out.println("Process number: " + self);
          } catch (NumberFormatException e) {
            invalidArgs(e.getMessage());
          }
        } else
          invalidArgs("Unknown argument: "+args[arg]);
        arg++;
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      e.printStackTrace();
      invalidArgs(e.getMessage());
    }

    /*
     * gets a new uninitialized Channel with the specified Appl
     * session created. Remaining sessions are created by default. Just tell the
     * channel to start.
     */
    Channel channel = getChannel(buildProcessSet(filename, self));
    try {
      channel.start();
    } catch (AppiaDuplicatedSessionsException ex) {
      System.err.println("Sessions binding strangely resulted in "
          + "one single sessions occurring more than " + "once in a channel");
      System.exit(1);
    }

    /* All set. Appia main class will handle the rest */
    System.out.println("Starting Appia...");
    Appia.run();
  }

  /**
   * Prints a error message and exit.
   * @param reason the reason of the failure
   */
  private static void invalidArgs(String reason) {
    System.out
        .println("Invalid args: "+reason+"\nUsage DiningPhilosophers -f filemane -n proc_number.");
    System.exit(1);
  }
}
