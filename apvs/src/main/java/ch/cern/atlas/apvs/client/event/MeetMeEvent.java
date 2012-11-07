package ch.cern.atlas.apvs.client.event;

import ch.cern.atlas.apvs.client.domain.Conference;
import ch.cern.atlas.apvs.client.settings.ConferenceRooms;
import ch.cern.atlas.apvs.eventbus.shared.RemoteEvent;
import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
import ch.cern.atlas.apvs.eventbus.shared.RequestRemoteEvent;

import com.google.web.bindery.event.shared.HandlerRegistration;

public class MeetMeEvent extends RemoteEvent<MeetMeEvent.Handler>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public interface Handler{
		
		void onMeetMeEvent(MeetMeEvent event);
	}
	
	private static final Type<MeetMeEvent.Handler> TYPE = new Type<MeetMeEvent.Handler>();
	
	public static HandlerRegistration register(RemoteEventBus eventBus, MeetMeEvent.Handler handler){
		return eventBus.addHandler(TYPE, handler);
	}
	
	public static HandlerRegistration subscribe(RemoteEventBus eventBus, MeetMeEvent.Handler handler){
		HandlerRegistration registration = register(eventBus, handler);
		eventBus.fireEvent(new RequestRemoteEvent(MeetMeEvent.class));
		
		return registration;
	}

	private ConferenceRooms conferenceRooms;
	
	public MeetMeEvent(){
	}
	
	public MeetMeEvent(ConferenceRooms conferenceRooms){
		this.conferenceRooms = conferenceRooms;
	}
	
	public ConferenceRooms getConferenceRooms(){
		return conferenceRooms;
	}
	
	public Conference getConfereceMembers(String room) {
		return conferenceRooms.get(room);
	}
	
	@Override
	public Type<MeetMeEvent.Handler> getAssociatedType() {
		return TYPE;
	}
	
	@Override
	protected void dispatch(Handler handler) {
		handler.onMeetMeEvent(this);
	}
}









