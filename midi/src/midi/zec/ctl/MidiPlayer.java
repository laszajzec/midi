package midi.zec.ctl;

import java.util.ArrayList;
import java.util.List;

public abstract class MidiPlayer {

	protected final MidiSender sender;
	protected List<MyMidiMessage> collectedNotes = new ArrayList<>();
	
	protected MidiPlayer(MidiSender sender) {
		this.sender = sender;
	}

	/**
	 * @see <a href="https://en.wikipedia.org/wiki/Chord_(music)">Wikipedia</a>
	 */
	protected ChordGaps getChordGaps(String chordSymbName) {
		if (chordSymbName.startsWith("Xsus4"))		return new ChordGaps(5, new int[]{0, 5, 7});		// sus 4
		else if (chordSymbName.startsWith("Xsus2"))	return new ChordGaps(5, new int[]{0, 2, 7});		// sus2
		else if (chordSymbName.startsWith("Xo7"))	return new ChordGaps(3, new int[]{0, 3, 6, 9});		// mol dim 7
		else if (chordSymbName.startsWith("Xo"))	return new ChordGaps(2, new int[]{0, 3, 6});		// mol dim
		else if (chordSymbName.startsWith("XmM7"))	return new ChordGaps(4, new int[]{0, 3, 7, 11});	// mol mol dur 7
		else if (chordSymbName.startsWith("Xm7"))	return new ChordGaps(3, new int[]{0, 3, 7, 10});	// mol 7
		else if (chordSymbName.startsWith("Xm6"))	return new ChordGaps(3, new int[]{0, 3, 7, 9});		// mol 6
		else if (chordSymbName.startsWith("Xm"))	return new ChordGaps(2, new int[]{0, 3, 7});		// mol base chord
		else if (chordSymbName.startsWith("XM7"))	return new ChordGaps(3, new int[]{0,4, 7, 11 });	// dur 7
		else if (chordSymbName.startsWith("X7"))	return new ChordGaps(2, new int[]{0, 4, 7, 10});	// dominant 7
		else if (chordSymbName.startsWith("X6"))	return new ChordGaps(2, new int[]{0, 4, 7, 9});		// dur 6
		else if (chordSymbName.startsWith("X07"))	return new ChordGaps(3, new int[]{0, 3, 6, 10});	// mol 07
		else if (chordSymbName.startsWith("X+7"))	return new ChordGaps(3, new int[]{0, 4, 8, 10});	// aug 7
		else if (chordSymbName.startsWith("X+"))	return new ChordGaps(2, new int[]{0, 4, 8}); 		// aug 
		else if (chordSymbName.startsWith("X"))		return new ChordGaps(1, new int[]{0, 4, 7});		// dur base chord
		else {
			System.out.println("Chord not recognized: " + chordSymbName);
			return new ChordGaps(0, new int[]{0, 4, 7});
		}
	}
	
	protected record ChordGaps(int stringLength, int[] gaps) {};
}
