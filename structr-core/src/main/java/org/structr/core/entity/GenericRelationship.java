/**
 * Copyright (C) 2010-2014 Morgner UG (haftungsbeschränkt)
 *
 * This file is part of Structr <http://structr.org>.
 *
 * Structr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Structr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Structr.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.structr.core.entity;


//~--- JDK imports ------------------------------------------------------------

import java.util.LinkedHashSet;
import java.util.Set;
import org.neo4j.graphdb.Relationship;
import org.structr.core.property.PropertyKey;
import org.structr.core.property.PropertyMap;
import org.structr.common.PropertyView;
import org.structr.common.SecurityContext;
import org.structr.common.View;
import org.structr.common.error.ErrorBuffer;
import org.structr.common.error.FrameworkException;
import org.structr.core.app.StructrApp;
import org.structr.core.graph.NodeInterface;
import org.structr.core.property.SourceId;
import org.structr.core.property.TargetId;

//~--- classes ----------------------------------------------------------------

/**
 * A generic relationship entity that will be instantiated when an anonymous
 * relationship is encountered.
 *
 * @author Axel Morgner
 *
 */
public class GenericRelationship extends ManyToMany<NodeInterface, NodeInterface> {

	public static final SourceId startNodeId = new SourceId("startNodeId");
	public static final TargetId endNodeId   = new TargetId("endNodeId");

	public static final View uiView = new View(GenericRelationship.class, PropertyView.Ui,
		startNodeId, endNodeId, sourceId, targetId
	);

	public GenericRelationship() {}

	public GenericRelationship(SecurityContext securityContext, Relationship dbRelationship) {
		init(securityContext, dbRelationship, GenericRelationship.class);
	}

	@Override
	public Iterable<PropertyKey> getPropertyKeys(String propertyView) {

		Set<PropertyKey> keys = new LinkedHashSet<>();

		keys.addAll((Set<PropertyKey>) super.getPropertyKeys(propertyView));

		keys.add(startNodeId);
		keys.add(endNodeId);

		if (dbRelationship != null) {

			for (String key : dbRelationship.getPropertyKeys()) {
				keys.add(StructrApp.getConfiguration().getPropertyKeyForDatabaseName(entityType, key));
			}
		}

		return keys;
	}

	@Override
	public boolean onCreation(SecurityContext securityContext, ErrorBuffer errorBuffer) throws FrameworkException {
		return true;
	}

	@Override
	public boolean onModification(SecurityContext securityContext, ErrorBuffer errorBuffer) throws FrameworkException {
		return true;
	}

	@Override
	public boolean onDeletion(SecurityContext securityContext, ErrorBuffer errorBuffer, PropertyMap properties) throws FrameworkException {
		return true;
	}

	@Override
	public boolean isValid(ErrorBuffer errorBuffer) {
		return true;
	}

	@Override
	public Class<NodeInterface> getSourceType() {
		return NodeInterface.class;
	}

	@Override
	public Class<NodeInterface> getTargetType() {
		return NodeInterface.class;
	}

	@Override
	public String name() {
		return "GENERIC";
	}

//	@Override
//	public SourceId getSourceIdProperty() {
//		return startNodeId;
//	}
//
//	@Override
//	public TargetId getTargetIdProperty() {
//		return endNodeId;
//	}
}
