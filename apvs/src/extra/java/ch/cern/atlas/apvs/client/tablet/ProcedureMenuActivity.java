package ch.cern.atlas.apvs.client.tablet;

import java.util.ArrayList;
import java.util.List;

import ch.cern.atlas.apvs.client.ClientFactory;
import ch.cern.atlas.apvs.client.tablet.ProcedureEntrySelectedEvent.ProcedureEntry;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.googlecode.mgwt.dom.client.event.tap.TapEvent;
import com.googlecode.mgwt.dom.client.event.tap.TapHandler;
import com.googlecode.mgwt.mvp.client.MGWTAbstractActivity;
import com.googlecode.mgwt.ui.client.widget.celllist.CellSelectedEvent;
import com.googlecode.mgwt.ui.client.widget.celllist.CellSelectedHandler;

public class ProcedureMenuActivity extends MGWTAbstractActivity {

	private final ClientFactory clientFactory;

	private int oldIndex;

	private List<ProcedureMenuItem> items;

	public ProcedureMenuActivity(ClientFactory clientFactory) {
		this.clientFactory = clientFactory;

	}

	@Override
	public void start(AcceptsOneWidget panel, final EventBus eventBus) {
		final ProcedureMenuUI view = clientFactory.getProcedureMenuView();

		view.setBackButtonText("Home");
		view.setTitle("Procedures");

		addHandlerRegistration(view.getBackButton().addTapHandler(
				new TapHandler() {

					@Override
					public void onTap(TapEvent event) {
						ActionEvent.fire(eventBus, ActionNames.BACK);

					}
				}));
		items = createItems();
		view.renderItems(items);

		addHandlerRegistration(view.getList().addCellSelectedHandler(
				new CellSelectedHandler() {

					@Override
					public void onCellSelected(CellSelectedEvent event) {
						int index = event.getIndex();

						view.setSelectedIndex(oldIndex, false);
						view.setSelectedIndex(index, true);
						oldIndex = index;

						ProcedureEntrySelectedEvent.fire(eventBus, items.get(index)
								.getEntry());

					}
				}));

		panel.setWidget(view);
	}

	private List<ProcedureMenuItem> createItems() {
		ArrayList<ProcedureMenuItem> list = new ArrayList<ProcedureMenuItem>();
		list.add(new ProcedureMenuItem("Tile Drawer Extraction", ProcedureEntry.TILE_DRAWER_EXTRACTION));
//		list.add(new ProcedureMenuItem("Mural Painting", ProcedureEntry.MURAL_PAINTING));
		return list;
	}

}