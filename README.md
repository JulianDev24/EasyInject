#### About EasyInject
[EasyInject](https://github.com/JulianDev24/EasyInject) is a lightweight dependency injection framework for Java. 
It was originally developed for Minecraft to have a lightweight system, but it can also be used for general Java and Android applications. 
EasyInject allows developers to instantiate dependencies, provide additional dependencies, and inject dependencies into fields or constructors of other classes.

```xml

<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.JulianDev24.EasyInject</groupId>
        <artifactId>easyinjector-core</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

##### Usage - code examples
###### Create EasyInjector (the injector)
```java
EasyInjector easyInject = Easy.createInjector();
```
An application typically needs a single EasyInjector instance.

###### Instantiating dependencies
EasyInject allows for the injection of dependencies with either an @Inject annotated constructor or a default constructor without the need for any configuration. 

For example:
```java
public class A {
    @Inject
    public A(B b) {
        // ...
    }
}

public class B {
    @Inject
    public B(C c, D d) {
        // ...
    }
}

public class C {}

@Singleton
public class D {
    // something expensive or other reasons for being singleton
}
```
Creating an instance of A:
```java
A a = easyInject.getInstance(A.class);
```
###### Providing additional dependencies to EasyInject
EasyInject relies on configuration modules to provide dependencies for interfaces, third-party classes, or objects that require custom instantiation.
```java
public class MyModule {
    @Provides
    @Singleton // an app will probably need a single instance
    DataSource ds() {
        DataSource dataSource = // instantiate some DataSource
        return dataSource;
    }
}
```

Setting up EasyInject with module(s):

```java
EasyInjector easyInject = Easy.createInjector(new MyModule());
```
The DataSource dependency will now be available for injection:
```java
public class MyApp {
    @Inject
    public MyApp(DataSource ds) {
        // ...
    }
}
```
EasyInject injects dependencies into arguments of @Provides methods, making it especially useful for linking an implementation to an interface:
```java
public interface Foo {}

public class FooBar implements Foo {
    @Inject
    public FooBar(X x, Y y, Z z) {
        // ...
    }
}

public class MyModule {
    @Provides
    Foo foo(FooBar fooBar) {
        return fooBar;
    }
}

// injecting an instance of Foo interface will work using the MyModule above:
public class A {
    @Inject
    public A(Foo foo) {
        // ...
    }
}
```
EasyInject eliminates the need for manual instantiation by utilizing the @Provides method as a binding declaration.
###### Qualifiers
EasyInject accommodates the use of Qualifiers, such as @Named or custom qualifiers.
```java
public class MyModule {
    @Provides
    @Named("greeting")
    String greeting() {
        return "hi";
    }

    @Provides
    @SomeQualifier
    Foo some(FooSome fooSome) {
        return fooSome;
    }
}
```
Injecting:
```java
public class A {
    @Inject
    public A(@SomeQualifier Foo foo, @Named("greeting") String greet) {
        // ...
    }
}
```
Or directly from easyInject:
```java
String greet = easyInject.getInstance(String.class, "greeting");
Foo foo = easyInject.getInstance(Key.of(Foo.class, SomeQualifier.class));
```
###### Provider injection
EasyInject utilizes [Provider](https://docs.oracle.com/javaee/6/api/javax/inject/Provider.html)s to support lazy loading and manage circular dependencies:
```java
public class A {
    @Inject
    public A(Provider<B> b) {
        B b = b.get(); // fetch a new instance when needed
    }
}
```
Or getting a Provider directly from EasyInject:
```java
Provider<B> bProvider = easyInject.getProvider(B.class);
```
###### Override modules
```java
public class Module {
    @Provides
    DataSource dataSource() {
        // return a mysql datasource
    }

    // other @Provides methods
}

public class TestModule extends Module {
    @Override
    @Provides
    DataSource dataSource() {
        // return a h2 datasource
    }
}
```
###### Field injection
EasyInject primarily utilizes Constructor Injection when injecting within a dependency graph. However, 
it also supports Field Injection if explicitly prompted for a specific target object, such as during testing. 
A simple example of this can be seen in a JUnit test:
```java
public class AUnitTest {
    @Inject
    private Foo foo;
    @Inject
    private Bar bar;

    @Before
    public void setUp() {
        EasyInjector easyInjector = // obtain a EasyInjector instance
        easyInjector.injectObjectFields(this);
    }
}
```
###### Method injection
EasyInject does not support Field injection as a general practice. 
It can be avoided by using Providers and designing objects with immutability in mind, with dependencies injected through the constructor.

##### How it works under the hood
EasyInject utilizes reflection optimally to deliver dependencies without incurring any cost-intensive processes such as code generation, 
classpath scanning, or proxying.

A simple example with some explanation:
```java
class A {
    @Inject
    A(B b) {

    }
}

class B {

}
```
Without the use of EasyInject, class A could be instantiated with the following factory methods:
```java
A a() {
    return new A(b());
}

B b() {
    return new B();
}
```
EasyInject eliminates the need for factories and reduces the risk of changes causing merge conflicts. 
It does this by using reflection to create instances, calling a class's constructor with necessary dependencies and repeating the process for each subsequent dependency.
