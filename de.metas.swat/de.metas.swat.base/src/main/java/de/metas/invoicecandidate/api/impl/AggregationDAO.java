package de.metas.invoicecandidate.api.impl;

/*
 * #%L
 * de.metas.swat.base
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */


import java.util.Properties;

import org.adempiere.ad.dao.IQueryBL;
import org.adempiere.model.InterfaceWrapperHelper;
import org.compiere.model.Query;

import de.metas.adempiere.model.I_M_Product;
import de.metas.invoicecandidate.api.IAggregationDAO;
import de.metas.invoicecandidate.model.I_C_Invoice_Candidate;
import de.metas.invoicecandidate.model.I_C_Invoice_Candidate_Agg;
import de.metas.invoicecandidate.model.I_C_Invoice_Candidate_HeaderAggregation;
import de.metas.invoicecandidate.model.I_M_ProductGroup;
import de.metas.invoicecandidate.model.I_M_ProductGroup_Product;
import de.metas.util.Check;
import de.metas.util.Services;

public class AggregationDAO implements IAggregationDAO
{
	@Override
	public I_C_Invoice_Candidate_Agg retrieveAggregate(final I_C_Invoice_Candidate ic)
	{
		final Properties ctx = InterfaceWrapperHelper.getCtx(ic);
		final String trxName = InterfaceWrapperHelper.getTrxName(ic);

		final String wc =
				"true "
						+ " AND ("
						+ "  COALESCE(" + I_C_Invoice_Candidate_Agg.COLUMNNAME_AD_Org_ID + ",0)=0 OR "
						+ I_C_Invoice_Candidate_Agg.COLUMNNAME_AD_Org_ID + "=?"
						+ ") AND ("
						+ "  COALESCE(" + I_C_Invoice_Candidate_Agg.COLUMNNAME_C_BPartner_ID + ",0)=0 OR "
						+ I_C_Invoice_Candidate_Agg.COLUMNNAME_C_BPartner_ID + "=?"
						+ ") AND ("
						+ "  COALESCE(" + I_C_Invoice_Candidate_Agg.COLUMNNAME_M_ProductGroup_ID + ",0)=0 OR "
						+ I_C_Invoice_Candidate_Agg.COLUMNNAME_M_ProductGroup_ID + " IN ("
						+ "   select pg." + I_M_ProductGroup.COLUMNNAME_M_ProductGroup_ID
						+ "   from " + I_M_ProductGroup.Table_Name + " pg "
						+ "      join " + I_M_ProductGroup_Product.Table_Name + " pp on"
						+ "        pp." + I_M_ProductGroup_Product.COLUMNNAME_M_ProductGroup_ID + "=pg." + I_M_ProductGroup.COLUMNNAME_M_ProductGroup_ID
						+ "   where "
						+ "      pg." + I_M_ProductGroup.COLUMNNAME_IsActive + "='Y' AND "
						+ "      pp." + I_M_ProductGroup_Product.COLUMNNAME_IsActive + "='Y' AND "
						+ "      pp." + I_M_ProductGroup_Product.COLUMNNAME_M_Product_ID + "=? OR ("
						+ "         COALESCE(" + I_M_ProductGroup_Product.COLUMNNAME_M_Product_ID + ",0)=0 AND "
						+ "         " + I_M_ProductGroup_Product.COLUMNNAME_M_Product_Category_ID + "=?"
						+ "      )"
						+ "))";

		final String orderBy =
				// I_C_Invoice_Candidate_Agg.COLUMNNAME_C_BPartner_ID + " DESC";
				I_C_Invoice_Candidate_Agg.COLUMNNAME_SeqNo + ", "
						+ I_C_Invoice_Candidate_Agg.COLUMNNAME_AD_Org_ID + " DESC, "
						+ I_C_Invoice_Candidate_Agg.COLUMNNAME_C_Invoice_Candidate_Agg_ID;

		final I_M_Product product = InterfaceWrapperHelper.create(ic.getM_Product(), I_M_Product.class);

		return new Query(ctx, I_C_Invoice_Candidate_Agg.Table_Name, wc, trxName)
				.setParameters(
						ic.getAD_Org_ID(),
						ic.getBill_BPartner_ID(),
						product == null ? 0 : product.getM_Product_ID(),
						product == null ? 0 : product.getM_Product_Category_ID())
				.setOnlyActiveRecords(true)
				.setApplyAccessFilter(true)
				.setOrderBy(orderBy)
				.first(I_C_Invoice_Candidate_Agg.class);
	}

	@Override
	public int findC_Invoice_Candidate_HeaderAggregationKey_ID(I_C_Invoice_Candidate ic)
	{
		final String headerAggregationKeyCalc = ic.getHeaderAggregationKey_Calc();
		if (Check.isEmpty(headerAggregationKeyCalc, true))
		{
			return -1;
		}

		final int bpartnerId = ic.getBill_BPartner_ID();
		if (bpartnerId <= 0)
		{
			return -1;
		}

		//
		// Find existing header aggregation key ID
		final int headerAggregationKeyId = Services.get(IQueryBL.class)
				.createQueryBuilder(I_C_Invoice_Candidate_HeaderAggregation.class, ic)
				.addEqualsFilter(I_C_Invoice_Candidate_HeaderAggregation.COLUMN_HeaderAggregationKey, headerAggregationKeyCalc)
				.create()
				.firstIdOnly();
		if (headerAggregationKeyId > 0)
		{
			return headerAggregationKeyId;
		}

		//
		// Create a new header aggregation key record and return it
		final I_C_Invoice_Candidate_HeaderAggregation headerAggregationKeyRecord = InterfaceWrapperHelper.newInstance(I_C_Invoice_Candidate_HeaderAggregation.class, ic);
		headerAggregationKeyRecord.setHeaderAggregationKey(headerAggregationKeyCalc);
		headerAggregationKeyRecord.setHeaderAggregationKeyBuilder_ID(ic.getHeaderAggregationKeyBuilder_ID());
		headerAggregationKeyRecord.setC_BPartner_ID(bpartnerId);
		headerAggregationKeyRecord.setIsSOTrx(ic.isSOTrx());
		headerAggregationKeyRecord.setIsActive(true);
		InterfaceWrapperHelper.save(headerAggregationKeyRecord);
		return headerAggregationKeyRecord.getC_Invoice_Candidate_HeaderAggregation_ID();
	}
}
