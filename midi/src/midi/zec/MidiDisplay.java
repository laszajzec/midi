package midi.zec;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;

/**
 * Use this program to discover MIDI devices on your system.
 * 
 * @author Knute Snortum
 * @version 2017/06/17
 */
public class MidiDisplay {

	public static void main(String[] args) {
		new MidiDisplay().run();
	}

	private void run() {
		MidiDevice.Info[] deviceInfo = MidiSystem.getMidiDeviceInfo();
		if (deviceInfo.length == 0) {
			System.out.println("No MIDI devices found");
			return;
		}

		int count = 1;
		
		for (MidiDevice.Info info : deviceInfo) {

			try {
				MidiDevice device = MidiSystem.getMidiDevice(info);
				System.out.format("%n%2d %s (%s) %s%n", count, info.getDescription(), info.getName(), device.getClass().getSimpleName());
				System.out.format("  Vendor: %s Version: %s%n", info.getVendor(), info.getVersion());
				count++;
				printDeviceType(device);
				System.out.format("  Max receivers %s transmitters %s%n", maxToString(device.getMaxReceivers()), maxToString(device.getMaxTransmitters()));
			} catch (MidiUnavailableException e) {
				System.out.println("Can't get MIDI device");
				e.printStackTrace();
			}
		}
	}

	private void printDeviceType(MidiDevice device) {
		if (device instanceof Sequencer) {
			System.out.println("  This is a sequencer");
		} else if (device instanceof Synthesizer) {
			System.out.println("  This is a synthesizer");
		} else if (device instanceof Receiver) {
			System.out.println("  This is a receiver");
		} else if (device instanceof Receiver) {
			System.out.println("  This is a receiver");
		} else if (device instanceof Transmitter) {
			System.out.println("  This is a transmitter");
		} else {
			System.out.print("  This is a MIDI port " + device.getClass().getSimpleName());
			if (device.getMaxReceivers() != 0) {
				if (device.getMaxTransmitters() != 0) {
					System.out.print(" INOUT ");
				} else {
					System.out.print(" IN ");
				}
			} 
			if (device.getMaxTransmitters() != 0) {
				System.out.print(" OUT ");
			} 
			System.out.println();
		}
	}

	private String maxToString(int max) {
		return max == -1 ? new String(String.valueOf(Character.toString('\u221e'))) : String.valueOf(max);
	}
}