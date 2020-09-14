/***
 * author : Shezan Rohinton Mirzan
 */

package buildIndex;

import java.util.List;
import java.util.ArrayList;

public class Posting {

    public List<Integer> positions;
    private Integer docID;

    
    public Posting(Integer docID, Integer pos) {
        this.positions = new ArrayList<Integer>();
        this.positions.add(pos);
        this.docID = docID;
    }

    public void add(Integer pos) {
        this.positions.add(pos);
    }


    public Integer[] getPositionsArray() {
        return positions.stream().toArray(Integer[]::new);
    }

    public ArrayList<Integer> toIntegerArray() {
        ArrayList<Integer> postingArr = new ArrayList<Integer>();
        postingArr.add(docID);
        postingArr.add(positions.size());
        postingArr.addAll(positions);

        return postingArr;
    }

    public Integer getTermFreq() {
        return this.positions.size();
    }

    public Integer getDocId() {
        return this.docID;
    }


}