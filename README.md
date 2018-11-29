# Products UI: POSH-2007
Based on Insights UI

# How to run it:
    npm install -g bower grunt-cli
    npm install
    bower prune
    bower cache clean
    bower install

# Just the UI:
1) Run these commands (the first two once a week is usually ok)

    bower cache clean    
    bower update    
    grunt build serve -f
    
2) http://local.dev.poshly.com:9000/ 
should open automatically.

# With the Scala backend:
NOTE: you need to be on the vpn
1) Do this step once: Copy and paste the file "mikel-application.conf" and replace "mikel" by your user name. This is 
your local configuration, where you must change ports when using QMS or PIE locally.
2) Run Zookeeper and the backend (it will automatically run "grunt build -f"):

    Linux:     
    
        ~/zookeeper-3.4.8/bin/zkServer.sh start        
        sbt ~re-start
    
    macOS: 
    
        zkServer start
        sbt ~re-start
        
3) Go to
    http://local.dev.poshly.com:30030/


Use http://local.dev.poshly.com:9000/ to work on the frontend (to make use of livereload)
and http://local.dev.poshly.com:30030/ to use the UI while mainly working on the backend.

# Mobile API usage (local):
1) Run "sbt console" and "System.currentTimeMillis"
It will return a "TIMESTAMP"
2) Put that TIMESTAMP in: http://localhost:30030/token?timestamp=TIMESTAMP
ie: http://localhost:30030/token?timestamp=1490272266067
It will return a "TIMESTAMP-RESPONSE"
3) Open a new Terminal and run:
echo -n "poshly_TIMESTAMP_TIMESTAMP-RESPONSE_7d0f5be79c6c418d983fa1e92d1c7cb2_salt"  | shasum -a 256
ie: echo -n "poshly_1493809834563_ea685ad6-ecbd-4369-a515-e5c4ff1b66c3_7d0f5be79c6c418d983fa1e92d1c7cb2_salt"  | shasum -a 256
It will return an "ECHO-RESPONSE"
4) Call http://local.dev.poshly.com:30030/api/v2/authenticate/mobile?source=poshly&timestamp=TIMESTAMP&signature=ECHO-RESPONSE
ie: http://local.dev.poshly.com:30030/api/v2/authenticate/mobile?source=poshly&timestamp=1490272266067&signature=c85eb62726974f5d08625c49d1e10e1b83a09ffeb507287ff1a6fd698be8760d
It will return an "AUTH-TOKEN"
5) Use that AUTH-TOKEN whenever doing API calls, ie:
http://local.dev.poshly.com:30030/api/v2/products/all?token=ZMYA0RR4SENG1ZJJW0KR&q=Boots%20No.%207
