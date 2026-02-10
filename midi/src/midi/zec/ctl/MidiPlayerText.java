package midi.zec.ctl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import midi.zec.musictheory.Notes;
import midi.zec.musictheory.Notes.NotesEnum;

/**
 * <table>
 * <caption>Notation</caption>
 * <tr><th>control char</th><th>parameter</th><th>deswcription</th></tr>
 * <tr><td>|</td><td>[def attrs]</td><td>tact border and default values</td></tr>
 * <tr><td>p</td><td>[d:int]</td><td>pause with duration</td></tr>
 * <tr><td>[CDEFGAB]</td><td>[b#][]=int[attrs]</td><td>note</td></tr>
 * <tr><td>([CDEFGAB])</td><td>[b#][]=int[attrs]</td><td>simultaneous notes</td></tr>
 * <tr><td>+chordname</td><td>[attrs]</td><td>chord</td></tr>
 * </table>
 * 
 * <table>
 * <caption>Attributes</caption>
 * <tr><td>t:</td><td>int -> tempo</td></tr>
 * <tr><td>v:</td><td>int -> volume/speed</td></tr>
 * <tr><td>l:</td><td>int[.] length (duration)</td></tr>
 * <tr><td>i:</td><td>int -> instrument</td></tr>
 * <tr><td>s:</td><td>int -> strain (channel)</td></tr>
 * </table>
 * 
 */
public class MidiPlayerText extends MidiPlayer {
	
	private enum NoteAttribute {CHANNEL, DURATION, INSTRUMENT, OCTAVE, TEMPO, VOLUME};
	private final NoteAttributes current = new NoteAttributes();
	
	public MidiPlayerText() {
		super(MidiSender.get());
		current.set(NoteAttribute.CHANNEL, 0);
		current.set(NoteAttribute.INSTRUMENT, 73);
		current.set(NoteAttribute.OCTAVE, 4);
		current.set(NoteAttribute.TEMPO, 100);
		current.set(NoteAttribute.VOLUME, 70);
		current.set(NoteAttribute.DURATION, toMillis(4));
	}
	
	public void interpretText(String[] song) {
		for (String chord : song) {
			String[] tokens = chord.toLowerCase().split(" ");
			for (String token : tokens) {
				translateItem(token.toLowerCase());
			}
		}
		playNotes();
	}

	private void translateItem(String item) {
		System.out.println("Item: " + item);
		switch(item.charAt(0)) {
		case '|' -> decodeSeparator(item);
		case 'p' -> decodeAndPlayPause(item);
		case '(' -> decodeSimultaneous(item);
		case '+' -> decodeChord(item);
		case '°' -> decodeInstrument(item);
		default -> decodeNote(item);
		}
	}
	
	private void decodeAndPlayPause(String item) {
		NoteAttributes attrs = collectAttributes(item, 1);
		MyMidiMessage m = new MyMidiMessageNote(toMillis(attrs.get(NoteAttribute.DURATION)), current.get(NoteAttribute.CHANNEL));
		collectedNotes.add(m);
	}

	private void decodeSeparator(String item) {
		NoteAttributes attrs = collectAttributes(item, 1);
		for (NoteAttribute attr : attrs.changes) {
			current.set(attr, attrs.get(attr));
			if (attr == NoteAttribute.INSTRUMENT) {
				MyMidiMessage m = new MyMidiMessageCtl(current.get(attr), current.get(NoteAttribute.CHANNEL));
				collectedNotes.add(m);
			}
		}
	}
	
	private void decodeInstrument(String item) {
		String[] valuesStr = item.substring(1).split(",");
		if (valuesStr.length != 4) {
			System.out.println("Wong instrument " + item);
			return;
		}
		int[] values = new int[4];
		for (int i = 0; i < valuesStr.length; i++) {
			values[i] = Integer.parseInt(valuesStr[i]);
		}
		MyMidiMessageInstrument im = new MyMidiMessageInstrument(values[0], values[1], values[2], values[3]);
		collectedNotes.add(im);
	}
	
	private NoteAttributes collectAttributes(String item, int startIndex) {
		NoteAttributes attrs = new NoteAttributes();
		if (startIndex < item.length() - 1)  {
			ValueAndIndex parsed;
			while (startIndex < item.length()) {
				switch (item.substring(startIndex, startIndex + 2)) {
				case "l:": // length/duration
					parsed = parseInteger(item, startIndex + 2);
					attrs.set(NoteAttribute.DURATION, toMillis(parsed.value()));
					startIndex = parsed.newIndex();
					if (startIndex < item.length() && item.charAt(startIndex) == '.') {
						attrs.set(NoteAttribute.DURATION, attrs.get(NoteAttribute.DURATION) + attrs.get(NoteAttribute.DURATION) / 2);
						startIndex++;
					}
					break;
				case "v:": // volume
					parsed = parseInteger(item, startIndex + 2);
					attrs.set(NoteAttribute.VOLUME, parsed.value());
					startIndex = parsed.newIndex();
					break;
				case "s:": // strain/channel
					parsed = parseInteger(item, startIndex + 2);
					attrs.set(NoteAttribute.CHANNEL, parsed.value());
					startIndex = parsed.newIndex();
					break;
				case "i:": // instrument
					parsed = parseInteger(item, startIndex + 2);
					attrs.set(NoteAttribute.INSTRUMENT, parsed.value());
					startIndex = parsed.newIndex();
					break;
				case "t:": // tempo
					parsed = parseInteger(item, startIndex + 2);
					attrs.set(NoteAttribute.TEMPO, parsed.value());
					startIndex = parsed.newIndex();
					break;
				case ">:":
					parsed = parseInteger(item, startIndex + 2);
					attrs.set(NoteAttribute.OCTAVE, parsed.value());
					startIndex = parsed.newIndex();
					break;
				default:
					System.out.format("Unknown control %s in pos %d%n", item, startIndex);
					startIndex++;
					break;
				}
			}
		}
		attrs.consolidate(current);
		return attrs;
	}
	
	private void decodeSimultaneous(String item) {
		int endIndex = item.indexOf(')');
		if (endIndex < 0) {
			System.out.println("No closing parenthesis found: " + item);
			return;
		}
		NoteAttributes attrs = collectAttributes(item, endIndex + 1);
		String[] noteStrings = item.substring(1, endIndex).split(",");
		List<Integer> collectedPitch = new ArrayList<>(); 
		for (String noteString : noteStrings) {
			collectedPitch.add(getPitch(noteString));
		}
		addNotes(collectedPitch.stream().mapToInt(i->i).toArray(), attrs.get(NoteAttribute.DURATION), attrs.get(NoteAttribute.VOLUME), attrs.get(NoteAttribute.CHANNEL));
	}
	
	private int getPitch(String noteString) {
		NoteAndLength nl = getNoteEnum(noteString);
		return nl.baseNote().midiValue(nl.ovtave);
	}

	private void decodeChord(String item) {
		int startIndex = 1;
		String noteName = item.substring(1, 2).toUpperCase();
		if (item.length() > 2 && (item.charAt(2) == '#' || item.charAt(2) == 'b')) {
			noteName = noteName + item.charAt(2);
			startIndex += 1;
		}
		ChordGaps gaps = getChordGaps("X" + item.substring(startIndex));
		NoteAttributes noteAttributes = collectAttributes(item, startIndex + gaps.stringLength());
		NotesEnum baseNote = Notes.get(noteName);
		List<Integer> pitchVals = new ArrayList<>();
		for (int gap : gaps.gaps()) {
			NotesEnum chordNote = Notes.get(baseNote.ordinal() + gap);
			int correctedPitch = Notes.midiValue(chordNote, gap, noteAttributes.get(NoteAttribute.OCTAVE));
			pitchVals.add(correctedPitch);
		}
		addNotes(pitchVals.stream().mapToInt(i->i).toArray(), noteAttributes.get(NoteAttribute.DURATION), noteAttributes.get(NoteAttribute.VOLUME), noteAttributes.get(NoteAttribute.CHANNEL));
	}

	
	private ValueAndIndex parseInteger(String item, int index) {
		int val = 0;
		while (index < item.length() && Character.isDigit(item.charAt(index))) {
			val = val * 10 + item.charAt(index) - '0';
			index++;
		}
		return new ValueAndIndex(val, index);
	}
	
	private NoteAndLength getNoteEnum(String item) {
		int startIndex = 0;
		int octave = 4;
		String noteName = item.substring(0, 1);
		startIndex++;
		if (item.length() > 1 && (item.charAt(1) == '#' || item.charAt(1) == 'b')) {
			noteName = noteName + item.charAt(1);
			startIndex++;
		}
		NotesEnum baseNote = Notes.get(noteName);
		if (item.length() > startIndex && isChar(item, startIndex, ch -> Character.isDigit(ch))) {
			octave = item.charAt(startIndex) - '0';
			startIndex++;
		}
		return new NoteAndLength(baseNote, octave, startIndex);
	}
	
	private void decodeNote(String item) {
		int startIndex;
		String noteName = item.substring(0, 1).toUpperCase();
		if (item.length() > 1 && (item.charAt(1) == '#' || item.charAt(1) == 'b')) {
			noteName = noteName + item.charAt(1);
			startIndex = 2;
		} else {
			startIndex = 1;
		}
		NoteAttributes noteAttributes = collectAttributes(item, startIndex);
		NotesEnum baseNote = Notes.get(noteName);
		int pitch = baseNote.midiValue(noteAttributes.get(NoteAttribute.OCTAVE));
		addNote(pitch, noteAttributes.get(NoteAttribute.DURATION), noteAttributes.get(NoteAttribute.VOLUME), noteAttributes.get(NoteAttribute.CHANNEL));
	}

	private void addNote(int midiPitch, int duration, int volume, int channel) {
		MyMidiMessage m = new MyMidiMessageNote(midiPitch, duration, volume, channel);
		collectedNotes.add(m);
	}

	private void addNotes(int[] midiPitch, int duration, int volume, int channel) {
		MyMidiMessage m = new MyMidiMessageNote(midiPitch, duration, volume, channel);
		collectedNotes.add(m);
	}

	private void playNotes() {
		for (MyMidiMessage m : collectedNotes) {
			sender.play(m);
		}
		collectedNotes.clear();
		sender.finishSong();
		sender.close();
	}

	private boolean isChar(String item, int index, Predicate<Character> check) {
		if (index >= item.length()) return false;
		return check.test(item.charAt(index));
	}
	
	private int toMillis(int noteDuration) {
		if (noteDuration == 0) {
			System.out.println("Duration has been set to zero!");
			return 0;
		} else if (noteDuration > 0) {
			return 240_000 / current.get(NoteAttribute.TEMPO) / noteDuration;
		} else {
			return 240_000 / current.get(NoteAttribute.TEMPO) * (-noteDuration);
		}
	}


	private static class NoteAttributes {
		Map<NoteAttribute, Integer> values = new HashMap<>();
		Set<NoteAttribute> changes = new HashSet<>();
		
		private NoteAttributes set(NoteAttribute a, int value) {
			values.put(a, value);
			return this;
		}
		
		private int get(NoteAttribute a) {
			return values.get(a);
		}

		private NoteAttributes consolidate(NoteAttributes current) {
			for (NoteAttribute attr : NoteAttribute.values()) {
				if (values.containsKey(attr)) {
					if (values.get(attr) != current.get(attr)) {
						changes.add(attr);
					}
					
				} else {
					values.put(attr, current.get(attr));
				}
			}
			return this;
		}
	};

	private record ValueAndIndex(
			int value, 
			int newIndex) {};

	private record NoteAndLength(
			NotesEnum baseNote, 
			int ovtave, 
			int usedLength) {};

}


