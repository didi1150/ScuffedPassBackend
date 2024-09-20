package me.didi.PWMBackend.exceptions;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Thrown if an {@link UserDetailsService} implementation cannot locate a {@link User} by
 * its username.
 *
 * @author Dezhong Zhuang
 */
@SuppressWarnings("serial")
public class UserIdNotFoundException extends AuthenticationException {
	/**
	 * Constructs a <code>UserIdNotFoundException</code> with the specified
	 * message.
	 * 
	 * @param msg the detail message.
	 */
	public UserIdNotFoundException(String msg) {
		super(msg);
	}

	/**
	 * Constructs a {@code UsernameNotFoundException} with the specified message and
	 * root cause.
	 * 
	 * @param msg   the detail message.
	 * @param cause root cause
	 */
	public UserIdNotFoundException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
