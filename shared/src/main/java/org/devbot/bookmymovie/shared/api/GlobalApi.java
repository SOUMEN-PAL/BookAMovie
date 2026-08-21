package org.devbot.bookmymovie.shared.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RestController
@RequestMapping({ApiPaths.APP, ApiPaths.WEB})
public @interface GlobalApi {
}
