package com.stakevault.betting.bets.adapter.in.web;

import java.net.URI;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.stakevault.betting.bets.domain.model.BettingHouseAlreadyRegisteredException;
import com.stakevault.betting.bets.domain.model.BettingHouseNotFoundException;
import com.stakevault.betting.bets.domain.model.CatalogAlreadyRegisteredException;
import com.stakevault.betting.bets.domain.model.InvalidTenantSlugException;
import com.stakevault.betting.bets.domain.model.LocalizedDomainException;
import com.stakevault.betting.bets.domain.model.TenantAlreadyProvisionedException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class DomainExceptionHandler {

	private final MessageSource messageSource;

	public DomainExceptionHandler(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@ExceptionHandler({ TenantAlreadyProvisionedException.class, InvalidTenantSlugException.class,
			CatalogAlreadyRegisteredException.class, BettingHouseAlreadyRegisteredException.class,
			BettingHouseNotFoundException.class })
	public ProblemDetail handle(LocalizedDomainException exception, Locale locale, HttpServletRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.valueOf(exception.httpStatusCode()),
				ProblemDetailMessages.detail(exception, locale, messageSource));
		problem.setTitle(ProblemDetailMessages.title(exception, locale, messageSource));
		problem.setType(URI.create("https://docs/errors/" + ProblemDetailMessages.typeSlug(exception)));
		problem.setInstance(URI.create(request.getRequestURI()));
		return problem;
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleValidation(MethodArgumentNotValidException exception, Locale locale,
			HttpServletRequest request) {
		String fields = exception.getBindingResult().getFieldErrors().stream()
				.map(FieldError::getField)
				.distinct()
				.collect(Collectors.joining(", "));
		return validationFailed(fields, locale, request);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ProblemDetail handleMalformedBody(Locale locale, HttpServletRequest request) {
		return validationFailed("body", locale, request);
	}

	private ProblemDetail validationFailed(String fields, Locale locale, HttpServletRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
				messageSource.getMessage("error.validation-failed.detail", new Object[] { fields }, locale));
		problem.setTitle(messageSource.getMessage("error.validation-failed.title", null, locale));
		problem.setType(URI.create("https://docs/errors/validation-failed"));
		problem.setInstance(URI.create(request.getRequestURI()));
		return problem;
	}
}
