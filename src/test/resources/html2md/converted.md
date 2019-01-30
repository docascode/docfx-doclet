A test case defines the fixture to run multiple tests. To define a test case  


1.  implement a subclass of `TestCase`
2.  define instance variables that store the state of the fixture
3.  initialize the fixture state by overriding \{@link \#setUp()\}
4.  clean-up after a test by overriding \{@link \#tearDown()\}.

Each test runs in its own fixture so there can be no side effects among test runs. Here is an example:

    public class MathTest extends TestCase {
       protected double fValue1;
       protected double fValue2;
    
       protected void setUp() {
          fValue1= 2.0;
          fValue2= 3.0;
       }
    }

For each test implement a method which interacts with the fixture. Verify the expected results with assertions specified by calling \{@link junit.framework.Assert\#assertTrue(String, boolean)\} with a boolean.

    public void testAdd() {
          double result= fValue1 + fValue2;
          assertTrue(result == 5.0);
       }

Once the methods are defined you can run them. The framework supports both a static type safe and more dynamic way to run a test. In the static way you override the runTest method and define the method to be invoked. A convenient way to do so is with an anonymous inner class.

    TestCase test= new MathTest("add") {
       public void runTest() {
          testAdd();
       }
    };
    test.run();

The dynamic way uses reflection to implement \{@link \#runTest()\}. It dynamically finds and invokes a method. In this case the name of the test case has to correspond to the test method to be run.

    TestCase test= new MathTest("testAdd");
    test.run();

The tests to be run can be collected into a TestSuite. JUnit provides different *test runners* which can run a test suite and collect the results. A test runner either expects a static method `suite` as the entry point to get a test to run or it will extract the suite automatically.

    public static Test suite() {
       suite.addTest(new MathTest("testAdd"));
       suite.addTest(new MathTest("testDivideByZero"));
       return suite;
    }