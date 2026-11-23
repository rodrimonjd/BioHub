package co.edu.unbosque.biohub.autorizacion.usuario;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ConfiguracionClaves {

    @Bean
    public PasswordEncoder codificadorClaves() {
        return new BCryptPasswordEncoder();
    }
}
