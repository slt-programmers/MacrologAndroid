# Macrolog Android

## Files not to commit
The following files should never be committed to git and will reside in the ignore folder of
this project.
- macrologKeystore
- keystorePrivateKey.pepk
- keystore.properties (only for local use)
- secrets.tar
- macrolog-service-account-key.json


## Manual releasing

You can manually release a new version by using the Gradle 'publishBundle' task.
This task is part of the Gradle Play Publisher plugin 
(https://github.com/Triple-T/gradle-play-publisher).
 
It is configured in the build.gradle files and uses te SigningConfigs 
which are stored in the ignore folder for security reasons. 
For signing the app, the keystore and its passwords and alias is used. 

Be sure to increase the version code in the android.defaultConfig in the build.gradle file 
before running the task. The PlayStore won't handle the same version code twice.


## Automatic releasing

Automatic release to the PlayStore is also configured and will be done 
on merging to the release branch. 

The pipeline configuration resides in the travis.yml file. Before installing, travis will unpack
the encrypted secrets.tar containing the keystore and the service account key. 
The passwords and alias for the keystore are configured in travis settings itself 
(https://www.travis-ci.com/github/slt-programmers/macrologAndroid/settings) and are used to 
sign the app bundle. The service account key is used to automatically publish to the PlayStore.

 