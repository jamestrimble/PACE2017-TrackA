#!/bin/bash
for n in $(seq 10 10 100); do
    for p in 1 5 9; do
        for i in $(seq 1 10); do
            python random_graph.py $n .$p > gr/gr-$n-$p-$i.gr
        done
    done
done
