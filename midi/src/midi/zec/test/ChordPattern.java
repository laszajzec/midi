package midi.zec.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import midi.zec.musictheory.MusicScala;

public class ChordPattern {

	public static void main(String[] args) {
		ChordPattern pattern = new ChordPattern();
		pattern.composeChords();
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
			int leftNote = (baseNoteIndex + myIndices.get(0)) % 12;
			int currentValue = Math.abs(leftNote - 7); // Distance to "G"
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
