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
package jsettlers.mapcreator.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;

import jsettlers.common.landscape.ELandscapeType;
import jsettlers.common.map.MapLoadException;
import jsettlers.common.utils.MainUtils;
import jsettlers.logic.map.save.MapFileHeader;
import jsettlers.logic.map.save.MapFileHeader.MapType;
import jsettlers.logic.map.save.MapList;
import jsettlers.logic.map.save.loader.MapLoader;
import jsettlers.main.swing.SwingManagedJSettlers;

public class MapCreatorApp {
	private static final MapFileHeader DEFAULT = new MapFileHeader(MapType.NORMAL, "new map", null, "", (short) 300, (short) 300, (short) 1,
			(short) 10, null, new short[MapFileHeader.PREVIEW_IMAGE_SIZE * MapFileHeader.PREVIEW_IMAGE_SIZE]);
	private static final String[] GROUND_TYPES = new String[] { ELandscapeType.WATER8.toString(), ELandscapeType.GRASS.toString(),
			ELandscapeType.DRY_GRASS.toString(), ELandscapeType.SNOW.toString(), ELandscapeType.DESERT.toString(), };
	private final JFrame selectMapFrame;

	public static void main(String[] args) throws FileNotFoundException, IOException {
		SwingManagedJSettlers.setupResourceManagers(MainUtils.createArgumentsMap(args), "config.prp");
		new MapCreatorApp();
	}

	private MapCreatorApp() {
		JPanel newMap = createNewMapPanel();
		JPanel open = createOpenMapPanel();
		JPanel root = new JPanel();
		root.add(newMap);
		root.add(open);

		selectMapFrame = new JFrame("Select map");
		selectMapFrame.add(root);
		selectMapFrame.pack();
		selectMapFrame.setVisible(true);
		selectMapFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private JPanel createOpenMapPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Open map"));
		ArrayList<MapLoader> maps = MapList.getDefaultList().getFreshMaps();
		Object[] array = maps.toArray();
		Arrays.sort(array, new Comparator<Object>() {
			@Override
			public int compare(Object arg0, Object arg1) {
				if (arg0 instanceof MapLoader && arg1 instanceof MapLoader) {
					MapLoader mapLoader1 = (MapLoader) arg0;
					MapLoader mapLoader2 = (MapLoader) arg1;
					int nameComp = mapLoader1.getMapName().compareTo(mapLoader2.getMapName());
					if (nameComp != 0) {
						return nameComp;
					} else {
						return mapLoader1.toString().compareTo(mapLoader2.toString());
					}
				} else {
					return 0;
				}
			}
		});
		final JList<Object> mapList = new JList<Object>(array);
		panel.add(mapList);

		JButton button = new JButton("Open");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Object value = mapList.getSelectedValue();
				if (value instanceof MapLoader) {
					loadMap((MapLoader) value);
				}
			}
		});
		panel.add(button, BorderLayout.SOUTH);

		return panel;
	}

	private JPanel createNewMapPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		final MapHeaderEditor headerEditor = new MapHeaderEditor(DEFAULT, true);
		panel.add(headerEditor);
		headerEditor.setBorder(BorderFactory.createTitledBorder("Map settings"));

		JPanel ground = new JPanel();
		ground.setBorder(BorderFactory.createTitledBorder("Ground type"));
		panel.add(ground);

		final SpinnerListModel groundType = new SpinnerListModel(Arrays.asList(GROUND_TYPES));
		JSpinner groundTypes = new JSpinner(groundType);
		ground.add(groundTypes);

		JButton createMapButton = new JButton("Create map");
		panel.add(createMapButton);
		createMapButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new EditorWindow(headerEditor.getHeader(), ELandscapeType.valueOf(groundType.getValue().toString()));
				close();
			}
		});
		panel.add(Box.createRigidArea(new Dimension(20, 20)));
		return panel;
	}

	private void close() {
		selectMapFrame.setVisible(false);
		selectMapFrame.dispose();
	}

	protected void loadMap(MapLoader value) {
		try {
			new EditorWindow(value);
			close();
		} catch (MapLoadException e) {
			JOptionPane.showMessageDialog(selectMapFrame, e.getMessage());
			e.printStackTrace();
		}
	}
}
