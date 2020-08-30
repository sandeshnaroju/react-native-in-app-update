# react-native-in-app-update
Google released In App Update feature, but I did not find any `react-native` supported library for that, so I had to implement it for myself. 

### How to use ?
1. Open `android` folder in your react-native project with `Android Studio` and add `implementation 'com.google.android.play:core:1.7.3'` at the end of dependencies section of the `build.gradle(app)` file. Like below,
```
dependencies {
    implementation fileTree(dir: "libs", include: ["*.jar"])
    //noinspection GradleDynamicVersion
    implementation "com.facebook.react:react-native:+"  // From node_modules


    .......
    implementation 'com.google.android.play:core:1.7.3' // add it at the end
}

```
Cick sync after adding the dependency.         

2. Download `InAppUpdateModule.java` and `InAppUpdatePackage.java` files and place in them in the same directory of `MainActivity.java`(`android/app/src/main/java/<package>/`)
3. Change the package names in both `InAppUpdateModule.java` and `InAppUpdatePackage.java` to your project package name.
4. Now Open `MainApplication.java` and add our `InAppUpdatePackage` into `getPackages` method like below,
```
        @Override
        protected List<ReactPackage> getPackages() {
          @SuppressWarnings("UnnecessaryLocalVariable")
          List<ReactPackage> packages = new PackageList(this).getPackages();
          // Packages that cannot be autolinked yet can be added manually here, for example:
           //  packages.add(new MyReactNativePackage());
            packages.add(new InAppUpdatePackage());
          return packages;
        }
```
5. Download `InAppUpdate.js` and place it into your `react-native` project.
6. Import the `InAppUpdate.js` in any `js` file, wherever you want to use. And use it like below.

```
  componentDidMount () {
    InAppUpdate.checkUpdate() // this is how you check for update 
  }
```
7. That's it.


### What you should know?

1. It is `FLEXIBLE` update by dafault, if the user does not update in 5 days, it becomes `IMMEDIATE` update, you can change the number by editing `STALE_DAYS` in `InAppUpdateModule.java`. 
2. It does not include prioriy update, you can implement it by editing `checkUpdate` method in `InAppUpdateModule.java`. For more details check https://developer.android.com/guide/playcore/in-app-updates#check-priority


