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

import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import ch.uzh.ifi.attempto.aceview.model.event.ACEViewEvent;
import ch.uzh.ifi.attempto.aceview.model.event.ACEViewListener;
import ch.uzh.ifi.attempto.aceview.model.event.TextEventType;

public class ParametersTableModel extends AbstractTableModel {

	protected ACEPlannerConditionModel condition;
	protected ACEViewListener<ACEViewEvent<TextEventType>> aceTextManagerListener;

	private static final Logger logger = Logger.getLogger(ParametersTableModel.class);

	public enum ParamColumn implements TableColumnEditable {
		VALUE("Value", "V", true, String.class, true), NAME("Name", "Nm", true, String.class, true);

		private final String name;
		private final String abbr;
		private final boolean isVisible;
		private final boolean isEditable;
		private final Class<?> dataClass;

		private ParamColumn(String name, String abbr, boolean isVisible, Class<?> dataClass, boolean isEditable) {
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
		if (condition.getParameters() == null)
			return 0;
		return condition.getParameters().size();
	}

	@Override
	public String getColumnName(int col) {
		return ParamColumn.values()[col].getName();
	}

	@Override
	public Object getValueAt(int row, int col) {
		switch (col) {
		case 0:
			return condition.getParameters().get(row).getParameterValue();
		case 1:
			return condition.getParameters().get(row).getParameterName();
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
		return true;
	}

	/*
	 * Don't need to implement this method unless your table's data can change.
	 */
	@Override
	public void setValueAt(Object value, int row, int col) {
		if (col == 0) {
			if (value == null) {
				value = "";
			}
			condition.getParameters().get(row).setParameterValue(String.valueOf(value));
			logger.debug("Name updated");
			fireTableCellUpdated(row, col);
		}
		if (col == 1) {
			if (value == null) {
				value = "";
			}
			condition.getParameters().get(row).setParameterName(String.valueOf(value));
			logger.debug("Value updated");
			fireTableCellUpdated(row, col);
		}
	}

	public ParametersTableModel(ACEPlannerConditionModel condition) {
		initSnippets(condition);
	}

	private void initSnippets(ACEPlannerConditionModel condition) {
		if (condition == null) {
			logger.info("***Condition set is null");
		} else if (condition.getParameters() == null) {
			logger.info("***Condition set is but condition.getParameters() is null");
		} else {
			logger.info("***Condition set. Parameters : " + condition.getParameters().size());
			this.condition = condition;
		}
	}

	public List<ACEPlannerParameter> getParameters() {
		return condition.getParameters();
	}

	public void setCondition(ACEPlannerConditionModel condition) {
		initSnippets(condition);
	}

}