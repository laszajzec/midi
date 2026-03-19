package midi.zec.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import midi.zec.musictheory.MusicScala;

public class ChordPattern {

	public static void main(String[] args) {
		ChordPattern pattern = new ChordPattern();
//		pattern.generate();
		pattern.composeChords();
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
				noteNames.add(MusicScala.getNoteName(chord.get(i)));
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
	
	private void composeChords() {
		List<String> heading = new ArrayList<>();
		List<List<String>> chords = new ArrayList<>();
		Set<String> rawChordNames = new TreeSet<String>(MusicScala.getChordNames());
		heading.add("Base");
		for (String chordName : rawChordNames) {
			heading.add(chordName);
		}
		for (int baseNoteIndex = 0; baseNoteIndex < 12; baseNoteIndex++) {
			composeChordsForBaseNote(baseNoteIndex, rawChordNames, chords);
		}
		// print result
		System.out.println(heading.stream().collect(Collectors.joining(";")));
		for (List<String> chord : chords) {
			System.out.println(chord.stream().collect(Collectors.joining(";")));
			System.out.println(";");
		}
	}
	
	private void composeChordsForBaseNote(int baseNoteIndex, Set<String> rawChordNames, List<List<String>> chords) {
		List<String> chordsForBase = new ArrayList<>();
		chordsForBase.add(MusicScala.getNoteName(baseNoteIndex));
		for (String chordName : rawChordNames) {
			chordsForBase.add(composeAChord(baseNoteIndex, MusicScala.getChordIndices(chordName)));
		}
		chords.add(chordsForBase);
	}
	
	private String composeAChord(int baseNoteIndex, int[] indices) {
		List<String> notes = new ArrayList<>();
		for (int ind : indices) {
			notes.add(MusicScala.getNoteName(baseNoteIndex + ind));
		}
		return notes.stream().collect(Collectors.joining("-"));
	}
	
	private int mod(int orig) {
		return Math.floorMod(orig,  12);
//		if (orig < 0) return orig + 12;
//		if (orig > 11) return orig - 12;
//		return orig;
	}
}
