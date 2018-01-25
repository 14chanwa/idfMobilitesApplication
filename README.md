# idfMobilitesApplication
A small Java application connecting to the French Île-de-France Mobilités API (monotoring in real time Paris transportation network).


<p align="center">
<img src="https://raw.githubusercontent.com/14chanwa/idfMobilitesApplication/master/wiki_resources/example_rerA_chatelet.png">
</p>


<p align="center">
<img src="https://raw.githubusercontent.com/14chanwa/idfMobilitesApplication/master/wiki_resources/example2_rerA_chatelet.png">
</p>


The project consists in an application that queries the next departures for a given stop and a given line using Île-de-France Mobilités's web API, then parses the query and stores it in an object format. It also includes some graphical interface that displays the information more conveniently.


### Description


The project consists in the following files:


*/src/DataRetriever.java*: the main file of the project. Implements static methods to query the web API and parse the results.


*/src/Departure.java*: implements an object to store the query results in a usable way.


*/src/InformationPanel.java*: builds a JPanel that can display a given set of Departures.


*/src/TrainInformationFrame.java*: builds a JFrame that handles information display for trains in 2 separate panels, one for each direction (when the attribute `sens` is provided, for instance "1: East" or "-1: West").


### Dependancies


This program makes use of the library JSON-java for query parsing. Details about the library can be found on the [GitHub page](https://github.com/stleary/JSON-java). The `.jar` files can be downloaded [here](http://mvnrepository.com/artifact/org.json/json) for instance.
