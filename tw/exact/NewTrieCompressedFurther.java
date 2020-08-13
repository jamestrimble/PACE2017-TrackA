package tw.exact;

import java.util.ArrayList;
import java.util.Arrays;

class NewTrieCompressedFurther implements SupersetDataStructure {
    private int targetWidth;
    private TrieNode root;
    private int size;

    private class TrieNode {
        private TrieNode[] children = new TrieNode[0];
        private XBitSet subtrieIntersectionOfNSets;
        private int[] key = new int[0];
        private XBitSet[] SSets = new XBitSet[0];

        TrieNode(int[] key, XBitSet initialIntersectionOfNSets) {
            this.key = key;
            subtrieIntersectionOfNSets = initialIntersectionOfNSets;
        }

        int commonPrefixLength(int[] a, int[] b) {
            int minLen = a.length <= b.length ? a.length : b.length;
            int retval = 0;
            for (int i=0; i<minLen; i++) {
                if (a[i] != b[i]) {
                    return retval;
                }
                ++retval;
            }
            return retval;
        }

        TrieNode getOrAddChildNode(int[] key, XBitSet SSet, XBitSet NSet) {
            for (int i=0; i<children.length; i++) {
                TrieNode child = children[i];
                int prefixLen = commonPrefixLength(child.key, key);
                if (prefixLen == child.key.length) {
                    // child.key is a prefix of key
                    child.subtrieIntersectionOfNSets.and(NSet);
                    return child;
                } else if (prefixLen != 0) {
                    int[] prefix = Arrays.copyOf(key, prefixLen);
                    TrieNode node = new TrieNode(prefix, (XBitSet) child.subtrieIntersectionOfNSets.clone());
                    node.subtrieIntersectionOfNSets.and(NSet);
                    node.children = new TrieNode[] {child};
                    child.key = Arrays.copyOfRange(child.key, prefixLen, child.key.length);
                    children[i] = node;
                    return node;
                }
            }
            // Node not found; add and return it
            TrieNode node = new TrieNode(key, (XBitSet) NSet.clone());
            children = Arrays.copyOf(children, children.length + 1);
            children[children.length - 1] = node;
            return node;
        }

        private void query(XBitSet queryS, XBitSet queryN, int k,
                int budget, ArrayList<XBitSet> out_list) {
            if (subtrieIntersectionOfNSets.subtract(queryN).cardinality() > k) {
                return;
            }
            if (queryS.intersects(subtrieIntersectionOfNSets)) {
                return;
            }
            for (XBitSet SSet : SSets) {
                if (queryS.isSubset(SSet)) {
                    out_list.add((XBitSet) subtrieIntersectionOfNSets.clone());
                    break;
                }
            }
            for (TrieNode child : children) {
                int newBudget = budget;
                for (int v : child.key) {
                    if (!queryN.get(v)) {
                        --newBudget;
                    }
                }
                if (newBudget >= 0) {
                    child.query(queryS, queryN, k, newBudget, out_list);
                }
            }
        }
    }

    NewTrieCompressedFurther(int n, int targetWidth) {
        this.targetWidth = targetWidth;
        XBitSet initialIntersectionOfNSets = new XBitSet();
        initialIntersectionOfNSets.set(0, n);
        root = new TrieNode(new int[0], initialIntersectionOfNSets);
    }

    public void put(XBitSet SSet, XBitSet NSet) {
        root.subtrieIntersectionOfNSets.and(NSet);
        TrieNode node = root;

        int[] key = NSet.toArray();

        while (key.length != 0) {
            node = node.getOrAddChildNode(key, SSet, NSet);
            key = Arrays.copyOfRange(key, node.key.length, key.length);
        }
        node.SSets = Arrays.copyOf(node.SSets, node.SSets.length + 1);
        node.SSets[node.SSets.length - 1] = SSet;
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
            root.query(component, neighbours, k, k, list);
        }
    }

    public int[] getSizes() {
        return new int[] {size};
    }
}
