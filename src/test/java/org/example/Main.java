package org.example;

import org.eclipse.rdf4j.model.Model;

public class Main {

	public static void main(String[] args) {

		
		BuildingModel buildingModel = new BuildingModel();
		Model newBuilding = buildingModel.createBuildingModel();
		new PathFinder().FindAPathOut(newBuilding, "H2", false);
	}
	
	

}
