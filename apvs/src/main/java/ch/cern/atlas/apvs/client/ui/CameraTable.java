package ch.cern.atlas.apvs.client.ui;

import java.util.List;

import org.moxieapps.gwt.highcharts.client.Chart;

import ch.cern.atlas.apvs.client.ClientFactory;
import ch.cern.atlas.apvs.client.domain.HistoryMap;
import ch.cern.atlas.apvs.client.domain.InterventionMap;
import ch.cern.atlas.apvs.client.event.HistoryMapChangedEvent;
import ch.cern.atlas.apvs.client.event.InterventionMapChangedRemoteEvent;
import ch.cern.atlas.apvs.client.event.PtuSettingsChangedRemoteEvent;
import ch.cern.atlas.apvs.client.settings.PtuSettings;
import ch.cern.atlas.apvs.client.widget.UpdateScheduler;
import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class CameraTable extends SimplePanel implements Module {

	private FlexTable table = new FlexTable();
	private PtuSettings settings;
	private InterventionMap interventions;
	private HistoryMap historyMap;
	private List<String> ptuIds;
	
	private UpdateScheduler scheduler = new UpdateScheduler(this);
	
	private ClientFactory factory;

	@Override
	public boolean configure(Element element, ClientFactory clientFactory,
			Arguments args) {
		
		this.factory = clientFactory;
		
		table.setWidth("100%");
		add(table);
				
		RemoteEventBus eventBus = clientFactory.getRemoteEventBus();
		
		InterventionMapChangedRemoteEvent.subscribe(eventBus,
				new InterventionMapChangedRemoteEvent.Handler() {

					@Override
					public void onInterventionMapChanged(
							InterventionMapChangedRemoteEvent event) {
						interventions = event.getInterventionMap();

						ptuIds = interventions.getPtuIds();

						configChanged();
						scheduler.update();
					}

				});

		PtuSettingsChangedRemoteEvent.subscribe(eventBus,
				new PtuSettingsChangedRemoteEvent.Handler() {

					@Override
					public void onPtuSettingsChanged(
							PtuSettingsChangedRemoteEvent event) {
						settings = event.getPtuSettings();
						configChanged();
						scheduler.update();
					}
				});
		
		HistoryMapChangedEvent.subscribe(clientFactory,
				new HistoryMapChangedEvent.Handler() {

					@Override
					public void onHistoryMapChanged(HistoryMapChangedEvent event) {
						historyMap = event.getHistoryMap();
						configChanged();
						scheduler.update();
					}
				});

		
		return true;
	}
	
	@Override
	public boolean update() {
		return false;
	}

	private void configChanged() {
		table.clear();
		
		if ((ptuIds == null) || (settings == null) || (historyMap == null)) {
			return;
		}
		
		int row = 0;
		int column = 0;
		int labelColumn = 0;
		for(String ptuId: ptuIds) {

			Label label = new Label(ptuId);
			label.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
			table.setWidget(row, labelColumn, label);
			// 2
			table.getFlexCellFormatter().setColSpan(row, labelColumn, 1);
			labelColumn++;

			Widget helmet = new ImageView(settings.getCameraUrl(ptuId, CameraView.HELMET));
			table.setWidget(row+1, column, helmet);
			// 25%
			table.getCellFormatter().setWidth(row+1, column, "50%");
			
//			SpecificTimeView timeView = new SpecificTimeView();
//			Chart chart = timeView.createSingleChart(factory, "DoseRate", ptuId, historyMap, interventions, false);
//			Label chart = new Label("TEST "+(row+2)+" "+column+" "+ptuId);
//			table.setWidget(row+2, column, chart);
			// 25%
//			table.getCellFormatter().setWidth(row+2, column, "50%");

			column++;

//			Widget hand = new ImageView(settings.getCameraUrl(ptuId, CameraView.HAND));
//			table.setWidget(row+1, column, hand);
//			table.getCellFormatter().setWidth(row+1, column, "25%");
//			column++;
			
			// 3
			if (column >= 2) {
				column = 0;
				labelColumn = 0;
				row += 2;
			}
		}
	}
}
