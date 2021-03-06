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
package jsettlers.graphics.sound;

import jsettlers.common.landscape.ELandscapeType;
import jsettlers.common.map.shapes.MapRectangle;
import jsettlers.common.mapobject.EMapObjectType;
import jsettlers.common.mapobject.IMapObject;
import jsettlers.graphics.map.MapDrawContext;

/**
 * Plays the background sound for birds and landscapes.
 *
 * @author michael
 */
public class BackgroundSound implements Runnable {
	private static final float VOLUME = .4f;
	private final MapDrawContext map;
	private final SoundManager sound;
	private final Thread thread;
	private final Object waitMutex = new Object();
	private boolean stopped = false;

	private final int INDEX_BIRDS1 = 69;
	private final int INDEX_BIRDS2 = 70;
	private final int INDEX_WATER = 68;
	private final int INDEX_DESERT = 73;

	public BackgroundSound(MapDrawContext map, SoundManager sound) {
		this.map = map;
		this.sound = sound;

		this.thread = new Thread(this, "background-sound");
		this.thread.start();
	}

	@Override
	public void run() {
		try {
			while (!stopped) {
				waitTime(300);
				if (stopped) {
					break;
				}
				MapRectangle screen = map.getScreenArea();
				if (screen == null) {
					continue;
				}
				int line = (int) (Math.random() * screen.getLines());

				int x =
						screen.getLineStartX(line)
								+ (int) (Math.random() * screen.getLineLength());
				int y = screen.getLineY(line);

				if (hasTree(x, y)) {
					if (Math.random() < .5) {
						sound.playSound(INDEX_BIRDS1, VOLUME, VOLUME);
					} else {
						sound.playSound(INDEX_BIRDS2, VOLUME, VOLUME);
					}
					waitTime(800); // < Do not play it to often
				} else if (hasWater(x, y)) {
					sound.playSound(INDEX_WATER, VOLUME, VOLUME);
					waitTime(200);
				} else if (hasDesert(x, y)) {
					sound.playSound(INDEX_DESERT, VOLUME, VOLUME);
					waitTime(500);
				}
			}
		} catch (Throwable e) {
			System.out.println("Sound thread died because of an exception.");
			e.printStackTrace();
		}
	}

	private void waitTime(int time) {
		synchronized (waitMutex) {
			try {
				waitMutex.wait(time);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private boolean hasDesert(int x, int y) {
		return map.checkMapCoordinates(x, y) && map.getVisibleStatus(x, y) != 0
				&& map.getLandscape(x, y) == ELandscapeType.DESERT;
	}

	private boolean hasWater(int x, int y) {
		return map.checkMapCoordinates(x, y) && map.getVisibleStatus(x, y) != 0
				&& map.getLandscape(x, y) == ELandscapeType.WATER1;
	}

	private boolean hasTree(int cx, int cy) {
		for (int x = cx - 2; x <= cx + 2; x++) {
			for (int y = cy - 2; y <= cy + 2; y++) {
				if (map.checkMapCoordinates(x, y)
						&& map.getVisibleStatus(x, y) != 0
						&& hasTreeObject(x, y)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean hasTreeObject(int x, int y) {
		IMapObject o = map.getMap().getMapObjectsAt(x, y);
		while (o != null) {
			EMapObjectType type = o.getObjectType();
			if (type == EMapObjectType.TREE_ADULT
					|| type == EMapObjectType.TREE_DEAD) {
				return true;
			}
			o = o.getNextObject();
		}
		return false;
	}

	public void stop() {
		synchronized (waitMutex) {
			stopped = true;
			waitMutex.notifyAll();
		}
	}

}
