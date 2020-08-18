package tw.exact;

import java.util.ArrayList;
import java.util.Arrays;

class TrieNodeOptimised {
    private TrieNodeOptimised[] children = new TrieNodeOptimised[0];
    XBitSet subtrieUnionOfSSets;
    XBitSet subtrieIntersectionOfNSets;
    long[] interLongs;
    private int key;
    private int kword;
    private long kbit;
    private XBitSet[] SSets = new XBitSet[0];

    TrieNodeOptimised(int key, XBitSet initialIntersectionOfNSets) {
        this.key = key;
        this.kword = key / 64;
        this.kbit = 1L << (key % 64);
        subtrieIntersectionOfNSets = initialIntersectionOfNSets;
        interLongs = subtrieIntersectionOfNSets.toLongArray();
        subtrieUnionOfSSets = new XBitSet();
    }

    void addSSet(XBitSet SSet) {
        SSets = Arrays.copyOf(SSets, SSets.length + 1);
        SSets[SSets.length - 1] = (XBitSet) SSet.clone();
    }

    TrieNodeOptimised getOrAddChildNode(int key, XBitSet SSet, XBitSet NSet) {
        for (TrieNodeOptimised child : children) {
            if (child.key == key) {
                child.subtrieIntersectionOfNSets.and(NSet);
                child.interLongs = child.subtrieIntersectionOfNSets.toLongArray();
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

    void query2(XBitSet queryS, XBitSet queryN, long[] queryNLongs, int k,
            int budget, ArrayList<XBitSet> out_list) {
        int count = 0;
        for (int i=0; i<interLongs.length; i++) {
            count += Long.bitCount(interLongs[i] & ~queryNLongs[i]);
        }
        if (count > k) {
            return;
        }
//        if (subtrieIntersectionOfNSets.subtract(queryN).cardinality() > k) {
//            return;
//        }
        if (!queryS.isSubset(subtrieUnionOfSSets)) {
            return;
        }
        for (XBitSet SSet : SSets) {
            if (queryS.isSubset(SSet)) {
                out_list.add((XBitSet) subtrieIntersectionOfNSets.clone());
                break;
            }
        }
        for (TrieNodeOptimised child : children) {
            int newBudget = (0 != (queryNLongs[kword] & kbit)) ? budget : budget - 1;
            if (newBudget >= 0) {
                child.query2(queryS, queryN, queryNLongs, k, newBudget, out_list);
            }
        }
    }

    void query(XBitSet queryS, XBitSet queryN, int k,
            int budget, ArrayList<XBitSet> out_list) {
        if (subtrieIntersectionOfNSets.subtract(queryN).cardinality() > k) {
            return;
        }
        if (!queryS.isSubset(subtrieUnionOfSSets)) {
            return;
        }
        for (XBitSet SSet : SSets) {
            if (queryS.isSubset(SSet)) {
                out_list.add((XBitSet) subtrieIntersectionOfNSets.clone());
                break;
            }
        }
        for (TrieNodeOptimised child : children) {
            int newBudget = queryN.get(child.key) ? budget : budget - 1;
            if (newBudget >= 0) {
                child.query(queryS, queryN, k, newBudget, out_list);
            }
        }
    }
}
