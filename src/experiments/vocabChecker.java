package experiments;

import buildIndex.IndexBuilder;
import retriever.InvertedIndex;

import java.util.Map;
import java.util.Set;

public class vocabChecker {

    public boolean vocabCheck(String dataFile) {
        try {

            IndexBuilder builder = new IndexBuilder();
            builder.buildIndex(dataFile, false);

            InvertedIndex invIdxUncompressed = new InvertedIndex(false);
            invIdxUncompressed.loadLookupTable();
            Map<String,retriever.lookupData> lookupUncompressed = invIdxUncompressed.getLookupTable();

            builder = new IndexBuilder();
            builder.buildIndex(dataFile, true);

            InvertedIndex invIdxCompressed = new InvertedIndex(true);
            invIdxCompressed.loadLookupTable();
            Map<String,retriever.lookupData> lookupCompressed = invIdxCompressed.getLookupTable();


            for (Map.Entry<String,retriever.lookupData> entry : lookupUncompressed.entrySet()){
                String term = entry.getKey();
                int termCount = entry.getValue().termCount;
                int docCount = entry.getValue().documentCount;
                if(!lookupCompressed.containsKey(term))
                    return false;
                if(lookupCompressed.get(term).termCount != termCount)
                    return false;
                if(lookupCompressed.get(term).documentCount != docCount)
                    return false;

            }


        } catch (Exception e) {

        }
        return true;
    }



}
