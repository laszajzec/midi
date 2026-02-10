package midi.zec.ctl;

public class MyMidiMessage {
	
	enum MessageType {SINGLE_NOTE, CHORD, PAUSE, SUSTAIN, INSTRUMENT, FINISH}
	
	public final MessageType messageType;
	public final int channel;
	
	protected MyMidiMessage(MessageType type, int channel) {
		this.messageType = type;
		this.channel = channel;
	}
	
	// closing
	public MyMidiMessage() {
		this.messageType = MessageType.FINISH;
		this.channel = -1;
	}

	@Override
	public String toString() {
		return String.format(" M s", messageType);
	}
}
