/**
 * Copyright (C) 2012 - 2013 Xeiam LLC http://xeiam.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.xeiam.xchange.bitstamp.service;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.FormParam;

import si.mazi.rescu.ParamsDigest;
import si.mazi.rescu.RestInvocation;

/**
 * @author Benedikt Bünz
 */
public class BitstampDigest implements ParamsDigest {

  private static final String HMAC_SHA_256 = "HmacSHA256";

  private final Mac mac256;
  private final String clientId;
  private final String apiKey;

  /**
   * Constructor
   * 
   *
   * @param secretKeyBase64
   * @param clientId
   *@param apiKey @throws IllegalArgumentException if key is invalid (cannot be base-64-decoded or the decoded key is invalid).
   */
  private BitstampDigest(String secretKeyBase64, String clientId, String apiKey) throws IllegalArgumentException {

    this.clientId = clientId;
    this.apiKey = apiKey;
    try {
      mac256 = Mac.getInstance(HMAC_SHA_256);
      mac256.init(new SecretKeySpec(secretKeyBase64.getBytes(), HMAC_SHA_256));
    } catch (InvalidKeyException e) {
      throw new IllegalArgumentException("Invalid key for hmac initialization.", e);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Illegal algorithm for post body digest. Check the implementation.");
    }
  }

  public static BitstampDigest createInstance(String secretKeyBase64, String clientId, String apiKey) throws IllegalArgumentException {

    return secretKeyBase64 == null ? null : new BitstampDigest(secretKeyBase64, clientId, apiKey);
  }

  @Override
  public String digestParams(RestInvocation restInvocation) {

    mac256.update(restInvocation.getParamValue(FormParam.class, "nonce").toString().getBytes());
    mac256.update(clientId.getBytes());
    mac256.update(apiKey.getBytes());

    return String.format("%064x", new BigInteger(1, mac256.doFinal())).toUpperCase();
//    return Base64.encodeBytes(mac256.doFinal()).trim();
  }
}