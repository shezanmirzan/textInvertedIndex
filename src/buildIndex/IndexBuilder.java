package buildIndex;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import utils.vByteEncodeDecode;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;



public class IndexBuilder {
    private Map<Integer,String> docToscene;
    private Map<Integer,String> docToplay;
    private Map<String,PostingList> invertedLists;
    private Map<Integer,Integer> docTolength;

    private boolean toCompress;
    private int printFlag = 1;

    private double averageSceneLength;
    private String shortestScene;
    private String shortestPlay;
    private String longestPlay;


    public IndexBuilder() {
        docToscene = new HashMap<Integer, String>();
        docToplay = new HashMap<Integer, String>();
        invertedLists = new HashMap<String, PostingList>();
        docTolength = new HashMap<Integer, Integer>();
    }
    private void parseFile(String filename) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonObj = (JSONObject) parser.parse(new FileReader(filename));
            JSONArray scenes = (JSONArray) jsonObj.get("corpus");
            for(int idx = 0;idx<scenes.size();idx++) {
                JSONObject scene = (JSONObject) scenes.get(idx);
                int docID = idx+1;
                String sceneID = (String) scene.get("sceneId");
                docToscene.put(docID,sceneID);
                String playID = (String) scene.get("playId");
                docToplay.put(docID,playID);

                String text = (String) scene.get("text");
                String[] words = text.split("\\s+");
                docTolength.put(docID,  words.length);

                for(int pos=0;pos<words.length;pos++) {
                    String word = words[pos];
                    invertedLists.putIfAbsent(word,new PostingList());
                    invertedLists.get(word).add(docID,pos+1);
                }
            }
        }
        catch(ParseException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, Integer> buildLenMap( Map<Integer,String> map){

        Map<String,Integer> lengthMap = new HashMap<String, Integer>();
        for (Map.Entry<Integer,String> entry : map.entrySet()) {
            if(lengthMap.containsKey(entry.getValue())) {
                lengthMap.put(entry.getValue(), lengthMap.get(entry.getValue())+  this.docTolength.get(entry.getKey()));
            }
            lengthMap.putIfAbsent(entry.getValue(), this.docTolength.get(entry.getKey())+ this.docTolength.get(entry.getKey()));
        }
        return lengthMap;
    }

    private void computeStatistics(){

        //  ****** Average Scene Length ******//
        Set<String> uniqueScenes = new HashSet<String>();
        for(String scene: this.docToscene.values()){
            uniqueScenes.add(scene);
        }
        double totScenes = (double) uniqueScenes.size();
        double totSceneLen = 0;

        for (Map.Entry<Integer,Integer> entry : this.docTolength.entrySet()) {
            totSceneLen += entry.getValue();
        }
        this.averageSceneLength = (totSceneLen)/totScenes;
        //System.out.println("Average Scene Length: "+ averageSceneLength);


        // *****Shortest Scene ******* //
        Map<String,Integer>  lengthScene = buildLenMap(docToscene);
        String shortestScene = "";
        Integer lenLeast = Integer.MAX_VALUE;
        for (Map.Entry<String,Integer> entry : lengthScene.entrySet()) {
            if(entry.getValue()<lenLeast) {
                lenLeast = entry.getValue();
                shortestScene = entry.getKey();
            }
        }
        //System.out.println("Shortest Scene: "+ shortestScene);
        this.shortestScene = shortestScene;

        // *****Shortest Play ******* //
        Map<String,Integer>  lengthPlay = buildLenMap(docToplay);
        String shortestPlay = "";
        lenLeast = Integer.MAX_VALUE;
        for (Map.Entry<String,Integer> entry : lengthPlay.entrySet()) {
            if(entry.getValue()<lenLeast) {
                lenLeast = entry.getValue();
                shortestPlay = entry.getKey();
            }
        }
       //System.out.println("Shortest Play: "+ shortestPlay);
        this.shortestPlay = shortestPlay;

        // *****Longest Play ******* //
        String longestPlay = "";
        int lenMax = Integer.MIN_VALUE;
        for (Map.Entry<String,Integer> entry : lengthPlay.entrySet()) {
            if(entry.getValue()>lenMax) {
                lenMax = entry.getValue();
                longestPlay = entry.getKey();
            }
        }
        //System.out.println("Longest Play: "+ longestPlay);
        this.longestPlay = longestPlay;
    }

    private void saveStatistics(String filename){
        try{
            List<String> lines = new ArrayList<>();
            lines.add("Average Length of Scene is: "+this.averageSceneLength);
            lines.add("Shortest Scene is: "+this.shortestScene);
            lines.add("Shortest Play is: "+this.shortestPlay);
            lines.add("Longest Play is: "+this.longestPlay);

            Path file = Paths.get(filename);
            Files.write(file, lines,Charset.forName("UTF-8"));

        }catch(Exception e){
            e.printStackTrace();
        }
    }


    private void saveStringMap(String filename, Map<Integer,String> map) {
        List<String> lines = new ArrayList<>();
        map.forEach((k,v)->lines.add(k+" "+v));
        try {
            Path file = Paths.get(filename);
            Files.write(file, lines,Charset.forName("UTF-8"));
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void saveDocLengths(String filename){
        List<String> lines = new ArrayList<>();
        docTolength.forEach((k,v)->lines.add(k+" "+v));
        try {
            Path file = Paths.get(filename);
            Files.write(file, lines,Charset.forName("UTF-8"));
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void saveInvertedLists(String lookupname, String invListname) {
        long offset = 0;
        try {
            PrintWriter lookupWriter = new PrintWriter(lookupname, "UTF-8");
            RandomAccessFile invListWriter = new RandomAccessFile(invListname, "rw");
            //Compression comp = CompressionFactory.getCompressor(compression);

            for(Map.Entry<String, PostingList> entry:invertedLists.entrySet()) {
                String term = entry.getKey();
                PostingList postingList = entry.getValue();
                int docTermFreq = postingList.docCount();
                int collectionTermFreq = postingList.termFreq();
                //No compression
                Integer[] posts = postingList.toIntegerArray();
                ByteBuffer byteBuffer = ByteBuffer.allocate(posts.length*8);
                for(int p:posts)
                    byteBuffer.putInt(p);


                //For compression
                if(toCompress) {
                    byteBuffer = ByteBuffer.allocate(posts.length*8);
                    vByteEncodeDecode comp = new vByteEncodeDecode();
                    comp.VByteEncode(posts,byteBuffer);
                }


                byte [] array =  byteBuffer.array();
                invListWriter.write(array,0,byteBuffer.position());
                long bytesWritten = invListWriter.getFilePointer()-offset;
                //System.out.println();
                lookupWriter.println(term+" "+offset+" "+bytesWritten+" "+docTermFreq+" "+collectionTermFreq);
                offset = invListWriter.getFilePointer();
            }
            invListWriter.close();
            lookupWriter.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    public void buildIndex(String source, boolean compress) {
        this.toCompress = compress;
        String invFile = compress?"invListCompressed":"invList";
        parseFile(source);
        saveStringMap("sceneId.txt",docToscene);
        saveStringMap("playIds.txt",docToplay);
        saveDocLengths("docLength.txt");
        saveInvertedLists("lookup.txt",invFile);
        computeStatistics();
        saveStatistics("stats.txt");
    }

}