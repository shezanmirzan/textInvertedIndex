package utils;


import java.nio.IntBuffer;

public class deltaEncodeDecode {
    public void deltaEncoder(Integer[] postingList){

        int i = 0;
        int docPrev = 0;
        int posPrev = 0;
        int posStart = 0;
        int docStart = postingList[i++];
        while(i<postingList.length) {
            int count = postingList[i++];
            try {
                docPrev = postingList[i+count];
                postingList[i+count] -= docStart;
                docStart = docPrev;
            } catch(ArrayIndexOutOfBoundsException e) {

            }
            posStart = postingList[i];

            for(int j = 1; j<count; j++) {
                posPrev = postingList[i+j];
                postingList[i+j] -= posStart;
                posStart = posPrev;
            }
            i+=count+1;
        }

    }

    public void deltaDecoder(IntBuffer buffer){
        int [] bufArr = buffer.array();
        int i = 0;
        while(i<buffer.position()) {
            int firstDoc = bufArr[i++];
            int count = bufArr[i++];
            try {
                bufArr[i+count] += firstDoc;
            }catch(ArrayIndexOutOfBoundsException e) {

            }
            int initPos = bufArr[i];
            int j=1;
            while(j<count) {
                bufArr[i+j] += initPos;
                initPos = bufArr[i+j];
                j++;
            }
            i+=count;
        }
    }

}
