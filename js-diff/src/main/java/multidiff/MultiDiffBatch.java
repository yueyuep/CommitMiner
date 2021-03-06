package multidiff;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import commitminer.analysis.Commit;
import commitminer.analysis.SourceCodeFileChange;
import commitminer.analysis.Commit.Type;
import commitminer.analysis.annotation.AnnotationMetricsPostprocessor;
import commitminer.analysis.factories.ICommitAnalysisFactory;
import commitminer.analysis.options.Options;
import commitminer.batch.GitProjectAnalysis;
import commitminer.js.diff.factories.CommitAnalysisFactoryAnnotationMetrics;

public class MultiDiffBatch {

	/** The directory where repositories are checked out. **/
	public static final String CHECKOUT_DIR =  new String("repositories");
	
	public static void main(String[] args) {

		/* Get the options from the command line args. */
		MultiDiffOptions options = new MultiDiffOptions();
		CmdLineParser parser = new CmdLineParser(options);

		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			MultiDiffBatch.printUsage(e.getMessage(), parser);
			return;
		}

		/* Print the help page. */
		if(options.getHelp()) {
			MultiDiffBatch.printHelp(parser);
			return;
		}

		/* The analysis we will be using. */
		ICommitAnalysisFactory analysisFactory = new CommitAnalysisFactoryAnnotationMetrics(options.getChangeImpact());
		
		/* The metrics post-processor. */
		AnnotationMetricsPostprocessor postProc = new AnnotationMetricsPostprocessor(options.getOutputFile());
		try {
			postProc.writeHeader();
		} catch (IOException e1) {
			System.err.println("MultiDiffBatch::main -- Could not write to output file.");
			return;
		}

		/* Set the options for this run. */
		Options.createInstance(options.getDiffMethod(), options.getChangeImpact());
		
		GitProjectAnalysis gitProjectAnalysis;
		try {

			/* Checkout or pull the project. */
            gitProjectAnalysis = GitProjectAnalysis.fromURI(options.getURI(),
            		CHECKOUT_DIR, postProc, analysisFactory);
            
            /* Run the analysis on the project history. */
			gitProjectAnalysis.analyze();

		} catch (Exception e) {
			e.printStackTrace(System.err);
		}	

	}
	
	/**
	 * Prints the help file for main.
	 * @param parser The args4j parser.
	 */
	private static void printHelp(CmdLineParser parser) {
        System.out.print("Usage: MultiDiffBatch ");
        parser.printSingleLineUsage(System.out);
        System.out.println("\n");
        parser.printUsage(System.out);
        System.out.println("");
	}

	/**
	 * Prints the usage of main.
	 * @param error The error message that triggered the usage message.
	 * @param parser The args4j parser.
	 */
	private static void printUsage(String error, CmdLineParser parser) {
        System.out.println(error);
        System.out.print("Usage: MultiDiff ");
        parser.printSingleLineUsage(System.out);
        System.out.println("");
	}


	/**
	 * @return a dummy commit. 
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