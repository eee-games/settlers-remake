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
package jsettlers.graphics.map.controls.original.panel.content;

import jsettlers.common.buildings.EBuildingType;
import jsettlers.common.map.IGraphicsGrid;
import jsettlers.common.movable.EMovableType;
import jsettlers.common.position.ShortPoint2D;
import jsettlers.common.selectable.ISelectionSet;
import jsettlers.graphics.action.Action;
import jsettlers.graphics.action.ConvertAction;
import jsettlers.graphics.action.EActionType;
import jsettlers.graphics.localization.Labels;
import jsettlers.graphics.utils.UIPanel;

/**
 * Displays a selection of specialists.
 * 
 * @author michael
 */
public class SpecialistSelection implements IContentProvider {

	private static final EMovableType[] specialists = new EMovableType[] {
			EMovableType.PIONEER, EMovableType.THIEF, EMovableType.GEOLOGIST,
	};

	/**
	 * Rows of selectables
	 */
	public static int ROWS = 10;

	private final UIPanel panel;

	public SpecialistSelection(ISelectionSet selection) {
		panel = new UIPanel();

		SoilderSelection.addRowsToPanel(panel, selection, specialists);

		UIPanel stop =
				new UILabeledButton(Labels.getString("stop"),
						new Action(EActionType.STOP_WORKING));
		UIPanel work =
				new UILabeledButton(Labels.getString("work"), new Action(
						EActionType.START_WORKING));

		panel.addChild(stop, .1f, .1f, .5f, .2f);
		panel.addChild(work, .5f, .1f, .9f, .2f);

		if (selection.getMovableCount(EMovableType.PIONEER) > 0) {
			UIPanel convert =
					new UILabeledButton(Labels.getString("convert_all_to_BEARER"),
							new ConvertAction(EMovableType.BEARER,
									Short.MAX_VALUE));
			panel.addChild(convert, .1f, .2f, .9f, .3f);
		}
	}

	@Override
	public UIPanel getPanel() {
		return panel;
	}

	@Override
	public ESecondaryTabType getTabs() {
		return null;
	}

	@Override
	public void displayBuildingBuild(EBuildingType type) {
	}

	@Override
	public void showMapPosition(ShortPoint2D pos, IGraphicsGrid grid) {
	}

}
