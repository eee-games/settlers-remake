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
package jsettlers.logic.map.save;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

import jsettlers.common.map.IMapData;
import jsettlers.common.map.MapLoadException;
import jsettlers.common.resources.ResourceManager;
import jsettlers.input.PlayerState;
import jsettlers.logic.map.newGrid.GameSerializer;
import jsettlers.logic.map.newGrid.MainGrid;
import jsettlers.logic.map.save.IMapLister.IMapListerCallable;
import jsettlers.logic.map.save.MapFileHeader.MapType;
import jsettlers.logic.map.save.loader.MapLoader;
import jsettlers.logic.timer.RescheduleTimer;

/**
 * This is the main map list.
 * <p>
 * It lists all available maps, and it can be used to add maps to the game.
 * <p>
 * TODO: load maps before they are needed, to decrease startup time.
 * 
 * @author michael
 * @author Andreas Eberle
 */
public class MapList implements IMapListerCallable {
	private static class DefaultMapListFactory implements IMapListFactory {
		@Override
		public MapList getMapList() {
			return new MapList(ResourceManager.getSaveDirectory());
		}
	}

	public static final String MAP_EXTENSION = ".map";
	private static IMapListFactory mapListFactory = new DefaultMapListFactory();
	private final IMapLister mapsDir;
	private final IMapLister saveDir;

	private final ArrayList<MapLoader> freshMaps = new ArrayList<MapLoader>();
	private final ArrayList<MapLoader> savedMaps = new ArrayList<MapLoader>();

	private boolean fileListLoaded = false;

	public MapList(File dir) {
		this(new DirectoryMapLister(new File(dir, "maps"), false),
				new DirectoryMapLister(new File(dir, "save"), true));
	}

	public MapList(IMapLister mapsDir, IMapLister saveDir) {
		this.mapsDir = mapsDir;
		this.saveDir = saveDir;
	}

	private void loadFileList() {
		freshMaps.clear();
		savedMaps.clear();

		mapsDir.getMaps(this);
		saveDir.getMaps(this);

		Collections.sort(freshMaps);
		Collections.sort(savedMaps);
	}

	@Override
	public synchronized void foundMap(IListedMap map) {
		try {
			MapLoader loader = MapLoader.getLoaderForFile(map);
			MapType type = loader.getFileHeader().getType();
			if (type == MapType.SAVED_SINGLE) {
				savedMaps.add(loader);
			} else {
				freshMaps.add(loader);
			}
		} catch (MapLoadException e) {
			System.err.println("Cought exception while loading header for "
					+ map.getFileName());
			e.printStackTrace();
		}
	}

	public synchronized ArrayList<MapLoader> getSavedMaps() {
		if (!fileListLoaded) {
			loadFileList();
			fileListLoaded = true;
		}
		return savedMaps;
	}

	public synchronized ArrayList<MapLoader> getFreshMaps() {
		if (!fileListLoaded) {
			loadFileList();
			fileListLoaded = true;
		}
		return freshMaps;
	}

	/**
	 * Gives the {@link MapLoader} for the map with the given id.
	 * 
	 * @param id
	 *            The id of the map to be found.
	 * @return Returns the corresponding {@link MapLoader}<br>
	 *         or null if no map with the given id has been found.
	 */
	public MapLoader getMapById(String id) {
		ArrayList<MapLoader> maps = new ArrayList<MapLoader>();
		maps.addAll(getFreshMaps());
		maps.addAll(getSavedMaps());

		for (MapLoader curr : maps) {
			if (curr.getMapID().equals(id)) {
				return curr;
			}
		}
		return null;
	}

	public MapLoader getMapByName(String mapName) {
		ArrayList<MapLoader> maps = new ArrayList<MapLoader>();
		maps.addAll(getFreshMaps());
		maps.addAll(getSavedMaps());

		for (MapLoader curr : maps) {
			if (curr.getMapName().equals(mapName)) {
				return curr;
			}
		}
		return null;
	}

	/**
	 * saves a static map to the given directory.
	 * 
	 * @param header
	 *            The header to use.
	 * @param data
	 *            The data to save.
	 * @throws IOException
	 *             If any IO error occurred.
	 */
	public synchronized void saveNewMap(MapFileHeader header, IMapData data) throws IOException {
		OutputStream out = null;
		try {
			out = mapsDir.getOutputStream(header);
			MapSaver.saveMap(header, data, out);
		} finally {
			if (out != null) {
				out.close();
			}
		}
		loadFileList();
	}

	/**
	 * Saves a map to disk. The map logic should be paused while calling this method.
	 * 
	 * @param state
	 * @param grid
	 * @throws IOException
	 */
	public synchronized void saveMap(PlayerState[] playerStates, MainGrid grid)
			throws IOException {
		MapFileHeader header = grid.generateSaveHeader();
		OutputStream outStream = saveDir.getOutputStream(header);

		header.writeTo(outStream);

		ObjectOutputStream oos = new ObjectOutputStream(outStream);
		oos.writeObject(playerStates);
		GameSerializer gameSerializer = new GameSerializer();
		gameSerializer.save(grid, oos);
		RescheduleTimer.saveTo(oos);

		oos.close();

		loadFileList();
	}

	/**
	 * Saves a random map to the given file.
	 * 
	 * @param header
	 *            The header to save
	 * @param definition
	 *            The random map rule text.
	 * @throws IOException
	 */
	public synchronized void saveRandomMap(MapFileHeader header,
			String definition) throws IOException {
		OutputStream out = mapsDir.getOutputStream(header);
		MapSaver.saveRandomMap(header, definition, out);
		loadFileList();
	}

	public ArrayList<MapLoader> getSavedMultiplayerMaps() {
		// TODO: save multiplayer maps, so that we can load them.
		return null;
	}

	/**
	 * gets the list of the default directory.
	 * 
	 * @return
	 */
	public static MapList getDefaultList() {
		return mapListFactory.getMapList();
	}

	public void deleteLoadableGame(MapLoader game) {
		game.delete();
		savedMaps.remove(game);
		loadFileList();
	}

	public static void setDefaultList(IMapListFactory factory) {
		mapListFactory = factory;
	}

}
