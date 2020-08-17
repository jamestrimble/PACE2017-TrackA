package tw.exact;

import java.util.ArrayList;
import java.util.Arrays;

class SetTrieNode {
    private SetTrieNode[] children = new SetTrieNode[0];
    XBitSet subtrieUnionOfSSets;
    XBitSet subtrieIntersectionOfNSets;
    private int key;
    private XBitSet NSet = null;
    private XBitSet SSet = null;

    SetTrieNode(int key, XBitSet initialIntersectionOfNSets) {
        this.key = key;
        subtrieIntersectionOfNSets = initialIntersectionOfNSets;
        subtrieUnionOfSSets = new XBitSet();
    }

    void addSets(XBitSet SSet, XBitSet NSet) {
        this.SSet = (XBitSet) SSet.clone();
        this.NSet = (XBitSet) NSet.clone();
    }

    SetTrieNode getOrAddChildNode(int key, XBitSet SSet, XBitSet NSet) {
        for (SetTrieNode child : children) {
            if (child.key == key) {
                child.subtrieIntersectionOfNSets.and(NSet);
                child.subtrieUnionOfSSets.or(SSet);
                return child;
            }
        }
        // Node not found; add and return it
        SetTrieNode node = new SetTrieNode(key, (XBitSet) NSet.clone());
        node.subtrieUnionOfSSets.or(SSet);
//        int insertionPoint = 0;
//        while (insertionPoint < children.length && key > children[insertionPoint].key) {
//            insertionPoint += 1;
//        }
        children = Arrays.copyOf(children, children.length + 1);
//        for (int i=children.length-1; i>insertionPoint; i--) {
//            children[i] = children[i-1];
//        }
//        children[insertionPoint] = node;
        children[children.length - 1] = node;
        return node;
    }

    void query(XBitSet queryS, XBitSet queryN, int k,
            ArrayList<XBitSet> out_list) {
        if (subtrieIntersectionOfNSets.subtract(queryN).cardinality() > k) {
            return;
        }
        if (!queryS.isSubset(subtrieUnionOfSSets)) {
            return;
        }
        if (this.SSet != null &&
                this.NSet.subtract(queryN).cardinality() <= k &&
                queryS.isSubset(this.SSet)) {
            out_list.add((XBitSet) this.NSet.clone());
        }
        int nextS = queryS.nextSetBit(key + 1);
        if (nextS == -1) {
            for (SetTrieNode child : children) {
                child.query(queryS, queryN, k, out_list);
            }
        } else {
            for (SetTrieNode child : children) {
                if (child.key > nextS) {
                    continue;
                }
                child.query(queryS, queryN, k, out_list);
            }
        }
    }
}
