#!/bin/bash
p=1
for n in $(seq 5 5 55); do
    for i in $(seq 1 3); do
        python random_graph.py $n .$p > gr/gr-$n-$p-$i.gr
    done
done
p=5
for n in $(seq 10 10 90); do
    for i in $(seq 1 3); do
        python random_graph.py $n .$p > gr/gr-$n-$p-$i.gr
    done
done
p=9
for n in $(seq 50 50 450); do
    for i in $(seq 1 3); do
        python random_graph.py $n .$p > gr/gr-$n-$p-$i.gr
    done
done
