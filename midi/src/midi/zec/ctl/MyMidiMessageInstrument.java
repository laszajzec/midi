package midi.zec.ctl;

public class MyMidiMessageInstrument extends MyMidiMessage {
	
	int low;
	int high;
	int instrument;

	// Instrument
	public MyMidiMessageInstrument(int low, int high, int instrument, int channel) {
		super(MessageType.INSTRUMENT, channel);
		this.low = low;
		this.high = high;
		this.instrument = instrument;
	}
	
}
