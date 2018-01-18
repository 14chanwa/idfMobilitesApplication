# idfMobilitesApplication
A small Java application connecting to the French Île-de-France Mobilités API (monotoring in real time Paris transportation network).


The project consists in an application that queries the next departures for a given stop and a given line using Île-de-France Mobilités's web API, then parses the query and stores it in an object format. I will also add some interface that enables a user to use the program more conveniently. Later on, I plan to transpose this program to Python and use it in a Rasbpi project of my own.


### Description


The project consists in the following files:


*/src/DataRetriever.java*: the main file of the project. Implements static methods to query the web API and parse the results.


*/src/Departure.java*: implements an object to store the query results in a usable way.


### Dependancies


This program makes use of the library JSON-java for query parsing. Details about the library can be found on the [GitHub page](https://github.com/stleary/JSON-java). The `.jar` files can be downloaded [here](http://mvnrepository.com/artifact/org.json/json) for instance.