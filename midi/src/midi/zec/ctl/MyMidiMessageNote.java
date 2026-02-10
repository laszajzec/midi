package midi.zec.ctl;

import java.util.Arrays;

public class MyMidiMessageNote extends MyMidiMessage {
	public final int noteDuration; // in milliseconds
	public final int volume;
	public final int[] notes;

	// single note
	public MyMidiMessageNote(int midiPitch, int duration, int volume, int channel) {
		super(MessageType.SINGLE_NOTE, channel);
		this.notes = new int[] {midiPitch};
		this.volume = volume;
		this.noteDuration = duration;
	}

	// chord
	public MyMidiMessageNote(int[] midiPitch, int duration, int volume, int channel) {
		super(MessageType.CHORD, channel);
		this.notes = midiPitch;
		this.volume = volume;
		this.noteDuration = duration;
	}

	// pause
	public MyMidiMessageNote(int duration, int channel) {
		super(MessageType.PAUSE, channel);
		this.noteDuration = duration;
		this.volume = 0;
		this.notes = null;
	}

	@Override
	public String toString() {
		return String.format("M %s dur %d v: %d Pitch %s", messageType, noteDuration, volume, Arrays.toString(notes));
	}
}
