package midi.zec.ctl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

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

	    private long firstNoteTime = -1;
		private final MidiTakeFromKeyboard parent;
		private final Set<Integer> activeNotes = new HashSet<>();
		
		public MidiInputReceiver(MidiTakeFromKeyboard parent) {
			this.parent = parent;
		}
		
//		@Override
//		public void sendOld(MidiMessage message, long timeStamp) {
//			if (message instanceof ShortMessage sm) {
//				int cmd = sm.getCommand();
//				int note = sm.getData1();
//				int velocity = sm.getData2();
//				int channel = sm.getChannel();
//				long current = System.currentTimeMillis();
//				long time = current - start;
//
//				if (cmd == ShortMessage.NOTE_ON && velocity > 0) {
//					long step = current - last;
//					last = current;
//					System.out.format("%7d NOTE ON   | Ch %d | Note %s (%d) | Vel %3d | Step %d%n", time, channel, Notes.NOTE_NAME[note % 12], note, velocity, step);
//					newNote(new NoteGot(note, time));
//				} else if (cmd == ShortMessage.NOTE_OFF || (cmd == ShortMessage.NOTE_ON && velocity == 0)) {
////					System.out.format("%7d NOTE OFF  | Ch %d | Note %d%n", time, channel, note, velocity);
//					if (note == 24 || note == 36) {
//						System.out.println("Finishing...");
//						parent.finish();
//					}
//				} else if (cmd == 240) {
//					// ignore
//				} else {
//					System.out.format("%7d ? %d  | Ch %d | %d | %d | %n", time, cmd, channel, note, velocity);
//				}
//			}
//		}

	    @Override
	    public void send(MidiMessage message, long timeStamp) {
	    	if (message instanceof ShortMessage) {
	    		ShortMessage sm = (ShortMessage) message;
	    		if (sm.getCommand() == ShortMessage.NOTE_ON && sm.getData2() > 0) {
//					System.out.format("%7d NOTE ON   | Ch %d | Note %s (%d) | Vel %3d%n", timeStamp, sm.getChannel(), Notes.NOTE_NAME[sm.getData1() % 12], sm.getData1(), sm.getData2());
	    			if (firstNoteTime == -1) {
	    				firstNoteTime = System.currentTimeMillis();
	    			}
	    			activeNotes.add(sm.getData1());
	    		} else if (sm.getCommand() == ShortMessage.NOTE_OFF ||
	    				(sm.getCommand() == ShortMessage.NOTE_ON && sm.getData2() == 0)) {
    				detectChord();
//					System.out.format("%7d NOTE OFF  | Ch %d | Note %s (%d)%n", timeStamp, sm.getChannel(), Notes.NOTE_NAME[sm.getData1() % 12], sm.getData1(), sm.getData2());
	    			activeNotes.remove(sm.getData1());
	    			if (sm.getData1() == 24 || sm.getData1() == 36) {
	    				System.out.println("Finishing...");
	    				parent.finish();
	    			}

	    		} else {
	    			// other MIDI command
	    		}
	    	}
	    }

	    private void detectChord() {
//            System.out.println("Detect chord: " + activeNotes.size());
	        if (activeNotes.size() < 3)
	            return;

	        List<Integer> pitchClasses = new ArrayList<>();
	        for (int note : activeNotes) {
	            pitchClasses.add(note % 12);
	        }

	        Collections.sort(pitchClasses);

	        for (int root : pitchClasses) {
	            int[] intervals = new int[pitchClasses.size()];
	            int index = 0;
	            for (int note : pitchClasses) {
	                int interval = (note - root + 12) % 12;
	                intervals[index] = interval;
	                index++;
	            }

	            Arrays.sort(intervals);

	            String chord = MusicScala.intervallsToChord(intervals);
	            if (chord != null) {
	                System.out.println("Erkannt: " + Notes.getNoteName(root) + " " + chord);
	                return;
	            }
	        }
	    }

//		private void newNote(NoteGot note) {
//			System.out.format("add note %d (%d)%n", note.pitch, collectedNotes.size());
//			if (collectedNotes.isEmpty()) {
//				collectedNotes.add(note);
//			} else if ((note.time - collectedNotes.get(0).time) < chordTimeLimit) {
//				collectedNotes.add(note);
//			} else {
//				collectedNotes.clear();
//				collectedNotes.add(note);
//			}
//			if (collectedNotes.size() > 2) {
//				printChordName();
//				collectedNotes.clear();
//			}
//		}
		
//		private void printChordName() {
//			List<Integer> chordNotes = collectedNotes.stream().map(x -> x.pitch).toList();
//			String toTest = chordNotes.stream().map(x -> Notes.NOTE_NAME[x % 12]).collect(Collectors.joining("-", "[", "]"));
//			String chordName1 = MusicScala.getChordName(chordNotes);
//			String chordName2 = MusicScala.getChordName2(chordNotes);
//			System.out.format("  -- chord %s --> %s (%s)%n", toTest, chordName1, chordName2);
//		}

		@Override
		public void close() {
		}
	}
	
//	private record NoteGot(int pitch, long time) {};
}
