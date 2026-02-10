package midi.zec.musictheory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Notes {
	
	//													   0    1     2    3     4    5    6     7    8     9    10    11
	public static final String[] NOTE_NAME = new String[]{"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

	public static enum NotesEnum {
		C("C", null, 60),		// 0
		Db("C#", "Db", 61),		// 1
		D("D", null, 62),		// 2
		Eb("D#", "Eb", 63),		// 3
		E("E", null, 64),		// 4
		F("F", null, 65),		// 5
		Gb("F#", "Gb", 66),		// 6
		G("G", null, 67),		// 7
		Ab("G#", "Ab", 68),		// 8
		A("A", null, 69),		// 9
		Bb("A#", "Bb", 70),		// 10
		B("B", null, 71);		// 11

		private final String nameUp;
		private final String nameDown;
		private final int midiValue;
		private static final int maxLen = 5;
		
		private NotesEnum(String up, String down, int midiValue) {
			this.nameUp = up;
			this.nameDown = down == null ? up : down;
			this.midiValue = midiValue;
		}
		public String getNameUp() {
			return nameUp;
		}
		public String getNameDown() {
			return nameDown;
		}
		public String getName() {
			if (nameUp.equals(nameDown)) {
				return center(nameUp);
			} else {
				return center(nameUp + "/" + nameDown);
			}
		}
		
		public int midiValue() {
			return midiValue;
		}

		public int midiValue(int octave) {
			return midiValue + 12 * (octave - 4);
		}

		private String center(String s) {
			StringBuilder str = new StringBuilder();
			int before = (maxLen - s.length()) / 2;
			for (int i = 0; i < before; i++) str.append(' ');
			str.append(s);
			int after = maxLen - before - s.length();
			for (int i = 0; i < after; i++) str.append(' ');
			return str.toString();
		}
	}
	
	public static enum NoteLength {
		FULL, HALF, QUARTER, EIGTHTS, SIXTEENTH
	}
	
	private static final Map<String, NotesEnum> notesDict = new HashMap<>();
	static {
		for (NotesEnum aNote : NotesEnum.values()) {
			notesDict.put(aNote.getNameUp().toUpperCase(), aNote);
			if (aNote.getNameDown() != null) {
				notesDict.put(aNote.getNameDown().toUpperCase(), aNote);
			}
		}
	}
	
	public static NotesEnum get(int i) {
		int index = i % NotesEnum.values().length;
		return NotesEnum.values()[index < 0 ? index + NotesEnum.values().length : index];
	}
	
	public static List<NotesEnum> getScale(NotesEnum baseNote, int[] steps) {
		List<NotesEnum> scale = new ArrayList<>();
		int startIndex = baseNote.ordinal();
		for (int step : steps) {
			scale.add(get(startIndex + step));
		}
		return scale;
	}
	
	public static NotesEnum get(String code) {
		return notesDict.getOrDefault(code.toUpperCase(), null);
	}
	
	public static int midiValue(NotesEnum baseNote, int displacement, int octave) {
		int basePitch = baseNote.midiValue(octave);
		return basePitch + displacement;
	}
	

}
