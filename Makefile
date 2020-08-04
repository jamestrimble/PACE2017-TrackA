all:	exact heuristic

exact:
	javac -d "bin" tw/exact/*.java

heuristic:
	javac -d "bin" tw/heuristic/*.java

clean: 
	rm -f bin/tw/*/*.class tw/*/*.class
