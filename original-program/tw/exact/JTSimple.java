/*
 * Copyright (c) 2017, Hisao Tamaki
 */

package tw.exact;

import java.util.ArrayList;

public class JTSimple {
    int graph_n;
    int targetWidth;
    
    public JTSimple(int n, int targetWidth) {
        this.graph_n = n;
//        this.targetWidth = targetWidth;
//        
//        int k = 33 - Integer.numberOfLeadingZeros(targetWidth);
//        sieves = new BlockSieve[k];
//        for (int i = 0; i < k; i++) {
//            int margin = (1 << i) - 1;
//            sieves[i] = new BlockSieve(n, targetWidth, margin);
//        }
    }
    
    public void put(XBitSet vertices, XBitSet neighbors) {
//        int ns = neighbors.cardinality();
//        int margin = targetWidth + 1 - ns;
//        int i = 32 - Integer.numberOfLeadingZeros(margin);
//        sieves[i].put(vertices, neighbors);
    }
    
    public void put(XBitSet vertices, int neighborSize, XBitSet value) {
//        int margin = targetWidth + 1 - neighborSize;
//        int i = 32 - Integer.numberOfLeadingZeros(margin);
//        sieves[i].put(vertices, value);
    }
    
    public void collectSuperblocks(XBitSet component, XBitSet neighbors, 
            ArrayList<XBitSet> list) {
//        for (BlockSieve sieve: sieves) {
//            sieve.collectSuperblocks(component, neighbors, list);
//        }
    }
    
    public int[] getSizes() {
        // a dummy implementation
        int sizes[] = new int[1];
        sizes[0] = -1;
        return sizes;
    }

    public static void main(String[] args) {
        System.out.println("Test");
    }
}
