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
package jsettlers.logic.map.newGrid.partition.manager.objects;

import java.io.Serializable;

import jsettlers.common.movable.EDirection;
import jsettlers.common.position.ILocatable;
import jsettlers.common.position.ShortPoint2D;
import jsettlers.logic.buildings.Building;

public final class BricklayerRequest implements ILocatable, Serializable {
	private static final long serialVersionUID = -1673422793657988587L;

	public final Building building;
	public final ShortPoint2D bricklayerTargetPos;
	public final EDirection direction;
	private boolean creationRequested;

	public BricklayerRequest(Building building, ShortPoint2D bricklayerTargetPos, EDirection direction) {
		this.building = building;
		this.bricklayerTargetPos = bricklayerTargetPos;
		this.direction = direction;
	}

	@Override
	public final ShortPoint2D getPos() {
		return building.getPos();
	}

	public boolean isCreationRequested() {
		return creationRequested;
	}

	public void creationRequested() {
		creationRequested = true;
	}
}