package experiments;

import java.util.ArrayList;

import retriever.InvertedIndex;

import java.io.PrintWriter;
import java.util.*;

public class GenerateTerms {
    String writeFile = "queryterms.txt";
    public void generateQuery(int numTerms, boolean isCompressed) {
        try {
            InvertedIndex index = new InvertedIndex(isCompressed);
            index.loadAll();
            Set<String> vocab =index.getVocabulary();
            ArrayList<String> words =new ArrayList<String>();
            words.addAll(vocab);

            PrintWriter queryWriter =new PrintWriter(writeFile,"UTF-8");

            Random rand = new Random(System.currentTimeMillis());
            for(int i=0;i<100;i++) {
                Set<Integer>indexes = new HashSet<Integer>();
                while(indexes.size()<numTerms) {
                    int idx = rand.nextInt(words.size()-1);
                    indexes.add(idx);
                }
                String result =" ";
                for(int idx:indexes) {
                    result+=words.get(idx);
                    result+=" ";
                }
                result = result.trim();
                queryWriter.println(result);
            }
            queryWriter.close();
        }catch(Exception e) {
        }
    }
}
