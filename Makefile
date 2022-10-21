all:
	cd parser; gmake source
	javac parser/*.java *.java 
	jar cvfe r16compiler.jar Assembler *.class parser/*.class
	rm -rf *.class parser/*.class