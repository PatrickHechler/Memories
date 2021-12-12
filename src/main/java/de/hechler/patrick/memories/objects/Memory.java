package de.hechler.patrick.memories.objects;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Memory implements Serializable {
	
	/** UID */
	private static final long serialVersionUID = -8960325510104304615L;
	
	private static int FLAG_FINISHED  = 0x00000001;
	private static int FLAG_HIGH_PRIO = 0x00000002;
	private static int FLAG_LOW_PRIO  = 0x00000004;
	private static int FLAG_SUSPENDED = 0x00000008;
	
	private int                  flags;
	private String               title;
	private String[]             lines;
	private Calendar             finishDate;
	private Calendar             lastModDate;
	private Calendar             createDate;
	private Map <Memory, String> links;
	
	
	
	public Memory() {
		this.title = "";
		this.lines = new String[0];
		this.flags = 0;
		this.finishDate = null;
		this.lastModDate = null;
		this.createDate = null;
		this.links = new HashMap<>();
	}
	
	public Memory(String title, String[] lines, Calendar createDate) {
		this.title = title;
		this.lines = lines.clone();
		this.flags = 0;
		this.finishDate = null;
		this.lastModDate = null;
		this.createDate = (Calendar) createDate.clone();
		this.links = new HashMap<>();
	}
	
	
	
	public void setFinished(boolean f) {
		flags = f ? (flags | FLAG_FINISHED) : (flags & ~FLAG_FINISHED);
	}
	
	public void setHighPriorised(boolean hp) {
		flags = hp ? (flags | FLAG_HIGH_PRIO) : (flags & ~FLAG_HIGH_PRIO);
	}
	
	public void setLowPriorised(boolean lp) {
		flags = lp ? (flags | FLAG_LOW_PRIO) : (flags & ~FLAG_LOW_PRIO);
	}
	
	public void setSuspended(boolean s) {
		flags = s ? (flags | FLAG_SUSPENDED) : (flags & ~FLAG_SUSPENDED);
	}
	
	public void setFinishDate(Calendar finishDate) {
		this.finishDate = finishDate;
	}
	
	public void setCreateDate(Calendar createDate) {
		this.createDate = createDate;
	}
	
	public void setLastModDate(Calendar lastModDate) {
		this.lastModDate = lastModDate;
	}
	
	public void setLines(String[] lines) {
		this.lines = Objects.requireNonNull(lines, "no null value allowed");
	}
	
	public void setTitle(String title) {
		this.title = Objects.requireNonNull(title, "no null title allowed");
	}
	
	public boolean isFinished() {
		return (flags & FLAG_FINISHED) != 0;
	}
	
	public boolean isHighPriorised() {
		return (flags & FLAG_HIGH_PRIO) != 0;
	}
	
	public boolean isLowPriorised() {
		return (flags & FLAG_LOW_PRIO) != 0;
	}
	
	public boolean isSuspended() {
		return (flags & FLAG_SUSPENDED) != 0;
	}
	
	public Calendar getFinishDate() {
		return finishDate == null ? null : (Calendar) finishDate.clone();
	}
	
	public Calendar getCreateDate() {
		return createDate == null ? null :  (Calendar) createDate.clone();
	}
	
	public Calendar getLastModDate() {
		return lastModDate == null ? null : (Calendar) lastModDate.clone();
	}
	
	public String[] getLines() {
		return lines.clone();
	}
	
	public String getTitle() {
		return title;
	}
	
	public Map <Memory, String> getLinks() {
		return Collections.unmodifiableMap(links);
	}
	
	public String putLink(Memory mem, String discription) {
		return links.put(mem, discription);
	}
	
	public String removeLink(Memory mem) {
		return links.remove(mem);
	}
	
	public void removeAllLinks() {
		links.clear();
	}
	
}
