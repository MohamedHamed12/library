package io.pillopl.library.lending.patronprofile.web;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsConfiguration;

import static org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType.HAL_FORMS;

@Configuration
@EnableAutoConfiguration
@EnableHypermediaSupport(type = HAL_FORMS)
@ComponentScan
public class WebConfiguration {

    @Bean
    HalFormsConfiguration halFormsConfiguration() {
        return new HalFormsConfiguration()
                .withDefaultSingleTemplate(true);
    }
}
