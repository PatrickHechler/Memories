package de.hechler.patrick.memories.objects;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import de.hechler.patrick.memories.exceptions.ActException;
import de.hechler.patrick.zeugs.NumberConvert;
import de.hechler.patrick.zeugs.objects.QuedReaderable;

public class MemoryList implements Runnable {
	
	private static final Comparator <Memory> DEFAULT_COMPARATOR = new DefaultMemoryComparator();
	
	private volatile boolean     exit;
	private PrintStream          out;
	private final QuedReaderable reader;
	private final Scanner        in;
	private Set <Memory>         memories;
	
	
	
	public MemoryList() {
		this(System.out);
	}
	
	public MemoryList(PrintStream out, char[]... input) {
		this(out);
	}
	
	public MemoryList(PrintStream out, String... input) {
		this(out);
	}
	
	public MemoryList(PrintStream out, Readable... in) {
		this(out);
	}
	
	public MemoryList(PrintStream out) {
		this.exit = false;
		this.out = out;
		this.reader = new QuedReaderable(0);
		this.in = new Scanner(this.reader);
		this.memories = new TreeSet <>(DEFAULT_COMPARATOR);
	}
	
	
	
	public boolean isExit() {
		return exit;
	}
	
	public Scanner getIn() {
		return this.in;
	}
	
	public PrintStream getOut() {
		return this.out;
	}
	
	public void setOut(PrintStream out) {
		this.out = out;
	}
	
	public void setExit(boolean exit) {
		this.exit = exit;
	}
	
	@Override
	public synchronized void run() throws ActException {
		while ( !this.exit) {
			act();
		}
	}
	
	private void act() throws ActException {
		if (this.exit) {
			return;
		}
		String str = in.next();
		try {
			switch (str.toLowerCase()) {
			case "exit":
				this.exit = true;
				return;
			case "print":
				print();
				break;
			case "add":
				add();
				break;
			case "remove":
				remove();
				break;
			case "load":
				load();
				break;
			case "save":
				save();
				break;
			case "show":
				show();
				break;
			case "help":
				help();
				break;
			default:
				final String msg = "unknown command: '" + str + "'";
				this.out.println(msg);
				throw new Exception(msg);
			}
		} catch (Exception | AssertionError e) {
			this.out.println("there was an exception: " + e.getClass());
			this.out.println("  msg: " + e.getMessage());
			throw new ActException(e);
		}
	}
	
	public void printhelp() {
		help();
	}
	
	private void help() {
		this.out.println("help");
		this.out.println("  for this message");
		this.out.println("show <NUMBER>");
		this.out.println("  to show the memory at the index");
		this.out.println("save [force] \"<FILE>\"");
		this.out.println("  to save the memories to the file");
		this.out.println("  if the file exist already force needs to be set");
		this.out.println("load [force] \"<FILE>\"");
		this.out.println("  to load the memories from the file");
		this.out.println("  if I have already memories force needs to be set");
		this.out.println("remove <NUMBER> <TITLE>");
		this.out.println("  to remove the Memory at the index");
		this.out.println("  if the memory at the index does not have title the operation will fail");
		this.out.println("add <TITLE>");
		this.out.println("<LINES>");
		this.out.println("<EMPTY_LINE>");
		this.out.println("  to add a new memory with the given title");
		this.out.println("  and the given lines");
		this.out.println("print");
		this.out.println("  to show all memor< titles with there index");
		this.out.println("exit");
		this.out.println("  to stop running");
	}
	
	private void show() {
		int index = this.in.nextInt();
		Memory mem = null;
		for (Iterator <Memory> iterator = this.memories.iterator(); index >= 0; index -- ) {
			mem = iterator.next();
		}
		if (mem == null) {
			this.out.println("no memory (either your index is negative or my map is currupt)");
			throw new NullPointerException("no memory (either your index is negative or my map is currupt)");
		}
		this.out.println("Memory: " + mem.getTitle());
		boolean flags = false;
		this.out.println("  flags:");
		if (mem.isSuspended()) {
			flags = true;
			this.out.println("    suspended");
		}
		if (mem.isHighPriorised()) {
			flags = true;
			this.out.println("    high-prio");
		}
		if (mem.isLowPriorised()) {
			flags = true;
			this.out.println("    low-prio");
		}
		if (mem.isFinished()) {
			flags = true;
			this.out.println("    finished");
		}
		if ( !flags) {
			this.out.println("    none");
		}
		printDate("  finish-date:   ", mem.getFinishDate());
		printDate("  last-mod-date: ", mem.getLastModDate());
		printDate("  creation-date: ", mem.getCreateDate());
		this.out.println("  links:");
		Map <Memory, String> links = mem.getLinks();
		if (links.isEmpty()) {
			this.out.println("    none");
		}
		for (Entry <Memory, String> entry : links.entrySet()) {
			Memory key = entry.getKey();
			String val = entry.getValue();
			this.out.println("    mem: " + key.getTitle());
			this.out.println("      discription: " + val);
		}
		this.out.println("  value:");
		for (String line : mem.getLines()) {
			this.out.println("    " + line);
		}
	}
	
	private void printDate(String prefix, Calendar cal) {
		if (cal == null) {
			this.out.println(prefix + "none set");
		} else {
			this.out.println(prefix + cal.toInstant().toString());
		}
	}
	
	private static final int SAVE_START_MAGIG = 0x8F47A51D;
	private static final int SAVE_END_MAGIG   = 0xA4BC9715;
	
	private static final String FORCE     = "force";
	private static final int    FORCE_LEN = FORCE.length();
	
	@SuppressWarnings("unchecked")
	private void load() throws FileNotFoundException, IOException, ClassNotFoundException, RuntimeException, AssertionError {
		boolean force = false;
		String file = this.in.nextLine().trim();
		while (file.isEmpty()) {
			file = this.in.nextLine().trim();
		}
		if (file.startsWith(FORCE)) {
			String newFile = file.substring(FORCE_LEN).trim();
			if (newFile.isEmpty() || (newFile.length() != file.length() - FORCE_LEN)) {
				force = true;
				file = newFile;
				while (file.isEmpty()) {
					file = this.in.nextLine().trim();
				}
			}
		}
		if (file.startsWith("\"")) {
			file = file.substring(1);
			int i = file.indexOf('"');
			if (i == -1) {
				this.out.println("illegal file-string (leading '\"', but no ending '\"' character)");
				throw new RuntimeException("illegal file-string (leading '\"', but no ending '\"' character)");
			}
			String zw = file.substring(i + 1);
			file = file.substring(0, i);
			if ( !zw.trim().isEmpty()) {
				this.reader.appendAtStart(zw);
			}
		} else {
			String zw = file;
			file = file.replaceFirst("^([^\\s]+)\\s.*$", "$1");
			zw = zw.substring(file.length()).trim();
			if ( !zw.isEmpty()) {
				this.reader.appendAtStart(zw);
			}
		}
		Path path = Paths.get(file);
		if ( !Files.exists(path)) {
			this.out.println("file does not exist");
			throw new FileNotFoundException("file does not exist");
		}
		try (ObjectInput objstr = new ObjectInputStream(new FileInputStream(path.toFile()))) {
			byte[] bytes = new byte[4];
			objstr.read(bytes);
			int start = NumberConvert.byteArrToInt(bytes);
			if (start != SAVE_START_MAGIG) {
				throw new AssertionError("save does not have the start magig magic: " + Integer.toHexString(SAVE_START_MAGIG) + " readed start: " + Integer.toHexString(start));
			}
			objstr.read(bytes);
			int len = NumberConvert.byteArrToInt(bytes);
			if (len < 0) {
				throw new AssertionError("(length < 0); length=" + len);
			}
			Object mems = objstr.readObject();
			if ( ! (mems instanceof Set)) {
				throw new ClassCastException("deseraialized save object is not a instance of Set");
			}
			Set <Memory> newMems = (Set <Memory>) mems;
			if (newMems.size() != len) {
				throw new IllegalStateException("");
			}
			objstr.read(bytes);
			int end = NumberConvert.byteArrToInt(bytes);
			if (end != SAVE_END_MAGIG) {
				throw new AssertionError("save does not have the end magig magic: " + Integer.toHexString(SAVE_END_MAGIG) + " readed end: " + Integer.toHexString(start));
			}
			if (force || this.memories.isEmpty()) {
				this.memories = newMems;
			} else {
				throw new IllegalStateException("force is not set!");
			}
		}
	}
	
	private void save() throws FileNotFoundException, IOException {
		boolean force = false;
		String file = this.in.nextLine().trim();
		while (file.isEmpty()) {
			file = this.in.nextLine().trim();
		}
		if (file.startsWith(FORCE)) {
			String newFile = file.substring(FORCE_LEN).trim();
			if (newFile.isEmpty() || (newFile.length() != file.length() - FORCE_LEN)) {
				force = true;
				file = newFile;
				while (file.isEmpty()) {
					file = this.in.nextLine().trim();
				}
			}
		}
		if (file.startsWith("\"")) {
			file = file.substring(1);
			int i = file.indexOf('"');
			if (i == -1) {
				this.out.println("illegal file-string (leading '\"', but no ending '\"' character)");
			}
			String zw = file.substring(i + 1);
			file = file.substring(0, i);
			if ( !zw.trim().isEmpty()) {
				this.reader.appendAtStart(zw);
			}
		} else {
			String zw = file;
			file = file.replaceFirst("^([^\\s]+)\\s.*$", "$1");
			zw = zw.substring(file.length()).trim();
			if ( !zw.isEmpty()) {
				this.reader.appendAtStart(zw);
			}
		}
		Path path = Paths.get(file);
		if ( !force && Files.exists(path)) {
			throw new FileAlreadyExistsException("file exist already");
		}
		try (ObjectOutput objout = new ObjectOutputStream(new FileOutputStream(path.toFile()))) {
			objout.write(NumberConvert.intToByteArr(SAVE_START_MAGIG));
			objout.write(NumberConvert.intToByteArr(this.memories.size()));
			objout.writeObject(this.memories);
			objout.write(NumberConvert.intToByteArr(SAVE_END_MAGIG));
		}
	}
	
	private void print() {
		int i = 0;
		for (Iterator<Memory> iter = this.memories.iterator(); iter.hasNext(); i ++) {
			Memory mem = iter.next();
			Calendar cd = mem.getCreateDate();
			if (cd == null) {
				this.out.println("[" + i + "]: title: " + mem.getTitle());
			} else {
				this.out.println("[" + i + "]: title: " + mem.getTitle() + "   created: " + cd.toInstant().toString());
			}
		}
	}
	
	private void add() {
		String title = this.in.next();
		String line = this.in.nextLine();
		if (line.trim().isEmpty()) {
			line = this.in.nextLine();
		} else {
			line = line.substring(1);
		}
		List <String> lines = new ArrayList <>();
		while ( !line.isEmpty()) {
			lines.add(line);
			line = this.in.nextLine();
		}
		Calendar now = Calendar.getInstance();
		Memory mem = new Memory(title, lines.toArray(new String[lines.size()]), now);
		this.memories.add(mem);
	}
	
	private void remove() {
		int index = this.in.nextInt();
		final int origI = index;
		String title = this.in.next();
		Memory mem = null;
		Iterator <Memory> iter;
		for (iter = memories.iterator(); index >= 0; index -- ) {
			mem = iter.next();
		}
		if (mem == null) {
			this.out.println("no memory (either your index is negative or my map is currupt)");
			throw new NullPointerException("no memory (either your index is negative or my map is currupt)");
		}
		if ( !mem.getTitle().equals(title)) {
			this.out.println("the memory at the index " + origI + " has not the title " + title);
			throw new AssertionError("the memory at the index " + origI + " has not the title " + title);
		}
		iter.remove();
	}
	
	public void append(char[] chars) {
		this.reader.append(chars);
	}
	
	public void append(String str) {
		this.reader.append(str);
	}
	
	public void append(Readable r) {
		this.reader.append(r);
	}
	
}
