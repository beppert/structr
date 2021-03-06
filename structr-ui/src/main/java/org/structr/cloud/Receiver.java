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

import org.structr.cloud.message.Message;
import java.io.ObjectInputStream;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 *
 * @author Christian Morgner
 */
public class Receiver extends Thread {

	private final Queue<Message> inputQueue = new ArrayBlockingQueue<>(10000);
	private ObjectInputStream inputStream   = null;
	private CloudConnection connection      = null;

	public Receiver(final CloudConnection connection, final ObjectInputStream inputStream) {

		super("Receiver of " + connection.getName());
		this.setDaemon(true);

		this.inputStream = inputStream;
		this.connection  = connection;
	}

	@Override
	public void run() {

		while (connection.isConnected()) {

			try {

				final Message message = (Message)inputStream.readObject();
				if (message != null) {

					inputQueue.add(message);
				}

			} catch (Throwable t) {

				connection.close();
			}
		}
	}

	public Message receive() {
		return inputQueue.poll();
	}
}
