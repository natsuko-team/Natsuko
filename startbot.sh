while true
do
	mv ./target/natsuko-0.0.1-SNAPSHOT-jar-with-dependencies.jar ./target/previous.jar
	echo "Saved previous build"
	RESULT=$(mvn package)
	EC="$?"
	echo "$RESULT" | grep BUILD
	if [[ "$EC" -eq 130 ]] ; then
	exit 130
	fi
	if [[ "$EC" -ne 0 ]] ; then
		mv ./target/previous.jar ./target/natsuko-0.0.1-SNAPSHOT-jar-with-dependencies.jar
		echo "================================"
		echo "..........BUILD FAILED.........."
		echo "================================"
		echo "Restored previous build"
	fi
	java -jar target/natsuko-0.0.1-SNAPSHOT-jar-with-dependencies.jar -DLogback.configurationFile=/home/natsuko/natsuko/logback.xml
	if [[ "$EC" -eq 130 ]] ; then
        exit 130
        fi
done
