import buildIndex.IndexBuilder;
import experiments.QueryTimeCalculator;
import experiments.GenerateTerms;
import experiments.vocabChecker;
import experiments.ComputeDiceCoeff;


public class mainRun {

    public static void main(String[] args){

        try {

            String dataFile = args[0];
            boolean isCompress = Boolean.parseBoolean(args[1]);

            System.out.println("Assignment 1 : Inverted Index");

            //1. Index Build
            IndexBuilder builder = new IndexBuilder();
            builder.buildIndex(dataFile, isCompress);

            //2. Generate shorter query file
            GenerateTerms query = new GenerateTerms();
            query.generateQuery(7,isCompress);

            //3. Generate longer query file
            ComputeDiceCoeff dice = new ComputeDiceCoeff();
            dice.generateQuery("queryterms.txt",isCompress);

            //4. Measure Retrieval Time
            QueryTimeCalculator timeCalculator = new QueryTimeCalculator();
            timeCalculator.computeTime("queryterms.txt","diced-queries.txt",5,isCompress,dataFile);

            //5. Vocab Checking
            vocabChecker vocabCheck = new vocabChecker();
            if(!vocabCheck.vocabCheck(dataFile))
                System.out.println("Vocabulary match unsuccessful");
        }
        catch(Exception e){

        }

    }
}
