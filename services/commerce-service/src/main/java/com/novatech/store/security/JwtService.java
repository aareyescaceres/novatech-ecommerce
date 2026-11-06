package com.novatech.store.security;

import com.novatech.store.user.AppUser;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final JwtEncoder encoder;
    private final long hours;
    public JwtService(JwtEncoder encoder,@Value("${app.jwt.hours}") long hours) {
        this.encoder = encoder;
        this.hours = hours;
    }

    public String token(AppUser user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer("novatech-store").subject(user.id.toString()).issuedAt(now).expiresAt(now.plus(hours, ChronoUnit.HOURS)).claim("email", user.email).claim("role", user.role).build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
