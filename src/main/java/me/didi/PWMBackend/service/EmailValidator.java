package me.didi.PWMBackend.service;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

@Service
public class EmailValidator implements Predicate<String> {

	private static final String regexPattern = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";

	@Override
	public boolean test(String email) {
		return patternMatches(email);
	}

	private boolean patternMatches(String emailAddress) {
		return Pattern.compile(regexPattern).matcher(emailAddress).matches();
	}

}
