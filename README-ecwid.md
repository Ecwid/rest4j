This project consists of two libraries: rest4j-core and rest4j-generator. Generator project currently is not supported.
   

## How to build project
0. Install maven and jdk-11
`brew install maven && brew cask install java11`
2. Go to `core` folder
3. Run `mvn install -DskipTests -Dmaven.javadoc.skip=true -Dgpg.skip`