/*
 *
 * Written by Aaron Wilkin 8-5-19
 * 
 */

package irdp.protocols.tutorialDA.diningPhilosophers;

import irdp.protocols.tutorialDA.events.SampleSendableEvent;
import net.sf.appia.core.AppiaEventException;
import net.sf.appia.core.Direction;
import net.sf.appia.core.message.Message;

import java.util.StringTokenizer;


/**
 * Class that reads from the keyboard and generates events to the appia Channel.
 * 
 * @author nuno
 */
public class DiningPhilosophersApplReader extends Thread {

  private DiningPhilosophersApplSession parentSession;
  private java.io.BufferedReader keyb;
  private String local = null;

  public DiningPhilosophersApplReader(DiningPhilosophersApplSession parentSession) {
    super();
    this.parentSession = parentSession;
    keyb = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
  }

  public void run() {
    while (true) {
      try {
        try {
          Thread.sleep(500);
        } catch (InterruptedException e) {
        }
        System.out.print("> ");
        local = keyb.readLine();
        if (local.equals(""))
          continue;
        StringTokenizer st = new StringTokenizer(local);
        /*
         * creates the event, push the message and sends this to the appia
         * channel.
         */
        SampleSendableEvent asyn = new SampleSendableEvent();
        Message message = asyn.getMessage();
        asyn.setCommand(st.nextToken());
        String msg = "";
        while (st.hasMoreTokens())
          msg += (st.nextToken() + " ");
        message.pushString(msg);
        asyn.asyncGo(parentSession.channel, Direction.DOWN);
      } catch (java.io.IOException e) {
        e.printStackTrace();
      } catch (AppiaEventException e) {
        e.printStackTrace();
      }
    }
  }
}
