package retriever;


public class lookupData {
        /***
         *
         * Class stores information about the loopuptable once loaded.
         */

        public long offset;
        public int numBytes;
        public int documentCount;
        public int termCount;

        public lookupData(long offsetInput, int numBytesInput, int docCount, int termCountInput){
            offset = offsetInput;
            numBytes = numBytesInput;
            documentCount = docCount;
            termCount = termCountInput;
        }
    }
