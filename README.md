# InYourFace_Dev
Important note:

One of the limitations of our current application is that it uses a free demo version of Kairos, as a proof of concept. Because of this, the number of API calls per minute to Kairos per app key is capped. This has the potential to lock up the calls to Kairos when multiple users test the application at the same time. As a solution, we provide a way for each user/grader to select their own name when enrolling images of their face as the user of the application in the PhotoActivity. Each grader's name has a different Kairos app id and app key associated with it that the application will use for all calls for Kairos. (The paid version of Kairos will fix this issue; unfortunately we're broke college students)

----------------------------------------------------------------------------------------------------------------

This app utilizes the Kairos API (a facial recognition API) to perform facial emotion/attention analysis and authentication. 

Some of the requirements for this application are the device administrator (since it locks the phone programmatically) and usage stats access (it needs to be able to get the package name of whichever application is in the foreground at the the moment). 

On first launch of the app, the user will be asked to create, confirm, and enter a password (we want a password because one main purpose of the app is security - locking the phone when the facial recognition recognizes an invalid user - and we don't want intruders to have access to the settings fragment). 

Then the user will be presented with a settings fragment. First thing to do is to enroll yourself under "Enroll User". There will be two buttons "Add" and "Delete All". "Add" will open the camera and ask the user for a photo, which on result will be enrolled into a gallery on the Kairos database (basically "the cloud", not our local database). Enrolling multiple photos is recommended for increased recognition accuracy. The enrolled photos serve as a "baseline" with which all later photos taken in the background service will be compared against. "Delete All" will delete all photos from the Kairos database. 

The user can then toggle settings to disable/enable facial recognition, device lock (on incorrect recognition), and facial analysis. Any of the features that are activated will be run in the background service - which can be triggered by clicking "Start Running!" at the very top of the settings page. There is also an option to choose the interval at which the background service can be run. 

----------------------------------------------------------------------------------------------------------------

Note:
Since we're posting data to Kairos for recognition and analysis, there will be a two or three-second delay between sending the photo and Kairos returning the data. Therefore the interval preference might not entirely correspond to the rate at which the data is returned and loaded into the database. This also means that the app requires an internet connection to run. This is another limiation of our demo version access to Kairos. Premium versions offer access to an offline SDK so that all facial recognition can be performed locally. 

----------------------------------------------------------------------------------------------------------------

When the background service is triggered, a background camera will fire, take a front-face picture of the user, send it to Kairos, and perform the chosen transactions (recognition/analysis). There is another preference at the bottom of the settings fragment - "Show Toasts" - which will activate/deactivate toasts displaying returned data from Kairos. Activating toasts allows you to monitor the services running in the background and the data collected from Kairos. 

Recognition - 
If the taken picture matches with any of the enrolled photos (as determined by Kairos's machine learning), a toast will be displayed (if option is on) showing that a valid user identified. If the taken picture does not match (and is distinctively a face), "Invalid user identified" will be displayed. Or if no faces can be identified in the taken picture, "No faces identified" will be displayed.

Analysis -
The taken picture can also be sent to Kairos for emotional/attention analysis. Kairos will return a JSON string containing an informational snapshot about the user's fear, joy, sadness, disgust, anger, and surprise, as well as about the user's attentiveness, as numbers rated between 0 and 100. These will be inserted into a local database and visualized in another fragment "Analysis" - which contains a line chart plotting attention over time, and an application-specific radar chart plotting the average of each of the six emotional values. We have a foreground app checker for several popular apps (like Facebook, Chrome, and YouTube). As the background service runs, it also detects what application is running in the foreground - a different radar chart is created for each of the apps - so we can get emotional profiles for various apps (and see which apps you should probably spend less time on).









