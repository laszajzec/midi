package midi.zec.test;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;

public class TestMixed {

	private List<MidiDevice> devices;
	
	public static void main(String[] args) {
		new TestMixed().test();
	}
	
	public void test() {
		listMidi();
		playSong();
	}
	
	private void listMidi() {
		devices = new ArrayList<>();
		MidiDevice device;
		MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
		for (int i = 0; i < infos.length; i++) {
			try {
				device = MidiSystem.getMidiDevice(infos[i]);
				devices.add(device);
				System.out.format("%2d %s i: %s r: %d t: %d Pos: %d%n", i, device.getClass().getName(), device.getDeviceInfo().toString(), device.getMaxReceivers(), device.getMaxTransmitters(), device.getMicrosecondPosition());
				if (device instanceof Synthesizer s) {
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
		System.out.format("%s %d%n", prefix, instrs.length);
		StringBuilder b = new StringBuilder();
		int count = 0;
		for (Instrument instr : instrs) {
			b.append(String.format(" (%3d) %14s", count, instr.getName()));
			count++;
			if (count % 10 == 9) {
				System.out.format("%s%n", b.toString());
				b = new StringBuilder();
			}
		}
		if (b.length() > 0) {
			System.out.format("%s%n", b.toString());
		}
		
	}

	private void playSong() {
		try {
			Synthesizer synth = MidiSystem.getSynthesizer();
			synth.open();
			Soundbank soundbank = synth.getDefaultSoundbank();
			Instrument[] instruments = soundbank.getInstruments();
			synth.loadAllInstruments(soundbank);
			MidiChannel[] channels = synth.getChannels();
			MidiChannel channel = channels[0];

			for (int i = 0; i < 235; i++) {
				System.out.format("%d %s%n", i, instruments[i].getName());
				selectInstrument(channel, i);
				playNotes(channel);
			}
			synth.close();
		} catch (MidiUnavailableException e) {
			e.printStackTrace();
		}
	}
	
	private void selectInstrument(MidiChannel channel, int instrNumb) {
		channel.programChange(instrNumb);
	}
	
	private void playNotes(MidiChannel channel) {
		int velocity = 80;
		int duration = 250;
        int C4 = 60;
        int F4 = 65;
        int A4 = 69;

		playANote(channel, C4, velocity, duration);
		playANote(channel, F4, velocity, duration);
		playANote(channel, A4, velocity, duration);
	}
	
	private void playANote(MidiChannel channel, int pitch, int velocity, int duration) {
        channel.noteOn(pitch, velocity);
        try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        channel.noteOff(pitch);
	}
}
