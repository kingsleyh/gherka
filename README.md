# Generate pretty [cucumber-jvm](https://github.com/cucumber/cucumber-jvm) or JBehave html pages of your features 

This is a java project which converts your text based BDD scripts from either Cucumber or JBehave into pretty html versions.


## Background

Cucumber-JVM is a test automation tool following the principles of Behavioural Driven Design and living documentation. Specifications are written in a concise human readable form and executed in continuous integration. 

This project allows you to create nice looking and syntax highlighted version of the plain text BDD files for easy reading.

## Install

1. Make sure you have Java installed

2. Download the [gherka-jar]
(https://github.com/masterthought/gherka/downloads) jar file.

## Use

It's pretty easy - just run the jar on the command line:

    java -jar gherka.jar -folders /home/kingsley/bdd-scenarios,/home/kingsley/more -o /home/kings/bdd-html -suffixes story,feature

Gherka will locate all the files with the suffixes provided in the folders provided and generate html output to the output folder provided. An index.html file is generated showing an overview:

![overview page]
(https://github.com/masterthought/gherka/raw/master/.README/overview.png)

And there are also feature specific pages that link from the overview (including the original files for posterity):

![feature file]
(https://github.com/masterthought/gherka/raw/master/.README/feature-file.png)

There are 2 css files and the default one is style.css a dark theme. If you delete it and rename light-style.css to style.css you will get the light theme. If you change the css the style.css will not be regenerated unless you delete it and run again:

![light overview theme]
(https://github.com/masterthought/gherka/raw/master/.README/overview-light.png)

And the feature:

![light feature theme]
(https://github.com/masterthought/gherka/raw/master/.README/feature-light.png)

## Develop

Interested in contributing to Gherka?  Great!  Start [here]
(https://github.com/masterthought/gherka).
