package com.tsystems.javaschool.repos;

import com.tsystems.javaschool.entities.City;
import com.tsystems.javaschool.entities.Truck;

import java.util.List;

/**
 * Created by Igor Avdeev on 8/24/16.
 */
public class TruckRepository extends BaseRepository {
    public TruckRepository() {
        super(Truck.class);
    }

    public List<Truck> findReadyToGoTrucks(City city, int minCapacity)
    {
        return createEm().createQuery("select t from Truck t " +
                "where t.condition = :condition and t.capacityKg >= :min_capacity and t.city = :city")
                .setParameter("condition", Truck.Condition.OK)
                .setParameter("min_capacity", minCapacity)
                .setParameter("city", city)
                .getResultList();
    }
}
