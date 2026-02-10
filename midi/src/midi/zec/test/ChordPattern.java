package midi.zec.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ChordPattern {
	//													   0    1     2    3     4    5    6     7    8     9    10    11
	private static final String[] noteName = new String[]{"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

	public static void main(String[] args) {
		new ChordPattern().generate();
	}
	
	private void intTest() {
		System.out.format("3 %d%n", Math.floorMod(3, 12));
		System.out.format("-3 %d%n", Math.floorMod(-3, 12));
	}

	private void generate() {
		intTest();

		generatePattern(new int[] {0, 4, 7}, "Major");
		generatePattern(new int[] {0, 3, 7}, "Minor");
//		for (int i = 0; i < 12; i++) {
//			int baseNoteIndex = i;
//			List<Integer> chord = new ArrayList<>();
//			List<String> noteNames = new ArrayList<>(); 
//			chord.add(baseNoteIndex);
//			chord.add((baseNoteIndex + 4) % 12);
//			chord.add((baseNoteIndex + 7) % 12);
//			noteNames.add(noteName[chord.get(0)]);
//			noteNames.add(noteName[chord.get(1)]);
//			noteNames.add(noteName[chord.get(2)]);
//			printChords(chord, noteNames);
//
//			Collections.rotate(chord, 1);
//			Collections.rotate(noteNames, 1);
//			printChords(chord, noteNames);
//
//			Collections.rotate(chord, 1);
//			Collections.rotate(noteNames, 1);
//			printChords(chord, noteNames);
//
//			System.out.println("");
//		}
		
	}
	
	private void generatePattern(int[] indices, String comment) {
		System.out.println(comment);
		for (int noteStartIndex = 0; noteStartIndex < 12; noteStartIndex++) {
			List<Integer> chord = new ArrayList<>();
			List<String> noteNames = new ArrayList<>();
			for (int i = 0; i < indices.length; i++) {
				int chordIndex = indices[i];
				chord.add((noteStartIndex + chordIndex) % 12);
				noteNames.add(noteName[chord.get(i)]);
			}
			printChords(chord, noteNames);

			Collections.rotate(chord, 1);
			Collections.rotate(noteNames, 1);
			printChords(chord, noteNames);

			Collections.rotate(chord, 1);
			Collections.rotate(noteNames, 1);
			printChords(chord, noteNames);

			System.out.println("");
		}
		
	}
	
	private void printChords(List<Integer> chord, List<String> noteNames) {
		Integer minIndex = chord.get(0);
		String indices = chord.stream().map(x -> String.format("%2d", mod(x-minIndex) % 12)).collect(Collectors.joining(","));
		String notes = noteNames.stream().map(x -> String.format("%2s", x)).collect(Collectors.joining(","));
		System.out.format(" %s (%s)", indices, notes);
	}
	
	private int mod(int orig) {
		return Math.floorMod(orig,  12);
//		if (orig < 0) return orig + 12;
//		if (orig > 11) return orig - 12;
//		return orig;
	}
}
