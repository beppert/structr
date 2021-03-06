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
package org.structr.schema;

import org.structr.core.notion.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.structr.core.property.PropertyKey;
import org.structr.core.property.PropertyMap;
import org.structr.common.SecurityContext;
import org.structr.core.GraphObject;
import org.structr.core.entity.AbstractNode;

//~--- JDK imports ------------------------------------------------------------

import org.structr.common.error.FrameworkException;
import org.structr.common.error.PropertiesNotFoundToken;
import org.structr.common.error.TypeToken;
import org.structr.core.JsonInput;
import org.structr.core.Result;
import org.structr.core.app.App;
import org.structr.core.app.StructrApp;
import org.structr.core.graph.NodeInterface;

//~--- classes ----------------------------------------------------------------

/**
 * Deserializes a {@link GraphObject} using a type and a set of property values.
 *
 * @author Christian Morgner
 */
public class SchemaDeserializationStrategy<S, T extends NodeInterface> implements DeserializationStrategy<S, T> {

	private static final Logger logger = Logger.getLogger(TypeAndPropertySetDeserializationStrategy.class.getName());
	
	protected Set<PropertyKey> identifyingPropertyKeys = null;
	protected Set<PropertyKey> foreignPropertyKeys     = null;
	protected boolean createIfNotExisting              = false;
	protected Class targetType                         = null;

	//~--- constructors ---------------------------------------------------

	public SchemaDeserializationStrategy(final boolean createIfNotExisting, final Class targetType, final Set<PropertyKey> identifyingPropertyKeys, final Set<PropertyKey> foreignPropertyKeys) {
		this.createIfNotExisting     = createIfNotExisting;
		this.identifyingPropertyKeys = identifyingPropertyKeys;
		this.foreignPropertyKeys     = foreignPropertyKeys;
		this.targetType              = targetType;
	}

	//~--- methods --------------------------------------------------------

	@Override
	public T deserialize(SecurityContext securityContext, Class<T> type, S source) throws FrameworkException {

		if (source instanceof JsonInput) {
			
			PropertyMap attributes = PropertyMap.inputTypeToJavaType(securityContext, type, ((JsonInput)source).getAttributes());
			return deserialize(securityContext, type, attributes);
		}
		
		if (source instanceof Map) {
			
			PropertyMap attributes = PropertyMap.inputTypeToJavaType(securityContext, type, (Map)source);
			return deserialize(securityContext, type, attributes);
		}
		
		return null;
	}

	private T deserialize(final SecurityContext securityContext, final Class<T> type, final PropertyMap attributes) throws FrameworkException {

		final App app = StructrApp.getInstance(securityContext);
		
		if (attributes != null) {
			
			Result<T> result = Result.EMPTY_RESULT;
			
			// Check if properties contain the UUID attribute
			if (attributes.containsKey(GraphObject.id)) {

				result = new Result(app.get(attributes.get(GraphObject.id)), false);
				
			} else {

				
				boolean attributesComplete = true;
				
				// Check if all property keys of the PropertySetNotion are present
				for (PropertyKey key : identifyingPropertyKeys) {
					attributesComplete &= attributes.containsKey(key);
				}
				
				if (attributesComplete) {
					
					result = app.nodeQuery(type).and(attributes).getResult();
					
				}
			}

			// just check for existance
			final int size = result.size();
			switch (size) {

				case 0:

					if (createIfNotExisting) {

						// remove attributes that do not belong to the target node
						final PropertyMap foreignProperties = new PropertyMap();

						for (final Iterator<PropertyKey> it = attributes.keySet().iterator(); it.hasNext();) {

							final PropertyKey key = it.next();
							if (foreignPropertyKeys.contains(key)) {

								// move entry to foreign map and remove from attributes
								foreignProperties.put(key, attributes.get(key));
								it.remove();
							}
						}
						
						// create node and return it
						T newNode = app.create(type, attributes);
						if (newNode != null) {

							// test set notion attributes for relationship creation
							Map<String, PropertyMap> notionPropertyMap = (Map<String, PropertyMap>)securityContext.getAttribute("notionProperties");
							if (notionPropertyMap == null) {

								notionPropertyMap = new LinkedHashMap<>();
								securityContext.setAttribute("notionProperties", notionPropertyMap);
							}

							notionPropertyMap.put(newNode.getUuid(), foreignProperties);
							
							return newNode;
						}						
					}

					break;

				case 1:
					return getTypedResult(result, type);

				default:

					logger.log(Level.SEVERE, "Found {0} nodes for given type and properties, property set is ambiguous!\n"
						+ "This is often due to wrong modeling, or you should consider creating a uniquness constraint for " + type.getName(), size);

					break;
			}

			throw new FrameworkException(type.getSimpleName(), new PropertiesNotFoundToken(AbstractNode.base, attributes));
		}
		
		return null;
	}
	
	private T getTypedResult(Result<T> result, Class<T> type) throws FrameworkException {
		
		GraphObject obj = result.get(0);

		if (!type.isAssignableFrom(obj.getClass())) {
			throw new FrameworkException(type.getSimpleName(), new TypeToken(AbstractNode.base, type.getSimpleName()));
		}

		return result.get(0);
	}
	
	
}
