package jsettlers.logic.map.newGrid.partition.manager.manageables;

import jsettlers.common.material.EMaterialType;
import jsettlers.common.position.ILocatable;
import jsettlers.common.position.ISPosition2D;

public interface IManageableBearer extends IManageable, ILocatable {

	void executeJob(ISPosition2D offer, ISPosition2D request, EMaterialType materialType);

}
