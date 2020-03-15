Shutterstock tags
===============================================

This project will add shutterstock Windows tags to your exif on photos.

Before start:

prepare your shutterstock photos, put all of your photos inside some folder,
each photos must to respect the following naming convention:

**shutterstock_1223002882.jpg**

where only numbers can be different.

Usage:

`mvn clean package` - will create jar with dependencies under the target folder

`java -jar leprosorium-1.0-SNAPSHOT-jar-with-dependencies.jar C:\User\Photos` - will parse all the photos inside some folder example `C:\User\Photos`
and will create `/tagged` folder where you can find photos with taggs from shutterstock.

For big amounts of photos you can add additional memory for java process like:
`java -Xmx 2056mb -jar leprosorium-1.0-SNAPSHOT-jar-with-dependencies.jar C:\User\Photos` 

