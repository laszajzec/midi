package midi.zec.ctl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

import midi.zec.musictheory.MusicScala;
import midi.zec.musictheory.Notes;

public class MidiTakeFromKeyboard {
	
	private Semaphore sem = new Semaphore(1);
	
	public static void main(String[] args) throws Exception {
		new MidiTakeFromKeyboard().receive();
	}
	
	public void receive() throws MidiUnavailableException, InterruptedException {
		MidiControl.midiDevices().stream().forEach(System.out::println);
		System.out.println("Starte MIDI Listener...");
		MidiControl control = MidiControl.get("USB MIDI Interface", "MidiInDevice");
		Transmitter transmitter = control.getTransmitter();
		transmitter.setReceiver(new MidiInputReceiver(this));
		System.out.println("Warte auf MIDI-Daten...");
		sem.acquire();
		sem.acquire();
		transmitter.close();
		control.close();;
		System.out.println("Finished...");
	}
	
	private void finish() {
		sem.release();
	}
	
	static class MidiInputReceiver implements Receiver {

		private static final long chordTimeLimit = 200;
		private final MidiTakeFromKeyboard parent;
		private long start = System.currentTimeMillis();
		private long last = start;
		private final List<NoteGot> collectedNotes = new ArrayList<>();
		
		public MidiInputReceiver(MidiTakeFromKeyboard parent) {
			this.parent = parent;
		}
		
		@Override
		public void send(MidiMessage message, long timeStamp) {
			if (message instanceof ShortMessage sm) {
				int cmd = sm.getCommand();
				int note = sm.getData1();
				int velocity = sm.getData2();
				int channel = sm.getChannel();
				long current = System.currentTimeMillis();
				long time = current - start;

				if (cmd == ShortMessage.NOTE_ON && velocity > 0) {
					long step = current - last;
					last = current;
					System.out.format("%7d NOTE ON   | Ch %d | Note %s (%d) | Vel %3d | Step %d%n", time, channel, Notes.NOTE_NAME[note % 12], note, velocity, step);
					newNote(new NoteGot(note, time));
				} else if (cmd == ShortMessage.NOTE_OFF || (cmd == ShortMessage.NOTE_ON && velocity == 0)) {
//					System.out.format("%7d NOTE OFF  | Ch %d | Note %d%n", time, channel, note, velocity);
					if (note == 24 || note == 36) {
						System.out.println("Finishing...");
						parent.finish();
					}
				} else if (cmd == 240) {
					// ignore
				} else {
					System.out.format("%7d ? %d  | Ch %d | %d | %d | %n", time, cmd, channel, note, velocity);
				}
			}
		}
		
		private void newNote(NoteGot note) {
			System.out.format("add note %d (%d)%n", note.pitch, collectedNotes.size());
			if (collectedNotes.isEmpty()) {
				collectedNotes.add(note);
			} else if ((note.time - collectedNotes.get(0).time) < chordTimeLimit) {
				collectedNotes.add(note);
			} else {
				collectedNotes.clear();
				collectedNotes.add(note);
			}
			if (collectedNotes.size() > 2) {
				printChordName();
				collectedNotes.clear();
			}
		}
		
		private void printChordName() {
			List<Integer> chordNotes = collectedNotes.stream().map(x -> x.pitch).toList();
			String toTest = chordNotes.stream().map(x -> Notes.NOTE_NAME[x % 12]).collect(Collectors.joining("-", "[", "]"));
			String chordName1 = MusicScala.getChordName(chordNotes);
			String chordName2 = MusicScala.getChordName2(chordNotes);
			System.out.format("  -- chord %s --> %s (%s)%n", toTest, chordName1, chordName2);
		}

		@Override
		public void close() {
		}
	}
	
	private record NoteGot(int pitch, long time) {};
}
