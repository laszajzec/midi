package midi.zec.test;

import javax.sound.midi.*;
import java.util.*;

public class ChatGPTChordDetector implements Receiver  {

	private Set<Integer> activeNotes = new HashSet<>();
    private static final long CHORD_WINDOW_MS = 80;
    private long firstNoteTime = -1;

    public static void main(String[] args) throws Exception {
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();

        for (MidiDevice.Info info : infos) {
            MidiDevice device = MidiSystem.getMidiDevice(info);
            if (device.getMaxTransmitters() != 0) {
                device.open();
                Transmitter transmitter = device.getTransmitter();
                transmitter.setReceiver(new ChatGPTChordDetector());
                System.out.println("Verbunden mit: " + info.getName());
                break;
            }
        }
    }

    @Override
    public void send(MidiMessage message, long timeStamp) {
    	 if (message instanceof ShortMessage) {
    	        ShortMessage sm = (ShortMessage) message;

    	        if (sm.getCommand() == ShortMessage.NOTE_ON && sm.getData2() > 0) {

    	            if (firstNoteTime == -1)
    	                firstNoteTime = System.currentTimeMillis();

    	            activeNotes.add(sm.getData1());

    	            if (System.currentTimeMillis() - firstNoteTime > CHORD_WINDOW_MS) {
    	                detectChord();
    	                firstNoteTime = -1;
    	            }
    	        }
    	 }
    }

    private void detectChord() {
        if (activeNotes.size() < 3)
            return;

        List<Integer> pitchClasses = new ArrayList<>();
        for (int note : activeNotes) {
            pitchClasses.add(note % 12);
        }

        Collections.sort(pitchClasses);

        for (int root : pitchClasses) {
            List<Integer> intervals = new ArrayList<>();
            for (int note : pitchClasses) {
                int interval = (note - root + 12) % 12;
                intervals.add(interval);
            }

            Collections.sort(intervals);

            String chord = matchChord(intervals);
            if (chord != null) {
                System.out.println("Erkannt: " + noteName(root) + " " + chord);
                return;
            }
        }
    }

    private String matchChord(List<Integer> intervals) {

        if (intervals.equals(Arrays.asList(0,4,7)))
            return "Dur";

        if (intervals.equals(Arrays.asList(0,3,7)))
            return "Moll";

        if (intervals.equals(Arrays.asList(0,3,6)))
            return "Vermindert";

        if (intervals.equals(Arrays.asList(0,4,8)))
            return "Übermäßig";

        if (intervals.equals(Arrays.asList(0,4,7,10)))
            return "7";

        if (intervals.equals(Arrays.asList(0,4,7,11)))
            return "Maj7";

        if (intervals.equals(Arrays.asList(0,3,7,10)))
            return "m7";

        return null;
    }

    private String noteName(int pitchClass) {
        String[] names = {"C","C#","D","D#","E","F","F#","G","G#","A","A#","B"};
        return names[pitchClass];
    }

    @Override
    public void close() {}

}
