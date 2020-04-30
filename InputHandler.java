package minesweeper;

import java.util.InputMismatchException;


public class InputHandler {

	/** Throws exception if the input is empty */
	public static void notEmptyOrThrow(String test) throws SelectionException {
		if (test.isEmpty()) {
			throw new SelectionException();
		}
	}
	
	public static void singleCharacterOrThrow(String test) throws SelectionException{
		if(test.length() != 1) {
			throw new SelectionException("Must be a single character");
		}
	}

	/** Throws exception if the input string is not strictly numerical */
	public static void isNumbersOrThrow(String test) throws SelectionException {
		if (!test.matches("[+-]?\\d*(\\.\\d+)?") || test.matches("[+-]*?")) {
			throw new SelectionException("Selection must be a number");
		}
	}

	/** Given the name of the exception an integer to be tested and an upper and
	 *  lower bound Throws SelectionException if test is outside of those bounds */
	public static void inRangeOrThrow(String name, int test, int a, int b) throws SelectionException {
		if (test < a || test > b) {
			throw new SelectionException(name + " must be between " + a + " and " + b);
		}
	}

	/** Throws exception if the input is not a letter */
	public static void isAlphabeticalOrThrow(String test) throws WordException {
		if (!test.matches("^[a-zA-Z]*$")) {
			throw new WordException("must only contain letters");
		}
	}
	
	/** Throws exception if the input contains any whitespace*/
	public static void noWhiteSpaceOrThrow(String test) throws SelectionException{
		if(test.matches("^(.*\\s+.*)+$")) {
			throw new SelectionException("Cannot contain whitespace");
		}
	}
	
	
}


@SuppressWarnings("serial")
class WordException extends InputMismatchException{

	WordException(){
		super("Cannot contain numbers or special character");
	}
	
	WordException(String message){
		super(message);
	}
	
}


@SuppressWarnings("serial")
class SelectionException extends IndexOutOfBoundsException{

	SelectionException(){
		super("You didn't enter anything");
	}
	
	SelectionException(String message){
		super(message);
	}
	
}

