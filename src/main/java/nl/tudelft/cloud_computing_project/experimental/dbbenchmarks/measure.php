<?php

$q = "SELECT id, filesize FROM Job WHERE jobstatus = 1 AND id NOT IN (SELECT job_id FROM Assignment) ORDER BY priority, submission_time";

$c= mysql_connect($argv[1], "cloudocr", "cloudocr");
mysql_select_db("cloudocr_db");

for($i = 0; $i < $argv[2]; $i++) {
        $start = microtime(true);

        $r = mysql_query($q);
        while($row = mysql_fetch_assoc($r)) {}

        $end = microtime(true);

        echo $argv[3] . ($end-$start) . "\r\n";
}
?>