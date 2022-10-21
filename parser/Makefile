all:
	jjtree grammar.jjt
	javacc grammar.jj
	javac *.java

source: clean
	jjtree grammar.jjt
	javacc grammar.jj

clean:
	rm -rf *.java
	rm -rf *.class