package ch.cern.atlas.apvs.ptu.server;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import ch.cern.atlas.apvs.domain.Measurement;
import ch.cern.atlas.apvs.domain.Ptu;

import com.cedarsoftware.util.io.JsonReader;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;

public class PtuReader implements Runnable,
		HasValueChangeHandlers<Measurement<?>> {

	private Socket socket;
	private HandlerManager handlerManager = new HandlerManager(this);
	private Map<Integer, Ptu> ptus = new HashMap<Integer, Ptu>();
	
	public PtuReader(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {

		try {
			JsonReader reader = new PtuJsonReader(socket.getInputStream());
			while (true) {
				Object object = reader.readObject();
				if (object instanceof Measurement<?>) {
					Measurement<?> measurement = (Measurement<?>) reader
							.readObject();
					
					int ptuId = measurement.getPtuId();
					Ptu ptu = ptus.get(ptuId);
					if (ptu == null) {
						ptu = new Ptu(ptuId);
						ptus.put(ptuId, ptu);
					}
					ptu.add((Measurement<Double>)measurement);
					
					ValueChangeEvent.fire(this, measurement);
				}
			}
		} catch (IOException e) {
			System.err.println(getClass() + " " + e);
		} finally {
			try {
				close();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<Measurement<?>> handler) {
		return handlerManager.addHandler(ValueChangeEvent.getType(), handler);
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		handlerManager.fireEvent(event);
	}

	public void close() throws IOException {
		socket.close();
	}

	public Ptu getPtu(int ptuId) {
		return ptus.get(ptuId);
	}
}
