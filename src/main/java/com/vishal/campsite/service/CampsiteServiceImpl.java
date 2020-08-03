package com.vishal.campsite.service;


import com.vishal.campsite.model.Campsite;
import com.vishal.campsite.model.Reserve;
import com.vishal.campsite.repository.CampsiteRepository;
import com.vishal.campsite.repository.ReserveRepository;
import com.vishal.campsite.util.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service("campsiteService")
public class CampsiteServiceImpl implements CampsiteService {
    @Autowired
    CampsiteRepository campsiteRepository;

    @Autowired
    ReserveRepository reserveRepository;

    @PersistenceContext
    EntityManager entityManager;

    private static final Logger logger = LoggerFactory.getLogger(CampsiteServiceImpl.class);

    @Override
    public Campsite getCampsiteAvailability(Long campsiteId, LocalDate fromDate, LocalDate toDate) {

        LocalDate today = LocalDate.now();

        if (fromDate != null && toDate != null) {
            if (fromDate.isEqual(toDate))
                throw new RuntimeException("fromDate and toDate could not be the same");
            if (fromDate.isBefore(today) || fromDate.isEqual(today))
                throw new RuntimeException("fromDate could not be equal or before current date");
            if (toDate.isBefore(today) || toDate.isEqual(today))
                throw new RuntimeException("toDate could not be equal or before current date");
            if (toDate.isBefore(fromDate))
                throw new RuntimeException("toDate should be after fromDate");
        } else {
            if (fromDate != null ^ toDate != null) {
                throw new RuntimeException("Cannot send only one date");
            } else {
                fromDate = today.plusDays(Utilities.MINIMUM_DAYS_AHEAD);
                toDate = fromDate.plusMonths(Utilities.MAXIMUM_MONTHS_AHEAD);
            }
        }

        logger.debug("From date => " + fromDate);
        logger.debug("To date => " + toDate);

        Campsite campsite = campsiteRepository.findById(campsiteId).get();
        if (campsite != null) {
            campsite.setAvailableDays(new HashSet<Long>());
            for (LocalDate i = fromDate; i.isBefore(toDate); i = i.plusDays(1)) {
                if (campsite.getReserves().isEmpty()) {
                    Long availDay = Instant.from(i.atStartOfDay(ZoneId.systemDefault()).toInstant()).toEpochMilli();
                    campsite.getAvailableDays().add(availDay);
                    logger.debug("Available date => " + i);
                } else {
                    for (Reserve r: campsite.getReserves()) {
                        if (!dateOverlapsWithReserve(i, r)) {
                            Long availDay = Instant.from(i.atStartOfDay(ZoneId.systemDefault()).toInstant()).toEpochMilli();
                            campsite.getAvailableDays().add(availDay);
                            logger.debug("Available date => " + i);
                        } else {
                            break;
                        }
                    }
                }
            }
        }
        return campsite;
    }

    @Override
    public boolean existsCampsite(Long campsiteId) {
        return campsiteRepository.existsById(campsiteId);
    }

    @Override
    public synchronized void reserve(Reserve reserve, Long campsiteId) {
        validateCreateReserve(reserve, campsiteId);
        reserve.setCampsite(campsiteRepository.findById(campsiteId).get());
        reserveRepository.save(reserve);
    }

    @Override
    public boolean existsReserve(Long reserveId) {
        return reserveRepository.findById(reserveId).isPresent();
    }

    @Override
    public void deleteReserve(Long reserveId) {
        reserveRepository.deleteById(reserveId);
    }

    @Override
    public void updateReserve(Reserve reserve, Long reserveId) {
        validateUpdateReserve(reserve, reserveId);
        Reserve oldReserve = reserveRepository.findById(reserveId).get();
        oldReserve.setEmail(reserve.getEmail());
        oldReserve.setFullName(reserve.getFullName());
        oldReserve.setArrivalDate(reserve.getArrivalDate());
        oldReserve.setDepartureDate(reserve.getDepartureDate());
        reserveRepository.save(oldReserve);
    }

    @Override
    public List<Campsite> getCampsitesAvailability(LocalDate arrivalDate, LocalDate departureDate) {
        List<Campsite> campsites = (List<Campsite>) campsiteRepository.findAll();
        for (Campsite c: campsites) {
            getCampsiteAvailability(c.getId(), arrivalDate, departureDate);
        }
        return campsites;
    }

    @Override
    public Reserve getReserve(Long reserveId) {
        return reserveRepository.findById(reserveId).get();
    }

    @Override
    public List<Reserve> getAllReserves() {
        List<Reserve> reserves = new ArrayList<Reserve>();
        for (Reserve r: reserveRepository.findAll()) {
            reserves.add(r);
        }
        return reserves;
    }

    private boolean dateOverlapsWithReserve(LocalDate i, Reserve r) {
        LocalDate arrivalDate = Utilities.getDateFromUnixTime(r.getArrivalDate());
        LocalDate departureDate = Utilities.getDateFromUnixTime(r.getDepartureDate());
        if ( (i.isEqual(arrivalDate) || i.isAfter(arrivalDate)) && i.isBefore(departureDate) )
            return true;
        else
            return false;
    }

    private void validateDateParameters(LocalDate arrivalDate, LocalDate departureDate) {
        LocalDate today = LocalDate.now();

        /* Arrival date cannot be before tomorrow or after one month from today */
        if (arrivalDate.isBefore(today.plusDays(Utilities.MINIMUM_DAYS_AHEAD)) ||
                arrivalDate.isAfter(today.plusMonths(Utilities.MAXIMUM_MONTHS_AHEAD))) {
            throw new RuntimeException("Invalid arrival date => " + arrivalDate);
        }

        /* Departure date cannot be before arrival date */
        if (departureDate.isBefore(arrivalDate)) {
            throw new RuntimeException("Departure date cannot be before arrival date");
        }

        /* Reserves can last three days maximum */
        if (Period.between(arrivalDate, departureDate).getDays() > Utilities.MAXIMUM_RESERVE_DAYS) {
            throw new RuntimeException("Cannot reserve for more than " + Utilities.MAXIMUM_RESERVE_DAYS + " days");
        }
    }

    private void validateCreateReserve(Reserve reserve, Long campsiteId) {
        LocalDate arrivalDate = Utilities.getDateFromUnixTime(reserve.getArrivalDate());
        LocalDate departureDate = Utilities.getDateFromUnixTime(reserve.getDepartureDate());

        validateDateParameters(arrivalDate, departureDate);

        /* Check if new reserve overlaps to existing one */
        List<Reserve> reserves = campsiteRepository.findById(campsiteId).get().getReserves();
        validateOverlappingReserves(arrivalDate, departureDate, reserves);
    }

    private void validateOverlappingReserves(LocalDate arrivalDate, LocalDate departureDate, List<Reserve> reserves) {

        /* Set containing actual reserved days */
        Set<LocalDate> daysReserved = new HashSet<LocalDate>();
        for (Reserve r: reserves) {
            LocalDate rArrivalDate = Utilities.getDateFromUnixTime(r.getArrivalDate());
            LocalDate rDepartureDate = Utilities.getDateFromUnixTime(r.getDepartureDate());
            for (LocalDate i = rArrivalDate; i.isBefore(rDepartureDate); i = i.plusDays(1)) {
                daysReserved.add(i);
            }
        }
        /* Check if days of the new reserve are contained in the reserved days */
        for (LocalDate i = arrivalDate; i.isBefore(departureDate); i = i.plusDays(1)) {
            if (daysReserved.contains(i) ) {
                throw new RuntimeException("New reserve overlaps with existing one");
            }
        }
    }

    private void validateUpdateReserve(Reserve reserve, Long reserveId) {
        LocalDate arrivalDate = Utilities.getDateFromUnixTime(reserve.getArrivalDate());
        LocalDate departureDate = Utilities.getDateFromUnixTime(reserve.getDepartureDate());

        validateDateParameters(arrivalDate, departureDate);

        /* Retrieve all reserves except the one to be updated */
        List<Reserve> reserves = entityManager.createQuery("SELECT rs FROM Reserve rs WHERE campsite_id = :campsite_id AND id != :reserve_id", Reserve.class)
                .setParameter("campsite_id", reserveRepository.findById(reserveId).get().getCampsite().getId())
                .setParameter("reserve_id", reserveId)
                .getResultList();

        /* Checks if the updated reserve overlaps with existing ones */
        validateOverlappingReserves(arrivalDate, departureDate, reserves);
    }


}
