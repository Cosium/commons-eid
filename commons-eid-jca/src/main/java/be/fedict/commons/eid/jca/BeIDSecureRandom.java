/*
 * Commons eID Project.
 * Copyright (C) 2014 - 2018 BOSA.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License version 3.0 as published by
 * the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, see https://www.gnu.org/licenses/.
 */

package be.fedict.commons.eid.jca;

import be.fedict.commons.eid.client.BeIDCard;
import be.fedict.commons.eid.client.BeIDCards;
import be.fedict.commons.eid.client.CancelledException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.smartcardio.CardException;
import java.security.SecureRandomSpi;

/**
 * eID based implementation of a secure random generator. Can be used to seed
 * for example a mutual SSL handshake. This secure random generator does not
 * feature eID auto recovery.
 * <br>
 * Usage:
 * 
 * <pre>
 * SecureRandom secureRandom = SecureRandom.getInstance(&quot;BeID&quot;);
 * </pre>
 * 
 * @author Frank Cornelis
 * 
 */
public class BeIDSecureRandom extends SecureRandomSpi {

	private static final Log LOG = LogFactory.getLog(BeIDSecureRandom.class);

	private BeIDCard beIDCard;

	@Override
	protected void engineSetSeed(byte[] seed) {
		LOG.debug("engineSetSeed");
	}

	@Override
	protected void engineNextBytes(byte[] bytes) {
		LOG.debug("engineNextBytes: " + bytes.length + " bytes");
		BeIDCard beIDCard = getBeIDCard(false);
		byte[] randomData;
		try {
			try {
				randomData = beIDCard.getChallenge(bytes.length);
			} catch (Exception e) {
				beIDCard = getBeIDCard(true);
				randomData = beIDCard.getChallenge(bytes.length);
			}
		} catch (CardException e) {
			throw new RuntimeException(e);
		}
		System.arraycopy(randomData, 0, bytes, 0, bytes.length);
	}

	@Override
	protected byte[] engineGenerateSeed(int numBytes) {
		LOG.debug("engineGenerateSeed: " + numBytes + " bytes");
		BeIDCard beIDCard = getBeIDCard(false);
		byte[] randomData;
		try {
			randomData = beIDCard.getChallenge(numBytes);
		} catch (CardException e) {
			throw new RuntimeException(e);
		}
		return randomData;
	}

	private BeIDCard getBeIDCard(boolean autoRecover) {
		if (autoRecover) {
			beIDCard = null;
		}
		if (null != beIDCard) {
			return beIDCard;
		}

		BeIDCards beIDCards = new BeIDCards();
		try {
			beIDCard = beIDCards.getOneBeIDCard();
		} catch (CancelledException e) {
			throw new RuntimeException(e);
		}

		return beIDCard;
	}
}
