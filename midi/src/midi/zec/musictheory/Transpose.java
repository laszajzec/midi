package midi.zec.musictheory;

import java.util.ArrayList;
import java.util.List;

public class Transpose {
	
//	private String origSong = "	g g g g f e d c c e g c5 c5 c5 ";
	private String origSong = "	a a a a g f f e d d f a d5 d5 d5 d5 c5 c5 a# a# a g";
	private String origSignature = "";
	private int transpositionDisplacement = 16; 
	private List<Note> origNotes;
	List<Note> corrected;
	List<Note> transposed;
	int i;

	public static void main(String[] args) {
		Transpose obj = new Transpose();
		obj.transposeArgs (args);
//		obj.test();
	}
	
	private void transposeArgs(String[] args) {
		if (args.length >= 1) {
			origSong = args[0];
		}
		if (args.length >= 2) {
			origSignature = args[1];
		}
		if (args.length >= 3) {
			transpositionDisplacement = Integer.parseInt(args[2]);
		}
		transpose();
	}

	private void transpose() {
		origNotes = new ArrayList<>();
		i = 0;
		while (i < origSong.length()) {
			if (!discardSpaces()) {
				origNotes.add(readNote());
			}
		}
//		System.out.print("Orig       ");
//		System.out.println(origSong);
		System.out.print("Orig notes ");
		printNotes(origNotes);
		System.out.print("\nCorrected  ");
		corrected = correctBySignature();
		printNotes(corrected);
		System.out.print("\nTransposed ");
		transposed = transposeByDisplacement(corrected);
		printNotes(transposed);
	}

	private boolean discardSpaces() {
		boolean wasSpace = false;
		while (i < origSong.length() && Character.isWhitespace(origSong.charAt(i))) {
			i++;
			wasSpace = true;
		}
		return wasSpace;
	}
	
	private String readModifier() {
		if (i < origSong.length()) {
			if ('#' == origSong.charAt(i)) {
				i++;
				return "#";
			} else if ('b' == origSong.charAt(i)) {
				i++;
				return("b");
			}
		}
		return "";
	}
	
	private int readInt() {
		int octave = 0;
		while (i < origSong.length() && Character.isDigit(origSong.charAt(i))) {
			octave = octave * 10 + (origSong.charAt(i) - '0');
			i++;
		}
		return octave == 0 ? 4 : octave;
	}
	
	private int findNote(String name) {
		int i = 0;
		while (i < MusicScala.noteName.length) {
			if (MusicScala.noteName[i].equals(name)) {
				return i;
			}
			i++;
		}
		return -1;
	}
	
	private Note readNote() {
		char noteName = Character.toUpperCase(origSong.charAt(i));
		i++;
		String modifier = readModifier();
		int octave = readInt();
		String fullNoteName = noteName + modifier; 
		int noteIndex = findNote(fullNoteName);
		return new Note(noteIndex, octave);
	}
	
	private List<Integer> toPitch(List<String> noteNames) {
		List<Integer> indices = new ArrayList<>();
		for (String noteName : noteNames) {
			indices.add(findNote(noteName));
		}
		return indices;
	}

	List<Integer> decreaseFlat  = toPitch(List.of("B", "E", "A", "D", "F", "G", "C"));
	List<Integer> increaseSharp = toPitch(List.of("C", "D", "E", "F", "G", "A", "B"));
	private List<Note> correctBySignature() {
		if (origSignature.isEmpty()) return origNotes;
		int shift = 0;
		int count = Character.isDigit(origSignature.charAt(0)) ? Integer.parseInt(origSignature.substring(0, origSignature.length() - 1)) : 1;
		List<Integer> toCorrect = null;
		if ('b' == origSignature.charAt(origSignature.length() - 1)) {
			shift = -1;
			toCorrect = decreaseFlat;
		} else if ('#' == origSignature.charAt(origSignature.length() - 1)) {
			shift = 1;
			toCorrect = increaseSharp;
		} else {
			System.out.println("Illegal signiture " + origSignature);
		}
		List<Note> corrected = new ArrayList<>();
		for (Note note : origNotes) {
			int ind = toCorrect.indexOf(note.pitch);
			if (ind >= 0 && ind < count) {
				corrected.add(Note.create(note.pitch + shift, note.octave));
			} else {
				corrected.add(note);
			}
		}
		return corrected;
	}

	private List<Note> transposeByDisplacement(List<Note> toTranspose) {
		List<Note> transposed = new ArrayList<>();
		for (Note note : toTranspose) {
			transposed.add(Note.create(note.pitch + transpositionDisplacement, note.octave));
		}
		return transposed;
	}

	private void printNotes(List<Note> transposed) {
		int pos = 0;
		for (Note note : transposed) {
			System.out.print(note.toString(origSignature));
			System.out.print(" ");
			pos ++;
			if (pos > 40) {
				System.out.println("");
				pos = 0;
			}
		}
	}
	
	private void test() {
		origSong = "c c# d d# e f f# g g# a a# b";
		transpositionDisplacement = 0;
		for (String sign : List.of("b" , "#")) {
			for (int i = 0; i <= 7; i++) {
				origSignature = i == 0 ? "" : "" + i + sign;
				System.out.println("\n\n-- " + origSignature);
				transpose();
				System.out.println("\nDiff     ");
				for (int j = 0; j < origNotes.size(); j++) {
					if (corrected.get(j).pitch != origNotes.get(j).pitch) {
						System.out.print(" " + transposed.get(j).toString(origSignature));
					}
				}
			}
		}
	}
	
	record Note(int pitch, int octave) {
		public static Note create(int pitch, int octave) {
			while (pitch >= MusicScala.noteName.length) {
				pitch -= MusicScala.noteName.length;
				octave++;
			}
			while (pitch < 0) {
				pitch += MusicScala.noteName.length;
				octave--;
			}
			return new Note(pitch, octave);
		}
		
		public String toString(String signature) {
			StringBuilder out = new StringBuilder();
			String[] noteName = signature.isEmpty() || signature.endsWith("#") ? MusicScala.noteName : MusicScala.noteNameDown;
			out.append(noteName[pitch]);
			if (octave != 4) out.append("" + octave);
			return out.toString();
		}
	};
}
