package tw.exact;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

class NewTrieToLatex {
    public static void main(String[] args) throws IOException {
        int featureFlags = Integer.parseInt(args[0]);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int n = Integer.parseInt(br.readLine());
        int targetWidth = Integer.parseInt(br.readLine());
        NewTrie trie = new NewTrie(targetWidth);
        String input;
        while ((input = br.readLine()) != null) {
            String[] sets = input.split(" ");
            String[] setSStrings = sets[0].split(",");
            String[] setNStrings = sets[1].split(",");
            XBitSet setS = new XBitSet(n);
            XBitSet setN = new XBitSet(n);
            for (String s : setSStrings) {
                setS.set(Integer.parseInt(s));
            }
            for (String s : setNStrings) {
                setN.set(Integer.parseInt(s));
            }
            trie.put(setS, setN);
        }
        br.close();
        trie.printLatex(featureFlags);
    }
}
