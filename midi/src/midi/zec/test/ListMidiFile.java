package midi.zec.test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;

public class ListMidiFile {

	private static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
	private final List<List<MidiEvent>> channels = new ArrayList<>(16);
	private Map<String, MidiEvent> openNotes = new HashMap<>();
	private List<NoteMessage> channelNotes;
	private double tempoMilli = 512.82;

	public static void main(String[] args) throws InvalidMidiDataException, IOException, MidiUnavailableException {
		new ListMidiFile(args);
	}
	
	public ListMidiFile(String[] args) throws InvalidMidiDataException, IOException, MidiUnavailableException {
		String filePath = args.length > 0 ? args[0] : "c:/Temp/Kotta/MIDI/DEEP PURPLE.Child in time.mid";
		for (int i = 0; i < 16; i++) channels.add(new ArrayList<>());
		Sequence s = loadFile(filePath);
		System.out.format("File %s opened...%n", filePath);
		Track[] tracks = s.getTracks();
		System.out.format("Num of tracks: %d%n", tracks.length);
		for (Track track : tracks) {
			System.out.println("-- Track");
			listTrack(track);
		}
		listChannels();
//		play(s);
	}
	
	private Sequence loadFile(String f) throws InvalidMidiDataException, IOException {
			return MidiSystem.getSequence(new File(f));
	}

	private void listTrack(Track t) {
		for (int i = 0; i < t.size(); i++) {
			MidiEvent e = t.get(i);
			MidiMessage message = e.getMessage();
			if (message instanceof ShortMessage sm) {
				channels.get(sm.getChannel()).add(e);
				System.out.print("Channel: " + sm.getChannel() + " ");
				if (sm.getCommand() == ShortMessage.NOTE_ON) {
					int key = sm.getData1();
					int octave = (key / 12)-1;
					int note = key % 12;
					String noteName = NOTE_NAMES[note];
					int velocity = sm.getData2();
					System.out.println("Note on, " + noteName + octave + " key=" + key + " velocity: " + velocity);
				} else if (sm.getCommand() == ShortMessage.NOTE_OFF) {
					int key = sm.getData1();
					int octave = (key / 12)-1;
					int note = key % 12;
					String noteName = NOTE_NAMES[note];
					int velocity = sm.getData2();
					System.out.println("Note off, " + noteName + octave + " key=" + key + " velocity: " + velocity);
				} else if (sm.getCommand() == ShortMessage.CONTROL_CHANGE) {
					int data1 = sm.getData1();
					int data2 = sm.getData2();
					if (data1 == 121 && data2 == 0)  {
						System.out.println("Control change, reset");
					} else {
						System.out.println("Control change, 1= " + data1 + " 2= " + data2);
					}
				} else if (sm.getCommand() == ShortMessage.PITCH_BEND) {
					int data1 = sm.getData1();
					int data2 = sm.getData2();
					System.out.println("Pich bend, 1= " + data1 + " 2= " + data2);
				} else {
					System.out.println("Command:" + sm.getCommand());
				}
			} else if (message instanceof MetaMessage mm) {
				int type = mm.getType();
				byte[] data = mm.getData();
				String dataAsString = Arrays.toString(data);
				String asText = switch (type) {
				case 0x00 -> "Sequence Number (Defines sequence order) ?";
				case 0x01 -> {
					dataAsString = new String(data, StandardCharsets.UTF_8);
					yield "Text Event (General purpose text)";}
				case 0x02 -> {
					dataAsString = new String(data, StandardCharsets.UTF_8);
					yield "Copyright Notice (Copyright information)";
					}
				case 0x03 -> {
					dataAsString = new String(data, StandardCharsets.UTF_8);
					yield "Sequence/Track Name (Name of the track)";
					}
				case 0x04 -> "Instrument Name (Name of the instrument) ?";
				case 0x05 -> "Lyric (Lyrics for karaoke) ?";
				case 0x06 -> "Marker (Marker text) ?";
				case 0x07 -> "Cue Point (Cue point text) ?";
				case 0x20 -> "MIDI Channel Prefix " + new String(data, StandardCharsets.UTF_8);
				case 0x21 -> "MIDI Port (Specifies a MIDI port) ?";
				case 0x2F -> "End of Track (Required to mark the end)";
				case 0x51 -> {
					int tempo = (data[0] & 0xff) << 16 | (data[1] & 0xff) << 8 | (data[2] & 0xff);
					int bpm = 60000000 / tempo;
					tempoMilli = tempo / 1000;
					dataAsString = String.format("(tempo  %d, BPM %d)", tempo, bpm);
					yield "Set Tempo (Microseconds per beat)";}
				case 0x54 -> "SMPTE Offset (Timing offset) ?";
				case 0x58 -> {
					dataAsString = String.format("(Numerator %d, denominator %d, clock/tick %d, 32nd/beat %d)", data[0], data[1], data[2], data[3]);
					yield "Time Signature ";
				}
				case 0x59 -> "Key Signature (Key signature details) ?";
				case 0x7F -> "Sequencer-Specific (Custom info) ?";
				default -> "Unknown meata ?";
				};
				System.out.format("Meta: %s %s%n", asText, dataAsString);
			} else if (message instanceof SysexMessage se) {
				byte[] data = se.getData();
				byte[] msg = se.getMessage();
				System.out.format("Sysex message %s (%s)%n", Arrays.toString(data), Arrays.toString(msg));
			} else {
				System.out.println("Message type not recognozed: " + message.getClass().getName());
			}
		}
	}
	
	private void listChannels() {
//		for (int i = 0; i < channels.size(); i++) {
//			listChannel(i, channels.get(i));
//		}
		listChannel(1, channels.get(1));
	}
	
	private void listChannel(int channel, List<MidiEvent> messages) {
		if (messages.isEmpty()) return;
		channelNotes = new ArrayList<>();
		System.out.format("%n-- Channel %d:  %6d%n", channel, messages.size());
		for (int i = 0; i < messages.size(); i++) {
			String msg = decodeMessage(messages.get(i));
			System.out.println(msg);
			compressMessage(messages.get(i));
		}
		listChannelNotes();
	}
	
	private void compressMessage(MidiEvent event) {
		if (event.getMessage() instanceof ShortMessage sm) {
			if (sm.getCommand() == ShortMessage.NOTE_ON) {
				MidiEvent oldEvent = openNotes.put(getNoteName(sm), event);
				if (oldEvent != null) {
					System.out.println("Wrong sequence of ON note " + getNoteName(sm));
				}
			} else if (sm.getCommand() == ShortMessage.NOTE_OFF) {
				String noteName = getNoteName(sm);
				MidiEvent onNote = openNotes.get(noteName);
				if (onNote != null) {
					ShortMessage smOn = (ShortMessage)onNote.getMessage();
					NoteMessage m = new NoteMessage(noteName, onNote.getTick(), smOn.getData2(), (int)(event.getTick() - onNote.getTick()));
					channelNotes.add(m);
					openNotes.remove(noteName);
				} else {
					System.out.println("Wrong sequence of OFF note " + getNoteName(sm));
				}
			}
		}
	}
	
	private void listChannelNotes() {
		for (NoteMessage m : channelNotes) {
			double duration = m.duration / tempoMilli; 
			System.out.format("+++ %3s %6d %3d %s%n", m.name, m.startTime, m.duration(), formatDuration(duration));
		}
	}
	
	private final double[] noteUnits = new double[]{1.0/2.0, 3.0/4.0, 1.0/4.0, 3.0/8.0, 1.0/8.0, 3.0/16.0, 1.0/16.0, 3.0/32.0, 1.0/32.0};
	private final String[] noteSign = new String[] { "2",     "2.",     "4",     "4.",    "8",     "8.",     "16",     "16.",     "32"};
	private String nearestDuration(double dur) {
		double minDiff = Double.MAX_VALUE;
		int indexMin = -1;
		for (int i = 0; i < noteUnits.length; i++) {
			if (Math.abs(noteUnits[i] - dur) < minDiff) {
				minDiff = Math.abs(noteUnits[i] - dur);
				indexMin = i;
			}
		}
		return noteSign[indexMin];
	}
	
	private String formatDuration(double dur) {
		if (dur > 0.85) return String.format("%.0f", dur);
		return nearestDuration(dur);
	}
	
	private String decodeMessage(MidiEvent event) {
		int command;
		MidiMessage message = event.getMessage();
		long time = event.getTick();
		if (message instanceof ShortMessage sm) {
			command = sm.getCommand();
			return switch (command) {
			case ShortMessage.NOTE_ON -> decodeNote(sm, time);
			case ShortMessage.NOTE_OFF -> decodeNote(sm, time);
			default -> "CONTROL";
			};
		} else {
			return "IGNORED";
		}
	}
	
	private String decodeNote(ShortMessage sm, long time) {
		String onOff = sm.getCommand() == ShortMessage.NOTE_ON ? "on " : "off";
		int key = sm.getData1();
		int octave = (key / 12)-1;
		int note = key % 12;
		String noteName = NOTE_NAMES[note];
		int velocity = sm.getData2();
		return String.format("Note %s %s%d (%d, %3d) %6d", onOff, noteName, octave, key, velocity, time);
	}
	
	private String getNoteName(ShortMessage sm) {
		int key = sm.getData1();
		int note = key % 12;
		int octave = (key / 12)-1;
		String noteName = NOTE_NAMES[note];
		return noteName + octave;
	}

	private void play(Sequence s) throws MidiUnavailableException, InvalidMidiDataException {
		Sequencer sequencer;
		// Get default sequencer.
		sequencer = MidiSystem.getSequencer(); 
		if (sequencer == null) {
			System.out.println("No sequencer found");
		} else {
		    // Acquire resources and make operational.
		    sequencer.open();
		}
		sequencer.setSequence(s);
		sequencer.start();
	}
	
	record NoteMessage(
			String name,
			long startTime,
			int volume,
			int duration
			) {};
}
