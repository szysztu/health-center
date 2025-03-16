package org.example.bookingsystemapp.repositories;

import org.example.bookingsystemapp.entities.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
}
