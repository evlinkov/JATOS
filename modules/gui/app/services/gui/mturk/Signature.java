package services.gui.mturk;

import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import play.Logger;
import play.Logger.ALogger;

/**
 * This class defines common routines for generating authentication signatures
 * for AWS Platform requests.
 */
public class Signature {

	private static final ALogger LOGGER = Logger.of(Signature.class);

	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

	/**
	 * Computes RFC 2104-compliant HMAC signature.
	 * 
	 * @param data
	 *            The data to be signed.
	 * @param key
	 *            The signing key.
	 * @return The Base64-encoded RFC 2104-compliant HMAC signature.
	 * @throws java.security.SignatureException
	 *             when signature generation fails
	 */
	public static String calculateRFC2104HMAC(String data, String key) {
		String result = null;
		try {
			// get an hmac_sha1 key from the raw key bytes
			SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(),
					HMAC_SHA1_ALGORITHM);

			// get an hmac_sha1 Mac instance and initialize with the signing key
			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(signingKey);

			// compute the hmac on input data bytes
			byte[] rawHmac = mac.doFinal(data.getBytes());

			// base64-encode the hmac
			// result = Encoding.EncodeBase64(rawHmac);
			result = Base64.getEncoder().encodeToString(rawHmac);
		} catch (Exception e) {
			// throw new SignatureException(
			// "Failed to generate HMAC : " + e.getMessage());
			// TODO
			LOGGER.error("Failed to generate HMAC : ", e);
		}
		return result;
	}
}