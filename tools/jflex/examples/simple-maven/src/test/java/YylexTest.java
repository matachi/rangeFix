import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

import junit.framework.TestCase;

/**
 * This is an integration test.
 * 
 * The class Yylex is generated by JFLex from
 * <code>src/main/jflex/simple.flex</code>.
 * 
 * @author regis
 * 
 */
public class YylexTest extends TestCase {

	private static final String OUTPUT_FILE = "target/output.actual";

	/**
	 * Test that Yylex parser behaves like expected.
	 * 
	 * @throws IOException
	 */
	public void testOutput() throws IOException {
		String[] argv = new String[1];
		argv[0] = "src/test/resources/test.txt";

		// the Yylex prints status on stdout
		File actual = new File("target/output.actual");
		actual.delete();
		FileOutputStream fos = new FileOutputStream(OUTPUT_FILE, true);
		System.setOut(new PrintStream(fos));

		Yylex.main(argv);
		
		fos.close();
		
		// test actual is expected
		File expected = new File("src/test/resources/output.good");
		assertTrue(expected.isFile());
		assertTrue(actual.isFile());

		BufferedReader actualContent = new BufferedReader(
				new FileReader(actual));
		BufferedReader expectedContent = new BufferedReader(new FileReader(
				expected));

		for (int lineNumber = 1;lineNumber!=-1; lineNumber++) {
			String expectedLine = expectedContent.readLine();
			String actualLine = actualContent.readLine();			
			assertEquals("Line "+lineNumber, expectedLine, actualLine);
			if (expectedLine==null) lineNumber=-2; //EOF
		}
	}
}
