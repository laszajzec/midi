package midi.zec.test;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;

public class TestMixed {

	public static void main(String[] args) {
		new TestMixed().test();

	}
	
	public void test() {
		listMidi();
	}
	
	
	private void listMidi() {
		MidiDevice d;
		MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
		for (int i = 0; i < infos.length; i++) {
			try {
				d = MidiSystem.getMidiDevice(infos[i]);
				System.out.format("%s i: %s r: %d t: %d Pos: %d%n", d.getClass().getName(), d.getDeviceInfo().toString(), d.getMaxReceivers(), d.getMaxTransmitters(), d.getMicrosecondPosition());
				if (d instanceof Synthesizer s) {
					printInstruments("  Loaded ", s.getLoadedInstruments());
					printInstruments("  Available ", s.getAvailableInstruments());
					Soundbank defSB = s.getDefaultSoundbank();
					System.out.format("    Soundbank %s %s%n", defSB.getName(), defSB.getVendor());
				}
			} catch (MidiUnavailableException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void printInstruments(String prefix, Instrument[] instrs) {
		StringBuilder b = new StringBuilder();
		int count = 0;
		for (Instrument instr : instrs) {
			b.append(String.format("%12s", instr.getName()));
			count++;
			if (count >= 10) {
				System.out.format("%s %s%n", prefix, b.toString());
				b = new StringBuilder();
				count = 0;
			}
		}
		if (b.length() > 0) {
			System.out.format("%s %s%n", prefix, b.toString());
		}
		
	}

}
