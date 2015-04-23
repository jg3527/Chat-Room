JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	UserInputListener.java \
	MyProtocol.java \
	Debug.java \
	ClientThread.java \
	Client.java\

default: classes

classes: $(CLASSES:.java=.class)

clean:
	rm -f *.class
