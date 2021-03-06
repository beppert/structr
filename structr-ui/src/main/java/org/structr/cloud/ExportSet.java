/**
 * Copyright (C) 2010-2014 Morgner UG (haftungsbeschränkt)
 *
 * This file is part of Structr <http://structr.org>.
 *
 * Structr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Structr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Structr.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.structr.cloud;

import java.util.LinkedHashSet;
import java.util.Set;
import org.structr.common.Syncable;
import org.structr.core.graph.NodeInterface;
import org.structr.core.graph.RelationshipInterface;
import org.structr.dynamic.File;

/**
 *
 * @author Christian Morgner
 */
public class ExportSet {

	private final Set<NodeInterface> nodes                 = new LinkedHashSet<>();
	private final Set<RelationshipInterface> relationships = new LinkedHashSet<>();
	private int size                                       = 0;

	private ExportSet() {}

	public boolean add(final Syncable data) {

		if (data.isNode()) {

			if (nodes.add(data.getSyncNode())) {

				size++;

				if (data.getSyncNode() instanceof File) {

					size += (((File)data.getSyncNode()).getSize().intValue() / CloudService.CHUNK_SIZE) + 2;
				}

				// node was new (added), return true
				return true;
			}

		} else {

			if (relationships.add(data.getSyncRelationship())) {

				size++;

				// rel was new (added), return true
				return true;
			}
		}

		// arriving here means node was not added, so we return false
		return false;
	}

	public Set<NodeInterface> getNodes() {
		return nodes;
	}

	public Set<RelationshipInterface> getRelationships() {
		return relationships;
	}

	public int getTotalSize() {
		return size;
	}

	// ----- public static methods -----
	public static ExportSet getInstance() {
		return new ExportSet();
	}

	public static ExportSet getInstance(final Syncable start, final boolean recursive) {

		final ExportSet exportSet = new ExportSet();

		exportSet.collectSyncables(start, recursive);

		return exportSet;
	}

	private void collectSyncables(final Syncable start, boolean recursive) {

		add(start);

		if (recursive) {

			// collect children
			for (final Syncable child : start.getSyncData()) {

				if (child != null && add(child)) {

					collectSyncables(child, recursive);
				}
			}
		}
	}
}
