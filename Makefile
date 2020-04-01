.PHONY: build

default:

# make DIR=Square genDir
genDir:
	./gradlew run --args="src/test/resources/$(DIR)"

# make FILE=SimpleCall gen
gen:
	./gradlew run --args="src/test/resources/$(FILE)/$(FILE).jack"

clean:
	rm *.xml
