/*
 * This file is part of ACE View.
 * Copyright 2008-2009, Attempto Group, University of Zurich (see http://attempto.ifi.uzh.ch).
 *
 * ACE View is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * ACE View is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with ACE View.
 * If not, see http://www.gnu.org/licenses/.
 */

package mt.uol.smi.attempto.aceview.planner.model;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import ch.uzh.ifi.attempto.aceview.ACESnippet;
import ch.uzh.ifi.attempto.aceview.ACETextManager;
import ch.uzh.ifi.attempto.aceview.model.event.ACEViewEvent;
import ch.uzh.ifi.attempto.aceview.model.event.ACEViewListener;
import ch.uzh.ifi.attempto.aceview.model.event.TextEventType;
import mt.uol.smi.attempto.aceview.planner.model.event.ACEPlannerEvent;
import mt.uol.smi.attempto.aceview.planner.model.event.ACEPlannerListener;
import mt.uol.smi.attempto.aceview.planner.model.event.ModelEventType;

public class EffectSnippetsTableModel extends AbstractTableModel {

//	protected List<ACEPlannerEffectTuple> effects;
	protected ACEViewListener<ACEViewEvent<TextEventType>> aceTextManagerListener;
	protected ACEPlannerListener<ACEPlannerEvent<ModelEventType>> acePlannerManagerListener;
	private ACEPlannerActionModel actionModel;

	private static final Logger logger = Logger.getLogger(AvailableSnippetsTableModel.class);

	public enum Column implements TableColumnEditable {
		SNIPPET("Effect", "E", true, ACESnippet.class, false), NEGATE("Negate?", "N?", true, Boolean.class, true);

		private final String name;
		private final String abbr;
		private final boolean isVisible;
		private final boolean isEditable;
		private final Class<?> dataClass;

		private Column(String name, String abbr, boolean isVisible, Class<?> dataClass, boolean isEditable) {
			this.name = name;
			this.abbr = abbr;
			this.isVisible = isVisible;
			this.dataClass = dataClass;
			this.isEditable = isEditable;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getAbbr() {
			return abbr;
		}

		@Override
		public boolean isVisible() {
			return isVisible;
		}

		public boolean isNumeric() {
			return dataClass.equals(Integer.class);
		}

		@Override
		public Class<?> getDataClass() {
			return dataClass;
		}

		@Override
		public boolean isEditable() {
			return isEditable;
		}

	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public int getRowCount() {
		if (actionModel == null || actionModel.getEffects() == null)
			return 0;
		return actionModel.getEffects().size();
	}

	@Override
	public String getColumnName(int col) {
		return Column.values()[col].getName();
	}

	@Override
	public Object getValueAt(int row, int col) {
		switch (col) {
		case 0:
			return actionModel.getEffects().get(row).getUnformattedResult().getAceSnippet();
		case 1:
			return actionModel.getEffects().get(row).isNegated();
		default:
			return "n/a";
		}
	}


	/*
	 * JTable uses this method to determine the default renderer/ editor for each
	 * cell. If we didn't implement this method, then the last column would contain
	 * text ("true"/"false"), rather than a check box.
	 */
	@Override
	public Class<?> getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	/*
	 * Don't need to implement this method unless your table's editable.
	 */
	@Override
	public boolean isCellEditable(int row, int col) {
		// Note that the data/cell address is constant,
		// no matter where the cell appears onscreen.
		if (col < 1) {
			return false;
		}
		return true;
	}

	/*
	 * Don't need to implement this method unless your table's data can change.
	 */
	@Override
	public void setValueAt(Object value, int row, int col) {
		if (col == 1) {
			if (value == null) {
				value = "false";
			}
			actionModel.getEffects().get(row).setNegated(Boolean.valueOf(value.toString()));
			logger.debug("Changed negation");
			fireTableCellUpdated(row, col);
		}
	}

	public ACEPlannerActionModel getActionModel() {
		return actionModel;
	}

	public EffectSnippetsTableModel(ACEPlannerActionModel actionModel) {
		this.actionModel = actionModel;
		initSnippets(getActionModel());		
		acePlannerManagerListener = new ACEPlannerListener<ACEPlannerEvent<ModelEventType>>() {
			@Override
			public void handleChange(ACEPlannerEvent<ModelEventType> event) {
				initSnippets(getActionModel());
				fireTableDataChanged();
			}
		};

		ACETextManager.getInstance().addListener(aceTextManagerListener);
	}

	private void initSnippets(ACEPlannerActionModel actionModel) {
		this.actionModel = actionModel;
	}
	
	public void setAction(ACEPlannerActionModel actionModel) {
		initSnippets(actionModel);
	}
}