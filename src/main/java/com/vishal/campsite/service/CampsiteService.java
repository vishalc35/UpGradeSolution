package com.vishal.campsite.service;

import com.vishal.campsite.model.Campsite;
import com.vishal.campsite.model.Reserve;

import java.time.LocalDate;
import java.util.List;

public interface CampsiteService {

    Campsite getCampsiteAvailability(Long campsiteId, LocalDate arrivalDate, LocalDate departureDate);

    boolean existsCampsite(Long campsiteId);

    void reserve(Reserve reserve, Long campsiteId);

    boolean existsReserve(Long reserveId);

    void deleteReserve(Long reserveId);

    void updateReserve(Reserve reserve, Long reserveId);

    List<Campsite> getCampsitesAvailability(LocalDate arrivalDate, LocalDate departureDate);

    Reserve getReserve(Long reserveId);

    List<Reserve> getAllReserves();
}
