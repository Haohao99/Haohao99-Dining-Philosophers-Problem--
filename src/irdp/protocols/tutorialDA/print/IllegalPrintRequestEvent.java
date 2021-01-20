package irdp.protocols.tutorialDA.print;
import net.sf.appia.core.Event;

public class IllegalPrintRequestEvent extends Event {

	int rqid;
	Status status;

	void setId(int rid) {
		rqid = rid;
	}

	void setStatus(Status s) {
		status = s;
	}

	int getId() {
		return rqid;
	}

	Status getStatus() {
		return status;
	}
}
