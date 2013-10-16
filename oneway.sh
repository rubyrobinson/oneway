#!/bin/sh

rm output.txt outputf.txt

for i in `seq 1 10`;
        do
javac CreateDistribution.java
javac Tester.java
java Tester medium short

javac oneway/sim/Oneway.java
cat config.txt >> output.txt
cat config.txt >> outputf.txt
java oneway.sim.Oneway g5 config.txt timing2.txt false false 2>&1 | grep 'penalty' >> output.txt
java oneway.sim.Oneway g5f config.txt timing2.txt false false 2>&1 | grep 'penalty' >> outputf.txt
echo $i done
done
