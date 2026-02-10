package midi.zec.test;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

import midi.zec.ctl.MidiPlayerText;

public class TestScala {

	public static void main(String[] args) throws MidiUnavailableException, InvalidMidiDataException, InterruptedException {
		new TestScala().start();
	}
	
	public void start() throws MidiUnavailableException, InvalidMidiDataException, InterruptedException {
//		testAll();
//		aura_lee();
//		ode_an_die_freude();
//		parker();
//		chords();
//		begleit();
		stille_nacht();
	}
	
	private void aura_lee() throws InvalidMidiDataException, MidiUnavailableException, InterruptedException {
//		MidiSender sender = MidiSender.get();
//		sender.setInstrument(0, 75);
//		sender.setBpm(120);
//		sender.setSustain(0, true);
		String[] song1 = {"|t:120i:80v:80s:0l:4 c", "f", "e", "f", "g", "d", "gl:2", "f", "el:4", "d", "e", "fl:1"};
//		String[] song = {"?73 c4", "f4", "e4", "f4", "g4", "d4", "g2", "f4", "e4", "d4", "e4", "f1"};
		interpretString(song1);
//		interpretString(song, 140);
//		sender.setSustain(0, false);
//		interpretString(song, 120);
	}
	
	private void testAll() {
		String[] song = {
				"|t:125i:99s:0l:1",
//				"|t:125i:99s:0 p pd c d# eb fv:50>:2",
//				"|l:1 +C +F +Gm",
				"+C (C,E,G)"
		};		
		interpretString(song);
	}
	
	private void ode_an_die_freude() throws InvalidMidiDataException, MidiUnavailableException, InterruptedException {
//		sender.setInstrument(0, 53);
//		sender.setBpm(120);
		String[] song = {
				"e e f g g f e d c c d e 4", "e 4.", "d 8", "d 2",
				"e e f g g f e d c c d e 4", "d 4.", "c 8", "c 2",
				"d d e c d f e c d f e d c d g 4",
				"e e f g g f e d c c d e 4", "d 4.", "c 8", "c 2"				
		};
		interpretString(song, 120);
	}
	
	private void parker() throws InvalidMidiDataException, MidiUnavailableException, InterruptedException {
		String[] song = {
				"A4 D4 B3 A4 F#4 B3 D4 E4 C#4 D4 G#4 F#4 A4 E4 B3 E4 D4 B3 D4 A4 4",
				"a4 d4 b3 a4 a4 f#4 b3 d4 e3 c#4 d4 g#4 f#4 a4 e4 b3 e4 d4 b3 d4 a4 4"
		};
		interpretString(song, 60);
	}
	
	private void begleit() throws InvalidMidiDataException, MidiUnavailableException, InterruptedException {
//		sender.setInstrument(1, 0);
//		sender.setBpm(60);
//		sender.setSustain(1, true);
		String[] song = {
			"-C 1", "-G 1", "-B 1"	
		};
		interpretString(song, 60);
	}
	
	private void chords() throws InvalidMidiDataException, MidiUnavailableException, InterruptedException {
//		sender.setBpm(120);
//		sender.setInstrument(0, 53);
//		sender.setInstrument(1, 18);
		String[] song = {"c 4", "c e g 1", "g b d5 1"};
		interpretString(song, 120);
	}
	
	private void stille_nacht() throws InvalidMidiDataException, MidiUnavailableException, InterruptedException {
//		sender.setInstrument(0, 0);
//		sender.setInstrument(1, 18);
//		sender.setBpm(60);
//		sender.setSustain(1, true);
		String[] song = {
				"|t:80s:0l:4",
				"°112,0,19,0",
				"+Cs:1l:1",
				"(el:8.,gl:16) (a,f)l:8 (e,g) (c,e) pl:16", // 1
//				"e g 8.", "f a 16", "e g 8", "c e 4", "p 8",
//				"-G f d5 4", "f d5 8", "g b 4.",
//				"-C e c5 4", "e c5 8", "e g 4.", // 2
//				"f a 4", "f a 8", "a c5 8", "g b 16", "f a 8",
//				"e g 8.", "f a 16", "e g 8", "d e 4.",
//				"f a 4", "f a 8", "a c5 8.", "g b 16", "f a 8", // 3
//				"e g 8.", "f a 16", "e g 4", "c e 4.",
//				"b d5 8", "b d5 16", "b d5 8", "d5 f5 8.", "b d5 16", "g b 4", // 4
//				"e c5 4.", "g e5 4.",
//				"f c5 8.", "f g 16", "c e 8", "e g 8.", "d f 16", "b3 d 8", // 5
//				"c 4.", "c 4", "p 8"				
		};
		interpretString(song, 60);
	}
	
	private void interpretString(String[] song) {
		MidiPlayerText player = new MidiPlayerText();
		player.interpretText(song);
	}

	private void interpretString(String[] song, int bpm) throws InterruptedException, InvalidMidiDataException, MidiUnavailableException {
		MidiPlayerText player = new MidiPlayerText();
		player.interpretText(song);
	}
	
	
	/*
0 2 3 4# 5 6 7 Lydian
0 2 3 4 5 6 7  Ionian
9 2 3 4 5 6 7b Mixolydian
0 2 3b 4 5 6 7b Dorian
0 2 3b 4 5 6b 7b Aeolian
0 2b 3b 4 5 6b 7b Phrygian
0 2b 3b 4 5b 6b 7b Locrian 
	 */

}
