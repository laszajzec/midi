package midi.zec.ctl;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

public class MidiControl {
	
	private static MidiControl instance;
	
	private MidiDevice midiDevice;
	private Receiver midiReceiver;
	private Transmitter midiTransmitter;
	
	/*
	 * SoftSynthesizer-Gervill
	 * RealTimeSequencer-Real Time Sequencer
	 * MidiOutDevice-Microsoft MIDI Mapper
	 * MidiOutDevice-Microsoft GS Wavetable Synth
	 * MidiOutDevice-USB MIDI Interface
	 * MidiInDevice-USB MIDI Interface
	 */
	public static List<String> midiDevices() throws MidiUnavailableException {
		List<String> devs = new ArrayList<>();
		MidiDevice testDevice = null;
		for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
			testDevice = MidiSystem.getMidiDevice(info);
			devs.add(testDevice.getClass().getSimpleName() + "-" + info.getName());
		}
		return devs;
	}
	
	private MidiControl(String devName, String className) {
		try {
			midiDevice = initDevice(devName, className);
		} catch (MidiUnavailableException e) {
			e.printStackTrace();
			midiDevice = null;
			return;
		}
		try {
			midiDevice.open();
		} catch (MidiUnavailableException e) {
			e.printStackTrace();
		}
	}
	
	public static MidiControl get() {
			return MidiControl.get("USB MIDI Interface", "MidiInDevice");
	}

	public static MidiControl get(String devInfo, String className) {
		if (instance == null) {
			instance = new MidiControl(devInfo, className);
		}
		return instance; 
	}
	
	public static void reset() {
		if (instance != null) {
			instance.resetTech();
		}
	}
	
	public  void resetTech() {
		if (midiDevice != null) {
			if (midiDevice.isOpen()) {
				midiDevice.close();
			}
		}
	}

	private MidiDevice initDevice(String devName, String className) throws MidiUnavailableException {
		MidiDevice testDevice = null;
		for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
			testDevice = MidiSystem.getMidiDevice(info);
			if (info.getName().contains(devName) && className.equals(testDevice.getClass().getSimpleName())) {
				break;
			}
		}
		if (testDevice == null) {
			System.out.println("No MIDI device found.");
		}
		return testDevice;
	}
	
	public Receiver getReceiver() throws MidiUnavailableException {
		if (midiReceiver == null) {
			midiReceiver = midiDevice.getReceiver();
		}
		return midiReceiver;
	}

	public Transmitter getTransmitter() throws MidiUnavailableException {
		if (midiTransmitter == null) {
			midiTransmitter = midiDevice.getTransmitter();
		}
		return midiTransmitter;
	}
	
	public void close() {
		if (midiReceiver != null) {
			midiReceiver.close();
		}
		if (midiTransmitter != null) {
			midiTransmitter.close();
		}
		if (midiDevice != null) {
			midiDevice.close();
		}

	}

}
