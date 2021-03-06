package commitminer.js.diff.test;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import commitminer.analysis.Commit;
import commitminer.analysis.CommitAnalysis;
import commitminer.analysis.SourceCodeFileChange;
import commitminer.analysis.Commit.Type;
import commitminer.analysis.annotation.Annotation;
import commitminer.analysis.annotation.AnnotationFactBase;
import commitminer.analysis.factories.ICommitAnalysisFactory;
import commitminer.js.diff.DiffCommitAnalysisFactory;

public class EvalUserDiffAnalysis {

	/**
	 * Tests data mining data set construction.
	 * @param args The command line arguments (i.e., old and new file names).
	 * @throws Exception
	 */
	protected void runTest(SourceCodeFileChange sourceFileChange,
						   Set<Annotation> expectedAnnotations,
						   boolean checkSize) throws Exception {

		Commit commit = getCommit();
		commit.addSourceCodeFileChange(sourceFileChange);

		/* Builds the data set with our custom queries. */
		AnnotationFactBase factBase = AnnotationFactBase.getInstance(sourceFileChange);

		/* Set up the analysis. */
		ICommitAnalysisFactory commitFactory = new DiffCommitAnalysisFactory();
		CommitAnalysis commitAnalysis = commitFactory.newInstance();

		/* Run the analysis. */
		commitAnalysis.analyze(commit);

        /* Print the data set. */
		factBase.printDataSet();

        /* Verify the expected annotations match the actual annotations. */
		SortedSet<Annotation> actualAnnotations = factBase.getAnnotations();
		for(Annotation expectedAnnotation : expectedAnnotations) {
			Assert.assertTrue(actualAnnotations.contains(expectedAnnotation));
		}

	}

	@Test
	public void testTutorial() throws Exception {

		/* The test files. */
		String src = "./test/input/diff/tutorial_old.js";
		String dst = "./test/input/diff/tutorial_new.js";

		/* Read the source files. */
		SourceCodeFileChange sourceCodeFileChange = getSourceCodeFileChange(src, dst);

		/* Build the expected feature vectors. */
		Set<Annotation> expected = new HashSet<Annotation>();

		this.runTest(sourceCodeFileChange, expected, false);

	}

	@Test
	public void testEnv() throws Exception {

		/* The test files. */
		String src = "./test/input/diff/env_old.js";
		String dst = "./test/input/diff/env_new.js";

		/* Read the source files. */
		SourceCodeFileChange sourceCodeFileChange = getSourceCodeFileChange(src, dst);

		/* Build the expected feature vectors. */
		Set<Annotation> expected = new HashSet<Annotation>();

		this.runTest(sourceCodeFileChange, expected, false);

	}

	@Test
	public void testCall() throws Exception {

		/* The test files. */
		String src = "./test/input/diff/call_old.js";
		String dst = "./test/input/diff/call_new.js";

		/* Read the source files. */
		SourceCodeFileChange sourceCodeFileChange = getSourceCodeFileChange(src, dst);

		/* Build the expected feature vectors. */
		Set<Annotation> expected = new HashSet<Annotation>();

		this.runTest(sourceCodeFileChange, expected, false);

	}

	@Test
	public void testCon() throws Exception {

		/* The test files. */
		String src = "./test/input/diff/con_old.js";
		String dst = "./test/input/diff/con_new.js";

		/* Read the source files. */
		SourceCodeFileChange sourceCodeFileChange = getSourceCodeFileChange(src, dst);

		/* Build the expected feature vectors. */
		Set<Annotation> expected = new HashSet<Annotation>();

		this.runTest(sourceCodeFileChange, expected, false);

	}

	@Test
	public void testTernary() throws Exception {

		/* The test files. */
		String src = "./test/input/diff/ternary_old.js";
		String dst = "./test/input/diff/ternary_new.js";

		/* Read the source files. */
		SourceCodeFileChange sourceCodeFileChange = getSourceCodeFileChange(src, dst);

		/* Build the expected feature vectors. */
		Set<Annotation> expected = new HashSet<Annotation>();

		this.runTest(sourceCodeFileChange, expected, false);

	}

	@Test
	public void testDynProp() throws Exception {

		/* The test files. */
		String src = "./test/input/diff/pm2_old.js";
		String dst = "./test/input/diff/pm2_new.js";

		/* Read the source files. */
		SourceCodeFileChange sourceCodeFileChange = getSourceCodeFileChange(src, dst);

		/* Build the expected feature vectors. */
		Set<Annotation> expected = new HashSet<Annotation>();

		this.runTest(sourceCodeFileChange, expected, false);

	}

	/**
	 * @return A dummy commit for testing.
	 */
	public static Commit getCommit() {
		return new Commit("test", "http://github.com/saltlab/Pangor", "c0", "c1", Type.BUG_FIX);
	}

	/**
	 * @return A dummy source code file change for testing.
	 * @throws IOException
	 */
	public static SourceCodeFileChange getSourceCodeFileChange(String srcFile, String dstFile) throws IOException {
		String buggyCode = readFile(srcFile);
		String repairedCode = readFile(dstFile);
		return new SourceCodeFileChange(srcFile, dstFile, buggyCode, repairedCode);
	}

	/**
	 * Reads the contents of a source code file into a string.
	 * @param path The path to the source code file.
	 * @return A string containing the source code.
	 * @throws IOException
	 */
	private static String readFile(String path) throws IOException {
		return FileUtils.readFileToString(new File(path));
	}

}