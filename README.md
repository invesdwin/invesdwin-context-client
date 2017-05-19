# invesdwin-context-client
This project provides frontend modules to create desktop and web clients for the [invesdwin-context](https://github.com/subes/invesdwin-context) module system.

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
- [BetterSwingApplicationFramework](https://sourceforge.net/projects/bsaf/): this provides a framework to handle the application lifecycline, internationalization and action binding. It is an improved version of the reference implementation for [JSR-296](https://en.wikipedia.org/wiki/Swing_Application_Framework).
- [AssertJ Swing](http://joel-costigliola.github.io/assertj/assertj-swing.html): to write unit tests for your swing views by automating actions.
- There are also a few more dependencies added which provide some more swing components you can use in your views ([SwingX](https://github.com/tmyroadctfig/swingx), [Jide OSS](https://github.com/jidesoft/jide-oss), [Spring Rich Client](http://spring-rich-c.sourceforge.net), [JCalendar](https://toedter.com/jcalendar/)) and some icon sets ([Tango](https://commons.wikimedia.org/wiki/Tango_icons), [Silk](http://www.famfamfam.com/lab/icons/silk/)).
- [WindowBuilder](https://eclipse.org/windowbuilder/): this is the recommended WYSIWYG editor for creating panels for your views. Alternatively if you prefer Netbeans over Eclipse, you can still use [Matisse](https://netbeans.org/features/java/swing.html) (or any other preferred IDE/editor).
- [invesdwin-norva](https://github.com/subes/invesdwin-norva): this provides an optional annotation processor that generates constants classes for your models, so you can reference bean paths directly to be refactoring safe instead of hardcoding strings for them everywhere.
- [invesdwin-aspects](https://github.com/subes/invesdwin-aspects): this includes a nice `PropertyChangeSupportedAspect` that fires the update events for your model properties as required by the beans binding framework according to the java bean specification. Also it includes an `EventDispatchThreadAspect` and a utility class that simplifies working with the [EDT](https://en.wikipedia.org/wiki/Event_dispatching_thread).

To build your own swing applications, the following tools are available:

- **ARichApplication**: implement this as a spring bean to provide an entry point for your application, define a menu, handle startup parameters and do some lifecycle configuration. The application can be started with `de.invesdwin.context.client.swing.Main` which will use your provided bean to initialize the GUI. You can provide application properties files as resources for your implementation of this bean (reusing the functionality of JSR-296). Be aware that only one such can be active in your application context. You will have to use customized context loading to load the correct one if you have to integrate multiple ones. Or you could provide a master implementation that integrates multiple applications (delegating to the other implementations and creating a combined menu and so on). Mostly you won't need something as complicated as that and one implementation should suffice in your distribution. You can also roll your own solution by deactivating the included main application handling via `RichApplicationContextLocation.deactivate()`.
- **AModel**: this is the base class for your model objects. This base class provides access to the corresponding `org.jdesktop.application.ResourceMap` and `javax.swing.ActionMap` via getters for convenience in working with JSR-296.
- **AView**: this is the base class for your panels. AView extends AModel itself since it accesses the same JSR-296 classes, but additionally provides a panel (override `initComponent`) and a binding for it (override `initBindingGroup` if the `GeneratedBinding` does not suffice). You could in fact define the view itself as your generic model parameter, but it is best practice to keep the model class separate from your view class, so you follow the separation that MVC encourages. But it is useful to know that you don't have to create a model class if you don't need one (e.g. for simple components that you want to integrate into other views). Set the names of your swing components to the appropriate bean paths of your model properties to make clear what you want to get the binding generated for (which is handled by the default implementation of `initBindingGroup` via `GeneratedBinding`). This follows the binding principles of the [Naked Objects Pattern](https://en.wikipedia.org/wiki/Naked_objects) without being a full implementation of that, because the UI is built manually and not generated from the model.
- **ATask**: this class can be used to define asynchronous tasks you want to track in the status bar of your main window. Start them from your button actions via `Application.getInstance().getContext().getTaskService().execute(task)`.
- **IGuiExceptionHandlerHook**: define a bean implementing this interface or register it manually via `GuiExceptionHandler.registerHook(hook)` to override the default error dialog that occurs for uncaught exceptions on a case by case decision.
- **RichApplicationStub**: this class is the mock you can use in your unit tests. Just inject this bean into your test and access the fixture via `getFrameFixture` for controlling the test window.
- **Dialogs**: this is a replacement for the `JOptionPane` class. It delegates to it but fixes the given dialogs to properly support html in the message and making anchor urls clickable.

For some examples you can have a look at the test cases and implemented components/views inside the module itself. For more fundamental documentation it is best to seek the documentation that is directly provided by the included frameworks.
