package midi.zec.ctl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.ShortMessage;

import midi.zec.ctl.MyMidiMessage.MessageType;

public class MidiSender {
	
	private static MidiSender instance;
	private static boolean log = false;

	private final MidiControl midiControl;
	private Map<Integer, SenderWorker> workers;
	private Map<Integer, Thread> threads;

	private MidiSender() {
		midiControl = MidiControl.get();
		workers = new HashMap<>();
		threads = new HashMap<>();
	}
	
	public static MidiSender get() {
		if (instance == null) {
			instance = new MidiSender();
		}
		return instance;
	}
	
	public void play(MyMidiMessage message) {
		SenderWorker w = getWorker(message.channel);
		try {
			w.push(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private SenderWorker getWorker(int channel) {
		SenderWorker worker = workers.getOrDefault(channel, null);
		if (worker == null) {
			worker = new SenderWorker(midiControl, channel);
			workers.put(channel, worker);
			Thread thread = new Thread(worker);
			threads.put(channel, thread);
			thread.start();
		}
		return worker;
	}
	
	public void close() {
		prt("Finish join started");
		waitForPlayEnding();
		threads = null;
		workers = null;
		midiControl.close();
	}
	
	public void finishSong() {
		workers.values().stream().forEach(w -> {
			try {
				w.push(new MyMidiMessage());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
	}
	
	public void waitForPlayEnding() {
		threads.values().stream().forEach(t -> {
			try {
				t.join();
				prt("Finish a thread stopped");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
	}
	
	private static void prt(String str) {
		if (log) System.out.println(str);
	}
	
	//--------------------------------------------------------------------
	private static class SenderWorker implements Runnable {
		
		private final MidiControl midiControl;
//		private int bpm = 60;
		public final int channel;
		private final BlockingQueue<MyMidiMessage> queue = new LinkedBlockingQueue<>();
		
		private SenderWorker(MidiControl midiControl, int channel) {
			this.midiControl = midiControl;
			this.channel = channel;
		}
		
//		private void setBpm(int bpm) {
//			this.bpm = bpm;
//		}
		
		private void push(MyMidiMessage message) throws InterruptedException {
			queue.put(message);
		}

		@Override
		public void run() {
			try {
				while (true) {
					// Waits until an element is available
					MyMidiMessage message = queue.take();
					prt("Item taken " + channel + message);
					if (message.messageType == MessageType.FINISH) {
						prt("Finish started " + channel);
						break;
					} else if (message instanceof MyMidiMessageNote m && message.messageType == MessageType.PAUSE) {
						Thread.sleep(m.noteDuration);
					} else if (message instanceof MyMidiMessageCtl m && message.messageType == MessageType.SUSTAIN) {
						setSustain(channel, m.on);
					} else if (message instanceof MyMidiMessageInstrument m && message.messageType == MessageType.INSTRUMENT) {
						setInstrument(m.low, m.high, m.instrument, m.channel);
					} else if (message instanceof MyMidiMessageNote m) {
						prt(m.toString());
						playChord(m.notes, channel, m.volume, m.noteDuration);
					} else {
						prt("Message has not been understand: " + message);
					}
				}
				prt("Finish loop ended " + channel);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				System.out.println("Worker interrupted, shutting down.");
			} catch (InvalidMidiDataException e) {
				System.out.println("Invalid MIDI data");
				e.printStackTrace();
			} catch (MidiUnavailableException e) {
				System.out.println("Midi unavailable");
				e.printStackTrace();
			}
		}
		
		private void playChord(int[] noteMidiValues, int channel, int volume, int duration) throws InvalidMidiDataException, MidiUnavailableException, InterruptedException {
			System.out.println("Play chord");
			for (int note : noteMidiValues) {
				ShortMessage on = new ShortMessage();
				on.setMessage(ShortMessage.NOTE_ON, channel, note, volume);
				prt(String.format("Note %d ch %d dur %d on%n", note, channel, duration));
				midiControl.getReceiver().send(on, -1);
			}
			prt("Sleepig " + channel + " " + duration);
			Thread.sleep(duration);
			for (int note : noteMidiValues) {
				ShortMessage off = new ShortMessage();
				off.setMessage(ShortMessage.NOTE_OFF, channel, note, 0);
				prt(String.format("Note %d ch %d off%n", note, channel));
				midiControl.getReceiver().send(off, -1);
			}
		}

		private void setSustain(int channel, boolean on) throws InvalidMidiDataException, MidiUnavailableException {
			prt(String.format("Sustain set to %b in ch %d%n", on, channel));
			 ShortMessage sustain = new ShortMessage();
		        sustain.setMessage(ShortMessage.CONTROL_CHANGE, channel, 64, on ? 127 : 0);
		        midiControl.getReceiver().send(sustain, -1);
		}

		private void setInstrument(int low, int high, int instrument, int channel) throws InvalidMidiDataException, MidiUnavailableException, InterruptedException {
			System.out.format("Instrument set to %d %d %d in ch %d%n", low, high, instrument, channel);
			prt(String.format("Instrument set to %d %d %d in ch %d%n", low, high, instrument, channel));
  	 		// 2. Send Bank Select MSB (Control Change 0)
            ShortMessage msb = new ShortMessage();
            msb.setMessage(ShortMessage.CONTROL_CHANGE, channel, 0, high);
	        midiControl.getReceiver().send(msb, -1);

            // 3. Send Bank Select LSB (Control Change 32)
            ShortMessage lsb = new ShortMessage();
            lsb.setMessage(ShortMessage.CONTROL_CHANGE, channel, 32, low); 
	        midiControl.getReceiver().send(msb, -1);

            // 4. Send Program Change
            ShortMessage pc = new ShortMessage();
            pc.setMessage(ShortMessage.PROGRAM_CHANGE, channel, instrument, 0);
	        midiControl.getReceiver().send(pc, -1);
		}
	}
	
}
