package midi.zec.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
		for (int startNoteIndex = 0; startNoteIndex < 12; startNoteIndex++) {
			List<Integer> chord = new ArrayList<>();
			List<String> noteNames = new ArrayList<>();
			for (int chordTypeIndex = 0; chordTypeIndex < indices.length; chordTypeIndex++) {
				int chordIndex = indices[chordTypeIndex];
				chord.add((startNoteIndex + chordIndex) % 12);
				noteNames.add(MusicScala.getNoteName(chord.get(chordTypeIndex)));
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
	
	List<String> rawChordNames = Arrays.asList(new String[] {
			"", "XM", "XM7", "X7-", "X7", "X6", "X07", "X+7", "X+",
			"", "Xm", "Xm6", "Xm7", "XmM7", "Xo7", "Xo", "Xsus4", "Xsus2"});
	
	private void composeChords() {
		List<String> heading = new ArrayList<>();
		List<String> gaps = new ArrayList<>();
		List<List<String>> chords = new ArrayList<>();
		List<List<String>> leftHandChords = new ArrayList<>();
		if (rawChordNames.size() != MusicScala.getChordNames().size() + 2) {
			System.out.println(" -- Chord name list not correct! --");
		}
		for (String chordName : rawChordNames) {
			if (chordName.isEmpty()) {
				heading.add("Base");
				gaps.add("");
			} else {
				heading.add(chordName);
				gaps.add(Arrays.stream(MusicScala.getChordIndices(chordName)).boxed().map(i -> Integer.toString(i)).collect(Collectors.joining(" ")));
			}
		}
		for (int baseNoteIndex = 0; baseNoteIndex < 12; baseNoteIndex++) {
			composeChordsForBaseNote(baseNoteIndex, rawChordNames, chords, leftHandChords);
		}
		// print result
		System.out.println(heading.stream().collect(Collectors.joining(";")));
		System.out.println(gaps.stream().collect(Collectors.joining(";")));
		for (int i = 0; i < chords.size(); i++) {
			List<String> chord = chords.get(i);
			List<String> leftChord = leftHandChords.get(i);
			System.out.println(chord.stream().collect(Collectors.joining(";")));
			System.out.println(leftChord.stream().collect(Collectors.joining(";")));
//			System.out.println(";");
		}
	}
	
	private void composeChordsForBaseNote(int baseNoteIndex, List<String> rawChordNames, List<List<String>> chords, List<List<String>> leftHandChords) {
		List<String> chordsForBase = new ArrayList<>();
		List<String> bestLeftHand = new ArrayList<>();
		for (String chordName : rawChordNames) {
			if (chordName.isEmpty()) {
				chordsForBase.add(MusicScala.getNoteName(baseNoteIndex));
				bestLeftHand.add("");
			} else {
				chordsForBase.add(composeAChord(baseNoteIndex, MusicScala.getChordIndices(chordName)));
				bestLeftHand.add(findBestLeftHand(baseNoteIndex, MusicScala.getChordIndices(chordName)));
			}
		}
		chords.add(chordsForBase);
		leftHandChords.add(bestLeftHand);
	}
	
	private String composeAChord(int baseNoteIndex, int[] indices) {
		List<String> notes = new ArrayList<>();
		for (int ind : indices) {
			notes.add(MusicScala.getNoteName(baseNoteIndex + ind));
		}
		return notes.stream().collect(Collectors.joining("-"));
	}
	
	private String findBestLeftHand(int baseNoteIndex, int[] indices) {
		// Lowest note near to "G" and minimal width
		List<Integer> myIndices = new ArrayList<>(Arrays.stream(indices).boxed().toList());
		List<Integer> bestRotation = null;
		int bestValue = Integer.MAX_VALUE; 
		for (int i = 0; i < indices.length; i++) {
			int currentValue = Math.abs(baseNoteIndex + myIndices.get(0) - 7); // Distance to "G"
			if (currentValue < bestValue) {
				bestValue = currentValue;
				bestRotation = new ArrayList<Integer>(myIndices);
			}
			Collections.rotate(myIndices, 1);
		}
		return bestRotation.stream().map(ind -> MusicScala.getNoteName(baseNoteIndex+ind)).collect(Collectors.joining("-"));
	}
	
	private int mod(int orig) {
		return Math.floorMod(orig,  12);
//		if (orig < 0) return orig + 12;
//		if (orig > 11) return orig - 12;
//		return orig;
	}
}
