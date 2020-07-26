.DEFAULT_GOAL := create_jar

create_jar:
	javac chess/backend/*.java chess/*.java
	jar cfe Chess.jar chess.Frontend chess/*
	rm chess/backend/*.class chess/*.class

