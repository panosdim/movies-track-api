package eu.deltasw.movies_track_api.config;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public class MyRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.reflection().registerType(io.jsonwebtoken.impl.security.KeysBridge.class);
        hints.reflection().registerType(io.jsonwebtoken.impl.DefaultJwtParserBuilder.class);
        hints.reflection().registerType(io.jsonwebtoken.jackson.io.JacksonSerializer.class);
    }
}
