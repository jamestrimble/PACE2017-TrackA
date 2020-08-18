package tw.exact;

import java.util.ArrayList;
import java.util.Arrays;

class NewTrieOptimised implements SupersetDataStructure {
    private int targetWidth;
    private TrieNodeOptimised root;
    private int size;
    private int n;

    public NewTrieOptimised(int n, int targetWidth) {
        this.n = n;
        this.targetWidth = targetWidth;
        XBitSet initialIntersectionOfNSets = new XBitSet();
        initialIntersectionOfNSets.set(0, n);
        root = new TrieNodeOptimised(-1, initialIntersectionOfNSets);
    }

    public void put(XBitSet SSet, XBitSet NSet) {
        root.updateIntersectionOfNSets(NSet);
        root.subtrieUnionOfSSets.or(SSet);
        TrieNodeOptimised node = root;
        // iterate over elements of NSet
        for (int i = NSet.nextSetBit(0); i >= 0; i = NSet.nextSetBit(i+1)) {
            node = node.getOrAddChildNode(i, SSet, NSet);
        }
        node.addSSet(SSet);
        ++size;
    }

    // for debugging
    void showList(ArrayList<XBitSet> bitsets) {
        for (XBitSet bs : bitsets) {
            System.out.println(bs);
        }
    }

    public void collectSuperblocks(XBitSet component, XBitSet neighbours,
            ArrayList<XBitSet> list) {
        int k = targetWidth + 1 - neighbours.cardinality();
        if (k >= 0) {
            long[] queryNLongs = neighbours.toLongArray();
            queryNLongs = Arrays.copyOf(queryNLongs, (n+63)/64);
            root.query(component, queryNLongs, k, k, list);
        }
    }

    public int[] getSizes() {
        return new int[] {size};
    }
}
