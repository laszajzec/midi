package midi.zec.test;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

import midi.zec.ctl.MidiSender;
import midi.zec.ctl.MyMidiMessage;
import midi.zec.ctl.MyMidiMessageInstrument;
import midi.zec.ctl.MyMidiMessageNote;

public class TestInstrumentAndScaleAndChord {
	
	private final MidiSender sender;

	public static void main(String[] args) throws MidiUnavailableException, InvalidMidiDataException, InterruptedException {
		new TestInstrumentAndScaleAndChord().start();
	}
	
	public TestInstrumentAndScaleAndChord() {
		sender = MidiSender.get();
	}
	
	public void start() throws MidiUnavailableException, InvalidMidiDataException, InterruptedException {
//		instrumentTest();
		playInstrument(112, 0, 19, 65, 2000);
//		playInstrument(0, 0, 73, 62, 2000);
		sender.finishSong();
		sender.close();

	}
	
	private void instrumentTest() {
		instrumenttest(112, 0, 16, 10);
//		instrumenttest(113, 0, 16, 10);
//		instrumenttest(114, 0, 16, 10);
	}
	
	private void instrumenttest(int low, int high, int start, int loops) {
		System.out.format("Bank %d %d%n", low, high);
		for (int i = 0; i < loops; i++) {
			int instr = start + i;
			System.out.println("Instr " + instr);
			MyMidiMessage mi = new MyMidiMessageInstrument(low, high, instr, 0);
			MyMidiMessage mn = new MyMidiMessageNote(65, 2000, 80, 0);
			sender.play(mi);
			sender.play(mn);
		}
	}

	private void playInstrument(int low, int high, int instr1, int ton1, int duration) {
		MyMidiMessage mi1 = new MyMidiMessageInstrument(low, high, instr1, 0);
		MyMidiMessage mn1 = new MyMidiMessageNote(ton1, 2000, 80, 0);
		sender.play(mi1);
		sender.play(mn1);
	}

	
}
