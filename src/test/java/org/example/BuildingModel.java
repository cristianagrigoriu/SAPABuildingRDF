package org.example;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

public class BuildingModel {
	public Model createBuildingModel() {
		ModelBuilder builder = new ModelBuilder();
		
		builder.setNamespace("ex", "http://example.org/");

		builder.namedGraph("ex:H1")
				.subject("ex:H1")
					.add("ex:hasExitDoor", true)
					.add("ex:hasWindow", false) //oare e necesar la toate?
					.add("ex:isConnectedTo", "ex:R1") //for possibilities of connection
					.add("ex:isLocatedInFrontOf", "ex:R1") //for direction
					.add("ex:isDistancedFromR1", 5)
					.add("ex:isOnFloor", 0);

		builder.namedGraph("ex:R1")
			.subject("ex:R1")
				.add("ex:hasExitDoor", false)
				.add("ex:hasWindow", true) 
				.add("ex:isConnectedTo", "ex:H1") 
				.add("ex:isLocatedBehind", "ex:H1")
				.add("ex:isDistancedFromH1", 5)
				.add("ex:isConnectedTo", "ex:R2") 
				.add("ex:isLocatedInFrontOf", "ex:R2")
				.add("ex:isDistancedFromR2", 10)
				.add("ex:isOnFloor", 0);
		
		builder.namedGraph("ex:R2")
		.subject("ex:R2")
			.add("ex:hasExitDoor", false)
			.add("ex:hasWindow", false) 
			.add("ex:isConnectedTo", "ex:R1") 
			.add("ex:isLocatedBehind", "ex:R1")
			.add("ex:isDistancedFromR1", 10)
			.add("ex:isConnectedTo", "ex:H2") 
			.add("ex:isLocatedUnderneath", "ex:H2") 
			.add("ex:hasStairs", true)
			.add("ex:isOnFloor", 0);
		
		builder.namedGraph("ex:H2")
		.subject("ex:H2")
			.add("ex:hasExitDoor", false)
			.add("ex:hasWindow", false) 
			.add("ex:isConnectedTo", "ex:R3")
			.add("ex:isLocatedOnTheLeftof", "ex:R3") //to get from H2 to R3, you have to make a left
			.add("ex:isDistancedFromR3", 3)
			.add("ex:isConnectedTo", "ex:R4") 
			.add("ex:isLocatedOnTheRightOf", "ex:R4")
			.add("ex:isDistancedFromR4", 7)
			.add("ex:isConnectedTo", "ex:R2") 
			.add("ex:isLocatedOver", "ex:R2") 
			.add("ex:hasStairs", true)
			.add("ex:isOnFloor", 1);
		
		builder.namedGraph("ex:R3")
		.subject("ex:R3")
			.add("ex:hasWindow", true)
			.add("ex:hasExitDoor", false)
			.add("ex:isConnectedTo", "ex:H2")
			.add("ex:isLocatedOnTheRightOf", "ex:H2") //to get from R3 to H2, you have to make a right
			.add("ex:isDistancedFromH2", 3)
			.add("ex:hasStairs", false)
			.add("ex:isOnFloor", 1);
		
		builder.namedGraph("ex:R4")
		.subject("ex:R4")
			.add("ex:hasExitDoor", false)
			.add("ex:hasWindow", false) 
			.add("ex:isConnectedTo", "ex:H2")
			.add("ex:isLocatedOnTheLeftof", "ex:H2") //to get from R4 to H2, you have to make a left
			.add("ex:isDistancedFromH2", 7)
			.add("ex:hasStairs", false)
			.add("ex:isOnFloor", 1);


		// We're done building, create our Model
		Model model = builder.build();

		// Each named graph is stored as a separate context in our Model
		for (Resource context: model.contexts()) {
			System.out.println("Named graph " + context + " contains: ");

			// write _only_ the statemements in the current named graph to the console,
			// in N-Triples format
			Rio.write(model.filter(null, null, null, context), System.out, RDFFormat.NTRIPLES);
			System.out.println();
		}
		
		return model;
	}
}
