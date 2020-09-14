package retriever;

import buildIndex.Posting;
import buildIndex.PostingList;
import utils.vByteEncodeDecode;

import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

public class InvertedIndex {

    String sceneFilename;
    String playFilename;
    String docLengthFilename;
    String lookupFilename;
    boolean compress;
    private Map<String, PostingList> invertedList = new HashMap<String, PostingList>();
    private Map<String,retriever.lookupData> lookupTable = new HashMap<String, retriever.lookupData>();
    private Map<Integer, Integer> docToLength = new HashMap<Integer, Integer>();
    private Map<Integer, String> docToscene = new HashMap<Integer, String>();
    private Map<Integer, String> docToplay = new HashMap<Integer, String>();

/*
    public InvertedIndex(String sceneIds,String playIds,String docLength,String lookup) {
        this.sceneFilename = sceneIds;
        this.playFilename = playIds;
        this.docLengthFilename = docLength;
        this.lookupFilename = lookup;
    }
*/
    public Map<String,retriever.lookupData> getLookupTable(){
        return this.lookupTable;
    }
    public InvertedIndex(boolean compress) {
        this.sceneFilename = "sceneId.txt";
        this.playFilename = "playIds.txt";
        this.docLengthFilename = "docLength.txt";
        this.lookupFilename = "lookup.txt";
        this.compress = compress;
    }

    public void loadLookupTable() throws IOException {
        try {
            List<String> allLines = Files.readAllLines(Paths.get("lookup.txt"));
            for (String line : allLines) {
                String[] data = line.split("\\s+");
                retriever.lookupData look = new retriever.lookupData(Long.parseLong(data[1]),Integer.parseInt(data[2]),Integer.parseInt(data[3]),Integer.parseInt(data[4]) );

                this.lookupTable.put(data[0],look);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadDocToLength() throws IOException {

        Path path = Paths.get(this.docLengthFilename);
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        for(String line:lines) {
            String[] rowData = line.split("\\s+");
            this.docToLength.put(Integer.parseInt(rowData[0]),Integer.parseInt(rowData[1]));
        }
    }

    public void loadDocToScene() throws IOException {

        Path path = Paths.get(this.sceneFilename);
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        for(String line:lines) {
            String[] rowData = line.split("\\s+");
            this.docToscene.put(Integer.parseInt(rowData[0]),rowData[1]);
        }
    }

    public void loadDocToPlay() throws IOException {

        Path path = Paths.get(this.playFilename);
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        for(String line:lines) {
            String[] rowData = line.split("\\s+");
            this.docToplay.put(Integer.parseInt(rowData[0]),rowData[1]);
        }
    }

    public void loadAll() throws IOException {
        loadLookupTable();
        loadDocToLength() ;
        loadDocToScene();
        loadDocToPlay();
    }

    public int getDocCount()  {
        return this.docToLength.keySet().size();
    }

    public Set<String> getVocabulary() throws IOException{
        return this.lookupTable.keySet();
    }
    public int getTermFreq(String term)  {
        return (this.lookupTable.get(term).termCount);
    }




    public PostingList retrievePosting(String term) throws IOException {

        PostingList ret = new PostingList();
        String invertedFilename = "invList";
        if(compress) invertedFilename = "invListCompressed";
        RandomAccessFile reader = new RandomAccessFile(invertedFilename, "rw");
        retriever.lookupData data = this.lookupTable.get(term);
        long offset = (long) data.offset;
        int buffLength = data.numBytes;
        byte[] buff = new byte[buffLength];
        reader.seek(offset);
        reader.read(buff,0,buffLength);
        IntBuffer intBuff = IntBuffer.allocate(buff.length);
        if(this.compress) {
            vByteEncodeDecode comp = new vByteEncodeDecode();
            comp.vByteDecode(buff,intBuff);
        }
        else {
            ByteBuffer bytes = ByteBuffer.wrap(buff);
            bytes.rewind();
            intBuff.put(bytes.asIntBuffer());
        }
        int [] rawData = new int[intBuff.position()];
        intBuff.rewind();
        intBuff.get(rawData);
        ret.fromIntegerArray(rawData);
        ret.startIteration();
        reader.close();
        return ret;
    }


    public PostingList fetchPosting(String term) throws IOException{

        PostingList postList = new PostingList();
        if(this.invertedList.containsKey(term)) {
            postList =  this.invertedList.get(term);
        }
        else {
            postList = retrievePosting(term);
        }
        this.invertedList.putIfAbsent(term, postList);
        postList.startIteration();
        return postList;
    }

    public List<Map.Entry<Integer,Double>> getQuery(String query, int k) throws IOException {
        PriorityQueue<Map.Entry<Integer, Double>> result =
                new PriorityQueue<>(Map.Entry.<Integer, Double>comparingByValue());
        String[] qTerms = query.split("\\s+");
        PostingList[] lists = new PostingList[qTerms.length];
        for (int i = 0; i < qTerms.length; i++) {
            lists[i] = fetchPosting(qTerms[i]);
        }
        for (int d = 1; d <= getDocCount(); d++) {
            Double currentScore = 0.0;
            for (PostingList postList : lists) {
                postList.skipTo(d);
                Posting post = postList.getCurrentPosting();
                if (post != null && post.getDocId() == d) {
                    currentScore += post.getTermFreq();
                }
            }
            result.add(new AbstractMap.SimpleEntry<Integer, Double>(d, currentScore));
            if (result.size() > k) {
                result.poll();
            }
        }
        ArrayList<Map.Entry<Integer, Double>> scores = new ArrayList<Map.Entry<Integer, Double>>();
        scores.addAll(result);
        scores.sort(Map.Entry.<Integer, Double>comparingByValue(Comparator.reverseOrder()));
        return scores;
    }
}
