package utils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

public class vByteEncodeDecode {
    /***
     * Using implementation from David Fischer
     * @param postingList
     * @param byteBuffer
     */

    deltaEncodeDecode deltaEncode = new deltaEncodeDecode();

    public void VByteEncode(Integer[] postingList, ByteBuffer byteBuffer){
        deltaEncode.deltaEncoder(postingList);
        for(int i: postingList){
            while(i>=128){
                byteBuffer.put((byte)(i&0x7F));
                i>>>=7;
            }
            byteBuffer.put((byte)(i|0x80));
        }
    }

    public void vByteDecode(byte[] byteArr, IntBuffer pList){
        int i = 0;
        while(i<byteArr.length){
            int position = 0;
            int result = ((int)byteArr[i]&0x7F);
            while((byteArr[i]&0x80) == 0){
                i++;
                position++;
                int unsigned_byte = ((int)byteArr[i]&0x7F);
                result |= (unsigned_byte << (7*position));
            }
            i++;
            pList.put(result);
        }
        deltaEncode.deltaDecoder(pList);
    }

}
