package fr.songbird.exception;


/**
 * Classe chargee de soulever une exception lorsqu'une chaine de caracteres est trop grande
 * @author songbird
 * @since 0.0.1_0-ALPHA
 */
public class TooBigStringException extends Exception{


	private static final long serialVersionUID = 1L;

	public TooBigStringException(String str)
	{
		super(str);
	}
	
	
}
