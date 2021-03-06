// Slightly complicated build file for use with pfn's excellent
// Android Scala sbt plugin.
//
// Please see here for details:
// https://github.com/pfn/android-sdk-plugin/blob/master/README.md

import android.Keys._

android.Plugin.androidBuild

organization := "lucoodevcourse"

name := "clickcounter-android-rxscala-http"

version := "0.3"

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

javacOptions ++= Seq("-target", "1.6", "-source", "1.6") // so we can build with Java 7 or 8

scalacOptions in Compile ++= Seq("-feature", "-unchecked", "-deprecation")

platformTarget in Android := "android-19"

resolvers += "Local Maven Repository" at "file://" + Path.userHome + "/.m2/repository"

//resolvers += "laufer@bintray" at "http://dl.bintray.com/laufer/maven"

libraryDependencies ++= Seq(
  "org.robolectric" % "robolectric" % "2.3" % Test,
  "junit" % "junit" % "4.11" % Test,
  "org.scalamock" %% "scalamock-scalatest-support" % "3.2" % Test,
  "org.scalatest" %% "scalatest" % "2.2.4" % Test,
  "io.reactivex" %% "rxscala" % "0.23.1",
  "io.reactivex" % "rxandroid" % "0.24.0"
)

val androidJars = (platformJars in Android, baseDirectory) map {
  (j, b) => Seq(Attributed.blank(b / "bin" / "classes"), Attributed.blank(file(j._1)))
}

// Make the actually targeted Android jars available to Robolectric for shadowing.
managedClasspath in Test <++= androidJars

// With this option, we cannot have dependencies in the test scope!
debugIncludesTests in Android := false

exportJars in Test := false

// Supress warnings so that Proguard will do its job.
proguardOptions in Android ++= Seq(
  "-dontwarn rx.internal.util.**",
  "-dontwarn android.test.**"
)

// Required so Proguard won't remove the actual instrumentation tests.
proguardOptions in Android ++= Seq(
  "-keep public class * extends junit.framework.TestCase",
  "-keepclassmembers class * extends junit.framework.TestCase { *; }"
)

apkbuildExcludes in Android ++= Seq(
  "LICENSE.txt",
  "META-INF/DEPENDENCIES",
  "META-INF/LICENSE",
  "META-INF/LICENSE.txt",
  "META-INF/NOTICE",
  "META-INF/NOTICE.txt"
)

ScoverageSbtPlugin.ScoverageKeys.coverageExcludedPackages := """.*\.TR.*;.*\.TypedLayoutInflater;.*\.TypedResource;.*\.TypedViewHolder;.*\.TypedLayoutInflater"""
