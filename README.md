# DBLIS

DBL Information Systems 2IOC0 Readme

We created a prototype to get the tweets based on certain keywords and we store the raw data in tweets.txt (all the tweets) and users.txt (all the users).

The program has a simple GUI where you can search for keywords in the keywords section. The Geocode checkbox enables you to set the longitude and latitude and a radius to search within that area.

Stream uses the Streaming API and continuously gets the tweets whereas search uses the REST API. Abort aborts the program.

Search also does 4 searches using the REST API in a 2 hour block.

The output is a list of most commonly used hashtags, followed by most common words and lastly most common locations with all the raw data stored in the 2 text files.


