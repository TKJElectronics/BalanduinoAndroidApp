This contains libraries that I have modified to suit my needs.

The aar dependencies for the libraries are then generated using the steps in this guide: <http://codebutler.com/2013/07/02/android-libraries-with-gradle-and-android-studio/>.

### GraphView:
* Source: <https://github.com/Lauszus/GraphView>
* Forked from: <https://github.com/jjoe64/GraphView>

### Android ViewPagerIndicator
* Source: <https://github.com/Lauszus/Android-ViewPagerIndicator>
* Forked from: <https://github.com/JakeWharton/Android-ViewPagerIndicator>

This is how I installed them respectively:

```
mvn install:install-file \
-DgroupId=com.jjoe64.graphview \
-DartifactId=graphview \
-Dversion=3.1 \
-DgeneratePom=true \
-Dpackaging=aar \
-Dfile=build/libs/GraphView.aar \
-DlocalRepositoryPath=../
```

```
mvn install:install-file \
-DgroupId=com.viewpagerindicator \
-DartifactId=viewpagerindicator \
-Dversion=2.4.1 \
-DgeneratePom=true \
-Dpackaging=aar \
-Dfile=library/build/libs/library-2.4.1.aar \
-DlocalRepositoryPath=../
```

```
mvn install:install-file \
-DgroupId=com.physicaloid \
-DartifactId=physicaloid \
-Dversion=1.0 \
-DgeneratePom=true \
-Dpackaging=aar \
-Dfile=PhysicaloidLibrary/build/libs/PhysicaloidLibrary-1.0.aar \
-DlocalRepositoryPath=../
```