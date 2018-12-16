package de.metas.bpartner.service;

import static de.metas.util.Check.isEmpty;
import static org.compiere.util.Util.errorIf;

/*
 * #%L
 * de.metas.adempiere.adempiere.base
 * %%
 * Copyright (C) 2015 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Nullable;

import org.adempiere.service.OrgId;
import org.compiere.model.I_AD_User;
import org.compiere.model.I_C_BP_Relation;
import org.compiere.model.I_C_BPartner;
import org.compiere.model.I_C_BPartner_Location;
import org.compiere.model.I_C_Location;
import org.compiere.util.Util;

import com.google.common.collect.ImmutableSet;

import de.metas.bpartner.BPartnerContactId;
import de.metas.bpartner.BPartnerId;
import de.metas.bpartner.BPartnerLocationId;
import de.metas.bpartner.BPartnerType;
import de.metas.lang.SOTrx;
import de.metas.pricing.PricingSystemId;
import de.metas.shipping.ShipperId;
import de.metas.util.ISingletonService;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

public interface IBPartnerDAO extends ISingletonService
{
	void save(I_C_BPartner bpartner);

	void save(I_C_BPartner_Location bpartnerLocation);

	void save(I_AD_User bpartnerContact);

	I_C_BPartner getById(final int bpartnerId);

	<T extends I_C_BPartner> T getById(int bpartnerId, Class<T> modelClass);

	I_C_BPartner getById(final BPartnerId bpartnerId);

	<T extends I_C_BPartner> T getById(BPartnerId bpartnerId, Class<T> modelClass);

	I_C_BPartner getByIdInTrx(BPartnerId bpartnerId);

	/**
	 * Retrieve {@link I_C_BPartner} assigned to given organization
	 *
	 * @param ctx
	 * @param orgId
	 * @param clazz
	 * @param trxName
	 * @return {@link I_C_BPartner}; never return null
	 * @throws OrgHasNoBPartnerLinkException if no partner was found
	 */
	<T extends I_C_BPartner> T retrieveOrgBPartner(Properties ctx, int orgId, Class<T> clazz, String trxName);

	Optional<BPartnerLocationId> getBPartnerLocationIdByExternalId(BPartnerId bpartnerId, String externalId);

	Optional<BPartnerLocationId> getBPartnerLocationIdByGln(BPartnerId bpartnerId, String gln);

	I_C_BPartner_Location getBPartnerLocationById(BPartnerLocationId bpartnerLocationId);

	boolean exists(BPartnerLocationId bpartnerLocationId);

	List<I_C_BPartner_Location> retrieveBPartnerLocations(final int bpartnerId);

	default List<I_C_BPartner_Location> retrieveBPartnerLocations(@NonNull final BPartnerId bpartnerId)
	{
		return retrieveBPartnerLocations(bpartnerId.getRepoId());
	}

	List<I_C_BPartner_Location> retrieveBPartnerLocations(Properties ctx, int bpartnerId, String trxName);

	List<I_C_BPartner_Location> retrieveBPartnerLocations(I_C_BPartner bpartner);

	default Set<Integer> retrieveBPartnerLocationCountryIds(@NonNull final BPartnerId bpartnerId)
	{
		return retrieveBPartnerLocations(bpartnerId)
				.stream()
				.map(I_C_BPartner_Location::getC_Location)
				.map(I_C_Location::getC_Country_ID)
				.collect(ImmutableSet.toImmutableSet());
	}

	/**
	 * @return Contacts of the partner, ordered by ad_user_ID, ascending
	 */
	List<I_AD_User> retrieveContacts(Properties ctx, int partnerId, String trxName);

	/**
	 * @return Contacts of the partner, ordered by ad_user_ID, ascending
	 */
	List<I_AD_User> retrieveContacts(I_C_BPartner bpartner);

	Optional<BPartnerContactId> getContactIdByExternalId(BPartnerId bpartnerId, String externalId);

	I_AD_User getContactById(BPartnerContactId contactId);

	/**
	 * Returns the <code>M_PricingSystem_ID</code> to use for a given bPartner.
	 *
	 *
	 * @param ctx
	 * @param bPartnerId the ID of the BPartner for which we need the pricing system id
	 * @param soTrx
	 *            <ul>
	 *            <li>if <code>true</code>, then the method first checks <code>C_BPartner.M_PricingSystem_ID</code> , then (if the BPartner has a C_BP_Group_ID) in
	 *            <code>C_BP_Group.M_PricingSystem_ID</code> and finally (if the C_BPArtner has a AD_Org_ID>0) in <code>AD_OrgInfo.M_PricingSystem_ID</code></li>
	 *            <li>if <code>false</code></li>, then the method first checks <code>C_BPartner.PO_PricingSystem_ID</code>, then (if the BPartner has a C_BP_Group_ID!) in
	 *            <code>C_BP_Group.PO_PricingSystem_ID</code>. Note that <code>AD_OrgInfo</code> has currently no <code>PO_PricingSystem_ID</code> column.
	 *            </ul>
	 * @param trxName
	 * @return M_PricingSystem_ID or 0
	 */
	PricingSystemId retrievePricingSystemId(Properties ctx, int bPartnerId, SOTrx soTrx, String trxName);

	PricingSystemId retrievePricingSystemId(BPartnerId bPartnerId, SOTrx soTrx);

	ShipperId getShipperId(BPartnerId bpartnerId);

	/**
	 * @param address
	 * @param po
	 * @param columnName
	 * @return true if an address with the flag columnName on true already exists in the table, false otherwise.
	 */
	boolean existsDefaultAddressInTable(I_C_BPartner_Location address, String trxName, String columnName);

	/**
	 * @param user
	 * @param trxName
	 * @return true if a contact with the flag defaultContact on true already exists in the table, false otherwise.
	 */
	boolean existsDefaultContactInTable(de.metas.adempiere.model.I_AD_User user, String trxName);

	/**
	 * Search after the BPartner when the value is given
	 *
	 * @param ctx
	 * @param value
	 * @return C_BPartner_Location object or null
	 */
	I_C_BPartner retrieveBPartnerByValue(Properties ctx, String value);

	/**
	 * Retrieve partner by exact value or by the ending string.
	 *
	 * Use case: why have BPartner-Values such as "G01234", but on ESR-payment documents, there is only "01234", because there it may only contain digits.
	 *
	 * @param ctx
	 * @param bpValue an exact bpartner value. Try to retrieve by that value first, if <code>null</code> or empty, directly try the fallback
	 * @param bpValueSuffixToFallback the suffix of a bpartner value. Only use if retrieval by <code>bpValue</code> produced no results. If <code>null</code> or empty, return <code>null</code>.
	 *
	 * @return a single bPartner or <code>null</code>
	 *
	 * @throws org.adempiere.exceptions.DBMoreThenOneRecordsFoundException if there is more than one matching partner.
	 */
	I_C_BPartner retrieveBPartnerByValueOrSuffix(Properties ctx, String bpValue, String bpValueSuffixToFallback);

	<T extends org.compiere.model.I_AD_User> T retrieveDefaultContactOrNull(I_C_BPartner bPartner, Class<T> clazz);

	/**
	 * Checks if there more BP Locations for given BP, excluding the given one.
	 *
	 * @param ctx
	 * @param bpartnerId
	 * @param excludeBPLocationId
	 * @param trxName
	 * @return true if there more BP locations for given BP, excluding the given one
	 */
	boolean hasMoreLocations(Properties ctx, int bpartnerId, int excludeBPLocationId, String trxName);

	/**
	 * Search the {@link I_C_BP_Relation}s for matching partner and location (note that the link without location is acceptable too)
	 *
	 * @param contextProvider
	 * @param partner
	 * @param location
	 * @return {@link I_C_BP_Relation} first encountered which is used for billing
	 */
	I_C_BP_Relation retrieveBillBPartnerRelationFirstEncountered(Object contextProvider, I_C_BPartner partner, I_C_BPartner_Location location);

	/**
	 * Retrieve default/first ship to location.
	 *
	 * @return ship to location or null
	 * @deprecated please consider using {@link #retrieveBPartnerLocation(BPartnerLocationQuery)} instead
	 */
	@Deprecated
	I_C_BPartner_Location retrieveShipToLocation(Properties ctx, int bPartnerId, String trxName);

	/**
	 * Retrieve all (active) ship to locations.
	 *
	 * NOTE: the default ship to location will be the first.
	 *
	 * @param bpartner
	 * @return all bpartner's ship to locations
	 */
	List<I_C_BPartner_Location> retrieveBPartnerShipToLocations(I_C_BPartner bpartner);

	/**
	 * Performs an non-strict search (e.g. if BP has only one address, it returns it even if it's not flagged as the default ShipTo address).
	 *
	 * @return bp location or null
	 */
	I_C_BPartner_Location getDefaultShipToLocation(BPartnerId bpartnerId);

	int getDefaultShipToLocationCountryId(BPartnerId bpartnerId);

	/**
	 * Retrieve default/first bill to location.
	 *
	 * @param ctx
	 * @param bPartnerId
	 * @param alsoTryBilltoRelation if <code>true</code> and the given partner has no billTo location, then the method also checks if there is a billTo-<code>C_BP_Relation</code> and if so, returns
	 *            that relation's bPartner location.
	 * @param trxName
	 * @return bill to location or null
	 * @deprecated please consider using {@link #retrieveBPartnerLocation(BPartnerLocationQuery)} instead
	 */
	@Deprecated
	I_C_BPartner_Location retrieveBillToLocation(Properties ctx,
			int bPartnerId,
			boolean alsoTryBilltoRelation,
			String trxName);

	/**
	 * Get the fit contact for the given partner and isSOTrx. In case of SOTrx, the salesContacts will have priority. Same for POTrx and PurcanseCOntacts In case of 2 entries with equal values in the
	 * fields above, the Default contact will have priority
	 *
	 * @param ctx
	 * @param bpartnerId
	 * @param isSOTrx
	 * @param trxName
	 * @return
	 */
	I_AD_User retrieveContact(Properties ctx, int bpartnerId, boolean isSOTrx, String trxName);

	Map<BPartnerId, Integer> retrieveAllDiscountSchemaIdsIndexedByBPartnerId(BPartnerType bpartnerType);

	BPartnerLocationId getBilltoDefaultLocationIdByBpartnerId(BPartnerId bpartnerId);

	BPartnerLocationId getShiptoDefaultLocationIdByBpartnerId(BPartnerId bpartnerId);

	String getBPartnerNameById(BPartnerId bpartnerId);

	Optional<BPartnerId> retrieveBPartnerIdBy(BPartnerQuery query);

	/**
	 * If there is at least one bPartner with the given {@code externalId} and either the given {@code orgId} or (depending on {@code includeAnyOrg}) {@code AD_Org_ID=0} (i.e. {@link OrgId#ANY}),
	 * The return it. Prefer the one with the specific orgId over the one with orgId "ANY".
	 * <p>
	 * If there is at least one bPartner with the given {@code bpartnerValue} and either the given {@code orgId} or (depending on {@code includeAnyOrg}) {@code AD_Org_ID=0} (i.e. {@link OrgId#ANY}),
	 * The return it. Prefer the one with the specific orgId over the one with orgId "ANY".
	 * <p>
	 * If there is at least one bPartner a bPartner-Location that has the given {@code locatorGln} and either the given {@code orgId} or (depending on {@code includeAnyOrg}) {@code AD_Org_ID=0} (i.e. {@link OrgId#ANY}),
	 * The return it. Prefer the one with the specific orgId over the one with orgId "ANY".
	 */
	@Value
	public static class BPartnerQuery
	{
		String bpartnerValue;
		String locatorGln;
		String externalId;
		OrgId orgId;

		boolean includeAnyOrg;
		boolean outOfTrx;
		boolean failIfNotExists;

		@Builder
		private BPartnerQuery(
				@Nullable final String externalId,
				@Nullable final String bpartnerValue,
				@Nullable final String locatorGln,
				@NonNull final OrgId orgId,
				@Nullable final Boolean includeAnyOrg,
				@Nullable final Boolean outOfTrx,
				@Nullable final Boolean failIfNotExists)
		{

			this.bpartnerValue = bpartnerValue;
			this.locatorGln = locatorGln;
			this.externalId = externalId;
			errorIf(isEmpty(bpartnerValue, true) && isEmpty(externalId, true) && isEmpty(locatorGln, true),
					"At least one of the given bpartnerValue, locatorGln or externalId needs to be non-empty");

			this.orgId = orgId;

			this.includeAnyOrg = Util.coalesce(includeAnyOrg, false);
			this.outOfTrx = Util.coalesce(outOfTrx, false);
			this.failIfNotExists = Util.coalesce(failIfNotExists, false);
		}
	}

	I_C_BPartner_Location retrieveBPartnerLocation(BPartnerLocationQuery query);

	@Value
	@Builder
	public static class BPartnerLocationQuery
	{
		public enum Type
		{
			BILL_TO, SHIP_TO, REMIT_TO;
		}

		@NonNull
		BPartnerId bpartnerId;

		@NonNull
		Type type;

		boolean alsoTryRelation;
	}
}
