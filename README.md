# InYourFace_Dev

This app utilizes the Kairos API (a facial recognition API) to perform facial emotion/attention analysis and authentication. 

This app requires device administrator (since it locks the phone programmatically) and usage stats access (it needs to be able to get the package name of whichever application is in the foreground at the the moment, and with Google's recent efforts to increase security, various direct methods to do so have been deprecated/disabled; in Android 7.0 the only way to do this is indirectly - by querying recent usage stats and getting the last application opened). 

On first launch of the app, the user will be asked to create, confirm, and enter a password (we want a password because one main purpose of the app is security - locking the phone when the facial recognition recognizes an invalid user - and we don't want intruders to have access to the settings fragment). 

Then the user will be presented with a settings fragment. The user can toggle 




