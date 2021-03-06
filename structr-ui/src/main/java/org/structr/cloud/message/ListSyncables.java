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
package org.structr.cloud.message;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.structr.cloud.CloudConnection;
import org.structr.cloud.ExportContext;
import org.structr.common.Syncable;
import org.structr.common.error.FrameworkException;
import org.structr.schema.SchemaHelper;

/**
 *
 * @author Christian Morgner
 */
public class ListSyncables extends Message<List<SyncableInfo>> {

	private List<SyncableInfo> syncables = null;
	private String type = null;

	public ListSyncables(final String type) {
		this.type = type;
	}

	@Override
	public void onRequest(CloudConnection serverConnection, ExportContext context) throws IOException, FrameworkException {

		final String[] rawTypes = StringUtils.split(type, ",");

		final Set<Class<Syncable>> types = new HashSet();

		if (type != null) {

			for (final String rawType : rawTypes) {

				Class entityClass = SchemaHelper.getEntityClassForRawType(rawType);

				if (entityClass != null && Syncable.class.isAssignableFrom(entityClass)) {

					types.add(entityClass);

				}

			}
		}

		this.syncables = serverConnection.listSyncables(types);
		serverConnection.send(this);
	}

	@Override
	public void onResponse(CloudConnection clientConnection, ExportContext context) throws IOException, FrameworkException {

		context.progress();
		clientConnection.setPayload(syncables);
	}

	@Override
	public void afterSend(CloudConnection connection) {
	}

	@Override
	public List<SyncableInfo> getPayload() {
		return syncables;
	}
}
