.PHONY: build

default:

build:
	./gradlew build

# make DIR=src/test/resources/project-11/Average genDir
genDir:
	./gradlew run --args="$(DIR)"
	mv *.vm "$(DIR)"

# make FILE=SimpleCall gen
gen:
	./gradlew run --args="$(FILE)"

clean:
	rm *.xml
