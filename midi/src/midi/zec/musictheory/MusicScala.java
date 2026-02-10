package midi.zec.musictheory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class MusicScala {

	// Diatonic Ionic
	public final int[] majorSteps		= {2, 2, 1, 2, 2, 2, 1}; // ionian
	public final int[] majorIndices		= toIndices(majorSteps);
	public final int[] minorSteps		= {2, 1, 2, 2, 1, 2, 2}; // aeolian
	public final int[] minorIndices		= toIndices(minorSteps);

	public final int[] dorianSteps		= {2, 1, 2, 2, 2, 1, 2};
	public final int[] dorianIndices	= toIndices(dorianSteps);
	public final int[] lydianSteps		= {2, 2, 2, 1, 2, 2, 1};
	public final int[] lydianIndices	= toIndices(lydianSteps);
	public final int[] mixolydianSteps	= {2, 2, 1, 2, 2, 1, 2};
	public final int[] mixolydianIndices= toIndices(mixolydianSteps);
	public final int[] phyrianSteps		= {1, 2, 2, 2, 1, 2, 2};
	public final int[] phyrianIndices	= toIndices(phyrianSteps);
	public final int[] locrianSteps		= {1, 2, 2, 1, 2, 2, 2};
	public final int[] locrianIndices	= toIndices(locrianSteps);
	
	public final int[] majorChord = {4, 7};
	public final int[] minorChord = {3, 7};
	public final int[] diminishedChord = {3, 6};
	public final int[] augmentedChord = {4, 8};

	private static final Map<String, int[]> chords = buildChords();
	private static final Map<String, String> chordToIndex = buildChordsFromIndex();
	private static final Map<String, String> indexToChord = buildIndexToChords();
	
	private static Map<String, int[]> buildChords() {
		Map<String, int[]> chordDict = new HashMap<>();
		chordDict.put("Xsus4",	new int[]{0, 5, 7});		// sus 4
		chordDict.put("Xsus2",	new int[]{0, 2, 7});		// sus2
		chordDict.put("Xo7",	new int[]{0, 3, 6, 9});		// mol dim 7
		chordDict.put("Xo",		new int[]{0, 3, 6});		// mol dim
		chordDict.put("XmM7",	new int[]{0, 3, 7, 11});	// mol mol dur 7
		chordDict.put("Xm7",	new int[]{0, 3, 7, 10});	// mol 7
		chordDict.put("Xm6",	new int[]{0, 3, 7, 9});		// mol 6
		chordDict.put("Xm",		new int[]{0, 3, 7});		// mol base chord
		chordDict.put("XM7",	new int[]{0, 4, 7, 11});	// dur 7
		chordDict.put("X7",		new int[]{0, 4, 7, 10});	// dominant 7
		chordDict.put("X6",		new int[]{0, 4, 7, 9});		// dur 6
		chordDict.put("X07",	new int[]{0, 3, 6, 10});	// mol 07
		chordDict.put("X+7",	new int[]{0, 4, 8, 10});	// aug 7
		chordDict.put("X+",		new int[]{0, 4, 8}); 		// aug 
		chordDict.put("X",		new int[]{0, 4, 7});		// dur base chord
		return chordDict;
	}
	
	private static Map<String, String> buildIndexToChords() {
		Map<String, String> chordDict = new HashMap<>();
		chordDict.put("4,7", "");
		chordDict.put("3,7", "m");
		return chordDict;
	}
	
	private static Map<String, String> buildChordsFromIndex() {
		Map<String, String> indexToChord = new HashMap<>();
		for (Map.Entry<String, int[]> chord : chords.entrySet()) {
			indexToChord.put(toString(chord.getValue()), chord.getKey());
		}
		return indexToChord;
	}
	
	public static String toString(int[] arr) {
		return Arrays.toString(arr);
	}
	
	public static String getChordName(List<Integer> pitch) {
		int[] pitches = pitch.stream().mapToInt(i->i).toArray();
		Arrays.sort(pitches);
		int[] notes = new int[pitches.length];
		for (int i = 0; i < notes.length; i++) { notes[i] = pitches[i] % 12; }
		Arrays.sort(notes);
		String[] noteNames = new String[notes.length];
		for (int i = 0; i < notes.length; i++) { noteNames[i] = Notes.NOTE_NAME[notes[i]]; }
		System.out.format("  -- Check chord %s %s%n", Arrays.toString(notes), Arrays.toString(noteNames));
		int minIndex = notes[0];
		for (int i = 0; i < pitch.size(); i++) { notes[i] = Math.floorMod(notes[i] - minIndex, 12); }
		String indices = Arrays.stream(notes).boxed().map(x -> Integer.toString(x)).collect(Collectors.joining(","));
		indices = indices.substring(2);
		ChordType chordType = unifiedChordIndices(indices);
		if (chordType == null) return "?";
		String chordName = indexToChord.getOrDefault(chordType.chordDescription, "??").trim();
		String baseNoteName = noteNames[chordType.indexOfBaseMNote % 12];
		int baseNoteOctave = pitches[chordType.indexOfBaseMNote] / 12;
		return String.format("%s%s%d", baseNoteName, chordName, baseNoteOctave);
	}
	
	public static String getChordName2(List<Integer> pitchesOrig) {
		ChordType chordType = null;
		List<Integer> pitches = new ArrayList<>();
		for (int p = 0; p < pitchesOrig.size(); p++) { pitches.add(pitchesOrig.get(p)); }
		pitches.sort((a, b) -> Integer.compare(a, b));
		for (int p = 0; p < pitches.size(); p++) { pitches.set(p, pitches.get(p) % 12); }
		String chordName = null;
		int i = 0;
		do {
			int[] relPos = computeRelaivePos(pitches);
			chordName = findChord(relPos);
			if (chordName != null) {
				chordType = new ChordType(chordName, i);
				break;
			}
			Collections.rotate(pitches, 1);
			i++;
		} while (i < pitches.size());
		if (chordType == null) {
			return "???";
		} else {
			int basePitch = pitchesOrig.get(chordType.indexOfBaseMNote);
			String baseNoteName = Notes.NOTE_NAME[basePitch % 12];
			int baseNoteOctave = basePitch / 12;
			return String.format("%s%s%d", baseNoteName, chordType.chordDescription.substring(1), baseNoteOctave);
		}
	}
	
	private static int[] computeRelaivePos(List<Integer> notes) {
		int[] gaps = new int[notes.size()];
		for (int i = 1; i < gaps.length; i++) {
			gaps[i] = Math.floorMod(notes.get(i) - notes.get(0), 12);
		}
		return gaps;
	}
	
	private static String findChord(int[] gaps) {
		for (Map.Entry<String, int[]> chordCandidat : chords.entrySet()) {
			if (Arrays.equals(chordCandidat.getValue(),gaps)) {
				return chordCandidat.getKey();
			}
		}
		return null;
	}
	
	
	public static int[] getChordIndices(String name) {
		return chords.getOrDefault(name, new int[]{});
	}
		
	private static ChordType unifiedChordIndices(String indices) {
		return switch (indices) {
		case "4,7" -> new ChordType("4,7", 0);
		case "5,9" -> new ChordType("4,7", 1);
		case "3,8" -> new ChordType("4,7", 2);

		case "3,7" -> new ChordType("3,7", 0);
		case "5,8" -> new ChordType("3,7", 1);
		case "4,9" -> new ChordType("3,7", 2);

		default -> null;
		};
	}
	
	public int[] toIndices(int[] steps) {
		int[] indices = new int[steps.length + 1];
		indices[0] = 0;
		for (int i = 0; i < steps.length; i++) {
			indices[i + 1] = indices[i] + steps[i]; 
		}
		return indices;
	}
	
	private record ChordType(String chordDescription, int indexOfBaseMNote) {}
}
