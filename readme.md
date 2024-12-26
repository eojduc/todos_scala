



to start, you need a postgres db on your system. I used https://postgresapp.com/ to install 

there is a file called db.sql in the root of this project. run that in your postgres db to create the tables
(try "psql -U postgres -f db.sql" in the terminal (you may need to change the user))

then go to build.sbt and update the credentials for your postgres db and other parameters

Scala's build tool is called "sbt". install with "brew install sbt"

then run "sbt update compile run" to start the server