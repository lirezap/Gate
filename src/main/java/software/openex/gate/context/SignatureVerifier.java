/*
 * ISC License
 *
 * Copyright (c) 2025, Alireza Pourtaghi <lirezap@protonmail.com>
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package software.openex.gate.context;

import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.slf4j.Logger;

import java.io.IOException;
import java.security.PEMDecoder;
import java.security.Provider;
import java.security.PublicKey;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readString;
import static java.nio.file.Path.of;
import static java.security.Signature.getInstance;
import static java.util.Base64.getDecoder;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Digital signature verifier component.
 *
 * @author Alireza Pourtaghi
 */
public final class SignatureVerifier {
    private static final Logger logger = getLogger(SignatureVerifier.class);

    private final Provider provider;
    private final PublicKey publicKey;

    SignatureVerifier(final Configuration configuration) {
        this.provider = new BouncyCastleFipsProvider();
        this.publicKey = decodePublicKey(configuration.loadString("signature.public_key_path"));
    }

    public boolean verify(final String content, final String signature) {
        return verify(content.getBytes(UTF_8), signature.getBytes(UTF_8));
    }

    public boolean verify(final byte[] contentBytes, final byte[] signatureBytes) {
        try {
            // TODO: Improve performance by using objects pool.
            final var signature = getInstance("SHA3-512withRSA", provider);
            signature.initVerify(publicKey);
            signature.update(contentBytes);

            return signature.verify(getDecoder().decode(signatureBytes));
        } catch (Exception ex) {
            logger.error("could not verify content: {} with signature: {}", new String(contentBytes), new String(signatureBytes));
            return false;
        }
    }

    private PublicKey decodePublicKey(final String publicKeyPath) {
        try {
            return PEMDecoder.of().decode(readString(of(publicKeyPath)), PublicKey.class);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
