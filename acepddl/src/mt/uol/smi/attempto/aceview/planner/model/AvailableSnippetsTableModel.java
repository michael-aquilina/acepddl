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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import ch.uzh.ifi.attempto.aceview.ACESnippet;
import ch.uzh.ifi.attempto.aceview.ACEText;
import ch.uzh.ifi.attempto.aceview.ACETextManager;
import ch.uzh.ifi.attempto.aceview.model.event.ACEViewEvent;
import ch.uzh.ifi.attempto.aceview.model.event.ACEViewListener;
import ch.uzh.ifi.attempto.aceview.model.event.TextEventType;
import mt.uol.smi.attempto.aceview.planner.ACEPlannerManager;
import mt.uol.smi.attempto.aceview.planner.model.event.ACEPlannerEvent;
import mt.uol.smi.attempto.aceview.planner.model.event.ACEPlannerListener;
import mt.uol.smi.attempto.aceview.planner.model.event.ModelEventType;

public class AvailableSnippetsTableModel extends AbstractTableModel {

	protected List<ACEPlannerSelectedSnippet> snippets;
	protected ACEViewListener<ACEViewEvent<TextEventType>> aceTextManagerListener;
	protected ACEPlannerListener<ACEPlannerEvent<ModelEventType>> acePlannerManagerListener;
	private ACEPlannerActionModel actionModel;

	private static final Logger logger = Logger.getLogger(EffectSnippetsTableModel.class);

	public enum Column implements TableColumnEditable {
		SNIPPET("Questions and Answers", "Q&A", true, ACESnippet.class, false), SELECTED("Use?", "U", true, Boolean.class, true);

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
		if (snippets == null)
			return 0;
		return snippets.size();
	}

	@Override
	public String getColumnName(int col) {
		return Column.values()[col].getName();
	}

	@Override
	public Object getValueAt(int row, int col) {
		switch (col) {
		case 0:
			return snippets.get(row).getAceSnippet();
		case 1:
			return snippets.get(row).isSelected();
		default:
			return "n/a";
		}
	}

	public List<ACEPlannerSelectedSnippet> getSelectedSnippets() {
		List<ACEPlannerSelectedSnippet> selected = new ArrayList<ACEPlannerSelectedSnippet>();
		if (snippets == null) {
			return selected;
		}

		for (ACEPlannerSelectedSnippet snippet : snippets) {
			if (snippet.isSelected()) {
				selected.add(snippet);
			}
		}
		return selected;
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
			snippets.get(row).setSelected(Boolean.valueOf(value.toString()));
			logger.debug("Changed selection");
			fireTableCellUpdated(row, col);
		}
	}

	public ACEPlannerActionModel getActionModel() {
		return actionModel;
	}

	public AvailableSnippetsTableModel(ACEPlannerActionModel actionModel) {
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
		Set<ACEPlannerSnippet> aceSnippets = new HashSet<ACEPlannerSnippet>(ACEPlannerManager.getInstance().getCurrentlyBeingModifiedACEPlannerModel().getSentencesUsed().getSnippets());
		
		snippets = new ArrayList<ACEPlannerSelectedSnippet>();
		for (ACEPlannerSnippet aceSnippet : aceSnippets) {
			boolean isSelected = false;
			if (actionModel != null) {
				isSelected = actionModel.hasCondition(aceSnippet.getAceSnippet()) || actionModel.hasEffect(aceSnippet.getAceSnippet());				
			}
			snippets.add(new ACEPlannerSelectedSnippet(aceSnippet.getAceSnippet(), isSelected));
		}
		Collections.sort(snippets, new ACEPlannerSnippetComparator());
	}

	public List<ACEPlannerSelectedSnippet> getSnippets() {
		return snippets;
	}

	public void setSnippets(List<ACEPlannerSelectedSnippet> snippets) {
		this.snippets = snippets;
	}

}