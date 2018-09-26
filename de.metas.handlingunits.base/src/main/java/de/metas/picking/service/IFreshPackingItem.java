package de.metas.picking.service;

import java.util.function.Predicate;

import de.metas.handlingunits.HUPIItemProductId;
import de.metas.inoutcandidate.model.I_M_ShipmentSchedule;
import de.metas.picking.legacy.form.IPackingItem;
import de.metas.quantity.Quantity;

/*
 * #%L
 * de.metas.fresh.base
 * %%
 * Copyright (C) 2016 metas GmbH
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

/**
 * You can obtain an instance via {@link FreshPackingItemHelper#create(java.util.Map)}.
 *
 * @author metas-dev <dev@metasfresh.com>
 *
 */
public interface IFreshPackingItem extends IPackingItem
{
	@Override
	IFreshPackingItem copy();

	HUPIItemProductId getHUPIItemProductId();

	@Override
	IFreshPackingItem subtractToPackingItem(Quantity subtrahent, Predicate<I_M_ShipmentSchedule> acceptShipmentSchedulePredicate);
}
