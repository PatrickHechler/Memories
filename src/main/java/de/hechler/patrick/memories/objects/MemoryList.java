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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import de.hechler.patrick.memories.exceptions.ActException;
import de.hechler.patrick.zeugs.NumberConvert;
import de.hechler.patrick.zeugs.objects.QuedReaderable;

public class MemoryList implements Runnable {
	
	private volatile boolean      saved;
	private volatile boolean      exit;
	private volatile PrintStream  out;
	private final QuedReaderable  reader;
	private final Scanner         in;
	private volatile Set <Memory> memories;
	
	
	
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
		this.saved = true;
		this.exit = false;
		this.out = out;
		this.reader = new QuedReaderable(0);
		this.in = new Scanner(this.reader);
		this.memories = new TreeSet <>();
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
		String str = this.in.next();
		try {
			switch (str.toLowerCase()) {
			case "exit":
				if ( !this.saved) {
					this.out.println("are you sure?");
					this.out.println("you have unsaved changes.");
					this.out.println("I have set the saved flag,");
					this.out.println("so you can repeat 'exit' to exit");
					this.out.println("if you do not want to exit");
					this.out.println("you can type any other command");
					this.out.println("(which will be executed)");
					this.saved = true;
					act();
					return;
				}
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
			case "lines":
				lines();
				break;
			case "finish":
				finish(true);
				break;
			case "unfinish":
				finish(false);
				break;
			case "help":
				help();
				break;
			default: {
				final String msg = "unknown command: '" + str + "'";
				this.out.println(msg);
				throw new Exception(msg);
			}
			}
		} catch (Exception | AssertionError e) {
			this.out.println("there was an exception: " + e.getClass());
			this.out.println("  msg: " + e.getMessage());
			throw new ActException(e);
		}
	}
	
	private void finish(boolean finish) {
		String dateStr = this.in.next();
		boolean date = "date".equalsIgnoreCase(dateStr);
		int index = date ? this.in.nextInt() : Integer.parseInt(dateStr);
		Iterator <Memory> iter = this.memories.iterator();
		Memory mem = readMemFromIndex(iter, index);
		String title = this.in.next();
		if ( !title.equals(mem.getTitle())) {
			String msg = "wron title: assertet: " + title + " memt-title: " + mem.getTitle();
			this.out.println(msg);
			throw new ActException(msg);
		}
		boolean mod = finish != mem.isFinished();
		mem.setFinished(finish);
		if (date) {
			Calendar cal = mem.getFinishDate();
			mem.setFinishDate(finish ? Calendar.getInstance() : null);
			mod |= Objects.equals(cal, mem.getCreateDate());
		}
		if (mod) {
			this.memories = new TreeSet <>(this.memories);
			this.saved = false;
		}
	}
	
	private void lines() {
		String mode = in.next();
		Memory mem = readMemFromIndex();
		boolean mod = false;
		switch (mode.toLowerCase()) {
		default:
			throw new ActException("unknown lies command: '" + mode + "'");
		case "set":
			mod = mem.getLines().length != 0;
			mem.setLines(new String[0]);
		case "add":
			String[] oldLines = mem.getLines();
			List <String> addLinesList = new ArrayList <>();
			String line = this.in.nextLine();
			if (line.isEmpty()) line = this.in.nextLine();
			for (; !line.isEmpty(); line = this.in.nextLine()) {
				addLinesList.add(line);
			}
			String[] addLines = addLinesList.toArray(new String[addLinesList.size()]);
			String[] newLines = new String[oldLines.length + addLines.length];
			System.arraycopy(oldLines, 0, newLines, 0, oldLines.length);
			System.arraycopy(addLines, 0, newLines, oldLines.length, addLines.length);
			mem.setLines(newLines);
			mod |= addLines.length != 0;
			if (mod) {
				mem.setLastModDate(Calendar.getInstance());
				this.memories = new TreeSet <>(this.memories);
				this.saved = false;
			}
		}
	}
	
	private Memory readMemFromIndex() {
		return readMemFromIndex(this.memories.iterator(), this.in.nextInt());
	}
	
	private Memory readMemFromIndex(Iterator <Memory> iter, int index) {
		Memory mem = null;
		for (; index >= 0; index -- ) {
			if ( !iter.hasNext()) {
				this.getOut().println("I do not have so much elements");
				throw new ActException("I do not have so much elements");
			}
			mem = iter.next();
		}
		if (mem == null) {
			this.getOut().println("illegal index: " + index);
			throw new ActException("illegal index: " + index);
		}
		return mem;
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
		this.out.println("finish [date] <NUMBER> <TITLE>");
		this.out.println("  to finish the Memory at the index");
		this.out.println("  if the memory at the index does not have title the operation will fail");
		this.out.println("  if date is set the date will also be savd");
		this.out.println("unfinish [date] <NUMBER> <TITLE>");
		this.out.println("  to unfinish the Memory at the index");
		this.out.println("  if the memory at the index does not have title the operation will fail");
		this.out.println("  if date is set the date will also be removed (if set)");
		this.out.println("add <TITLE>");
		this.out.println("<LINES>");
		this.out.println("<EMPTY_LINE>");
		this.out.println("  to add a new memory with the given title");
		this.out.println("  and the given lines");
		this.out.println("lines <add | set> <NUMBER>");
		this.out.println("<LINES>");
		this.out.println("<EMPTY_LINE>");
		this.out.println("  to add/set the value-lines of the memory with the given title");
		this.out.println("print");
		this.out.println("  to show all memory titles with there index");
		this.out.println("exit");
		this.out.println("  to stop running");
	}
	
	private void show() {
		Memory mem = readMemFromIndex();
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
			this.out.println(prefix + calGetTos(cal, Calendar.DAY_OF_MONTH, 0, 2) + '.' + calGetTos(cal, Calendar.MONTH, 1, 2) + '.' + calGetTos(cal, Calendar.YEAR, 0, 4) + '-'
				+ calGetTos(cal, Calendar.HOUR_OF_DAY, 0, 2) + ':' + calGetTos(cal, Calendar.MINUTE, 0, 2) + ':' + calGetTos(cal, Calendar.SECOND, 0, 2) + ':'
				+ calGetTos(cal, Calendar.MILLISECOND, 0, 3));
		}
	}
	
	private String calGetTos(Calendar cal, int index, int add, int len) {
		int value = cal.get(index) + add;
		StringBuilder b = new StringBuilder().append(value);
		char[] chars = new char[len - b.length()];
		Arrays.fill(chars, '0');
		return b.append(chars).toString();
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
		this.saved = true;
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
		this.saved = true;
	}
	
	private void print() {
		int i = 0;
		for (Iterator <Memory> iter = this.memories.iterator(); iter.hasNext(); i ++ ) {
			Memory mem = iter.next();
			Calendar cd = mem.getCreateDate();
			if (cd == null) {
				this.out.println("[" + i + "]: title: " + mem.getTitle());
			} else {
				printDate("[" + i + "]: title: " + mem.getTitle() + " created: ", cd);
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
		this.saved = false;
	}
	
	private void remove() {
		final int index = this.in.nextInt();
		String title = this.in.next();
		Iterator <Memory> iter = this.memories.iterator();
		Memory mem = readMemFromIndex(iter, index);
		if ( !mem.getTitle().equals(title)) {
			this.out.println("the memory at the index " + index + " has not the title " + title);
			throw new AssertionError("the memory at the index " + index + " has not the title " + title);
		}
		iter.remove();
		this.saved = false;
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
