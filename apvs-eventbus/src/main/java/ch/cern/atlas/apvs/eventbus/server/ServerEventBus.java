package ch.cern.atlas.apvs.eventbus.server;

import ch.cern.atlas.apvs.eventbus.shared.RemoteEvent;
import ch.cern.atlas.apvs.eventbus.shared.SimpleRemoteEventBus;

public class ServerEventBus extends SimpleRemoteEventBus {
	
	private static ServerEventBus instance;

	private EventBusServiceHandler eventBusServiceHandler;

	public static ServerEventBus getInstance() {
		if (instance == null) {
			instance = new ServerEventBus();
		}
		return instance;
	}
	
	private ServerEventBus() {
	}
	
	public void setEventBusServiceHandler(EventBusServiceHandler eventBusServiceHandler) {
		this.eventBusServiceHandler = eventBusServiceHandler;		
	}

	@Override
	public void fireEvent(RemoteEvent<?> event) {
		doFire(event);
	}

	@Override
	public void fireEventFromSource(RemoteEvent<?> event, int uuid) {
		doFire(event);
	}

	private void doFire(RemoteEvent<?> event) {
		// send out locally
		super.fireEvent(event);

		// send to remote
		if (eventBusServiceHandler != null) {
			eventBusServiceHandler.forwardEvent(event);
		}
	}

	public void forwardEvent(RemoteEvent<?> event) {
		super.fireEvent(event);
	}

}