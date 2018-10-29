# MyNameGame

This is my version of the name game. The original template was written in Java and was over two years old.

![](screencast.gif)

https://play.google.com/store/apps/details?id=wt.cr.com.mynamegame

## Outside Perspective:
1. Turn on the game and press start (Green arrow)
2. Make a guess who you think matches the name at the bottom
3. Change modes at the top
4. View worldwide stats and opt-in to adding your own

## The inside stuff: 
1. Kotlin, MVVM && databinding, Dependency Injection
2. Groupie RecyclerView adapter, Retrofit, RXJava
3. AndroidSpinKit, ObjectAnimator
4. Firestore database and login

## Android Stuff:
1. Gradle 3.2.1
2. Kotlin 1.2.71
3. Gradle Dist 4.10.2-all
4. Three activities (Splash, Home, Stats)

## Build it on your machine:
1. Add wt.cr.com.mynamegame to a [firebase project](https://console.firebase.google.com/u/0/)
2. Download the google-services.json and put it MyNameGame/app
3. Enable Google Sign In inside the Firebase project settings
4. Enable Cloud Firestore inside the Firebase project settings

### View1
![Question](https://github.com/SeaRoth/MyNameGame/blob/master/1.png?raw=true)

### View2
![Question](https://github.com/SeaRoth/MyNameGame/blob/master/2.png?raw=true)

### View3
![Question](https://github.com/SeaRoth/MyNameGame/blob/master/3.png?raw=true)

### View4
![Question](https://github.com/SeaRoth/MyNameGame/blob/master/4.png?raw=true)

Original Question: https://github.com/willowtreeapps/namegame_android