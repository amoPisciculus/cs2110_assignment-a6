SOURCE_PATH = ../src
DEST = javadoc
PACKAGES = danaus student

all: doc

doc:
	javadoc -sourcepath $(SOURCE_PATH) -d $(DEST) $(PACKAGES)

private:
	javadoc -private -sourcepath $(SOURCE_PATH) -d $(DEST) $(PACKAGES)

clean:
	rm -r $(DEST)
