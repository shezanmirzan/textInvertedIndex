package buildIndex;

import java.util.ArrayList;
import java.util.List;

public class PostingList {
    public List<Posting> postings;
    private int postingsIndex;

    public PostingList() {
        postings = new ArrayList<Posting>();
        postingsIndex = -1;
    }

    public void startIteration() {
        postingsIndex = 0;
    }
    public boolean hasMore() {
        return (postingsIndex>=0 && postingsIndex< postings.size());

    }
    public void skipTo(int docId) {
        while(postingsIndex<postings.size() && getCurrentPosting().getDocId()<docId) {
            postingsIndex++;
        }
    }
    public Posting getCurrentPosting() {
        Posting retval = null;
        try {
            retval = postings.get(postingsIndex);
        } catch(IndexOutOfBoundsException ex) {

        }
        return retval;
    }
    public Posting get(int index) {
        return postings.get(index);
    }
    public int docCount() {
        return postings.size();
    }
    public void add(Posting posting) {
        postings.add(posting);
        postingsIndex++;
    }
    public void add(Integer docId, Integer pos) {
        Posting cur = getCurrentPosting();
        if(cur!=null && cur.getDocId().equals(docId)) {
            cur.add(pos);
        }
        else {
            Posting posting = new Posting(docId, pos);
            add(posting);
        }
    }
    public Integer[] toIntegerArray() {
        ArrayList<Integer> postArr = new ArrayList<Integer>();
        for(Posting post: postings) {
            postArr.addAll(post.toIntegerArray());
        }
        return postArr.toArray(new Integer[postArr.size()]);
    }

    public void fromIntegerArray(int[] input) {
        int index=0;

        while(index<input.length) {
            int docId = input[index++];
            int count = input[index++];

            for(int i=0;i<count;i++) {
                int pos = input[index++];
                add(docId, pos);
            }
        }
        postingsIndex=0;
    }
    public int termFreq() {
        int freq=0;
        for(Posting p: postings) {
            freq+=p.getTermFreq();
        }
        return freq;
        //return postings.stream().mapToInt(p->p.getTermFreq()).sum();
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        int savedIdx = postingsIndex;
        startIteration();
        while(hasMore()) {
            Posting p = getCurrentPosting();
            int doc = p.getDocId();
            Integer []positions = p.getPositionsArray();
            buffer.append("{").append(doc).append(", ");
            buffer.append(positions.length).append(" [");

            for(int i:positions) {
                buffer.append(i).append(" ");
            }
            buffer.append(" ]} ");
            skipTo(doc+1);
        }
        postingsIndex=savedIdx;
        return buffer.toString();

    }


}
