package ch.cern.atlas.apvs.client.ui;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.cern.atlas.apvs.client.ClientFactory;
import ch.cern.atlas.apvs.client.service.InterventionServiceAsync;
import ch.cern.atlas.apvs.client.service.SortOrder;
import ch.cern.atlas.apvs.client.widget.ClickableHtmlColumn;
import ch.cern.atlas.apvs.client.widget.ClickableTextColumn;
import ch.cern.atlas.apvs.client.widget.DataStoreName;
import ch.cern.atlas.apvs.client.widget.EditableCell;
import ch.cern.atlas.apvs.client.widget.GenericColumn;
import ch.cern.atlas.apvs.client.widget.ListBoxField;
import ch.cern.atlas.apvs.client.widget.TextBoxField;
import ch.cern.atlas.apvs.ptu.shared.PtuClientConstants;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Fieldset;
import com.github.gwtbootstrap.client.ui.Form;
import com.github.gwtbootstrap.client.ui.Modal;
import com.github.gwtbootstrap.client.ui.ModalFooter;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.FormType;
import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RangeChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

public class InterventionView extends SimplePanel implements Module {

	private Logger log = LoggerFactory.getLogger(getClass().getName());

	private DataGrid<Intervention> table = new DataGrid<Intervention>();

	private boolean selectable = false;
	private boolean sortable = true;

	private final String END_INTERVENTION = "End Intervention";
	
	private InterventionServiceAsync interventionService;

	public InterventionView() {
		interventionService = InterventionServiceAsync.Util.getInstance();
	}

	@Override
	public boolean configure(Element element, ClientFactory clientFactory,
			Arguments args) {

		String height = args.getArg(0);

		table.setSize("100%", height);
		table.setEmptyTableWidget(new Label("No Interventions"));
		table.setVisibleRange(0, 20);

		add(table);

		AsyncDataProvider<Intervention> dataProvider = new AsyncDataProvider<Intervention>() {
			
			@SuppressWarnings("unchecked")
			@Override
			protected void onRangeChanged(HasData<Intervention> display) {
				log.info("ON RANGE CHANGED "+display.getVisibleRange());

				interventionService.getRowCount(
						new AsyncCallback<Integer>() {

							@Override
							public void onSuccess(Integer result) {
								table.setRowCount(result, true);
							}

							@Override
							public void onFailure(Throwable caught) {
								table.setRowCount(0);
							}
						});

				final Range range = display.getVisibleRange();
				System.err.println(range);

				final ColumnSortList sortList = table.getColumnSortList();
				SortOrder[] order = new SortOrder[sortList.size()];
				for (int i = 0; i < sortList.size(); i++) {
					ColumnSortInfo info = sortList.get(i);
					// FIXME #88 remove cast
					order[i] = new SortOrder(
							((DataStoreName) info.getColumn())
									.getDataStoreName(),
							info.isAscending());
				}

				if (order.length == 0) {
					order = new SortOrder[1];
					order[0] = new SortOrder("tbl_inspections.endtime", false);
				}

				interventionService.getTableData(range,
						order, new AsyncCallback<List<Intervention>>() {

							@Override
							public void onSuccess(List<Intervention> result) {
								System.err.println("RPC DB SUCCESS");
								table.setRowData(range.getStart(), result);
							}

							@Override
							public void onFailure(Throwable caught) {
								System.err.println("RPC DB FAILED");
								table.setRowCount(0);
							}
						});
			}
		};

		// Table
		dataProvider.addDataDisplay(table);

		AsyncHandler columnSortHandler = new AsyncHandler(table);
		table.addColumnSortHandler(columnSortHandler);

		// startTime
		ClickableTextColumn<Intervention> startTime = new ClickableTextColumn<Intervention>() {
			@Override
			public String getValue(Intervention object) {
				return PtuClientConstants.dateFormat.format(object
						.getStartTime());
			}

			@Override
			public String getDataStoreName() {
				return "tbl_inspections.starttime";
			}
		};
		startTime.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		startTime.setSortable(sortable);
		if (selectable) {
			startTime.setFieldUpdater(new FieldUpdater<Intervention, String>() {

				@Override
				public void update(int index, Intervention object, String value) {
					selectIntervention(object);
				}
			});
		}
		Header<String> interventionFooter = new Header<String>(new ButtonCell()) {
			@Override
			public String getValue() {
				return "Start a new Intervention";
			}
		};
		interventionFooter.setUpdater(new ValueUpdater<String>() {
			
			@Override
			public void update(String value) {
				
				Fieldset fieldset = new Fieldset();
				
				final ListBoxField userField = new ListBoxField("User");
				fieldset.add(userField);
				
				interventionService.getUsers(new AsyncCallback<List<User>>() {
					
					@Override
					public void onSuccess(List<User> result) {
						for (Iterator<User> i = result.iterator(); i.hasNext(); ) {
							User user = i.next();
							userField.addItem(user.getDisplayName(), user.getId());
						}
					}
					
					@Override
					public void onFailure(Throwable caught) {
						log.warn("Caught : "+caught);
					}
				});
				
				final ListBoxField ptu = new ListBoxField("PTU");
				fieldset.add(ptu);
				
				interventionService.getDevices(new AsyncCallback<List<Device>>() {
					
					@Override
					public void onSuccess(List<Device> result) {
						for (Iterator<Device> i = result.iterator(); i.hasNext(); ) {
							Device device = i.next();
							ptu.addItem(device.getName(), device.getId());
						}
					}
					
					@Override
					public void onFailure(Throwable caught) {
						log.warn("Caught : "+caught);
					}
				});
				
				final TextBoxField description = new TextBoxField("Description");
				fieldset.add(description);
				
				Form form = new Form();
				form.setType(FormType.HORIZONTAL);
				form.add(fieldset);
				
				final Modal m = new Modal();

				Button cancel = new Button("Cancel");
				cancel.addClickHandler(new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						m.hide();
					}
				});
				
				Button ok = new Button("Ok");
				ok.setType(ButtonType.PRIMARY);
				ok.addClickHandler(new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						m.hide();
						
						interventionService.addIntervention(userField.getId(), ptu.getId(), new Date(), description.getValue(), new AsyncCallback<Void>() {
							
							@Override
							public void onSuccess(Void result) {
								InterventionView.this.update();
							}
							
							@Override
							public void onFailure(Throwable caught) {
								log.warn("Failed");
							}
						});
					}
				});
								
				m.setTitle("Add a new Intervention");
				m.add(form);
				m.add(new ModalFooter(cancel, ok));
				m.show();			
			}
		});
		table.addColumn(startTime, new TextHeader("Start Time"), interventionFooter);

		// endTime
		EditableCell cell = new EditableCell() {
			@Override
			protected Class<? extends Cell<? extends Object>> getCellClass(
					Context context, Object value) {
				return value == END_INTERVENTION ? ButtonCell.class
						: TextCell.class;
			}
		};
		Column<Intervention, Object> endTime = new GenericColumn<Intervention>(
				cell) {
			@Override
			public String getValue(Intervention object) {
				return object.getEndTime() != null ? PtuClientConstants.dateFormat
						.format(object.getEndTime()) : END_INTERVENTION;
			}

			@Override
			public String getDataStoreName() {
				return "tbl_inspections.starttime";
			}
		};
		endTime.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		endTime.setSortable(sortable);
		endTime.setFieldUpdater(new FieldUpdater<Intervention, Object>() {

			@Override
			public void update(int index, Intervention object, Object value) {
				
				if (Window.confirm("Are you sure")) {
					interventionService.endIntervention(object.getId(), new Date(), new AsyncCallback<Void>() {

						@Override
						public void onSuccess(Void result) {
							InterventionView.this.update();
						}
						
						@Override
						public void onFailure(Throwable caught) {
							log.warn("Failed "+caught);
						}
						
					});
				}
			}
		});
		table.addColumn(endTime, "End Time");
		// twice for descending
		table.getColumnSortList().push(endTime);
		table.getColumnSortList().push(endTime);
		
		// Name
		ClickableHtmlColumn<Intervention> name = new ClickableHtmlColumn<Intervention>() {
			@Override
			public String getValue(Intervention object) {
				return object.getName();
			}

			@Override
			public String getDataStoreName() {
				return "tbl_users.lname";
			}
		};
		name.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		name.setSortable(sortable);
		if (selectable) {
			name.setFieldUpdater(new FieldUpdater<Intervention, String>() {

				@Override
				public void update(int index, Intervention object, String value) {
					selectIntervention(object);
				}
			});
		}
		Header<String> nameFooter = new Header<String>(new ButtonCell()) {
			@Override
			public String getValue() {
				return "Add a new User";
			}
		};
		nameFooter.setUpdater(new ValueUpdater<String>() {
			
			@Override
			public void update(String value) {
				
				Fieldset fieldset = new Fieldset();
				
				final TextBoxField fname = new TextBoxField("First Name");
				fieldset.add(fname);
				
				final TextBoxField lname = new TextBoxField("Last Name");
				fieldset.add(lname);
				
				final TextBoxField cernId = new TextBoxField("CERN ID");
				fieldset.add(cernId);
				
				Form form = new Form();
				form.setType(FormType.HORIZONTAL);
				form.add(fieldset);
				
				final Modal m = new Modal();

				Button cancel = new Button("Cancel");
				cancel.addClickHandler(new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						m.hide();
					}
				});
				
				Button ok = new Button("Ok");
				ok.setType(ButtonType.PRIMARY);
				ok.addClickHandler(new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						m.hide();
						
						interventionService.addUser(new User(0, fname.getValue(), lname.getValue(), cernId.getValue()), new AsyncCallback<Void>() {
							
							@Override
							public void onSuccess(Void result) {
								InterventionView.this.update();
							}
							
							@Override
							public void onFailure(Throwable caught) {
								log.warn("Failed "+caught);
							}
						});
					}
				});
								
				m.setTitle("Add a new User");
				m.add(form);
				m.add(new ModalFooter(cancel, ok));
				m.show();			
			}
		});
		table.addColumn(name, new TextHeader("Name"), nameFooter);

		// PtuID
		ClickableTextColumn<Intervention> ptu = new ClickableTextColumn<Intervention>() {
			@Override
			public String getValue(Intervention object) {
				return object.getPtuId();
			}

			@Override
			public String getDataStoreName() {
				return "tbl_devices.name";
			}
		};
		ptu.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		ptu.setSortable(sortable);
		if (selectable) {
			ptu.setFieldUpdater(new FieldUpdater<Intervention, String>() {

				@Override
				public void update(int index, Intervention object, String value) {
					selectIntervention(object);
				}
			});
		}
		Header<String> deviceFooter = new Header<String>(new ButtonCell()) {
			@Override
			public String getValue() {
				return "Add a new PTU";
			}
		};
		deviceFooter.setUpdater(new ValueUpdater<String>() {
			
			@Override
			public void update(String value) {
				Fieldset fieldset = new Fieldset();
				
				final TextBoxField ptuId = new TextBoxField("PTU ID");
				fieldset.add(ptuId);
				
				final TextBoxField ip = new TextBoxField("IP");
				fieldset.add(ip);
				
				final TextBoxField description = new TextBoxField("Description");
				fieldset.add(description);
				
				Form form = new Form();
				form.setType(FormType.HORIZONTAL);
				form.add(fieldset);
				
				final Modal m = new Modal();

				Button cancel = new Button("Cancel");
				cancel.addClickHandler(new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						m.hide();
					}
				});
				
				Button ok = new Button("Ok");
				ok.setType(ButtonType.PRIMARY);
				ok.addClickHandler(new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						m.hide();
						
						interventionService.addDevice(new Device(0, ptuId.getValue(), ip.getValue(), description.getValue()), new AsyncCallback<Void>() {
							
							@Override
							public void onSuccess(Void result) {
								InterventionView.this.update();
							}
							
							@Override
							public void onFailure(Throwable caught) {
								log.warn("Failed "+caught);
							}
						});
					}
				});
								
				m.setTitle("Add a new PTU");
				m.add(form);
				m.add(new ModalFooter(cancel, ok));
				m.show();				
			}
		});
		table.addColumn(ptu, new TextHeader("PTU ID"), deviceFooter);

		// Description
		ClickableHtmlColumn<Intervention> description = new ClickableHtmlColumn<Intervention>() {
			@Override
			public String getValue(Intervention object) {
				return object.getDescription();
			}

			@Override
			public String getDataStoreName() {
				return "tbl_inspections.dscr";
			}
		};
		description.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		description.setSortable(true);
		if (selectable) {
			description
					.setFieldUpdater(new FieldUpdater<Intervention, String>() {

						@Override
						public void update(int index, Intervention object,
								String value) {
							selectIntervention(object);
						}
					});
		}
		table.addColumn(description, "Description");

		// Selection
		if (selectable) {
			final SingleSelectionModel<Intervention> selectionModel = new SingleSelectionModel<Intervention>();
			table.setSelectionModel(selectionModel);
			selectionModel
					.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

						@Override
						public void onSelectionChange(SelectionChangeEvent event) {
							Intervention m = selectionModel.getSelectedObject();
							log.info(m + " " + event.getSource());
						}
					});
		}

		return true;
	}

	private void selectIntervention(Intervention intervention) {
	}

	private void update() {
		// FIXME #176 seems not to redo sql
		table.redraw();
		log.info("REFRESH");
		
		RangeChangeEvent.fire(table, table.getVisibleRange());
	}	
}