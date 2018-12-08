/*
 * This file is part of ACE View.
 * Copyright 2008-2010, Attempto Group, University of Zurich (see http://attempto.ifi.uzh.ch).
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

package ch.uzh.ifi.attempto.aceview;

/**
 * <p>
 * The ACE text manager keeps track of the open ACE texts, how they map to open
 * OWL ontologies, and which one of them is active.
 * </p>
 * 
 * <p>
 * The ACE text manager allows snippets to be added to and removed from the ACE
 * texts so that the corresponding ontology is updated by adding/removing the
 * affected axioms.
 * </p>
 * 
 * <p>
 * Note: selected snippet is independent from the ACE text, e.g. we can select a
 * snippet which does not belong to any text, e.g. entailed snippets are such.
 * </p>
 * 
 * @author Kaarel Kaljurand
 */
public final class ACETextManager {
	private static IACETextManager instance;

	public static IACETextManager getInstance() {
		if (instance == null) {
			instance = new ACETextManagerProtege();
		}
		return instance;
	}

	public static IACETextManager setInstance(IACETextManager textManagerInstance) {
		instance = textManagerInstance;
		return instance;
	}
}