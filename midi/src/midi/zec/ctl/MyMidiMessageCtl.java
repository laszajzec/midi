package midi.zec.ctl;

public class MyMidiMessageCtl extends MyMidiMessage {

	public final boolean on;
	public final int data;
	
	// sustain
	public MyMidiMessageCtl(boolean sustainOn, int channel) {
		super(MessageType.SUSTAIN, channel);
		this.on = sustainOn;
		this.data = 0;
	}

	// Instrument
	public MyMidiMessageCtl(int instrument, int channel) {
		super(MessageType.INSTRUMENT, channel);
		this.on = false;
		this.data = instrument;
	}

	@Override
	public String toString() {
		return String.format("M %s data %d on %b", messageType, data, on);
	}
}
