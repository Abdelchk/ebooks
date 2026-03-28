package fr.ensitech.ebooks.securingweb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

	@Autowired
	private PasswordExpirationInterceptor passwordExpirationInterceptor;

	// DÉSACTIVÉ - Nous n'utilisons plus les vues Thymeleaf, tout est géré par React
//	public void addViewControllers(ViewControllerRegistry registry) {
//		registry.addViewController("/index").setViewName("index");
//		registry.addViewController("/login").setViewName("login");
//		registry.addViewController("/register").setViewName("register");
//		registry.addViewController("/accueil").setViewName("accueil");
//	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// DÉSACTIVÉ - L'intercepteur n'est plus nécessaire car tout passe par les APIs REST
		// registry.addInterceptor(passwordExpirationInterceptor)
		// 		.addPathPatterns("/**")
		// 		.excludePathPatterns("/login", "/register", "/verify-email", "/forgot-password", "/reset-password", "/css/**", "/js/**");
	}

}
