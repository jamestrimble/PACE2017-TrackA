package tw.exact;

import java.util.ArrayList;
import java.util.Arrays;

class TrieNodeCompressed {
    private TrieNodeCompressed[] children = new TrieNodeCompressed[0];
    XBitSet subtrieUnionOfSSets;
    XBitSet subtrieIntersectionOfNSets;
    int[] key = new int[0];
    private XBitSet[] SSets = new XBitSet[0];

    TrieNodeCompressed(int[] key, XBitSet initialIntersectionOfNSets) {
        this.key = key;
        subtrieIntersectionOfNSets = initialIntersectionOfNSets;
        subtrieUnionOfSSets = new XBitSet();
    }

    void addSSet(XBitSet SSet) {
        SSets = Arrays.copyOf(SSets, SSets.length + 1);
        SSets[SSets.length - 1] = (XBitSet) SSet.clone();
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

    TrieNodeCompressed getOrAddChildNode(int[] key, XBitSet SSet, XBitSet NSet) {
        for (int i=0; i<children.length; i++) {
            TrieNodeCompressed child = children[i];
            int prefixLen = commonPrefixLength(child.key, key);
            if (prefixLen == child.key.length) {
                // child.key is a prefix of key
                child.subtrieIntersectionOfNSets.and(NSet);
                child.subtrieUnionOfSSets.or(SSet);
                return child;
            } else if (prefixLen != 0) {
                int[] prefix = Arrays.copyOf(key, prefixLen);
                TrieNodeCompressed node = new TrieNodeCompressed(prefix, (XBitSet) child.subtrieIntersectionOfNSets.clone());
                node.subtrieUnionOfSSets.or(child.subtrieUnionOfSSets);
                node.subtrieIntersectionOfNSets.and(NSet);
                node.subtrieUnionOfSSets.or(SSet);
                node.children = new TrieNodeCompressed[] {child};
                child.key = Arrays.copyOfRange(child.key, prefixLen, child.key.length);
                children[i] = node;
                return node;
            }
        }
        // Node not found; add and return it
        TrieNodeCompressed node = new TrieNodeCompressed(key, (XBitSet) NSet.clone());
        node.subtrieUnionOfSSets.or(SSet);
        children = Arrays.copyOf(children, children.length + 1);
        children[children.length - 1] = node;
        return node;
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
        for (TrieNodeCompressed child : children) {
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

    private void printLatexBitset(XBitSet bitset, String colour) {
        System.out.print("\\\\ [-1ex] \\scriptsize {\\color{" + colour + "} $");
        if (bitset.isEmpty()) {
            System.out.print("\\emptyset");
        } else {
            String comma = "";
            for (int v = bitset.nextSetBit(0); v >= 0; v = bitset.nextSetBit(v+1)) {
                System.out.print(comma + v);
                comma = " ";
            }
        }
        System.out.print("$} ");
    }

    void printLatex(int prevNodeNLength, ArrayList<Integer> currentNodeN, int featureFlags) {
        System.out.print("[{$");
        if (currentNodeN.isEmpty()) {
            System.out.print("\\emptyset");
        } else {
            for (int i=0; i<currentNodeN.size(); i++) {
            int v = currentNodeN.get(i);
                if (i == prevNodeNLength) {
                    System.out.print("\\mathbf{\\underline{");
                }
                System.out.print(v);
            }
            System.out.print("}}");
        }
        System.out.print("$ ");

        if (0 != (featureFlags & 1)) {
            printLatexBitset(subtrieIntersectionOfNSets, "black!50");
        }

        if (0 != (featureFlags & 2)) {
            printLatexBitset(subtrieUnionOfSSets, "blue");
        }

        if (0 != (featureFlags & 4)) {
            for (XBitSet SSet : SSets) {
                printLatexBitset(SSet, "blue!50");
            }
        }
        System.out.print("},align=center");
        if (SSets.length > 0) {
            System.out.print(",line width=.7mm");
        }
        for (TrieNodeCompressed child : children) {
            int len = currentNodeN.size();
            for (int v : child.key) {
                currentNodeN.add(v);
            }
            child.printLatex(len, currentNodeN, featureFlags);
            for (int v : child.key) {
                currentNodeN.remove(currentNodeN.size() - 1);
            }
        }
        System.out.print("]");
    }
}
