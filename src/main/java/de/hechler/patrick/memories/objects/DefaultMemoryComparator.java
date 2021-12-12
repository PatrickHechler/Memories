package de.hechler.patrick.memories.objects;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Comparator;

public class DefaultMemoryComparator implements Serializable, Comparator <Memory> {
	
	/** UID */
	private static final long serialVersionUID = 7047136304627581468L;
	
	public static DefaultMemoryComparator INSTANCE = new DefaultMemoryComparator();
	
	private DefaultMemoryComparator() {}
	
	public int compare(Memory a, Memory b) {
		int cmp = defaultCompare(a.getFinishDate(), b.getFinishDate());
		if (cmp == 0) {
			cmp = defaultCompare(a.getLastModDate(), b.getLastModDate());
		}
		if (cmp == 0) {
			cmp = defaultCompare(a.getCreateDate(), b.getCreateDate());
		}
		if (cmp == 0) {
			cmp = a.getTitle().compareTo(b.getTitle());
		}
		return cmp;
	}
	
	private static int defaultCompare(Calendar ac, Calendar bc) {
		int cmp;
		if (ac != null) {
			if (bc != null) {
				cmp = -ac.compareTo(bc);
			} else {
				cmp = -1;
			}
		} else if (bc != null) {
			cmp = 1;
		} else {
			cmp = 0;
		}
		return cmp;
	}
	
}
