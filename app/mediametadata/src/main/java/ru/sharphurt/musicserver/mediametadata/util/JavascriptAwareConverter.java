package ru.sharphurt.musicserver.mediametadata.util;

import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;

@UtilityClass
public class JavascriptAwareConverter {

    public static JacksonJsonHttpMessageConverter javascriptAwareConverter() {
        JacksonJsonHttpMessageConverter converter = new JacksonJsonHttpMessageConverter();
        converter.setSupportedMediaTypes(
            List.of(new MediaType("text", "javascript", StandardCharsets.UTF_8)));
        return converter;
    }

}
