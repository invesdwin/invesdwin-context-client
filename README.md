# invesdwin-context-client
This project provides frontend modules to create desktop and web clients for the invesdwin-context module system.

## Maven

Releases and snapshots are deployed to this maven repository:
```
http://invesdwin.de/artifactory/invesdwin-oss-remote
```

Dependency declaration:
```xml
<dependency>
	<groupId>de.invesdwin</groupId>
	<artifactId>invesdwin-context-client-wicket</artifactId>
	<version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Swing Module

The `invesdwin-context-client-swing` module provides some tools to simplify building swing applications. The following frameworks have been integrated to function as a platform:
- [MyDoggy](http://mydoggy.sourceforge.net/): this is a lightweight docking framework that provides a flexible container for your views.
- [BetterBeansBinding](https://github.com/stephenneal/betterbeansbinding): this allows to easily keep models in sync with view components. It is an improved version of the reference implementation for [JSR-295](https://jcp.org/en/jsr/detail?id=295).
- [BetterSwingApplicationFramework](https://sourceforge.net/projects/bsaf/): this provides a framework to handle the application lifecycline, internationalization and action binding. It is an improved version of the reference implementation for [JSR296](https://en.wikipedia.org/wiki/Swing_Application_Framework).
- [AssertJ-Swing](http://joel-costigliola.github.io/assertj/assertj-swing.html): to write unit tests for your swing views by automating actions.
- There are also a few more dependencies added which provide some more swing components you can use in your views ([SwingX](https://github.com/tmyroadctfig/swingx), [jide-oss](https://github.com/jidesoft/jide-oss), [spring-richclient](http://spring-rich-c.sourceforge.net), [JCalendar](https://toedter.com/jcalendar/)) and some icon sets ([Tango](https://commons.wikimedia.org/wiki/Tango_icons), [Silk](http://www.famfamfam.com/lab/icons/silk/)).
