package experiments;


import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Duration;
import java.time.Instant;

import buildIndex.IndexBuilder;
import retriever.InvertedIndex;
public class QueryTimeCalculator {
    public void computeTime(String shortList, String longList, int k, boolean toCompress, String dataInput) {

        try {
            InvertedIndex index = new InvertedIndex(toCompress);
            index.loadAll();
            @SuppressWarnings("unused")
            List<Map.Entry<Integer,Double>> results;
            Instant start,end;
            String inputFile = shortList;
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            String query;
            while((query=reader.readLine())!=null) {
                results = index.getQuery(query,k);
            }
            reader.close();
            reader = new BufferedReader(new FileReader(inputFile));
            start = Instant.now();
            while((query=reader.readLine())!=null) {
                results = index.getQuery(query,k);
            }

            end = Instant.now();
            reader.close();
            System.out.println("seven word queries took: "+Duration.between(start,end)+" where compression is : "+toCompress);


            IndexBuilder build = new IndexBuilder();
            build.buildIndex(dataInput, toCompress);
            index = new InvertedIndex(toCompress);
            index.loadAll();


            inputFile = longList;
            reader = new BufferedReader(new FileReader(inputFile));
            while((query = reader.readLine())!=null) {
                results = index.getQuery(query,k);
            }
            reader.close();
            reader = new BufferedReader(new FileReader(inputFile));
            start = Instant.now();
            while((query=reader.readLine())!=null) {
                results = index.getQuery(query,k);
            }
            end = Instant.now();
            reader.close();
            System.out.println("fourteen word queries took "+Duration.between(start, end)+" where compression is : "+toCompress);
        }catch(Exception ex) {
        }

    }


}
