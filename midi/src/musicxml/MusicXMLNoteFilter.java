package musicxml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MusicXMLNoteFilter {

    public static void main(String[] args) {
    	MusicXMLNoteFilter obj = new MusicXMLNoteFilter();
    	obj.collect();
    }
    
    private List<Notes> notes;
    private Notes current;
    
    public void collect() {
    	notes = new ArrayList<>();
    	
        try {
            File inputFile = new File("c:/Temp/Kotta/MusicXML/jeux-interdits.xml"); // Pfad zur MusicXML-Datei

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);

            doc.getDocumentElement().normalize();

            NodeList noteList = doc.getElementsByTagName("note");
            int noteCount = 0;

            for (int i = 0; i < noteList.getLength(); i++) {
                Node node = noteList.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element noteElement = (Element) node;

                    // Pausen überspringen
                    if (noteElement.getElementsByTagName("rest").getLength() > 0) {
                        continue;
                    }

                    // Pitch
                    Element pitch = (Element) noteElement.getElementsByTagName("pitch").item(0);
                    String step = pitch.getElementsByTagName("step").item(0).getTextContent();
                    String octave = pitch.getElementsByTagName("octave").item(0).getTextContent();

                    String alter = "";
                    if (pitch.getElementsByTagName("alter").getLength() > 0) {
                        String altVal = pitch.getElementsByTagName("alter").item(0).getTextContent();
                        if (altVal.equals("1")) alter = "#";
                        else if (altVal.equals("-1")) alter = "b";
                    }

                    // Dauer lesen
                    String duration = "unbekannt";
                    if (noteElement.getElementsByTagName("duration").getLength() > 0) {
                        duration = noteElement.getElementsByTagName("duration").item(0).getTextContent();
                    }
                    
                    //ZL
                    boolean toIgnore = false;
                    String staff = ((Element) noteElement.getElementsByTagName("staff").item(0)).getTextContent();
                    if (!"1".equals(staff)) toIgnore = true;
                    
                    Element beam = (Element)noteElement.getElementsByTagName("beam").item(0);
                    if (beam == null) {
                		current = new Notes();
                		notes.add(current);
                    } else {
                    	String beamPos = beam.getAttribute("number");
                    	String beamCont = beam.getTextContent();
                    	if ("begin".equals(beamCont)) {
                    		current = new Notes();
                    		notes.add(current);
                    	} else {
                    		toIgnore = true;
                    	}
                    }
                    
                    MyNote myNote = new MyNote(step + alter, octave, duration);
                    if (current == null) {
                		current = new Notes();
                		notes.add(current);
                    }
                    current.add(myNote);
                    
                    if (!toIgnore) {
//                    	System.out.println("Note: " + step + alter + octave + " | Dauer: " + duration);
                    	String noteString = step + alter + octave;
                    	System.out.format(" %-3s", noteString);
                    	noteCount++;
                    	if (noteCount % 30 == 0) System.out.println("");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    	System.out.println("");
    }
    
    private record MyNote(String name, String octave, String duration) {};
    
    private static class Notes {
    	List<MyNote> notes = new ArrayList<>();
    	
    	public void add(MyNote note) {
    		notes.add(note);
    	}
    };
}