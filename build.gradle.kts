// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.3.2")
    }
}

plugins {
    alias(libs.plugins.androidApplication) apply false
}

allprojects {
    repositories {
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
