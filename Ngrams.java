/*
    This program counts the Ngrams of a collection of documents and prints the Ngram and tye frequency of each Ngram
    and the frequnecy of characters to an output file.
    Author: Greg Bradner
    Date Created: 20 Jan 2019
*/

// represents files and directory pathnames 
// in an abstract manner
import java.io.File;

// reads data from files as streams of characters
import java.io.FileReader;

// reads text efficiently from character-input
// stream buffers 
import java.io.BufferedReader;

// for writing data to files
import java.io.PrintWriter;

// signals that an input/output (I/O) exception 
// of some kind has occurred
import java.io.IOException;

// compiled representation of a regular expressions
import java.util.regex.Pattern;

// matches a compiled regular expression with an input string
import java.util.regex.Matcher;

import java.util.*;

public class Ngrams {

	// no more than this many input files needs to be processed
	final static int MAX_NUMBER_OF_INPUT_FILES = 100;

	// an array to hold Gutenberg corpus file names
	static String[] inputFileNames = new String[MAX_NUMBER_OF_INPUT_FILES];

	static int fileCount = 0;

	// loads all files names in the directory subtree into an array
	// violates good programming practice by accessing a global variable
	// (inputFileNames)
	public static void listFilesInPath(final File path) {
		for (final File fileEntry : path.listFiles()) {
			if (fileEntry.isDirectory()) {
				listFilesInPath(fileEntry);
			} else if (fileEntry.getName().endsWith((".txt"))) {
				inputFileNames[fileCount++] = fileEntry.getPath();
				// fileNameListWriter.println(fileEntry.getPath());
				// System.out.println(fileEntry.getName());
				// System.out.println(fileEntry.getAbsolutePath());
				// System.out.println(fileEntry.getCanonicalPath());
			}
		}
	}

	// returns index of a character in the alphabet
	// uses zero-based indexing
	public static int getLetterValue(char letter) {
		return (int) Character.toUpperCase(letter) - 65;
	}

	public static void main(String[] args) {

		// did the user provide correct number of command line arguments?
		// if not, print message and exit
		if (args.length != 6) {
			System.err.println("Number of command line arguments must be 6");
			System.err.println("You have given " + args.length + " command line arguments");
			System.err.println("Incorrect usage. Program terminated");
			System.err
					.println("Correct usage: java Ngrams <path-to-input-files> <outfile-for-words> <outfile-for-char-counts>"
							+ "<outfile for unigrams> <outfile for bigrams> <outfile for trigrams>");
			System.exit(1);
		}

		// extract input file name from command line arguments
		// this is the name of the file from the Gutenberg corpus
		String inputFileDirName = args[0];
		System.out.println("Input files directory path name is: " + inputFileDirName);

		// collects file names and write them to
		listFilesInPath(new File(inputFileDirName));

		// System.out.println("Number of Gutenberg corpus files: " + fileCount);

		// br for efficiently reading characters from an input stream
		BufferedReader br = null;

		// wdWriter for writing extracted words to an output file
		PrintWriter wdWriter = null;
		
		// uniWriter for writing unigrams and their frequencies to an output file
		PrintWriter uniWriter = null; 
		
		// uniWriter for writing bigrams and their frequencies to an output file
		PrintWriter biWriter = null;
		
		// uniWriter for writing trigrams and their frequencies to an output file
		PrintWriter triWriter = null;

		// ccWriter for writing characters and their occurrence
		// counts to an output file
		PrintWriter ccWriter = null;

		// wordPattern specifies pattern for words using a regular expression
		Pattern wordPattern = Pattern.compile("[a-zA-Z]+");

		// wordMatcher finds words by spotting word word patterns with input
		Matcher wordMatcher;

		// a line read from file
		String line;

		// an extracted word from a line
		String word;
		
		// keeps track of the previous previous word
		String prev1 = "";
		
		// keeps track of the previous word
		String prev2 = "";

		// letter characters
		String alphabet = "abcdefghijklmnopqrstuvwxyz";
		
		// stopWords is a set of words that will not be counted as an ngram.
		Set<String> stopWords = new HashSet<String>();
		
		// add stopWords to set.
		insertStopWords(stopWords);

		//unigrams is a tree of unigrams and their frequencies and are sorted by key.
		final TreeMap<String, Integer> unigrams = new TreeMap<String, Integer>();
		
		//bigrams is a tree of bigrams and their frequencies and are sorted by key.
		final TreeMap<String, Integer> bigrams = new TreeMap<String, Integer>();
		
		//trigrams is a tree of trigrams and their frequencies and are sorted by key.
		final TreeMap<String, Integer> trigrams = new TreeMap<String, Integer>();
		
		// if init is true prev1 and prev2 will be set to the first and second words of the first document.
		boolean init = true;
		
		// open output file for writing words
		try {
			wdWriter = new PrintWriter(args[1], "UTF-8");
			System.out.println(args[1] + " successfully opened for writing words");
		} catch (IOException ex) {
			System.err.println("Unable to open " + args[1] + " for writing words");
			System.err.println("Program terminated\n");
			System.exit(1);
		}

		// array to keep track of character occurrence counts
		int[] charCountArray = new int[26];

		// initialize character counts
		for (int index = 0; index < charCountArray.length; index++) {
			charCountArray[index] = 0;
		}
		

		// process one file at a time
		for (int index = 0; index < fileCount; index++) {

			// open the input file, read one line at a time, extract words
			// in the line, extract characters in a word, write words and
			// character counts to disk files
			try {
				// get a BufferedReader object, which encapsulates
				// access to a (disk) file
				br = new BufferedReader(new FileReader(inputFileNames[index]));
				

				// as long as we have more lines to process, read a line
				// the following line is doing two things: makes an assignment
				// and serves as a boolean expression for while test
				while ((line = br.readLine()) != null) { 
					    
					// process the line by extracting words using the wordPattern
					wordMatcher = wordPattern.matcher(line);
					
					// if init is true true prev1 and prev2 will be set to the first and second words of the first document
					// and added to unigrams. This will only happen for the first loop to set the previous 2 words.
					if(init == true) {	
							
						wordMatcher.find();
						prev1 = line.substring(wordMatcher.start(), wordMatcher.end());
						prev1 = prev1.toLowerCase(); // gets the first word of the first document
						countCharFreq(charCountArray, alphabet, prev1); // count the chars of prev1
						
						wdWriter.println(prev1);
						
						// if prev1 is a stop word it will be set to the next word
						while(stopWords.contains(prev1)) {
							wordMatcher.find();
							prev1 = line.substring(wordMatcher.start(), wordMatcher.end());
							prev1 = prev1.toLowerCase();
							countCharFreq(charCountArray, alphabet, prev1); // count the chars of prev1
							wdWriter.println(prev1);
						}
						
						wordMatcher.find();
						prev2 = line.substring(wordMatcher.start(), wordMatcher.end());
						prev2 = prev2.toLowerCase(); // gets the word after prev1
						countCharFreq(charCountArray, alphabet, prev2);
						
						wdWriter.println(prev2);
						
						// if prev2 is a stop word it will be set to the next word
						while(stopWords.contains(prev2)) {
							wordMatcher.find();
							prev2 = line.substring(wordMatcher.start(), wordMatcher.end());
							prev2 = prev2.toLowerCase();
							countCharFreq(charCountArray, alphabet, prev2);
							wdWriter.println(prev2);
						}						
						
						insertUnigram(prev1, unigrams); // adds the first unigram to unigrams
						insertUnigram(prev2, unigrams); // adds the first unigram to unigrams
						
						// init is set to false so this if statement is not entered again
						init = false; 
					}
					
					// process one word at a time
					while (wordMatcher.find()) {
						
						// extract the word
						word = line.substring(wordMatcher.start(), wordMatcher.end());
						// set word to lowercase
						word = word.toLowerCase();
						// print word to output file with name args[1]
						wdWriter.println(word);
						// count the characters of the word
						countCharFreq(charCountArray, alphabet, word); 
						
						// checks to see if word is a stop word
						if(!stopWords.contains(word)) {
							
							// if word is not a stop word it is added as a unigram, bigram, and trigram
							insertUnigram(word, unigrams);
							insertBigram(prev2, word, bigrams);
							insertTrigram(prev1, prev2, word, trigrams);
							
							prev1 = prev2; // advances prev1 to the next word (the previous word)
							prev2 = word; // advance prev2 to the next word (the current word)
						} // end if -- if word is not a stop word       
					} // end while -- process one word.
				} // end while -- process one line
			} // end try
			catch (IOException ex) {
				System.err.println("File " + inputFileNames[index] + " not found. Program terminated.\n");
				System.exit(1);
			} // end catch
		} // end for -- process one file at a time

		
		// write letters and their counts to file named args[2]
		// open output file 2 for writing characters and their counts
		try {
			ccWriter = new PrintWriter(args[2], "UTF-8");
			System.out.println(args[2] + " successfully opened for writing character counts");
		} catch (IOException ex) {
			System.err.println("Unable to open " + args[2] + " for writing character counts");
			System.err.println("Program terminated\n");
			System.exit(1);
		}

		for (int index = 0; index < charCountArray.length; index++) {
			ccWriter.println(alphabet.charAt(index) + "\t" + charCountArray[index]);
		}

		// write unigrams and their frequencies to file named args[3]
		// open output file 3 for writing unigrams and their frequencies 
		try {
			uniWriter = new PrintWriter(args[3], "UTF-8");
			System.out.println(args[3] + " successfully opened for writing unigrams");
		} catch (IOException ex) {
			System.err.println("Unable to open " + args[3] + " for writing unigrams");
			System.err.println("Program terminated\n");
			System.exit(1);
		}	
		for (Map.Entry<String, Integer> entry : unigrams.entrySet()) {
			uniWriter.println(entry.getKey() + "\t" + (int) entry.getValue());
		}
	
		// write bigrams and their frequencies to file named args[4]
		// open output file 4 for writing unigrams and their frequencies
		try {
			biWriter = new PrintWriter(args[4], "UTF-8");
			System.out.println(args[4] + " successfully opened for writing bigrams");
		} catch (IOException ex) {
			System.err.println("Unable to open " + args[4] + " for bigrams");
			System.err.println("Program terminated\n");
			System.exit(1);
		}	
	
		for (Map.Entry<String, Integer> entry : bigrams.entrySet()) {
			biWriter.println(entry.getKey() + "\t" + (int) entry.getValue());
		}
		
		// write trigrams and their frequencies to file named args[5]
		// open output file 5 for writing trigrams and their frequencies
		try {
			triWriter = new PrintWriter(args[5], "UTF-8");
			System.out.println(args[5] + " successfully opened for writing trigrams");
		} catch (IOException ex) {
			System.err.println("Unable to open " + args[5] + " for trigrams");
			System.err.println("Program terminated\n");
			System.exit(1);
		}	
	
		for (Map.Entry<String, Integer> entry : trigrams.entrySet()) {
			triWriter.println(entry.getKey() + "\t" + (int) entry.getValue());
		}
		
		// close buffered reader.
		try {
			br.close();
		} catch (IOException ex) {
			System.err.println("Unable to close buffer reader");
			System.err.println("Program terminated\n");
			System.exit(1);
		}

		// close output file 1
		wdWriter.close();

		// close output file 2
		ccWriter.close();
		
		// close output file 3
		uniWriter.close();
		
		// close output file 4
		biWriter.close();
		
		// close output file 5
		triWriter.close();
	} // end main()
	
	// inserts a word into unigrams 
	public static void insertUnigram(String word, TreeMap<String, Integer> unigrams) {
		// if unigrams does not contain the word, it is added and it's frequency is set to 0
		if (!unigrams.containsKey(word))
			unigrams.put(word, 0);

		// adds 1 to the value of the word 
		unigrams.replace(word, unigrams.get(word)+1);
	} // end insertUnigram
	
	public static void insertBigram(String word1, String word2, TreeMap<String, Integer> bigrams) {
		// combines word1 and word2 into a biword
		String biword = word1 + " " + word2;
		
		// if bigrams does not contain the biword, it is added and it's frequency is set to 0
		if (!bigrams.containsKey(biword))
			bigrams.put(biword, 0);

		// adds 1 to the value of the word 
		bigrams.replace(biword, bigrams.get(biword)+1);
	} // end insertBigram
	
	public static void insertTrigram(String word1, String word2, String word3, TreeMap<String, Integer> trigrams) {
	// combines word1, word2 and word3 into a triword
		String triword = word1 + " " + word2 + " " + word3;
		
		// if trigrams does not contain the triword, it is added and it's frequency is set to 0
		if (!trigrams.containsKey(triword))
			trigrams.put(triword, 0);

		// adds 1 to the value of the word 
		trigrams.replace(triword, trigrams.get(triword)+1);
	} // end insertTrigram
	
	// counts the characters of a word and adds each character count to it's total
	public static void countCharFreq(int[] charCountArray, String alphabet, String word) {
		// process characters in a word
		for (int i = 0; i < word.length(); i++){          
      // if the character is a letter, increment the 
      // corresponding count, otherwise discard the character
      if (Character.isLetter(word.charAt(i)))
      	charCountArray[alphabet.indexOf(word.charAt(i))]++;							
		} // end for
	} // end countCharFreq
	
	// inserts stop words into stopWords 
	public static void insertStopWords(Set<String> stopWords) {
		stopWords.add("i");
		stopWords.add("me");
		stopWords.add("my");
		stopWords.add("myself");
		stopWords.add("we");
		stopWords.add("our");
		stopWords.add("ourselves");
		stopWords.add("you");
		stopWords.add("your");
		stopWords.add("yours");
		stopWords.add("yourself");
		stopWords.add("yourselves");
		stopWords.add("he");
		stopWords.add("him");
		stopWords.add("his");
		stopWords.add("himself");
		stopWords.add("she");
		stopWords.add("her");
		stopWords.add("hers");
		stopWords.add("herself");
		stopWords.add("it");
		stopWords.add("its");
		stopWords.add("itself");
		stopWords.add("they");
		stopWords.add("them");
		stopWords.add("their");
		stopWords.add("theirs");
		stopWords.add("themselves");
		stopWords.add("what");
		stopWords.add("which");
		stopWords.add("who");
		stopWords.add("whom");
		stopWords.add("this");
		stopWords.add("that");
		stopWords.add("these");
		stopWords.add("those");
		stopWords.add("am");
		stopWords.add("is");
		stopWords.add("are");
		stopWords.add("was");
		stopWords.add("were");
		stopWords.add("be");
		stopWords.add("been");
		stopWords.add("being");
		stopWords.add("have");
		stopWords.add("has");
		stopWords.add("had");
		stopWords.add("having");
		stopWords.add("do");
		stopWords.add("does");
		stopWords.add("did");
		stopWords.add("doing");
		stopWords.add("a");
		stopWords.add("an");
		stopWords.add("the");
		stopWords.add("and");
		stopWords.add("but");
		stopWords.add("if");
		stopWords.add("or");
		stopWords.add("because");
		stopWords.add("as");
		stopWords.add("until");
		stopWords.add("while");
		stopWords.add("of");
		stopWords.add("at");
		stopWords.add("by");
		stopWords.add("for");
		stopWords.add("with");
		stopWords.add("about");
		stopWords.add("against");
		stopWords.add("between");
		stopWords.add("into");
		stopWords.add("through");
		stopWords.add("during");
		stopWords.add("before");
		stopWords.add("after");
		stopWords.add("above");
		stopWords.add("below");
		stopWords.add("to");
		stopWords.add("from");
		stopWords.add("up");
		stopWords.add("down");
		stopWords.add("in");
		stopWords.add("out");
		stopWords.add("on");
		stopWords.add("off");
		stopWords.add("over");
		stopWords.add("under");
		stopWords.add("again");
		stopWords.add("further");
		stopWords.add("then");
		stopWords.add("once");
		stopWords.add("here");
		stopWords.add("there");
		stopWords.add("when");
		stopWords.add("where");
		stopWords.add("why");
		stopWords.add("how");
		stopWords.add("all");
		stopWords.add("any");
		stopWords.add("both");
		stopWords.add("each");
		stopWords.add("few");
		stopWords.add("more");
		stopWords.add("most");
		stopWords.add("other");
		stopWords.add("some");
		stopWords.add("such");
		stopWords.add("no");
		stopWords.add("nor");
		stopWords.add("not");
		stopWords.add("only");
		stopWords.add("own");
		stopWords.add("same");
		stopWords.add("so");
		stopWords.add("than");
		stopWords.add("too");
		stopWords.add("very");
		stopWords.add("s");
		stopWords.add("isn");
		stopWords.add("wasn");
		stopWords.add("weren");
		stopWords.add("don");
		stopWords.add("didn");
		stopWords.add("t");
		stopWords.add("can");
		stopWords.add("will");
		stopWords.add("ll");
		stopWords.add("just");
		stopWords.add("should");
		stopWords.add("now");
		stopWords.add("ve");
		stopWords.add("st");
		stopWords.add("would");
		stopWords.add("wouldn");
		stopWords.add("could");
		stopWords.add("couldn");
		stopWords.add("oh");
		stopWords.add("yes");
		stopWords.add("re");
		stopWords.add("d");
	} // end insertStopWords
	
} // class