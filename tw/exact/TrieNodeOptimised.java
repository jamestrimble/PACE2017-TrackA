package tw.exact;

import java.util.ArrayList;
import java.util.Arrays;

class TrieNodeOptimised {
    private TrieNodeOptimised[] children = new TrieNodeOptimised[0];
    XBitSet subtrieUnionOfSSets;
    long[] subtrieIntersectionOfNSetsLongs;
    private int key;
    private int kword;
    private long kbit;
    private XBitSet[] SSets = new XBitSet[0];

    TrieNodeOptimised(int key, XBitSet initialIntersectionOfNSets) {
        this.key = key;
        this.kword = key / 64;
        this.kbit = 1L << (key % 64);
        subtrieIntersectionOfNSetsLongs = initialIntersectionOfNSets.toLongArray();
        subtrieUnionOfSSets = new XBitSet();
    }

    void updateIntersectionOfNSets(XBitSet NSet) {
        long[] NSetLongs = NSet.toLongArray();
        int top = Math.min(NSetLongs.length, subtrieIntersectionOfNSetsLongs.length);
        for (int i=0; i<top; i++) {
            if (i == subtrieIntersectionOfNSetsLongs.length) {
                break;
            }
            subtrieIntersectionOfNSetsLongs[i] = subtrieIntersectionOfNSetsLongs[i] & NSetLongs[i];
        }
        int newLength = top;
        while (newLength > 0 && subtrieIntersectionOfNSetsLongs[newLength-1] == 0) {
            --newLength;
        }
        if (newLength < subtrieIntersectionOfNSetsLongs.length) {
            subtrieIntersectionOfNSetsLongs = Arrays.copyOf(subtrieIntersectionOfNSetsLongs, newLength);
        }
    }

    void addSSet(XBitSet SSet) {
        SSets = Arrays.copyOf(SSets, SSets.length + 1);
        SSets[SSets.length - 1] = (XBitSet) SSet.clone();
    }

    TrieNodeOptimised getOrAddChildNode(int key, XBitSet SSet, XBitSet NSet) {
        for (TrieNodeOptimised child : children) {
            if (child.key == key) {
                child.updateIntersectionOfNSets(NSet);
                child.subtrieUnionOfSSets.or(SSet);
                return child;
            }
        }
        // Node not found; add and return it
        TrieNodeOptimised node = new TrieNodeOptimised(key, (XBitSet) NSet.clone());
        node.subtrieUnionOfSSets.or(SSet);
        children = Arrays.copyOf(children, children.length + 1);
        children[children.length - 1] = node;
        return node;
    }

    void query(XBitSet queryS, long[] queryNLongs, int k,
            int budget, ArrayList<XBitSet> out_list) {
        int count = 0;
        for (int i=0; i<subtrieIntersectionOfNSetsLongs.length; i++) {
            count += Long.bitCount(subtrieIntersectionOfNSetsLongs[i] & ~queryNLongs[i]);
        }
        if (count > k) {
            return;
        }
        if (!queryS.isSubset(subtrieUnionOfSSets)) {
            return;
        }
        for (XBitSet SSet : SSets) {
            if (queryS.isSubset(SSet)) {
                XBitSet bs = new XBitSet();
                for (int i=0; i<subtrieIntersectionOfNSetsLongs.length; i++) {
                    long word = subtrieIntersectionOfNSetsLongs[i];
                    while (word != 0) {
                        int bit = Long.numberOfTrailingZeros(word);
                        bs.set(i * 64 + bit);
                        word ^= (1L << bit);
                    }
                }
                out_list.add(bs);
                break;
            }
        }
        for (TrieNodeOptimised child : children) {
            int newBudget = (0 != (queryNLongs[child.kword] & child.kbit)) ? budget : budget - 1;
            if (newBudget >= 0) {
                child.query(queryS, queryNLongs, k, newBudget, out_list);
            }
        }
    }
}
