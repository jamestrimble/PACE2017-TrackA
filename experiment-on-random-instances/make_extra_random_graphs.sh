#!/bin/bash
for n in $(seq 100 50 500); do
    for p in 9; do
        for i in $(seq 1 2); do
            python random_graph.py $n .$p > extra-gr/gr-$n-$p-$i.gr
        done
    done
done
