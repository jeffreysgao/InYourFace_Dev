# InYourFace_Dev
Important note:

For each tester, please copy one of the following API ID and key pairs into the "kairos_app_id" and "kairos_app_key" strings in strings.xml:

Application ID: 154cb3ee

Application Key: 77a92ff10a211d46e72ed89dcaef4dfe
Application Key: bc428f65fbb014845d6b10bf8faae92d
Application Key: 9f8902322ca0c35a811cf37fe7c01a16
Application Key: 6410abb08dfdc6c2563d9fea28dcf76b
Application Key: 95ed078d7978e22920a9ef93ff742394


This app utilizes the Kairos API (a facial recognition API) to perform facial emotion/attention analysis and authentication. 

This app requires device administrator (since it locks the phone programmatically) and usage stats access (it needs to be able to get the package name of whichever application is in the foreground at the the moment, and with Google's recent efforts to increase security, various direct methods to do so have been deprecated/disabled; in Android 7.0 the only way to do this is indirectly - by querying recent usage stats and getting the last application opened). 

On first launch of the app, the user will be asked to create, confirm, and enter a password (we want a password because one main purpose of the app is security - locking the phone when the facial recognition recognizes an invalid user - and we don't want intruders to have access to the settings fragment). 

Then the user will be presented with a settings fragment. First thing to do is to enroll yourself under "Enroll User". There will be two buttons "Add" and "Delete All". "Add" will open the camera and ask the user for a photo, which on result will be enrolled into a gallery on the Kairos database (basically "the cloud", not our local database). Multiple enroll is recommended for increased recognition accuracy. The enrolled photos serve as a "baseline" with which all later photos taken in the background service will be compared against. "Delete All" will delete all photos from the Kairos database. 

The user can then toggle settings to disable/enable facial recognition, device lock (on incorrect recognition), and facial analysis. Any of the features that are toggled on will be run in the background service - which can be triggered by clicking "Start Running!" at the very top of the settings fragment. There is also an option to choose the interval at which the background service can be run. 

When the background service is triggered, a background camera will fire, take a front-face picture of the user, send it to Kairos, and perform the chosen transactions (recognition/analysis). There is another preference at the bottom of the settings fragment - "Show Toasts" - which will activate/deactivate toasts displaying returned data from Kairos. 

Recognition - 
If the taken picture matches with any of the enrolled photos (as determined by Kairos's machine learning), a toast will be displayed (if option is on) showing that a valid user identified. If the taken picture does not match (and is distinctively a face), "Invalid user identified" will be displayed. Or if no faces can be identified in the taken picture, "No faces identified" will be displayed.

Analysis -
The taken picture can also be sent to Kairos for emotional/attention analysis. Kairos will return a JSON string containing an informational snapshot about the user's fear, joy, sadness, disgust, anger, and surprise, as well as about the user's attentiveness, as numbers rated between 0 and 100. These will be inserted into a local database and visualized in another fragment "Analysis" - which contains a line chart plotting attention over time, and an application-specific radar chart plotting the average of each of the six emotional values. We have a foreground app checker for several popular apps (like Facebook, Chrome, and YouTube). As the background service runs, it also detects what application is running in the foreground - a different radar chart is created for each of the apps - so we can get emotional profiles for various apps (and see which apps you should probably spend less time on).



Limitations of the app:

1. Since our app only uses one Kairos App Id, all of the installed apps will be sharing the same uploaded data. For example, if one user enrolls himself/herself, and another user himself/herself, then both of their faces will be recognized as legitimate by Kairos. In addition, if one user deletes the gallery from his/her phone, any other users' galleries (their enrolled photos) will also be deleted. So this app is designed for only one person to use at a time. (The paid version of Kairos will fix this issue; unfortunately we're broke college students)

2. Since we're posting data to Kairos for recognition and analysis, there will be a two or three-second delay between sending the photo and Kairos returning the data. Therefore the interval preference might not entirely correspond to the rate at which the data is returned and loaded into the database.







