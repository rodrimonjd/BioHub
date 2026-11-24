package co.edu.unbosque.biohub.autorizacion.identidad;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.text.ParseException;

public final class ClaveFirma {

    private static final Logger log = LoggerFactory.getLogger(ClaveFirma.class);
    private static final int BITS = 2048;

    private ClaveFirma() {}

    public static RSAKey cargarOCrear(Path archivo) {
        try {
            if (Files.exists(archivo)) {
                RSAKey clave = RSAKey.parse(Files.readString(archivo));
                if (!clave.isPrivate()) throw new IllegalStateException("El archivo " + archivo + " no tiene la clave privada");
                return clave;
            }
            RSAKey clave = new RSAKeyGenerator(BITS).keyIDFromThumbprint(true).generate();
            guardar(archivo, clave);
            log.info("Se creó la clave de firma de sesiones en {}", archivo.toAbsolutePath());
            return clave;
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer o guardar la clave de firma en " + archivo, e);
        } catch (ParseException | JOSEException e) {
            throw new IllegalStateException("La clave de firma de " + archivo + " no es válida", e);
        }
    }

    private static void guardar(Path archivo, RSAKey clave) throws IOException {
        Path carpeta = archivo.toAbsolutePath().getParent();
        Files.createDirectories(carpeta);
        if (archivo.getFileSystem().supportedFileAttributeViews().contains("posix")) {
            Files.createFile(archivo, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")));
        } else {
            Files.createFile(archivo);
        }
        Files.writeString(archivo, clave.toJSONString(), StandardOpenOption.TRUNCATE_EXISTING);
    }
}
