package midi.zec;

import javax.sound.midi.*;

public class MidiOutTest {

	public static void main(String[] args) throws Exception {

		MidiDevice yamaha = null;

		for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
			MidiDevice device = MidiSystem.getMidiDevice(info);
			if (info.getName().toUpperCase().contains("USB MIDI INTERFACE")) {
				yamaha = device;
				break;
			}
		}

		if (yamaha == null) {
			System.out.println("Yamaha MIDI device not found.");
			return;
		}
		for (int i = 22; i < 30; i++) {
			System.out.println("Playing instrument " + i);
			play(yamaha, i);
		}
	}
        
        private static void play(MidiDevice yamaha, int instrument) throws MidiUnavailableException, InvalidMidiDataException, InterruptedException {
            yamaha.open();
            Receiver receiver = yamaha.getReceiver();

            int channel = 0; // MIDI channel 1
            int velocity = 70;

            int[] notes = {60, 62, 64};

            ShortMessage programChange = new ShortMessage();
            programChange.setMessage(ShortMessage.PROGRAM_CHANGE, channel, instrument, 0);
            receiver.send(programChange, -1);

            Thread.sleep(200); // let synth switch instrument
            
            for (int note : notes) {
                ShortMessage on = new ShortMessage();
                on.setMessage(ShortMessage.NOTE_ON, channel, note, velocity);
                receiver.send(on, -1);

                Thread.sleep(500);

                ShortMessage off = new ShortMessage();
                off.setMessage(ShortMessage.NOTE_OFF, channel, note, 0);
                receiver.send(off, -1);

                Thread.sleep(200);
            }

            receiver.close();
            yamaha.close();

        }

}
