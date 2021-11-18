package de.hechler.patrick.memories;

import java.io.InputStreamReader;

import de.hechler.patrick.memories.exceptions.ActException;
import de.hechler.patrick.memories.objects.MemoryList;

public class MemoriesMain {
	
	public static void main(String[] args) {
		MemoryList memList = new MemoryList(System.out, args);
		memList.append(new InputStreamReader(System.in));
		memList.setExit(false);// (Unneeded)
		memList.printhelp();
		while ( !memList.isExit()) {
			try {
				memList.run();
			} catch (ActException e) {
				System.out.flush();
				e.printStackTrace(System.err);
				System.err.flush();
			}
		}
	}
	
}
