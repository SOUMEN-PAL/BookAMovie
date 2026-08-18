package org.devbot.bookmymovie.shared.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * USER / mobile API under {@value ApiPaths#APP}.
 * Map resources on methods ({@code @GetMapping("/movies")}), not a second type-level
 * {@code @RequestMapping} — Spring will not concatenate two type-level mappings.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RestController
@RequestMapping(ApiPaths.APP)
public @interface AppApi {}
