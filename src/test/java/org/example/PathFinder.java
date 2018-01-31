package org.example;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

public class PathFinder {
	/*I am out if I am in room that has an exitDoor or a window*/
	public void FindAPathOut (Model building, String startingPoint, boolean includeWindowsAsExits) {
		
		boolean startingPointFound = false;
		
		//check if the startingPoint is included in the building
		for (Resource context: building.contexts())
			if (context.toString().contains(startingPoint)) startingPointFound = true;

		//System.out.println(startingPointFound);
		
		if (startingPointFound) {
			List<String> roomsVisited = new ArrayList<String>();
			roomsVisited.add(startingPoint);
			//System.out.println("Rooms Visited: " + BuildDFSPathOut(building, startingPoint, includeWindowsAsExits, roomsVisited).toString());
			roomsVisited = BuildDFSPathOut(building, startingPoint, includeWindowsAsExits, roomsVisited);
			BuildReadableInstructions(BuildInstructionsFromPath(building, roomsVisited));
		}
	}
	
	private List<String> BuildDFSPathOut(Model building, String startingPoint, boolean includeWindowsAsExits, List<String> roomsVisited) {		
		ValueFactory vf = SimpleValueFactory.getInstance();
		String ex = "http://example.org/";
		IRI isConnectedTo = vf.createIRI(ex, "isConnectedTo");
		IRI hasExitDoor = vf.createIRI(ex, "hasExitDoor");
		IRI startingPointIRI = vf.createIRI(ex, startingPoint);
		
		//check if starting point doesn't initially have an exit
		Model ImmediateExits = building.filter((Resource)startingPointIRI, hasExitDoor, null);
		for (Statement st: ImmediateExits) {
			Value hasExit = st.getObject();
			if (hasExit instanceof Literal) {
				if (((Literal)hasExit).getLabel().equals("true")) {
					return roomsVisited; //we can go out from here
				}
			}
		}
		
		//get rooms connected to the current starting point and that we haven't passed yet
		Set<Value> rooms = building.filter(startingPointIRI, isConnectedTo, null).objects();
		Set<Value> roomsCopy = new HashSet<Value>();
		
		for (Value nextRoom: rooms) {
			String nextRoomShortName = getShortNameOfRoom(nextRoom.toString());
			if (!roomsVisited.contains(nextRoomShortName))
				roomsCopy.add(nextRoom); 
		}
		rooms = roomsCopy;
		if (rooms.isEmpty()) {
			return null;
		}
		
		for (Value nextRoom: rooms) {
			Model exits = building.filter((Resource)nextRoom, hasExitDoor, null);
			
			for (Statement st: exits) {
				Value hasExit = st.getObject();
				if (hasExit instanceof Literal) {
					if (((Literal)hasExit).getLabel().equals("true")) {
						//we can go out from here
						roomsVisited.add(getShortNameOfRoom(st.getSubject().toString()));
						return roomsVisited;
					}
					else {
						String newStartingPoint = getShortNameOfRoom(st.getSubject().toString());
						
						roomsVisited.add(newStartingPoint);
						List<String> result = BuildDFSPathOut(building, newStartingPoint, includeWindowsAsExits, roomsVisited);
						if (result == null) {
							roomsVisited.remove(roomsVisited.size() - 1);
						}
						else
							return result;
					}
				}
			}
		}
		return null;
	}
	
	private String getShortNameOfRoom(String longName) {
		String shortString = longName.substring(longName.indexOf('/') + 1, longName.length());
		int secondIndex = shortString.indexOf('/', shortString.indexOf('/')+1);
		return shortString.substring(secondIndex + 1);
	}
	
	public List<HashMap<String, String>> BuildInstructionsFromPath (Model building, List<String> path) {
		if (path == null) 
			return null;
		//System.out.println("Instructiuni:");
		List<HashMap<String, String>> instructions = new ArrayList<HashMap<String, String>>();
		for (int i=0; i<path.size()-1; i++) {
			ValueFactory vf = SimpleValueFactory.getInstance();
			String ex = "http://example.org/";
			IRI roomFrom = vf.createIRI(ex, path.get(i));
			IRI roomTo = vf.createIRI(ex, path.get(i+1));
			
			//check how to get from one room to another
			Model directions = building.filter(roomFrom, null, roomTo);
			
			for (Statement st: directions) {
				Value location = st.getPredicate();
				String locationDescription = location.toString();
				if (locationDescription.contains("isLocated")) {
					locationDescription = locationDescription.substring(locationDescription.indexOf("isLocated") + 9);
					System.out.println("Location Description: " + locationDescription);
					
					String direction = new String();
					switch(locationDescription) {
						case "Behind": direction = "in front"; break;
						case "InFrontOf": direction = "behind"; break;
						case "OnTheLeftOf": direction = "on the left"; break;
						case "OnTheRightOf": direction = "on the right"; break;
						case "Underneath": direction = "upstairs"; break;
						case "Over": direction = "downstairs"; break;
					}
					
					String distancePredicate = "isDistancedFrom" + path.get(i+1);
					IRI distancePredicateIRI = vf.createIRI(ex, distancePredicate);
					Model distances = building.filter(roomFrom, distancePredicateIRI, null); 
					String distanceString = new String();
					for (Statement dist: distances) {
						int distance = ((Literal)dist.getObject()).intValue();
						distanceString = Integer.toString(distance);
					}
					
					HashMap<String, String> dict = new HashMap<String, String>();
					dict.put(direction, distanceString);
					instructions.add(dict);
					System.out.println(instructions.toString());
				}
			}
		}
		return instructions;
	}
	
	public List<String> BuildReadableInstructions(List<HashMap<String, String>> instructions) {
		if (instructions == null)
			return null;
		
		List<String> readableInstructions = new ArrayList<String>();
		for (HashMap<String, String> instr: instructions) {
			String readableInstr = "Walk " + instr.entrySet().iterator().next().getKey();
			
			String ceva = instr.entrySet().iterator().next().getValue();
			
			if (!instr.entrySet().iterator().next().getValue().equals(""))
					readableInstr += " for " + instr.entrySet().iterator().next().getValue() + " steps";
					
			readableInstructions.add(readableInstr);
		}
		
		System.out.println(readableInstructions.toString());
		
		return readableInstructions;
	}
}
