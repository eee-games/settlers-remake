/*******************************************************************************
 * Copyright (c) 2015
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package jsettlers.network.client.task.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import jsettlers.network.infrastructure.channel.packet.Packet;

/**
 * 
 * @author Andreas Eberle
 * 
 */
public class SyncTasksPacket extends Packet {

	private int lockstepNumber;
	private List<TaskPacket> tasks;

	public SyncTasksPacket() {
	}

	public SyncTasksPacket(int packetNumber, List<TaskPacket> tasks) {
		this.lockstepNumber = packetNumber;
		this.tasks = tasks;
	}

	@Override
	public void serialize(DataOutputStream dos) throws IOException {
		dos.writeInt(lockstepNumber);
		dos.writeInt(tasks.size());

		for (TaskPacket curr : tasks) {
			curr.serialize(dos);
		}
	}

	@Override
	public void deserialize(DataInputStream dis) throws IOException {
		lockstepNumber = dis.readInt();
		int numberOfTasks = dis.readInt();
		tasks = new LinkedList<TaskPacket>();

		for (int i = 0; i < numberOfTasks; i++) {
			TaskPacket task = TaskPacket.DEFAULT_DESERIALIZER.deserialize(null, dis);
			tasks.add(task);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + lockstepNumber;
		result = prime * result + ((tasks == null) ? 0 : tasks.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SyncTasksPacket other = (SyncTasksPacket) obj;
		if (lockstepNumber != other.lockstepNumber)
			return false;
		if (tasks == null) {
			if (other.tasks != null)
				return false;
		} else if (!tasks.equals(other.tasks))
			return false;
		return true;
	}

	/**
	 * @return the packetNumber
	 */
	public int getLockstepNumber() {
		return lockstepNumber;
	}

	/**
	 * @return the tasks
	 */
	public List<TaskPacket> getTasks() {
		return tasks;
	}

}
