<?php
 header('Content-type: application/json');
 
 /*** mysql hostname ***/
 $hostname = 'localhost';
 
 /*** mysql username ***/
 $username = 'marcbrou39_dblis';
 
 /*** mysql password ***/
 $password = 'UDOfXu5Gr';

 try {
    $dbh = new PDO("mysql:host=$hostname;dbname=marcbrou39_dblis", $username, $password);
    
    selectMethod($dbh);
    
    /*** close the database connection ***/
    $dbh = null;
 } catch(PDOException $e) {
    //echo $e->getMessage();
 }
 
 /**
  * Selects Method from request
  */
 function selectMethod($dbh) {
 $Request_Method=$_REQUEST['method'] or die('Method name not found');
 
     if ($Request_Method=='addTweet') {
        $json = file_get_contents('php://input');
        $obj = json_decode($json);
        $tweetid = str_replace(array("[", "]"), "", $obj->{'tweetid'});
        $retweetid = str_replace(array("[", "]"), "", $obj->{'retweetid'});
        $retweets = $obj->{'retweets'};
        $favourites = $obj->{'favourites'};
        $text = $obj->{'text'};
        $creationTime = $obj->{'creationtime'};
        $countryCode = $obj->{'countrycode'};
        $language = $obj->{'language'};
        $userid = str_replace(array("[", "]"), "", $obj->{'userid'});
        $keywords = $obj->{'keywords'};
        if ($tweetid != null) {
            addTweet($dbh, $tweetid, $retweetid, $retweets, $favourites, $text, $creationTime, $countryCode, $language, $userid, $keywords);
        } else {
            echo 'false';
        }
        
     } else if ($Request_Method=='addUser') {
        $json = file_get_contents('php://input');
        $obj = json_decode($json);
        $userid = str_replace(array("[", "]"), "", $obj->{'userid'});
        $name = $obj->{'name'};
        $followers = $obj->{'followers'};
        $favourites = $obj->{'favourites'};
        $friends = $obj->{'friends'};
        if ($userid != null) {
            addUser($dbh, $userid, $name, $followers, $favourites, $friends);
        } else {
            echo 'false';
        }
        
     } else if ($Request_Method=='getSports') {
        echo getSports($dbh);
        
        
     }
 }
 
 function addTweet($dbh, $tweetid, $retweetid, $retweets, $favourites, $text, $creationTime, $countryCode, $language, $userid, $keywords) {
    if (!tweetExists($dbh, $tweetid)) {
        $sth = $dbh->prepare("insert into Tweets (id,retweetid,retweets,favourites,text,creationTime,countryCode,language,userID,keywords) values (:id,:reid,:retweets,:fav,:text,:time,:ccode,:lang,:uid,:keys)");
        $sth->bindParam(':id',$tweetid);
        $sth->bindParam(':reid',$retweetid);
        $sth->bindParam(':retweets',$retweets);
        $sth->bindParam(':fav',$favourites);
        $sth->bindParam(':text',$text);//,PDO::PARAM_LOB);
        $sth->bindParam(':time',$creationTime);
        $sth->bindParam(':ccode',$countryCode);
        $sth->bindParam(':lang',$language);
        $sth->bindParam(':uid',$userid);
        $sth->bindParam(':keys',$keywords);
        $sth->execute();
    }
    
    echo 'true';
 }
 
 function addUser($dbh, $userid, $name, $followers, $favourites, $friends) {
    if (!userExists($dbh, $userid)) {
        $sth = $dbh->prepare("insert into Users (id,name,followers,favourites,friends) values (:uid,:name,:fol,:fav,:friends)");
        $sth->bindParam(':uid',$userid);
        $sth->bindParam(':name',$name);//,PDO::PARAM_LOB);
        $sth->bindParam(':fol',$followers);
        $sth->bindParam(':fav',$favourites);
        $sth->bindParam(':friends',$friends);
        $sth->execute();
    }
    
    echo 'true';
 }
 
 function tweetExists($dbh, $tweetid) {
    $sth = $dbh->prepare("select count(*) from Tweets where id=:id");
    $sth->bindParam(':id',$tweetid);
    $sth->execute();
    $result = $sth->fetchColumn();
    
    if ($result == 1) {
        return true;
    }
    return false;
 }
 
 function userExists($dbh, $userid) {
    $sth = $dbh->prepare("select count(*) from Users where id=:uid");
    $sth->bindParam(':uid',$userid);
    $sth->execute();
    $result = $sth->fetchColumn();
    
    if ($result == 1) {
        return true;
    }
    return false;
 }
 
 function getSports($dbh) {
    $sth = $dbh->query("select * from Sports");
    $data = $sth->fetchAll(PDO::FETCH_ASSOC);
    return json_encode($data);
 }
 
 function getCountryTweets($dbh, $country) {
    $sth = $dbh->query("select * from Tweets where countryCode=:code");
    $sth->bindParam(':code',$country);
    $data = $sth->fetchAll(PDO::FETCH_ASSOC);
    return json_encode($data);
 }
 
 function getCommonSport($dbh, $country) {
    
 }
 
?>