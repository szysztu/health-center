package org.example.bookingsystemapp.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.example.bookingsystemapp.entities.Doctor;
import org.example.bookingsystemapp.entities.DoctorSchedule;
import org.example.bookingsystemapp.entities.Specialisation;
import org.example.bookingsystemapp.model.ScheduleCriteriaReturnTempDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class SchedulesCriteriaRepositoryImpl implements SchedulesCriteriaRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<ScheduleCriteriaReturnTempDTO> findSchedulesByCriteria(
            LocalDate startDay,
            LocalDate endDay,
            LocalTime startTime,
            LocalTime endTime,
            Specialisation specialisation) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ScheduleCriteriaReturnTempDTO> cq = cb.createQuery(ScheduleCriteriaReturnTempDTO.class);

        Root<DoctorSchedule> root = cq.from(DoctorSchedule.class);
        Join<DoctorSchedule, Doctor> joinDoctor = root.join("doctor");


        Predicate predicate = cb.conjunction();

        if (startDay != null) {
            predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("day"), startDay));
        }
        if (endDay != null) {
            predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("day"), endDay));
        }
        if (startTime != null) {
            predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("startTime"), startTime));
        }
        if (endTime != null) {
            predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("startTime"), endTime));
        }
        if (specialisation != null) {
            predicate = cb.and(predicate, cb.equal(joinDoctor.get("specialisation"), specialisation));
        }

        cq.select(cb.construct(ScheduleCriteriaReturnTempDTO.class,
                joinDoctor.get("id"),
                joinDoctor.get("lastName"),
                joinDoctor.get("specialisation"),
                root.get("day"),
                root.get("startTime"),
                root.get("booked")
        ));

        cq.where(predicate);

        return entityManager.createQuery(cq).getResultList();
    }


}